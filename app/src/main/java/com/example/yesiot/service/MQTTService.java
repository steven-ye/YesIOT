package com.example.yesiot.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.yesiot.R;
import com.example.yesiot.object.Constants;
import com.example.yesiot.util.SPUtils;
import com.example.yesiot.util.Utils;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MQTTService extends Service {
    public static final String TAG = MQTTService.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")
    private static MqttAndroidClient client;
    private MqttConnectOptions options;
    private MQTTCallBack mCallBack;

    private String host = "tcp://192.168.1.2:1883";
    private String userName;
    private String passWord;
    private String clientId;//客户端标识
    private int alive = 60;
    private int timeout = 30;
    private boolean autoReconnect = false;
    private static String lastWillTopic = "/yesiot/android/phone/lastwill";      //要订阅的主题
    private static String lastWill="";      //要订阅的主题


    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(getClass().getName(), "onCreate");

        Map<String,String> settings = SPUtils.getInstance().getMap(Constants.BROKER);
        if(TextUtils.isEmpty(settings.get("ip"))){
            Utils.showToast("没有设置连接信息或IP地址为空");
            return;
        }
        host = settings.get("protocol")+"://"+settings.get("ip")+":"+settings.get("port");
        clientId = settings.get("clientId");
        userName = settings.get("username");
        passWord = settings.get("password");
        if(userName==null) userName = "";
        if(passWord==null) passWord = "";
        if(clientId==null) clientId = Utils.getIMEIDeviceId(getApplicationContext());

        if(!TextUtils.isEmpty(settings.get("alive"))){
            alive = Integer.parseInt(settings.get("alive"));
        }
        if(!TextUtils.isEmpty(settings.get("timeout"))){
            timeout = Integer.parseInt(settings.get("timeout"));
        }
        if(!TextUtils.isEmpty(settings.get("topic"))){
            lastWillTopic = settings.get("topic");
        }
        if(!TextUtils.isEmpty(settings.get("message"))){
            lastWill = settings.get("message");
        }
        autoReconnect = Objects.equals(settings.get("auto"),"yes");
        Log.i(TAG, "Broker >>" + host);
        init();
    }

    public static boolean isConnected(){
        return client != null && client.isConnected();
    }

    public static void publish(String topic, String msg){
        int qos = 0;
        boolean retained = false;
        try {
            if (client != null){
                client.publish(topic, msg.getBytes(), (int) qos, retained);
            }else{
                Log.e(TAG,"发布失败：没有连接到broker.");
                Utils.showToast("发布失败：没有连接到broker.");
            }
        } catch (MqttException e) {
            e.printStackTrace();
            Log.v(TAG, "发布失败 >> " + e.getMessage());
        }
    }
    public static void subscribe(String topic,int qos){
        try {
            if (client != null){
                client.subscribe(topic, qos,null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.v(TAG,"Subscribed >> "+topic);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.e(TAG,"Failed to subscribe >> "+topic);
                    }
                });
            }else{
                Log.e(TAG,"订阅失败：没有连接到broker.");
                Utils.showToast("订阅失败：没有连接到broker.");
            }
        } catch (MqttException e) {
            e.printStackTrace();
            Log.v(TAG, "订阅失败 >> " + e.getMessage());
        }
    }

    private void init() {
        // 服务器地址（协议+地址+端口号）
        String uri = host;
        client = new MqttAndroidClient(this, uri, clientId);
        // 设置MQTT监听并且接受消息
        client.setCallback(mqttCallback);

        options = new MqttConnectOptions();
        // 清除缓存
        options.setCleanSession(true);
        // 设置超时时间，单位：秒
        options.setConnectionTimeout(timeout);
        // 心跳包发送间隔，单位：秒
        options.setKeepAliveInterval(alive);
        // 用户名
        options.setUserName(userName);
        // 密码
        options.setPassword(passWord.toCharArray());     //将字符串转换为字符串数组

        options.setAutomaticReconnect(autoReconnect);

        // last will message
        boolean doConnect = true;
        //String message = "{\"terminal_uid\":\"" + clientId + "\"}";
        Log.e(getClass().getName(), "message是:" + lastWill);
        int qos = 0;
        boolean retained = false;
        // 最后的遗嘱
        // MQTT本身就是为信号不稳定的网络设计的，所以难免一些客户端会无故的和Broker断开连接。
        //当客户端连接到Broker时，可以指定LWT，Broker会定期检测客户端是否有异常。
        //当客户端异常掉线时，Broker就往连接时指定的topic里推送当时指定的LWT消息。

        try {
            options.setWill(lastWillTopic, lastWill.getBytes(), qos, retained);
        } catch (Exception e) {
            Log.i(TAG, "Exception Occured", e);
            doConnect = false;
            iMqttActionListener.onFailure(null, e);
        }

        if (doConnect) {
            doClientConnection();
        }

    }

    @Override
    public void onDestroy() {
        stopSelf();
        try {
            if(client != null){
                client.disconnect();
                client.unregisterResources();
                //client.close();
                client = null;
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    /** 连接MQTT服务器 */
    private void doClientConnection() {
        if (!client.isConnected() && isConnectIsNormal()) {
            try {
                client.connect(options, null, iMqttActionListener);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    // MQTT是否连接成功
    private final IMqttActionListener iMqttActionListener = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken arg0) {
            Log.i(TAG, "连接成功 ");
            try {
                // 订阅myTopic话题
                client.subscribe(lastWillTopic,1);
            } catch (MqttException e) {
                e.printStackTrace();
            }
            if(mCallBack != null) {
                mCallBack.onSuccess();
            }
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            arg1.printStackTrace();
            Utils.showToast(getApplicationContext(),arg1.getMessage());
            // 连接失败，重连
            if(mCallBack != null) {
                mCallBack.onFailure();
            }
        }
    };

    // MQTT监听并且接受消息
    private final MqttCallback mqttCallback = new MqttCallback() {

        @Override
        public void messageArrived(String topic, MqttMessage message)  {

            String str1 = new String(message.getPayload());
            String str2 = topic + ";qos:" + message.getQos() + ";retained:" + message.isRetained();
            Log.i(TAG, "messageArrived: [" + topic + "] " + str1);
            Log.i(TAG, str2);
            if (mCallBack != null){
                mCallBack.onMessageArrived(topic,str1);
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {

        }

        @Override
        public void connectionLost(Throwable arg0) {
            // 失去连接，重连
            Utils.showToast("连接丢失");
            /*
            if(autoReconnect){
                doClientConnection();
            }*/
            if(mCallBack != null)  mCallBack.onLost();
        }
    };

    /** 判断网络是否连接 */
    private boolean isConnectIsNormal() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            String name = info.getTypeName();
            Log.i(TAG, "MQTT当前网络名称：" + name);
            return true;
        } else {
            Log.i(TAG, "MQTT 没有可用网络");
            return false;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.e(getClass().getName(), "onBind");
        return new CustomBinder();
    }

    public MQTTCallBack getCallBack(){
         return mCallBack;
    }
    public void setCallBack(MQTTCallBack callBack){
        mCallBack = callBack;
    }

    public class CustomBinder extends Binder {
        public MQTTService getService(){
            return MQTTService.this;
        }
    }

    public void createNotification(String title, String message){
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, new Intent(this,MQTTService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"MQTTService");//3、创建一个通知，属性太多，使用构造器模式

        Notification notification = builder
                .setTicker("MQTT Service")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setContentInfo("")
                .setContentIntent(pendingIntent)//点击后才触发的意图，“挂起的”意图
                .setAutoCancel(true)        //设置点击之后notification消失
                .build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        startForeground(0, notification);
        notificationManager.notify(0, notification);
    }

    public interface MQTTCallBack {
        void onSuccess();
        void onFailure();
        void onLost();
        void onMessageArrived(String topic, String message);
    }
}