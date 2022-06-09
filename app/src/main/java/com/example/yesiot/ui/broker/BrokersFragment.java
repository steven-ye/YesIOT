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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.yesiot.AbsFragment;
import com.example.yesiot.BrokerAdapter;
import com.example.yesiot.MainActivity;
import com.example.yesiot.R;
import com.example.yesiot.service.MQTTService;
import com.example.yesiot.ui.dialog.ConfirmDialog;
import com.example.yesiot.ui.dialog.ListDialog;
import com.example.yesiot.helper.BrokerHelper;
import com.example.yesiot.helper.DeviceHelper;
import com.example.yesiot.ui.home.HomeViewModel;
import com.example.yesiot.util.SPUtils;
import com.example.yesiot.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BrokersFragment extends AbsFragment {
    private HomeViewModel homeViewModel;
    private ListView listView;
    private BrokerAdapter adapter;
    private final List<Map<String,String>> list = new ArrayList<>();
    private int focusPosition = -1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(getActivity()).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_brokers, container, false);
        listView = root.findViewById(R.id.broker_listview);
        listView.setEmptyView(root.findViewById(R.id.text_list_empty));
        int selectedId = SPUtils.getInstance().getInt("broker_id");
        adapter = new BrokerAdapter(getActivity(), list);
        adapter.setSelectedId(selectedId);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            adapter.setSelectedPosition(position);
            focusPosition = position;
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String,String> map = list.get(position);
                int brokerId = Integer.parseInt(map.get("id"));
                //Navigation.findNavController(getView()).navigate(R.id.nav_device, args);
                String[] titles = new String[]{"启用","编辑","删除","取消"};
                int[] icons = new int[]{R.drawable.ic_baseline_done_24,R.drawable.ic_baseline_edit_24,
                        R.drawable.ic_baseline_delete_outline_24,R.drawable.ic_baseline_close_24};
                int[] colors = new int[]{Color.GREEN,Color.BLUE,Color.RED,Color.GRAY};
                List<Map<String,Object>> actions = new ArrayList<>();
                for(int i=0;i<titles.length;i++){
                    Map<String,Object> action = new HashMap<>();
                    action.put("icon", icons[i]);
                    action.put("title", titles[i]);
                    action.put("color",colors[i]);
                    actions.add(action);
                }

                ListDialog dialog = new ListDialog(actions);
                //dialog.setCancelable(false);
                dialog.show(getParentFragmentManager(), "ListDialog");
                dialog.setOnClickListener((v, pos) -> {
                    switch(titles[pos]){
                        case "启用":
                            int selectedId = SPUtils.getInstance().getInt("broker_id");
                            if(brokerId == selectedId) {
                                Utils.showToast("此连接已经启用");
                                return;
                            }
                            SPUtils.getInstance().putInt("broker_id",brokerId);
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
                            Navigation.findNavController(getView()).navigate(R.id.nav_broker, args);
                            break;
                        case "删除":
                            int useId = SPUtils.getInstance().getInt("broker_id");
                            if(useId == brokerId){
                                Utils.showToast("此连接正在使用，无法删除");
                                Utils.alert(getContext(),"此连接正在使用，无法删除");
                            }else if(DeviceHelper.hasDevice(brokerId)){
                                Utils.showToast("无法删除：该连接下有设备");
                                Utils.alert(getContext(),"无法删除：该连接下有设备");
                            }else if(BrokerHelper.has(brokerId)){
                                ConfirmDialog.show(getParentFragmentManager(), "确认要删除此连接？", v2 -> {
                                    if(DeviceHelper.remove(brokerId)) {
                                        Navigation.findNavController(getView()).navigateUp();
                                        Utils.showToast("删除设备成功");
                                        getList();
                                    }
                                });
                            }else{
                                Utils.showToast("连接信息不存在");
                            }
                            break;
                        default:
                            dialog.dismiss();
                    }
                });

                /*
                BrokerDialog dialog = new BrokerDialog();
                dialog.setCancelable(false);
                dialog.show(getParentFragmentManager(), "BrokerDialog");
                dialog.setOnClickListener(v->{
                    switch(v.getId()){
                        case R.id.action_select:
                            int selectedId = SPUtils.getInstance().getInt("broker_id");
                            if(idx == selectedId) {
                                Utils.showToast("此连接已经启用");
                                return;
                            }
                            SPUtils.getInstance().putInt("broker_id",idx);
                            adapter.setSelectedPosition(position);
                            getList();
                            homeViewModel.setCloud("offline");
                            MainActivity mainActivity = (MainActivity) getActivity();
                            mainActivity.startMqttService();
                            break;
                        case R.id.action_edit:
                            Bundle args = new Bundle();
                            args.putInt("id", idx);
                            args.putString("name", map.get("name"));
                            Navigation.findNavController(getView()).navigate(R.id.nav_broker, args);
                            break;
                        case R.id.action_delete:
                            BrokerHelper.remove(idx);
                            getList();
                            break;
                    }
                });

                 */
                return false;
            }
        });

        Button btnAdd = root.findViewById(R.id.button_add);
        btnAdd.setOnClickListener(v->{
            Navigation.findNavController(getView()).navigate(R.id.nav_broker);
        });

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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.home, menu);
        menu.findItem(R.id.action_setting).setVisible(false);
        observeMenu(menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.action_setting){
            Utils.showToast("Setting");
            return true;
        }else if(item.getItemId()==R.id.action_add){
            Navigation.findNavController(getView()).navigate(R.id.nav_broker);
        }
        return super.onOptionsItemSelected(item);
    }

    public void observeMenu(Menu menu){
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
}