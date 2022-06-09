package com.example.yesiot;

import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Size;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.yesiot.ui.dialog.ConfirmDialog;
import com.example.yesiot.util.Utils;

public abstract class AbsFragment extends Fragment {
    protected final String TAG = getClass().getSimpleName();
    protected Context mContext;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        //在fragment中使用oncreateOptionsMenu时需要在onCrateView中添加此方法，否则不会调用
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        mContext = getContext();
    }

    public MainActivity getMainActivity(){
        return (MainActivity)getActivity();
    }

    public void setTitle(String title) {
        getMainActivity().getSupportActionBar().setTitle(title);
    }

    public void confirm(String message, ConfirmDialog.OnConfirmListener okayListener){
        ConfirmDialog.show(getParentFragmentManager(),message,okayListener);
    }

    public void alert(String message){
        Utils.alert(getContext(), message);
    }

/*
    private void searchDevice() {
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
                client.setCallback(new TcpClient.TcpCallback() {
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
                            //deviceBean.setIp(ip);
                            if(handleFound(deviceBean)) {
                                getDeviceList();
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
                client.connect(ip, Constants.SCANNING_DEVICE_PORT, 500);
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
        ipScan.startScanning(getContext());
    }

    private boolean handleFound(DeviceBean deviceBean){
        Size size = Utils.getScreenSize(getActivity());
        int panelSize = Utils.dp2px(getContext(), Constants.DEFAULT_PANEL_SIZE);
        int margin = (size.getWidth() - panelSize * 2) / 3;

        Device device = DeviceHelper.get("code=? and broker_id=?",new String[]{deviceBean.getCode(),brokerId+""});
        //Log.v(TAG, "Device code: "+device.code);
        device.setCode(deviceBean.getCode());
        device.setName(deviceBean.getName());
        device.setTopic(deviceBean.getTopic());
        device.setSub(deviceBean.getTopic());
        device.setIp(deviceBean.getIp());
        if (device.getId() == 0) {
            Utils.showToast("发现新设备 "+ deviceBean.getName());
            device.setBrokerId(brokerId);
            if (DeviceHelper.save(device)){
                Utils.showToast("设备 "+ device.getName() +" 添加成功");
                List<String> pinList = deviceBean.getPins();
                for(String pin: pinList){
                    Panel panel = new Panel();
                    panel.deviceId = device.getId();
                    int position = pinList.indexOf(pin);
                    int left = position % 2 == 0 ? margin : 2 * margin + panelSize;
                    int top = (position/2) * (panelSize + 50) + 50;
                    panel.pos = left +"#" + top;
                    panel.title = "普通开关";
                    panel.unit = pin;
                    panel.width = panelSize;
                    panel.height = panelSize;
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

 */
}