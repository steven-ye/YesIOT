package com.example.yesiot.ui.device;

import android.content.Context;
import android.graphics.Color;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.yesiot.AbsFragment;
import com.example.yesiot.R;
import com.example.yesiot.DragLayout;
import com.example.yesiot.PanelLayout;
import com.example.yesiot.object.StateBean;
import com.example.yesiot.ui.dialog.ConfirmDialog;
import com.example.yesiot.ui.dialog.PanelDialog;
import com.example.yesiot.helper.DeviceHelper;
import com.example.yesiot.helper.PanelHelper;
import com.example.yesiot.object.Device;
import com.example.yesiot.object.MqttEvent;
import com.example.yesiot.object.Panel;
import com.example.yesiot.service.MQTTService;
import com.example.yesiot.service.TcpClient;
import com.example.yesiot.ui.home.HomeViewModel;
import com.example.yesiot.util.Utils;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class ControlFragment extends AbsFragment {
    HomeViewModel homeViewModel;
    ControlViewModel viewModel;
    DragLayout dragLayout;
    Device device;
    TabLayout tabLayout;
    TextView tvStatus;
    TcpClient tcpClient;
    boolean reconnect = false;
    boolean needSave = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        tcpClient = TcpClient.getInstance();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        homeViewModel =
                new ViewModelProvider(getMainActivity()).get(HomeViewModel.class);
        viewModel =
                new ViewModelProvider(this).get(ControlViewModel.class);
        View root = inflater.inflate(R.layout.fragment_control, container, false);
        tvStatus = root.findViewById(R.id.text_status);
        tvStatus.setText("");

        Bundle args = getArguments();
        device = DeviceHelper.get(args.getInt("id"));
        if(TextUtils.isEmpty(device.getPayload())){
            device.setPayload("status");
        }

        dragLayout = root.findViewById(R.id.dragLayout);
        dragLayout.setOnChangeListener((view, top, left) -> {
            needSave = true;
            Panel panel = ((PanelLayout)view).getPanel();
            String pos = top+"#"+left;
            Log.i(TAG,"Pos: <id:" + panel.id + "> " + pos);
            /*
            if(PanelHelper.savePos(id,pos)){
                Log.v(TAG,"位置已保存: " + pos);
            }else{
                Log.e(TAG,"位置保存失败: " + pos);
            }*/
        });
        dragLayout.setOnItemClickListener(view ->  {
            dragLayout.clearSelected();
            view.setSelected(true);

            if(dragLayout.isDraggable()) {
                Utils.showToast("长按编辑");
                return;
            }

            PanelLayout panelLayout = (PanelLayout)view;
            Panel panel = panelLayout.getPanel();

            if(panel.type==2) return;
            String message = panel.name;
            String value = panel.payload;
            if(panel.type==1){
                value = panelLayout.getValue().equals(panel.off) ? panel.on : panel.off;
                //message = message + "," + value;
            }
            if(!TextUtils.isEmpty(value)){
                message = message + "," + value;
            }

            if(viewModel.getOptionValue()>0){
                if(tcpClient.isConnect()){
                    tcpClient.send(message);
                    Log.i(TAG,"TCP << " + message);
                }else{
                    Utils.showToast("没有连接到设备");
                }
            }else{
                //MQTTService.publish(Utils.getTopic(device, "cmd"),message);
                publish(message);
            }

            Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            if(vibrator.hasVibrator()){
                vibrator.vibrate(100); // 振动1秒
            }
        });

        dragLayout.setOnItemLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                dragLayout.clearSelected();
                v.setSelected(true);

                if(!dragLayout.isDraggable()) return false;

                PanelLayout panelLayout = (PanelLayout) v;
                Panel panel = panelLayout.getPanel();

                PanelDialog dialog = new PanelDialog();
                dialog.setCancelable(false);
                dialog.show(getParentFragmentManager(), "PanelDialog");
                dialog.setOnClickListener(v1->{
                    if(v1.getId() == R.id.action_edit){
                        Bundle bundle = new Bundle();
                        bundle.putInt("id", panel.id);
                        bundle.putInt("deviceId",device.getId());
                        bundle.putString("title", "编辑面板");
                        Navigation.findNavController(getView()).navigate(R.id.nav_panel, bundle);
                        //PanelFormDialog formDialog = PanelFormDialog.show(getParentFragmentManager(),panel);
                        //formDialog.setCallback(newPanel->{});
                    }else if(v1.getId() == R.id.action_delete){
                        ConfirmDialog.show(getParentFragmentManager(),"确定要删除此功能面板？", v2 -> {
                            if(PanelHelper.delete(panel.id)){
                                Utils.showToast("删除面板成功");
                                dragLayout.removeView(panelLayout);
                                //panelViewList.remove(panelView);
                            }else{
                                Utils.showToast("删除面板失败");
                                Log.w(TAG, "删除面板失败 " + panel.id);
                            }
                        });
                    }
                });
                return false;
            }
        });

        viewModel.getList().observe(getViewLifecycleOwner(), panels -> {
            dragLayout.removeAllViews();
            for(Panel panel: panels){
                PanelLayout panelView = new PanelLayout(getContext(), panel);
                dragLayout.addView(panelView);
                if(panel.type==2){
                    panelView.setClickable(dragLayout.isDraggable());
                }
            }
            Log.w(TAG,"getList: "+ panels.size());
            publish(device.getPayload(),1000);
        });

        viewModel.getOption().observe(getViewLifecycleOwner(), option -> {
            if(option>0){
                startTcpConnect();
            }else{
                if(tcpClient.isConnect()){
                    tcpClient.disconnect();
                }
                reconnect = false;
                viewModel.setStatus("设备离线");
            }
        });

        viewModel.getStatus().observe(getViewLifecycleOwner(), text ->{
            tvStatus.setText(text);
            if(text.contains("在线") || text.contains("已连接")){
                tvStatus.setTextColor(Color.WHITE);
            }else{
                tvStatus.setTextColor(Color.LTGRAY);
            }
        });

        tabLayout = root.findViewById(R.id.tab_layout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewModel.setOptionValue(tab.getPosition());
                if(tab.getPosition()==0) publish(device.getPayload());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        tabLayout.getTabAt(viewModel.getOptionValue()).select();
        List<Panel> panels = PanelHelper.getList(device.getId());
        viewModel.setListValue(panels);

        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.control, menu);
        //observeMenu(menu);
        viewModel.getLocked().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean locked) {
                dragLayout.setDraggable(!locked);
                MenuItem menuItem = menu.findItem(R.id.action_lock);
                int menuIcon = locked ? R.drawable.ic_baseline_lock_24 : R.drawable.ic_baseline_lock_open_24;
                menuItem.setIcon(menuIcon);
                dragLayout.clearSelected();
            }
        });
        homeViewModel.getCloud().observe(getViewLifecycleOwner(), new Observer<String>() {
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
            viewModel.setLocked(dragLayout.isDraggable());
            if(!needSave){
                return true;
            }
            for(int i=0;i<dragLayout.getChildCount();i++){
                View view = dragLayout.getChildAt(i);
                //int id = view.getId();
                int left = view.getLeft();
                int top = view.getTop();
                if(dragLayout.isDraggable()){
                    PanelHelper.savePos(view.getId(),left+"#"+top);
                }
                if("2".equals(view.getTag().toString())){
                    view.setClickable(!dragLayout.isDraggable());
                }
            }
            needSave = false;
            Utils.showToast("布局已经保存");
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

    @Subscribe(threadMode = ThreadMode.POSTING, sticky = true)
    public void onEvent(MqttEvent event) {
        switch(event.getType()) {
            case MqttEvent.SUCCESS:
                Log.w(TAG,"MQTT服务器连接成功.");
                break;
            case MqttEvent.FAILURE:
                Log.w(TAG,"MQTT服务器连接 FAILURE");
                break;
            case MqttEvent.LOST:
                Log.w(TAG,"MQTT服务器连接 LOST");
                break;
            default:
                // tcp连接
                if(viewModel.getOptionValue()==1)return;
                // MQTT连接
                String topic = event.getTopic();
                String message = event.getMessage();
                Log.w(TAG, "["+topic+"]" + message);

                if (message.startsWith("{\"") && message.endsWith("\"}") && message.indexOf("\"code\":")>0){
                    try{
                        StateBean bean = new Gson().fromJson(JsonParser.parseString(message), StateBean.class);
                        if(device.getCode().equals(bean.code)) {
                            if ("offline".equals(bean.msg)) {
                                viewModel.setStatus("设备离线");
                                return;
                            }
                            updateState(bean.state);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        //Log.e(TAG, e.getMessage());
                    }
                }else{
                    String regex = Utils.getRegexBySub(Utils.getSubTopic(device));
                    if(topic.matches(regex)) {
                        if(message.equals("offline")) {
                            viewModel.setStatus("设备离线");
                            return;
                        }

                        viewModel.setStatus("设备在线");
                        if(message.equals(""))return;
                        onMessageArrived(message);
                    }
                }
        }
    }

    private void updateState(Map<String, String> map){
        int childCount = dragLayout.getChildCount();
        for(int i=0;i<childCount;i++){
            PanelLayout panelLayout = (PanelLayout)dragLayout.getChildAt(i);
            Panel panel = panelLayout.getPanel();
            if(map.containsKey(panel.name)){
                panelLayout.setValue(map.get(panel.name));
            }
        }
    }

    private void onMessageArrived(String message){
        Log.w(TAG, "Message Arrived: "+message);
        int childCount = dragLayout.getChildCount();
        for(int i=0;i<childCount;i++){
            PanelLayout panelLayout = (PanelLayout)dragLayout.getChildAt(i);
            Panel panel = panelLayout.getPanel();
            String[] data = new String[]{message};
            if(message.contains("#")){
                data = message.split("#");
            }
            for(String msg: data){
                if(!msg.startsWith(panel.name)) continue;
                //Log.w(TAG,panel.id+": "+panel.name+" : "+msg);
                //IApplication.getExecutor().execute();
                String value = msg.replaceFirst(panel.name+",","");
                panelLayout.setValue(value);
            }
        }
    }

    private void publish(String message){
        publish(message,0);
    }
    private void publish(String message, int mills){
        new Thread(() -> {
            try{
                Thread.sleep(mills);
                MQTTService.publish(Utils.getTopic(device),message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void startTcpConnect() {
        tcpClient.setCallback(tcpCallback);
        if(tcpClient.isConnect())return;
        reconnect = false;
        tcpClient.connect(device.getIp(),device.getPort());
        Log.i(TAG,"Start TCP connection");
        viewModel.setStatus("TCP连接中 ...");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(tcpClient != null){
            tcpClient.disconnect();
            tcpClient.setCallback(null);
        }
        if(needSave){
            Utils.showToast("布局未保存");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    TcpClient.TcpCallback tcpCallback = new TcpClient.TcpCallback() {
        @Override
        public void onConnectSuccess(String ip, int port) {
            viewModel.setStatus("TCP已连接");
            Utils.showToast("TCP连接成功: "+ ip+":"+port);
            Log.e(TAG, "TCP连接成功: "+ ip+":"+port);
            //tcpClient.send("status");
            reconnect = true;
        }

        @Override
        public void onConnectFail(String ip, int port) {
            if(viewModel.getOptionValue()==1){
                viewModel.setStatus("TCP未连接");
            }
            Utils.showToast("TCP连接已断开");
            Log.e(TAG,"TCP连接已断开");
            if(reconnect) tcpClient.connect(ip,port);
        }

        @Override
        public void onDataReceived(String message, int requestCode) {
            message = message.trim();
            //message = message.replaceAll("^state:","");
            //Utils.showToast("TCP message received: " + message);
            Log.e(TAG,"TCP >> " + message);
            onMessageArrived(message);
        }
    };
}