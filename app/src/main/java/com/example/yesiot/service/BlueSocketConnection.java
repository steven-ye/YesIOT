package com.example.yesiot.service;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class BlueSocketConnection implements ServiceConnection {

    private BlueSocketService blueSPPService;

    private static BlueSocketConnection instance;
    public static BlueSocketConnection getInstance(){
        if(instance==null){
            synchronized (BlueSocketConnection.class) {
                if(instance == null){
                    instance = new BlueSocketConnection();
                }
            }
        }
        return instance;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        blueSPPService = ((BlueSocketService.LocalBinder)iBinder).getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Log.i("BluetoothConnection", "Bluetooth Service连接断开");
    }

    public BlueSocketService getMqttService(){
        return blueSPPService;
    }

    public void setMqttCallBack(BlueSocketService.Listener listener){
        if(blueSPPService != null){
            blueSPPService.setListener(listener);
        }
    }
}