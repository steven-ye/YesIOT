package com.example.yesiot.helper;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;

import com.example.yesiot.object.Broker;
import com.example.yesiot.object.Constants;
import com.example.yesiot.object.Device;
import com.example.yesiot.object.DeviceBean;
import com.example.yesiot.object.Panel;
import com.example.yesiot.service.UdpClient;
import com.example.yesiot.util.LoadingDialog;
import com.example.yesiot.util.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

import java.util.Map;
import java.util.Objects;

public class ScanHelper implements UdpClient.Listener {
    final String TAG = "SearchHelper";
    Context mContext;
    LoadingDialog loadingDialog;
    UdpClient udpClient;
    int timeout = Constants.DEVICE_SEARCH_TIMEOUT;
    int found = 0;

    public static ScanHelper Builder(Context context){
        return new ScanHelper(context);
    }

    public ScanHelper(Context context) {
        mContext = context;
        loadingDialog = new LoadingDialog(context, "努力扫描中，请稍等 ...");
        //setTimeout(Constants.DEVICE_SEARCH_TIMEOUT);
        udpClient = new UdpClient();
        udpClient.setListener(this);
    }

    public void setTimeout(int timeout){
        this.timeout = timeout;
    }

    public void start(){
        udpClient.listen(Constants.DEVICE_SEARCH_PORT, timeout);
        udpClient.broadcast(Constants.DEVICE_SEARCH_PORT, Constants.DEVICE_SEARCH_MSG, Constants.SEARCH_DEVICE_TIMES);
        //udpClient.send("255.255.255.255",Constants.DEVICE_SEARCH_PORT, Constants.DEVICE_SEARCH_MSG);
        //byte[] message = {Constants.PACKET_PREFIX, Constants.PACKET_TYPE_SEARCH_DEVICE_REQ};
        //udpClient.broadcast(Constants.DEVICE_SEARCH_PORT, message);
    }

    @Override
    public void onStart() {
        loadingDialog.show();
    }

    @Override
    public void onStop() {
        loadingDialog.dismiss();
        if(found==0)Utils.showToast(mContext, "没有发现新设备");
    }

    @Override
    public void onDataReceive(String ip, int port, String message) {
        //Log.d(TAG,"Message from "+ip+":"+port);
        Log.d(TAG, message);
        if(message.startsWith("{") || message.endsWith("}")){
            try{
                DeviceBean deviceBean = new Gson().fromJson(JsonParser.parseString(message), DeviceBean.class);
                if(TextUtils.isEmpty(deviceBean.server)) return;
                /*
                Map<String, String> map = BrokerHelper.get("host=? and port=?", new String[]{deviceBean.server, deviceBean.port});
                int broker_id = Integer.parseInt(Objects.requireNonNull(map.get("id")));
                if(broker_id>0 && broker_id == Broker.id){
                */
                if(Broker.id > 0){
                    Device device = DeviceHelper.get("code=? and broker_id=?",new String[]{deviceBean.code, "" + Broker.id});
                    if(device.getId()>0){
                        device.setIp(ip);
                        DeviceHelper.save(device);
                    }else{
                        deviceBean.ip = ip;
                        deviceBean.brokerId = Broker.id;
                        handleFound(deviceBean);
                    }
                }
            }catch (Exception e){
                //e.printStackTrace();
                Log.e(TAG, e.toString());
            }
        }else if(message.startsWith(Constants.DEVICE_SEARCH_MSG)) {
            message = message.substring(Constants.DEVICE_SEARCH_MSG.length());
            if(message.isEmpty()) return;
            Log.d(TAG,message);
            if(message.indexOf(',') > 0){
                String[] m = message.split(",");
                if(Broker.host.equals(m[1]) && !DeviceHelper.has(Broker.id, m[0])){
                    udpClient.send(ip, port, Constants.DEVICE_SEARCH_MSG + "1");
                }
            }
        }
    }

    private void handleFound(DeviceBean deviceBean){
        Device device = new Device(deviceBean.name, deviceBean.code, deviceBean.ip);
        device.setBrokerId(Broker.id);
        device.setTopic(deviceBean.topic+"/set");
        device.setSub(deviceBean.topic);
        device.setIp(deviceBean.ip);
        device.setPort(deviceBean.tcp);
        device.setWeight(DeviceHelper.getMaxID()+1);
        //Device device = DeviceHelper.get("code=? and broker_id=?",new String[]{deviceBean.code,""+brokerId});
        Log.v(TAG, "Device code: "+device.getCode());
        Utils.showToast(mContext,"发现新设备 "+ deviceBean.name);

        if (DeviceHelper.save(device)){
            Log.w(TAG,"New Device: " + device);
            Size size = Utils.getScreenSize(mContext);
            int panelSize = Utils.dp2px(mContext, Constants.DEFAULT_PANEL_SIZE)/100;
            panelSize = panelSize * 100;
            int margin = (size.getWidth() - panelSize * 2) / 3;
            int position = 0;
            Map<String, Integer> map = deviceBean.events;
            for(String name: map.keySet()){
                int left = position % 2 == 0 ? margin : 2 * margin + panelSize;
                int top = (position/2) * (panelSize + 50) + 50;
                position++;

                Panel panel = new Panel();
                panel.name = name;
                panel.deviceId = device.getId();
                panel.pos = left +"#" + top;
                panel.unit = name;
                if(map.get(name) != null) panel.type = map.get(name);
                if(panel.type > 1) panel.type -= 1;

                panel.design = 1;
                panel.width = panelSize;
                panel.height = panelSize;
                panel.size = panelSize+"#"+panelSize;
                switch(panel.type){
                    case 1:
                        panel.title = "普通开关";
                        break;
                    case 2:
                        panel.title = "数据内容";
                        break;
                    case 3:
                        panel.title = "文本内容";
                        break;
                    default:
                        panel.title = "普通按钮";
                }
                if(panel.type==1){
                    panel.title = "普通开关";
                    panel.on = "1";
                    panel.off = "0";
                }else if(panel.type==2){
                    panel.unit = "";
                    panel.title = name;
                }
                if(PanelHelper.save(panel)){
                    Log.w(TAG,"添加按钮成功: "+ panel.id + " [" + panel.deviceId + "]");
                }else{
                    Log.e(TAG,"添加按钮失败: "+ panel.name);
                }
            }
            found++;
            Utils.showToast(mContext,"设备 "+ device.getName() +" 添加成功");
            if(null != mCallback){
                mCallback.onFound(device);
            }
        }else{
            Utils.showToast(mContext,"设备 "+ device.getName() +" 添加失败");
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