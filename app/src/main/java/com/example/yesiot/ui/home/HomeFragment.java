package com.example.yesiot.ui.home;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.yesiot.AbsFragment;
import com.example.yesiot.GridAdapter;
import com.example.yesiot.MainActivity;
import com.example.yesiot.R;
import com.example.yesiot.helper.BrokerHelper;
import com.example.yesiot.helper.DeviceHelper;
import com.example.yesiot.helper.SearchHelper;
import com.example.yesiot.object.Device;
import com.example.yesiot.object.MqttEvent;
import com.example.yesiot.service.MQTTService;
import com.example.yesiot.ui.dialog.DeviceDialog;
import com.example.yesiot.util.SPUtils;
import com.example.yesiot.util.Utils;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends AbsFragment {
    //final String TAG = "HomeFragment";
    private HomeViewModel homeViewModel;
    private GridView gridView;
    private GridAdapter adapter;
    private final List<Device> list = new ArrayList<>();
    private int brokerId=0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        //在fragment中使用oncreateOptionsMenu时需要在onCrateView中添加此方法，否则不会调用
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(getActivity()).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        gridView = root.findViewById(R.id.devGridView);
        gridView.setEmptyView(root.findViewById(R.id.text_list_empty));
        adapter = new GridAdapter(getActivity(), list);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Device device = list.get(position);
            Bundle args = new Bundle();
            args.putInt("id", device.getId());
            args.putString("title", device.getName());
            Navigation.findNavController(getView()).navigate(R.id.nav_control, args);
        });
        adapter.setOnActionClickListener((view, position) -> {
            Device device = list.get(position);
            String msg = "on";
            if(device.getState().equals("on")){
                msg = "off";
            }
            MQTTService.publish(Utils.getTopic(device), msg);
        });

        homeViewModel.getList().observe(getViewLifecycleOwner(), devices -> {
            list.clear();
            list.addAll(devices);
            adapter.notifyDataSetChanged();
            updateStatus();
        });

        SmartRefreshLayout refreshLayout = root.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(refreshlayout -> {
            refreshlayout.finishRefresh(2000/*,false*/);//传入false表示刷新失败
            MainActivity activity = (MainActivity) getActivity();
            activity.startMqttService();
            if(MQTTService.isConnected()){
                homeViewModel.setCloud("online");
            }
            getDeviceList();
        });

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        brokerId = SPUtils.getInstance().getInt("broker_id");
        if(BrokerHelper.has(brokerId)){
            getDeviceList();
        }
        if(MQTTService.isConnected()){
            homeViewModel.setCloud("online");
        }else{
            homeViewModel.setCloud("offline");
        }
    }

    private void getDeviceList(){
        List<Device> devices = DeviceHelper.getList(brokerId);
        homeViewModel.setListValue(devices);
    }

    private void updateStatus(){
        if(!MQTTService.isConnected()) return;
        for(Device device:list){
            String topic = Utils.getTopic(device);
            if(TextUtils.isEmpty(device.getPayload())){
                device.setPayload("status");
            }
            MQTTService.subscribe(Utils.getSubTopic(device));
            MQTTService.publish(topic, device.getPayload());
            Log.w(TAG, "MQTT << ["+topic+"] "+ device.getPayload());
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.home, menu);
        observeMenu(menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.action_setting){
            Navigation.findNavController(getView()).navigate(R.id.nav_devices);
        }else if(item.getItemId()==R.id.action_add){
            if(!BrokerHelper.has(brokerId)){
                Utils.showToast("没有选择连接信息");
                return super.onOptionsItemSelected(item);
            }
            DeviceDialog dialogFragment = new DeviceDialog();
            dialogFragment.setCancelable(false);
            dialogFragment.setOnClickListener(v -> {
                if(v.getId()==R.id.add_device){
                    Navigation.findNavController(getView()).navigate(R.id.nav_device);
                }else if(v.getId()==R.id.scan_device){
                    //searchDevice();
                    SearchHelper searchHelper = SearchHelper.Builder(getContext());
                    //searchHelper.setTimeout(1000);
                    searchHelper.setCallback(device -> getDeviceList());
                    searchHelper.start();
                }
            });
            dialogFragment.show(getParentFragmentManager(),"DeviceDialog");
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMqttEvent(MqttEvent event) {
        switch(event.getType()){
            case MqttEvent.SUCCESS:
                Log.d(TAG, "MQTT Connection success.");
                updateStatus();
                break;
            case MqttEvent.FAILURE:
                Log.d(TAG, "MQTT Connection failure.");
                break;
            case MqttEvent.LOST:
                Log.d(TAG, "MQTT Connection lost.");
                break;
            default:
                //只处理来自单片机的信息, 无前缀 #
                String topic = event.getTopic();
                String message = event.getMessage();
                Log.w(TAG,"["+topic+"] "+message);

                for(Device device:list){
                    int position = list.indexOf(device);
                    String regex= Utils.getRegexBySub(Utils.getSubTopic(device));
                    if (topic.matches(regex)) {
                        //Log.w(TAG,"["+regex+"] "+message);
                        if(message.equalsIgnoreCase("offline")) {
                            device.setStatus("offline");
                        }else{
                            device.setStatus("online");
                            if(message.equalsIgnoreCase("on")) {
                                device.setState("on");
                            }else if(message.equalsIgnoreCase("off")){
                                device.setState("off");
                            }
                        }
                        adapter.notifyDataSetChanged(gridView, position);
                    }
                }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}