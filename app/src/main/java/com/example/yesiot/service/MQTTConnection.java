package com.example.yesiot.service;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class MQTTConnection implements ServiceConnection {

    private MQTTService mqttService;
    private MQTTService.MQTTCallBack mqttCallBack;

    private static MQTTConnection instance;
    public static MQTTConnection getInstance(){
        if(instance==null){
            synchronized (MQTTConnection.class) {
                if(instance == null){
                    instance = new MQTTConnection();
                }
            }
        }
        return instance;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        mqttService = ((MQTTService.CustomBinder)iBinder).getService();
        mqttService.setCallBack(mqttCallBack);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Log.i("MQTTConnection", "MQTT连接断开");
    }

    public MQTTService getMqttService(){
        return mqttService;
    }

    public MQTTService.MQTTCallBack getMqttCallBack(){
        if(mqttService ==null) return null;
        return mqttService.getCallBack();
    }

    public void setMqttCallBack(MQTTService.MQTTCallBack callBack){
        mqttCallBack = callBack;
        if(mqttService != null){
            mqttService.setCallBack(mqttCallBack);
        }
    }
}