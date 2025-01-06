package com.example.yesiot.object;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.yesiot.helper.BrokerHelper;
import com.example.yesiot.util.SPUtil;
import com.example.yesiot.util.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Broker {
    public static int id = 0;
    public static int userId = 0;
    public static int port = 9501;
    public static String host = "bemfa.com";
    public static String path = "";
    public static String protocol = "tcp";
    public static String clientId = "181bc855e5877b283f33d5273e570b07";
    public static String username = "";
    public static String password = "";
    public static String topic = "yesiot";
    public static String message = "";
    public static int alive = 30;
    public static int timeout = 60;
    public static boolean session = false;
    public static boolean auto = true;
    public static boolean retained = false;
    public static int qos = 0;

    public static void get(){
        Map<String, String> map = new HashMap<>();
        id = SPUtil.getBrokerId();
        if(id==0) {
            map.put("name", "bemfa.com");
            map.put("host", host);
            map.put("port", port+"");
            map.put("path", "");
            map.put("protocol", protocol);
            map.put("clientId", clientId);
            map.put("username", username);
            map.put("password", password);
            map.put("topic", topic);
            map.put("message", message);
            map.put("alive", alive+"");
            map.put("timeout", timeout+"");
            map.put("auto", "yes");
            map.put("session", "yes");
            if(BrokerHelper.save(map)){
                id = 1;
                SPUtil.putBrokerId(id);
            }
        }else {
            try {
                map = BrokerHelper.get(Broker.id);
                host = map.get("host");
                port = Integer.parseInt(map.getOrDefault("port", "1883"));
                path = map.get("path");
                protocol = map.getOrDefault("protocol", "tcp");
                clientId = map.get("clientId");
                username = map.get("username");
                password = map.get("password");
                topic = map.get("topic");
                message = map.get("message");
                alive = Integer.parseInt(map.getOrDefault("alive", "30"));
                timeout = Integer.parseInt(map.getOrDefault("timeout", "60"));
                session = Objects.equals(map.get("alive"), "yes");
                auto = Objects.equals(map.get("auto"), "yes");
            } catch (Exception e) {
                Log.e("Broker.get", e.getMessage());
            }
        }

        if(TextUtils.isEmpty(protocol))protocol = "tcp";
        if(TextUtils.isEmpty(clientId))clientId= Utils.getRandomString(8);
    }

    public static String getUrl(){
        String url = protocol + "://" + host + ":" + port;
        if(!TextUtils.isEmpty(path)) url += path;
        return url;
    }
}
