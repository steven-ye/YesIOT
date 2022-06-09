package com.example.yesiot.helper;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;

import com.example.yesiot.object.Constants;
import com.example.yesiot.object.Device;
import com.example.yesiot.object.DeviceBean;
import com.example.yesiot.object.Panel;
import com.example.yesiot.service.MQTTService;
import com.example.yesiot.service.UdpClient;
import com.example.yesiot.util.LoadingDialog;
import com.example.yesiot.util.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SearchHelper implements UdpClient.Listener {
    final String TAG = "SearchHelper";
    Context mContext;
    LoadingDialog loadingDialog;
    UdpClient udpClient;
    int timeout = 0;
    int found = 0;

    public static SearchHelper Builder(Context context){
        return new SearchHelper(context);
    }

    public SearchHelper(Context context) {
        mContext = context;
        loadingDialog = new LoadingDialog(context, "努力扫描中，请稍等 ...");
        setTimeout(Constants.DEVICE_SEARCH_TIMEOUT);
        udpClient = new UdpClient();
        udpClient.setListener(this);
    }

    public void setTimeout(int timeout){
        this.timeout = timeout;
    }

    public void start(){
        udpClient.listen(Constants.DEVICE_SEARCH_PORT, timeout);

        //int brokerId = SPUtils.getInstance().getInt("broker_id");
        //Map<String,String> map = BrokerHelper.get(brokerId);
        //String message = map.get("host") + ":" + map.get("port");
        //udpClient.sendBroadcast(Constants.SCANNING_DEVICE_PORT, message);
        udpClient.send("255.255.255.255",Constants.DEVICE_SEARCH_PORT, Constants.DEVICE_SEARCH_MSG);
    }

    @Override
    public void onStart() {
        loadingDialog.show();
    }

    @Override
    public void onStop() {
        loadingDialog.dismiss();
        if(found==0)Utils.showToast("没有发现新设备");
    }

    @Override
    public void onDataReceive(String ip, int port, String message) {
        Log.v(TAG,"Message from "+ip+":"+port+" >> " + message);
        if(!message.startsWith("{") || !message.endsWith("}")) return;
        try{
            DeviceBean deviceBean = new Gson().fromJson(JsonParser.parseString(message), DeviceBean.class);
            if(TextUtils.isEmpty(deviceBean.host)) return;
            Map<String, String> map = BrokerHelper.get("host=? and port=?", new String[]{deviceBean.host, deviceBean.port+""});
            int broker_id = Integer.parseInt(Objects.requireNonNull(map.get("id")));
            if(broker_id>0 && broker_id == MQTTService.getBrokerId()){
                deviceBean.ip = ip;
                handleFound(deviceBean);
            }
        }catch (Exception e){
            e.printStackTrace();
            //Log.e(TAG, e.getMessage());
        }
    }

    private void handleFound(DeviceBean deviceBean){
        int brokerId = MQTTService.getBrokerId();
        Device device = DeviceHelper.get("code=? and broker_id=?",new String[]{deviceBean.code,""+brokerId});
        //Log.v(TAG, "Device code: "+device.code);
        if (device.getId() == 0) {
            Size size = Utils.getScreenSize(mContext);
            int panelSize = Utils.dp2px(mContext, Constants.DEFAULT_PANEL_SIZE)/100;
            panelSize = panelSize * 100;
            int margin = (size.getWidth() - panelSize * 2) / 3;

            Utils.showToast("发现新设备 "+ deviceBean.name);

            device.setCode(deviceBean.code);
            device.setName(deviceBean.name);
            device.setTopic(deviceBean.topicOut);
            device.setSub(deviceBean.topicIn);
            device.setIp(deviceBean.ip);
            device.setPort(deviceBean.tcpPort);
            device.setBrokerId(brokerId);

            if (DeviceHelper.save(device)){
                Log.w(TAG,"New Device: " + device.toString());
                int position = 0;
                Map<String, Integer> map = deviceBean.events;
                for(String name: map.keySet()){
                    int left = position % 2 == 0 ? margin : 2 * margin + panelSize;
                    int top = (position/2) * (panelSize + 50) + 50;

                    Panel panel = new Panel();
                    panel.deviceId = device.getId();
                    panel.pos = left +"#" + top;
                    panel.title = "普通按钮";
                    panel.name = name;
                    panel.unit = name;
                    panel.type = map.get(name);
                    panel.design = 1;
                    panel.width = panelSize;
                    panel.height = panelSize;
                    panel.size = panelSize+"#"+panelSize;
                    if(panel.type==1){
                        panel.title = "普通开关";
                        panel.on = "1";
                        panel.off = "0";
                    }else if(panel.type==2){
                        panel.unit = "";
                        panel.title = name;
                    }
                    if(PanelHelper.save(panel)){
                        Log.w(TAG,"添加按钮成功: "+ panel.id);
                    }else{
                        Log.e(TAG,"添加按钮失败: "+ panel.name);
                    }
                }
                found++;
                Utils.showToast("设备 "+ device.getName() +" 添加成功");
                if(null != mCallback){
                    mCallback.onFound(device);
                }
            }else{
                Utils.showToast("设备 "+ device.getName() +" 添加失败");
            }
        }
    }

    private Callback mCallback;
    public void setCallback(Callback callback){
        mCallback = callback;
    }

    public interface Callback{
        void onFound(Device device);
    }
}