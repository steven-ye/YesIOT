package com.example.yesiot.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
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
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.yesiot.AbsFragment;
import com.example.yesiot.adapter.GridAdapter;
import com.example.yesiot.MainActivity;
import com.example.yesiot.R;
import com.example.yesiot.helper.BrokerHelper;
import com.example.yesiot.helper.DeviceHelper;
import com.example.yesiot.helper.MqttHelper;
import com.example.yesiot.helper.ScanHelper;
import com.example.yesiot.object.Device;
import com.example.yesiot.object.MqttEvent;
import com.example.yesiot.object.StateBean;
import com.example.yesiot.service.MQTTService;
import com.example.yesiot.dialog.DeviceDialog;
import com.example.yesiot.util.SPUtil;
import com.example.yesiot.util.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Field;
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
        //setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
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
            Navigation.findNavController(requireView()).navigate(R.id.nav_control, args);
        });
        adapter.setOnActionClickListener((view, position) -> {
            Device device = list.get(position);
            String event = "on";
            if("on".equals(device.getState())){
                event = "off";
            }
            //MQTTService.publish(Utils.getTopic(device), event);
            MqttHelper.send(device, event);
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
            assert activity != null;
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
        brokerId = SPUtil.getBrokerId();
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
            if(TextUtils.isEmpty(device.getPayload())){
                device.setPayload("status");
            }
            MQTTService.subscribe(Utils.getSubTopic(device));
            //MQTTService.publish(topic, device.getPayload());
            MqttHelper.send(device, "status");
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onOptionsMenuCreated(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.home, menu);// 显示图标
        // 显示图标
        if (menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }
        observeMenu(menu);
        super.onOptionsMenuCreated(menu, inflater);
    }

    @Override
    public boolean onOptionsMenuSelected(@NonNull MenuItem item) {
        View mView = requireView();
        if(item.getItemId()==R.id.action_setting){
            Navigation.findNavController(mView).navigate(R.id.nav_brokers);
        }else if(item.getItemId()==R.id.action_device){
            Navigation.findNavController(mView).navigate(R.id.nav_devices);
        }else if(item.getItemId()==R.id.action_add){
            if(!BrokerHelper.has(brokerId)){
                showToast("没有选择连接信息");
                return true;
            }
            DeviceDialog dialogFragment = getDeviceDialog(mView);
            dialogFragment.show(getParentFragmentManager(),"DeviceDialog");
        }
        return super.onOptionsMenuSelected(item);
    }

    private @NonNull DeviceDialog getDeviceDialog(View mView) {
        DeviceDialog dialogFragment = new DeviceDialog();
        dialogFragment.setCancelable(false);
        dialogFragment.setOnClickListener(v -> {
            if(v.getId()==R.id.add_device){
                Navigation.findNavController(mView).navigate(R.id.nav_device);
            }else if(v.getId()==R.id.scan_device){
                //searchDevice();
                ScanHelper searchHelper = ScanHelper.Builder(getContext());
                //searchHelper.setTimeout(1000);
                searchHelper.setCallback(device -> getDeviceList());
                searchHelper.start();
            }
        });
        return dialogFragment;
    }

    public void observeMenu(Menu menu){
        homeViewModel.getCloud().observe(getViewLifecycleOwner(), s -> {
            //Utils.showToast("连接状态: " + s);
            MenuItem menuItem = menu.findItem(R.id.action_cloud);
            if(menuItem == null) return;
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
                //Log.w(TAG,"["+topic+"] "+message);
                if(message.startsWith("{") && message.endsWith("}")) {
                    try {
                        StateBean bean = new Gson().fromJson(JsonParser.parseString(message), StateBean.class);
                        //Log.w(TAG,bean.code + ","+bean.status);
                        if(!TextUtils.isEmpty(bean.notice)){
                            showToast(bean.notice);
                        }
                        if (TextUtils.isEmpty(bean.code)) return;
                        updateByCode(bean);
                    } catch (Exception e) {
                        e.printStackTrace();
                        //Log.e(TAG, e.getMessage());
                    }
                }else{
                    for(Device device:list){
                        int position = list.indexOf(device);
                        String regex= Utils.getRegexBySub(Utils.getSubTopic(device));
                        if (topic.matches(regex)) {
                            //Log.w(TAG,"["+regex+"] "+message);
                            if(message.equalsIgnoreCase("#offline")) {
                                device.setStatus("offline");
                            }else{
                                device.setStatus("online");
                                if(message.equalsIgnoreCase("#on")) {
                                    device.setState("on");
                                }else if(message.equalsIgnoreCase("#off")){
                                    device.setState("off");
                                }
                            }
                            adapter.notifyDataSetChanged(gridView, position);
                        }
                    }
                }
        }
    }

    private void updateByCode(StateBean bean){
        Log.d(TAG, bean.code + "," + bean.status + "," + bean.ip);
        for(Device device:list) {
            if(bean.code.equalsIgnoreCase(device.getCode())){
                if("offline".equalsIgnoreCase(bean.status)){
                    device.setStatus("offline");
                }else{
                    device.setStatus("online");
                }
                if("on".equalsIgnoreCase(bean.status)) {
                    device.setState("on");
                }else if("off".equalsIgnoreCase(bean.status)){
                    device.setState("off");
                }
                //更新IP地址
                if(!TextUtils.isEmpty(bean.ip) && !bean.ip.equals(device.getIp())){
                    device.setIp(bean.ip);
                    DeviceHelper.save(device);
                }
                adapter.notifyDataSetChanged(gridView, list.indexOf(device));
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    //当前选择的menuItem的id
    private int checkedItemId = R.id.menu_setting_wifi;

    @SuppressLint("RestrictedApi")
    private void showPopupMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), anchor);
        //引入菜单资源
        popupMenu.inflate(R.menu.popup);

        //设置选中
        popupMenu.getMenu().findItem(checkedItemId).setChecked(true);
        //菜单项的监听
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_setting_wifi:
                        checkedItemId = R.id.menu_setting_wifi;
                        showToast("WIFI");
                        break;

                    case R.id.menu_setting_gps:
                        checkedItemId = R.id.menu_setting_gps;
                        showToast("GPS");
                        break;

                    case R.id.menu_setting_userIcon:
                        showToast("USER_ICON");
                        break;
                }
                return true;
            }
        });

        //使用反射。强制显示菜单图标
        try {
            Field field = popupMenu.getClass().getDeclaredField("mPopup");
            field.setAccessible(true);
            @SuppressLint("RestrictedApi") MenuPopupHelper mHelper = (MenuPopupHelper) field.get(popupMenu);
            assert mHelper != null;
            mHelper.setForceShowIcon(true);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }

        //显示PopupMenu
        popupMenu.show();
    }
}