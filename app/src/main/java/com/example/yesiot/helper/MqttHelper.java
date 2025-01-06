package com.example.yesiot.helper;

import android.text.TextUtils;

import com.example.yesiot.object.Device;
import com.example.yesiot.object.EventBean;
import com.example.yesiot.service.MQTTService;
import com.google.gson.Gson;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MqttHelper {
    public static void send(Device device, String event){
        if(TextUtils.isEmpty(device.getTopic())){
            EventBean eventBean = new EventBean(device.getCode(), event);
            Gson gson = new Gson();
            String message = gson.toJson(eventBean);
            MQTTService.publish(message);
        }else{
            MQTTService.publish(device.getTopic(), event);
        }
    }

    public static void send(Device device, String event, int millis){
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(()->{
            try{
                Thread.sleep(millis);
                send(device, event);
            }catch (Exception e){
                e.printStackTrace();
            }
        });
    }
}
