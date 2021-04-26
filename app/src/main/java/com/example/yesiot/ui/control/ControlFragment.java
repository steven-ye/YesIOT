package com.example.yesiot.ui.control;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.yesiot.R;
import com.example.yesiot.DragLayout;
import com.example.yesiot.MainActivity;
import com.example.yesiot.PanelLayout;
import com.example.yesiot.dialog.ConfirmDialog;
import com.example.yesiot.dialog.PanelDialog;
import com.example.yesiot.helper.DeviceHelper;
import com.example.yesiot.helper.PanelHelper;
import com.example.yesiot.object.Constants;
import com.example.yesiot.object.Device;
import com.example.yesiot.object.Panel;
import com.example.yesiot.service.MQTTConnection;
import com.example.yesiot.service.MQTTService;
import com.example.yesiot.service.TcpClient;
import com.example.yesiot.util.Utils;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class ControlFragment extends Fragment implements MQTTService.MQTTCallBack {
    final String TAG = "ControlFragment";
    ControlViewModel viewModel;
    MainActivity mActivity;
    DragLayout dragLayout;
    List<PanelLayout> layouts = new ArrayList<>();
    Device device;
    MQTTConnection mqttConnection;
    TabLayout tabLayout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewModel =
                new ViewModelProvider(this).get(ControlViewModel.class);
        View root = inflater.inflate(R.layout.fragment_control, container, false);

        mActivity = (MainActivity) getActivity();
        //在fragment中使用oncreateOptionsMenu时需要在onCrateView中添加此方法，否则不会调用
        setHasOptionsMenu(true);

        Bundle args = getArguments();
        device = DeviceHelper.get(args.getInt("id"));

        dragLayout = root.findViewById(R.id.dragLayout);
        dragLayout.setOnChangeListener((view, top, left) -> {
            int id = Integer.parseInt(view.getTag().toString());
            //Log.i(TAG,"tag: " + id);
            String pos = top+"#"+left;
            //Log.i(TAG,"Save Pos: <id:" + id + "> " + pos);
            if(PanelHelper.savePos(id,pos)){
                Log.v(TAG,"位置已保存");
            }else{
                Log.e(TAG,"位置保存失败");
            }
        });

        viewModel.getList().observe(getViewLifecycleOwner(), panels -> {
            dragLayout.removeAllViews();
            for(Panel panel: panels){
                addPanelLayout(panel);//订阅自定义subscripe
                if(TextUtils.isEmpty(panel.sub))continue;
                MQTTService.subscribe(panel.sub,2);
                MQTTService.publish(getTopic(device,"cmd"),"status,"+panel.on);
            }
        });

        tabLayout = root.findViewById(R.id.tab_layout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewModel.setOptionValue(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mqttConnection = MQTTConnection.getInstance();
        mqttConnection.setMqttCallBack(this);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        startTcpConnect();
        tabLayout.getTabAt(viewModel.getOptionValue()).select();

        if(TextUtils.isEmpty(device.getSub())){
            device.setSub(getTopic(device,"status"));
        }
        if(TextUtils.isEmpty(device.getTopic())){
            device.setTopic(getTopic(device,"cmd"));
        }
        if(TextUtils.isEmpty(device.getPayload())){
            device.setPayload("status");
        }
        MQTTService.subscribe(device.getSub(),2);
        MQTTService.publish(device.getTopic(), device.getPayload());
        List<Panel> panels = PanelHelper.getList(device.getId());
        viewModel.setListValue(panels);
    }

    private void addPanelLayout(Panel panel) {
        PanelLayout panelView = new PanelLayout(getContext(), panel);
        dragLayout.setPos(panel.id+"",panel.pos);
        dragLayout.addView(panelView);
        layouts.add(panelView);

        panelView.setOnClickListener(v -> {
            if(dragLayout.getDraggable()){
                for(PanelLayout layout:layouts){
                    if(layout.isSelected())layout.setSelected(false);
                }
                v.setSelected(true);
                PanelDialog dialog = new PanelDialog();
                dialog.setCancelable(false);
                dialog.show(getParentFragmentManager(), "PanelDialgo");
                dialog.setOnClickListener(v1->{
                    if(v1.getId() == R.id.action_edit){
                        Bundle args = new Bundle();
                        args.putInt("id", panel.id);
                        args.putInt("deviceId",device.getId());
                        args.putString("title", "编辑面板");
                        Navigation.findNavController(getView()).navigate(R.id.nav_panel, args);
                        //PanelFormDialog formDialog = PanelFormDialog.show(getParentFragmentManager(),panel);
                        //formDialog.setCallback(newPanel->{});
                    }else if(v1.getId() == R.id.action_delete){
                        ConfirmDialog.show(getParentFragmentManager(),"确定要删除此功能面板？", v2 -> {
                            if(PanelHelper.remove(panel.id)){
                                Utils.showToast("删除面板成功");
                                dragLayout.removeView(panelView);
                                layouts.remove(panelView);
                            }
                        });
                    }
                });
            }else{
                if(panelView.isDataView())return;
                sendMessage(panel);
                Vibrator vibrator = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
                if(vibrator.hasVibrator()){
                    vibrator.vibrate(100); // 振动1秒
                }
            }
        });
    }

    public void sendMessage(Panel panel){
        panel.state = !panel.state;
        String message = panel.state ? panel.on : panel.off;
        if (TextUtils.isEmpty(message)) message = panel.payload;
        if (TextUtils.isEmpty(message)) return;

        if(viewModel.getOptionValue()>0){
            tcpClient.send(message);
            return;
        }
        if(!(mqttConnection.getMqttCallBack() instanceof ControlFragment)){
            mqttConnection.setMqttCallBack(this);
        }
        String topic = panel.topic;
        if(TextUtils.isEmpty(topic)){
            topic = getTopic(device, "cmd");
        }
        MQTTService.publish(topic,message);
        Log.i(TAG,"MQTT << ["+topic+"]" + message);
    }

    private String getTopic(Device device, String cmd){
        return Constants.TOPIC_PREFIX+"/"+device.getTheme()+"/"+device.getCode()+"/"+cmd;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.home, menu);
        //observeMenu(menu);
        viewModel.getLocked().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                dragLayout.setDraggable(!aBoolean);
                MenuItem menuItem = menu.findItem(R.id.action_lock);
                int menuIcon = aBoolean ? R.drawable.ic_baseline_lock_24 : R.drawable.ic_baseline_lock_open_24;
                menuItem.setIcon(menuIcon);
                for(PanelLayout layout:layouts){
                    if(layout.isSelected())layout.setSelected(false);
                }
            }
        });
        viewModel.getCloud().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                MenuItem menuItem = menu.findItem(R.id.action_cloud);
                int menuIcon = R.drawable.ic_baseline_cloud_queue_24;
                if ("online".equals(s)) {
                    menuIcon = R.drawable.ic_baseline_cloud_done_24;
                } else if ("offline".equals(s)) {
                    menuIcon = R.drawable.ic_baseline_cloud_off_24;
                }
                menuItem.setIcon(menuIcon);
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.action_lock){
            viewModel.setLocked(dragLayout.getDraggable());
            return true;
        }else if(item.getItemId()==R.id.action_add){
            Bundle args = new Bundle();
            args.putInt("id",0);
            args.putInt("deviceId",device.getId());
            args.putString("title", "添加面板");
            Navigation.findNavController(getView()).navigate(R.id.nav_panel, args);
            //Utils.showToast("添加功能面板");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSuccess() { }

    @Override
    public void onFailure() { }

    @Override
    public void onLost() { }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onMessageArrived(String topic, String message) {
        Log.e(TAG, "MQTT >> ["+topic+"]"+message);
        //Utils.showToast("MessageArrived："+message);

        if(message.equals(""))return;
        String regex = Utils.getRegexBySub(device.getSub());
        if(topic.matches(regex)){
            if(message.equals("online")||message.equals("offline")){
                viewModel.setCloud(message);
                return;
            }

        }

        for(PanelLayout panelLayout: layouts){
            Panel panel = panelLayout.getPanel();
            if(message.equals(panel.on)){
                panelLayout.setState("on");
            }else if(message.equals(panel.off)){
                panelLayout.setState("off");
            }
        }
    }

    TcpClient tcpClient;
    boolean reconnect = false;
    private void startTcpConnect() {
        tcpClient = TcpClient.getInstance();
        tcpClient.connect(device.getIp(),80);
        tcpClient.setCallback(tcpCallback);
        Log.i(TAG,"Start TCP connection");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tcpClient.disconnect();
        tcpClient.setCallback(null);
    }

    TcpClient.TcpCallback tcpCallback = new TcpClient.TcpCallback() {
        @Override
        public void onConnectSuccess(String ip, int port) {
            Utils.showToast("TCP Connected: "+ ip+":"+port);
            Log.e(TAG, "TCP Connected: "+ ip+":"+port);
            tcpClient.send("status");
            reconnect = true;
        }

        @Override
        public void onConnectFail(String ip, int port) {
            Utils.showToast("TCP Disconnected");
            Log.e(TAG,"TCP Disconnected");
            if(reconnect) tcpClient.connect(ip,port);
        }

        @Override
        public void onDataReceived(String message, int requestCode) {
            message = message.trim();
            //Utils.showToast("TCP message received: " + message);
            Log.e(TAG,"TCP >> " + message);
            for(PanelLayout panelLayout: layouts){
                Panel panel = panelLayout.getPanel();
                //Log.e(TAG,"Panel.on >> " + panel.on);
                if(message.equals(panel.on)){
                    panelLayout.setState("on");
                }else if(message.equals(panel.off)){
                    panelLayout.setState("off");
                }
            }
        }
    };
}