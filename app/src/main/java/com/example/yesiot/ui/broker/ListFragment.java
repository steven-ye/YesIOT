package com.example.yesiot.ui.broker;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.yesiot.AbsFragment;
import com.example.yesiot.adapter.BrokerAdapter;
import com.example.yesiot.MainActivity;
import com.example.yesiot.R;
import com.example.yesiot.helper.BrokerHelper;
import com.example.yesiot.helper.DeviceHelper;
import com.example.yesiot.service.MQTTService;
import com.example.yesiot.dialog.ConfirmDialog;
import com.example.yesiot.dialog.ListDialog;
import com.example.yesiot.ui.home.HomeViewModel;
import com.example.yesiot.util.SPUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ListFragment extends AbsFragment {
    private HomeViewModel homeViewModel;
    private BrokerAdapter adapter;
    private final List<Map<String,String>> list = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        View root =
                inflater.inflate(R.layout.fragment_brokers, container, false);
        ListView listView = root.findViewById(R.id.broker_listview);
        listView.setEmptyView(root.findViewById(R.id.text_list_empty));
        int selectedId = SPUtil.getBrokerId();
        adapter = new BrokerAdapter(getActivity(), list);
        adapter.setSelectedId(selectedId);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            adapter.setFocusPosition(position);
        });

        adapter.setOnItemClickListener((view, position) -> {
            showDialog(position);
        });


        Button btnAdd = root.findViewById(R.id.button_add);
        btnAdd.setOnClickListener(v-> Navigation.findNavController(requireView()).navigate(R.id.nav_broker));

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        getList();
        if(MQTTService.isConnected()){
            homeViewModel.setCloud("online");
        }else{
            homeViewModel.setCloud("offline");
        }
    }

    private void getList(){
        List<Map<String,String>> brokers = BrokerHelper.getList();
        list.clear();
        list.addAll(brokers);
        adapter.notifyDataSetChanged();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onOptionsMenuCreated(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.brokers, menu);
        // 显示图标
        if (menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }
        observeMenu(menu);
        super.onOptionsMenuCreated(menu, inflater);
    }

    @Override
    public boolean onOptionsMenuSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.action_add){
            Navigation.findNavController(requireView()).navigate(R.id.nav_broker);
        }
        return super.onOptionsMenuSelected(item);
    }

    private void observeMenu(Menu menu){
        homeViewModel.getCloud().observe(getViewLifecycleOwner(), s -> {
            //Utils.showToast("连接状态: " + s);
            MenuItem menuItem = menu.findItem(R.id.action_cloud);
            int menuIcon = R.drawable.ic_baseline_cloud_queue_24;
            if (s.equals("online")) {
                menuIcon = R.drawable.ic_baseline_cloud_done_24;
            } else if (s.equals("offline")) {
                menuIcon = R.drawable.ic_baseline_cloud_off_24;
            }
            menuItem.setIcon(menuIcon);
        });
    }

    private void showDialog(int position){
        Map<String,String> map = list.get(position);
        int brokerId = Integer.parseInt(Objects.requireNonNull(map.get("id")));
        String[] titles = new String[]{"启用","编辑","删除","取消"};
        ListDialog dialog = getListDialog(titles);
        //dialog.setCancelable(false);
        dialog.show(getParentFragmentManager(), "ListDialog");
        dialog.setOnClickListener((v, pos) -> {
            switch(titles[pos]){
                case "启用":
                    int selectedId = SPUtil.getBrokerId();
                    if(brokerId == selectedId) {
                        showToast("此连接已经启用");
                        return;
                    }
                    SPUtil.putBrokerId(brokerId);
                    adapter.setSelectedPosition(position);
                    getList();
                    homeViewModel.setCloud("offline");
                    MainActivity mainActivity = (MainActivity) getActivity();
                    assert mainActivity != null;
                    mainActivity.startMqttService();
                    break;
                case "编辑":
                    Bundle args = new Bundle();
                    args.putInt("id", brokerId);
                    args.putString("name", map.get("name"));
                    Navigation.findNavController(requireView()).navigate(R.id.nav_broker, args);
                    break;
                case "删除":
                    int activeId = SPUtil.getBrokerId();
                    if(1 == brokerId){
                        showToast("此连接禁止删除");
                        alert("此连接禁止删除 ！");
                    }else if(activeId == brokerId){
                        showToast("此连接正在使用，无法删除");
                        alert("此连接正在使用，无法删除");
                    }else if(DeviceHelper.hasDevice(brokerId)){
                        showToast("无法删除：该连接下有设备");
                        alert("无法删除：该连接下有设备");
                    }else if(BrokerHelper.has(brokerId)){
                        ConfirmDialog.show(getParentFragmentManager(), "确认要删除此连接？", v2 -> {
                            if(DeviceHelper.remove(brokerId)) {
                                Navigation.findNavController(requireView()).navigateUp();
                                showToast("删除设备成功");
                                getList();
                            }
                        });
                    }else{
                        showToast("连接信息不存在");
                    }
                    break;
                default:
                    dialog.dismiss();
            }
        });
    }

    @NonNull
    private static ListDialog getListDialog(String[] titles) {
        int[] icons = new int[]{R.drawable.ic_baseline_done_24,R.drawable.ic_baseline_edit_24,
                R.drawable.ic_baseline_delete_outline_24,R.drawable.ic_baseline_close_24};
        int[] colors = new int[]{Color.GREEN,Color.BLUE,Color.RED,Color.GRAY};
        List<Map<String,Object>> actions = new ArrayList<>();
        for(int i = 0; i< titles.length; i++){
            Map<String,Object> action = new HashMap<>();
            action.put("icon", icons[i]);
            action.put("title", titles[i]);
            action.put("color",colors[i]);
            actions.add(action);
        }

        return new ListDialog(actions);
    }
}