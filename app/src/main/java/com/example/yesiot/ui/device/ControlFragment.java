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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.yesiot.AbsFragment;
import com.example.yesiot.DragLayout;
import com.example.yesiot.PanelLayout;
import com.example.yesiot.R;
import com.example.yesiot.helper.DeviceHelper;
import com.example.yesiot.helper.MqttHelper;
import com.example.yesiot.helper.OkHttpHelper;
import com.example.yesiot.helper.PanelHelper;
import com.example.yesiot.object.Device;
import com.example.yesiot.object.MqttEvent;
import com.example.yesiot.object.Panel;
import com.example.yesiot.object.StateBean;
import com.example.yesiot.service.TcpClient;
import com.example.yesiot.dialog.ConfirmDialog;
import com.example.yesiot.dialog.PanelDialog;
import com.example.yesiot.ui.home.HomeViewModel;
import com.example.yesiot.util.Utils;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        //Log.d(TAG, "onCreateView");
        homeViewModel =
                new ViewModelProvider(getMainActivity()).get(HomeViewModel.class);
        viewModel =
                new ViewModelProvider(this).get(ControlViewModel.class);

        View root = inflater.inflate(R.layout.fragment_control, container, false);
        tvStatus = root.findViewById(R.id.text_status);
        tvStatus.setText("");

        Bundle args = getArguments();
        assert args != null;
        device = DeviceHelper.get(args.getInt("id"));
        Log.d(TAG, "DEVICE: " + device);
        if(TextUtils.isEmpty(device.getPayload())){
            device.setPayload("status");
        }

        OkHttpHelper http = new OkHttpHelper(httpCallback);

        dragLayout = root.findViewById(R.id.dragLayout);
        dragLayout.setOnChangeListener((view, top, left) -> {
            needSave = true;
            Panel panel = ((PanelLayout)view).getPanel();
            String pos = top+"#"+left;
            Log.i(TAG,"Pos: <id:" + panel.id + "> " + pos);
        });
        dragLayout.setOnItemClickListener(view ->  {
            dragLayout.clearSelected();
            view.setSelected(true);

            if(dragLayout.isDraggable()) {
                showToast("长按编辑");
                return;
            }

            PanelLayout panelLayout = (PanelLayout)view;
            Panel panel = panelLayout.getPanel();

            if(panel.type==2) return;
            String message = panel.name;
            String value = "";
            if(panel.type==0){
                value = panel.payload;
            }else if(panel.type==1){
                value = panelLayout.getValue().equals(panel.off) ? panel.on : panel.off;
                //panelLayout.setValue(value);
                //message = message + "," + value;
            }
            if(!TextUtils.isEmpty(value)){
                message = message + "," + value;
            }

            if(viewModel.getOptionValue()>0){
                if(viewModel.isOnline()){
                    http.get("http://" + device.getIp() + "/cmd?s=" + message);
                }else{
                    alert("设备离线");
                }
            }else{
                // mqtt连接
                if (viewModel.isOnline()) {
                    //publish(message);
                    MqttHelper.send(device, message);
                    panelLayout.setValue(value);
                } else {
                    alert("设备离线");
                }
            }

            Vibrator vibrator = (Vibrator) requireActivity().getSystemService(Context.VIBRATOR_SERVICE);
            if(vibrator.hasVibrator()){
                vibrator.vibrate(100); // 振动1秒
            }
        });

        dragLayout.setOnItemLongClickListener(v -> {
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
                    Navigation.findNavController(requireView()).navigate(R.id.nav_panel, bundle);
                    //PanelFormDialog formDialog = PanelFormDialog.show(getParentFragmentManager(),panel);
                    //formDialog.setCallback(newPanel->{});
                }else if(v1.getId() == R.id.action_delete){
                    ConfirmDialog.show(getParentFragmentManager(),"确定要删除此功能面板？", v2 -> {
                        if(PanelHelper.delete(panel.id)){
                            showToast("删除面板成功");
                            dragLayout.removeView(panelLayout);
                            //panelViewList.remove(panelView);
                        }else{
                            showToast("删除面板失败");
                            Log.w(TAG, "删除面板失败 " + panel.id);
                        }
                    });
                }
            });
            return false;
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
            MqttHelper.send(device, "status,state", 1000);
            //publish("status", 1000);
        });

        viewModel.getOption().observe(getViewLifecycleOwner(), option -> {
            if(option>0){
                //startTcpConnect();
                http.get("http://"+device.getIp()+"/cmd?s=status");
            }else{
                if(tcpClient.isConnect()){
                    tcpClient.disconnect();
                }
                reconnect = false;
                viewModel.setStatus("设备离线");
            }
        });

        viewModel.getOnline().observe(getViewLifecycleOwner(), vBoolean -> {
            String status = vBoolean ? "设备在线" : "设备离线";
            viewModel.setStatus(status);
        });

        viewModel.getStatus().observe(getViewLifecycleOwner(), text ->{
            tvStatus.setText(text);
            if(text.contains("在线") || text.contains("已连接")){
                tvStatus.setTextColor(Color.GREEN);
            }else{
                tvStatus.setTextColor(Color.LTGRAY);
            }
        });

        tabLayout = root.findViewById(R.id.tab_layout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewModel.setOptionValue(tab.getPosition());
                if(tab.getPosition()==0) {
                    MqttHelper.send(device, "status");
                }else{
                    http.get("http://"+device.getIp()+"/cmd?s=status");
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        Objects.requireNonNull(tabLayout.getTabAt(viewModel.getOptionValue())).select();
        List<Panel> panels = PanelHelper.getList(device.getId());
        viewModel.setListValue(panels);
        return root;
    }

    @Override
    public void onOptionsMenuCreated(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.control, menu);
        viewModel.getLocked().observe(getViewLifecycleOwner(), locked -> {
            dragLayout.setDraggable(!locked);
            MenuItem menuItem = menu.findItem(R.id.action_lock);
            int menuIcon = locked ? R.drawable.ic_baseline_lock_24 : R.drawable.ic_baseline_lock_open_24;
            menuItem.setIcon(menuIcon);
            dragLayout.clearSelected();
        });
        homeViewModel.getCloud().observe(getViewLifecycleOwner(), s -> {
            MenuItem menuItem = menu.findItem(R.id.action_cloud);
            int menuIcon = R.drawable.ic_baseline_cloud_queue_24;
            if ("online".equals(s)) {
                menuIcon = R.drawable.ic_baseline_cloud_done_24;
            } else if ("offline".equals(s)) {
                menuIcon = R.drawable.ic_baseline_cloud_off_24;
            }
            menuItem.setIcon(menuIcon);
        });
        //super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsMenuSelected(@NonNull MenuItem item) {
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
            showToast("布局已经保存");
            return true;
        }else if(item.getItemId()==R.id.action_add){
            Bundle args = new Bundle();
            args.putInt("id",0);
            args.putInt("deviceId",device.getId());
            args.putString("title", "添加面板");
            Navigation.findNavController(requireView()).navigate(R.id.nav_panel, args);
            //showToast("添加功能面板");
        }
        //return super.onOptionsItemSelected(item);
        return super.onOptionsMenuSelected(item);
    }

    @Subscribe(threadMode = ThreadMode.POSTING, sticky = true)
    public void onMqttEvent(MqttEvent event) {
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

                if(message.startsWith("{") && message.endsWith("}")) {
                    try{
                        StateBean bean = new Gson().fromJson(JsonParser.parseString(message), StateBean.class);
                        if(!device.getCode().equalsIgnoreCase(bean.code)) return;
                        boolean offline = "offline".equalsIgnoreCase(bean.status);
                        viewModel.setOnline(!offline);
                        if(!TextUtils.isEmpty(bean.notice)){
                            showToast(bean.notice);
                        }
                        updateState(bean.state);
                    }catch (Exception e){
                        //e.printStackTrace();
                        Log.e(TAG, e.toString());
                    }
                }else{
                    String regex = Utils.getRegexBySub(Utils.getSubTopic(device));
                    if(topic.matches(regex)) {
                        if(message.equals("#offline")) {
                            viewModel.setOnline(false);
                            return;
                        }
                        viewModel.setOnline(true);
                        if(message.startsWith("#")){
                            message = message.replaceFirst("#","");
                            updateState(message);
                        }
                    }
                }
        }
    }

    private void updateState(Map<String, String> map){
        if(map == null) return;
        int childCount = dragLayout.getChildCount();
        for(int i=0;i<childCount;i++){
            PanelLayout panelLayout = (PanelLayout)dragLayout.getChildAt(i);
            String name = panelLayout.getName();
            if(map.containsKey(name)){
                panelLayout.setValue(map.get(name));
            }
        }
    }

    private void updateState(String message){
        Log.w(TAG, "Update state: "+message);
        int childCount = dragLayout.getChildCount();
        for(int i=0;i<childCount;i++){
            PanelLayout panelLayout = (PanelLayout)dragLayout.getChildAt(i);
            String name = panelLayout.getName();
            String[] data = new String[]{message};
            if(message.contains("#")){
                data = message.split("#");
            }
            for(String msg: data){
                if(msg.startsWith(name)){
                    //Log.w(TAG,panel.id+": "+panel.name+" : "+msg);
                    //IApplication.getExecutor().execute();
                    String value = msg.replaceFirst(name+",","");
                    panelLayout.setValue(value);
                }
            }
        }
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
            showToast("布局未保存");
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
            showToast("TCP连接成功: "+ ip+":"+port);
            Log.e(TAG, "TCP连接成功: "+ ip+":"+port);
            //tcpClient.send("status");
            reconnect = true;
        }

        @Override
        public void onConnectFail(String ip, int port) {
            if(viewModel.getOptionValue()==1){
                viewModel.setStatus("TCP未连接");
            }
            showToast("TCP连接已断开");
            Log.e(TAG,"TCP连接已断开");
            if(reconnect) tcpClient.connect(ip,port);
        }

        @Override
        public void onDataReceived(String message, int requestCode) {
            message = message.trim();
            //message = message.replaceAll("^state:","");
            //showToast("TCP message received: " + message);
            Log.e(TAG,"TCP >> " + message);
            updateState(message);
        }
    };

    OkHttpHelper.Callback httpCallback = new OkHttpHelper.Callback() {
        @Override
        public void result(String message, int code) {
            Log.e(TAG, code + ": " + message);
            try{
                StateBean bean = new Gson().fromJson(JsonParser.parseString(message), StateBean.class);
                if(!TextUtils.isEmpty(bean.message)){
                    showToast(bean.message);
                }
                updateState(bean.state);
                viewModel.setOnline(code == 200);
            }catch (Exception e){
                //e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
        }
    };
}