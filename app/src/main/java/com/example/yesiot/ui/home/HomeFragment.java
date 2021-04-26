package com.example.yesiot.ui.home;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.yesiot.DeviceAdapter;
import com.example.yesiot.R;
import com.example.yesiot.dialog.ConfirmDialog;
import com.example.yesiot.helper.DeviceHelper;
import com.example.yesiot.helper.PanelHelper;
import com.example.yesiot.object.Constants;
import com.example.yesiot.object.Device;
import com.example.yesiot.object.DeviceBean;
import com.example.yesiot.object.Panel;
import com.example.yesiot.service.IPScan;
import com.example.yesiot.service.MQTTConnection;
import com.example.yesiot.service.MQTTService;
import com.example.yesiot.dialog.DeviceDialog;
import com.example.yesiot.service.TcpClient;
import com.example.yesiot.util.LoadingDialog;
import com.example.yesiot.util.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements MQTTService.MQTTCallBack {
    final String TAG = "HomeFragment";
    private HomeViewModel homeViewModel;
    private ListView listView;
    private DeviceAdapter adapter;
    private List<Device> list = new ArrayList<>();
    MQTTConnection mqttConnection;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        //在fragment中使用oncreateOptionsMenu时需要在onCrateView中添加此方法，否则不会调用
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        listView = root.findViewById(R.id.devListView);
        listView.setEmptyView(root.findViewById(R.id.text_list_empty));
        adapter = new DeviceAdapter(getActivity(), list);
        listView.setAdapter(adapter);
        adapter.setOnActionClickListener((view, position) -> {
            Device device = list.get(position);
            if(view.getId() == R.id.item_action_edit){
                Bundle bundle = new Bundle();
                bundle.putInt("id", device.getId());
                bundle.putString("title", "编辑设备");
                Navigation.findNavController(getView()).navigate(R.id.nav_device, bundle);
            }else if(view.getId() == R.id.item_action_delete){
                ConfirmDialog.show(getParentFragmentManager(), "确认要删除此设备？", v -> {
                    if(DeviceHelper.remove(device.getId())) {
                        list.remove(position);
                        List<Device> newList = new ArrayList<>(list);
                        homeViewModel.setListValue(newList);
                        Utils.showToast("删除设备成功");
                    }
                });
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Device device = list.get(position);
                Bundle args = new Bundle();
                args.putInt("id", device.getId());
                args.putString("title", device.getName());
                Navigation.findNavController(getView()).navigate(R.id.nav_control, args);
            }
        });

        homeViewModel.getList().observe(getViewLifecycleOwner(), devices -> {
            list.clear();
            list.addAll(devices);
            adapter.notifyDataSetChanged();
            for(Device device: list){
                if(TextUtils.isEmpty(device.getSub())){
                    device.setSub(getTopic(device,"status"));
                }
                if(TextUtils.isEmpty(device.getTopic())){
                    device.setTopic(getTopic(device,"cmd"));
                }
                if(TextUtils.isEmpty(device.getPayload())){
                    device.setPayload("status");
                }
                if(MQTTService.isConnected()){
                    MQTTService.subscribe(device.getSub(),2);
                    MQTTService.publish(device.getTopic(), device.getPayload());
                }
            }
        });
        homeViewModel.getLocked().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                adapter.setEditable(!aBoolean);
            }
        });

        return root;
    }

    @Override
    public void onStart() {
        mqttConnection = MQTTConnection.getInstance();
        mqttConnection.setMqttCallBack(this);
        if(MQTTService.isConnected()){
            homeViewModel.setCloud("online");
        }
        getDeviceList();
        super.onStart();
    }

    private void getDeviceList(){
        List<Device> devices = DeviceHelper.getList();
        homeViewModel.setListValue(devices);
    }

    private String getTopic(Device device, String cmd){
        return Constants.TOPIC_PREFIX+"/"+device.getTheme()+"/"+device.getCode()+"/"+cmd;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.home, menu);
        observeMenu(menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.action_lock){
            boolean locked = homeViewModel.getLocked().getValue();
            homeViewModel.setLocked(!locked);
            return true;
        }else if(item.getItemId()==R.id.action_add){
            DeviceDialog dialogFragment = new DeviceDialog();
            dialogFragment.setCancelable(false);
            dialogFragment.setOnClickListener(v -> {
                if(v.getId()==R.id.add_device){
                    Navigation.findNavController(getView()).navigate(R.id.nav_device);
                }else if(v.getId()==R.id.scan_device){
                    startScanning();
                }
            });
            dialogFragment.show(getParentFragmentManager(),"DeviceDialog");
        }
        return super.onOptionsItemSelected(item);
    }

    public void observeMenu(Menu menu){
        homeViewModel.getLocked().observe(getViewLifecycleOwner(), aBoolean -> {
            MenuItem menuItem = menu.findItem(R.id.action_lock);
            int menuIcon = aBoolean ? R.drawable.ic_baseline_lock_24 : R.drawable.ic_baseline_lock_open_24;
            menuItem.setIcon(menuIcon);
        });

        homeViewModel.getCloud().observe(getViewLifecycleOwner(), s -> {
            //Utils.showToast("Status changed: " + s);
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

    private void startScanning() {
        //showLoading("开始扫描 ...");
        LoadingDialog loadingDialog = new LoadingDialog(getContext(), "开始扫描 ...");
        loadingDialog.show();
        IPScan ipScan = IPScan.getInstance();
        ipScan.setOnScanListener(new IPScan.OnScanListener() {
            @Override
            public void onPingSuccess(String ip) {
                Log.v(TAG,"Scanned IP >> "+ip);
                TcpClient client = new TcpClient();
                //client.setOnDataReceiveListener(onDataReceiveListener);
                client.setOnDataReceiveListener(new TcpClient.OnDataReceiveListener() {
                    @Override
                    public void onConnectSuccess(String ip, int port) {
                        Log.v(TAG,"Socket Connected >> "+ip+":"+port);
                        client.send("setting");
                    }

                    @Override
                    public void onConnectFail(String ip, int port) {
                        Log.v(TAG,"Socket Connect Failed >> "+ip+":"+port);
                    }

                    @Override
                    public void onDataReceived(String message, int requestCode) {
                        client.disconnect();
                        message = message.trim();
                        Log.v(TAG,"DataReceived from "+ip+": " + message);
                        try{
                            DeviceBean deviceBean = new Gson().fromJson(JsonParser.parseString(message), DeviceBean.class);
                            //Log.v(TAG, "Found Device: "+deviceBean.getTheme());
                            //Utils.showToast("Found Device <theme> "+deviceBean.getTheme());
                            deviceBean.setIp(ip);
                            if(handleFound(deviceBean)) {
                                getDeviceList();
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
                client.connect(ip, Constants.SCANNING_TCP_PORT, 1000);
            }

            @Override
            public void onScanning(int progress) {
                Log.v(TAG,"Finished "+progress+"%");
            }

            @Override
            public void onScanningDone(List<String> ipList) {
                Log.v(TAG,"Scanning Done>>"+new Gson().toJson(ipList));
                loadingDialog.dismiss();
            }
        });
        ipScan.startScanning();
    }

    private boolean handleFound(DeviceBean deviceBean){
        Size size = Utils.getScreenSize(getActivity());
        int margin = (size.getWidth() - 500) / 3;

        Device device = DeviceHelper.get("code=? and ip=?",new String[]{deviceBean.getCode(),deviceBean.getIp()});
        //Log.v(TAG, "Device code: "+device.code);
        device.setCode(deviceBean.getCode());
        device.setName(deviceBean.getName());
        device.setTheme(deviceBean.getTheme());
        device.setIp(deviceBean.getIp());
        if (device.getId() == 0) {
            Utils.showToast("发现新设备 "+ deviceBean.getName());
            if (DeviceHelper.save(device)){
                Utils.showToast("设备 "+ device.getName() +" 添加成功");
                List<String> pinList = deviceBean.getPins();
                for(String pin: pinList){
                    Panel panel = new Panel();
                    panel.deviceId = device.getId();
                    int position = pinList.indexOf(pin);
                    int left = position % 2 == 0 ? margin : 2 * margin + 250;
                    int top = (position/2) * (250 + 50) + 50;
                    panel.pos = left +"#" + top;
                    panel.title = "普通开关";
                    panel.unit = pin;
                    panel.width = 250;
                    panel.height = 250;
                    if(pin.startsWith("gpio,")){
                        panel.type = 2;
                        panel.on = pin+",1";
                        panel.off = pin+",0";
                    }else if(pin.startsWith("pwm,")){
                        panel.title = "普通按纽";
                        panel.type = 0;
                        panel.payload = pin+",1";
                    }else{
                        panel.title = pin;
                        panel.unit = "";
                        panel.type = 5;
                    }
                    PanelHelper.save(panel);
                }
                //updateList();
                return true;
            }else{
                Utils.showToast("设备 "+ device.getName() +" 添加失败");
            }
        }
        return false;
    }

    @Override
    public void onSuccess() {
        homeViewModel.setCloud("online");
        MQTTService.subscribe("/yesiot/esp8266/status",0);
        //MQTTService.publish("/yesiot/esp8266/status/","online");
        Utils.showToast("MQTT服务器连接成功");
        for(Device device:list){
            MQTTService.subscribe(device.getSub(),2);
            MQTTService.publish(device.getTopic(), device.getPayload());
        }
    }

    @Override
    public void onFailure() {
        homeViewModel.setCloud("failure");
    }

    @Override
    public void onLost() {
        homeViewModel.setCloud("offline");
    }

    @Override
    public void onMessageArrived(String topic, String message) {
        Log.e(TAG, "MessageArrived: ["+ topic +"]"+message);
        message = message.trim();
        //Utils.showToast("Current Status: "  + homeViewModel.getCloud().getValue());
        if(topic.equals("/yesiot/esp8266/status")){
            homeViewModel.setCloud(message);
        }
        for(Device device:list){
            int position = list.indexOf(device);
            String regex= Utils.getRegexBySub(device.getSub());
            if (topic.matches(regex)) {
                if(message.equals("online")){
                    device.setStatus(getString(R.string.online));
                }else if (message.equals("offline")) {
                    device.setStatus(getString(R.string.offline));
                }else{
                    device.setStatus("");
                }
                adapter.notifyDataSetChanged(listView, position);
            }
        }
    }
}