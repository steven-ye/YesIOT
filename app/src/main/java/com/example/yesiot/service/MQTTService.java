package com.example.yesiot.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.example.yesiot.object.Broker;
import com.example.yesiot.util.Utils;

import info.mqtt.android.service.Ack;
import info.mqtt.android.service.MqttAndroidClient;
//import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Objects;

public class MQTTService extends Service {
    public static final String TAG = "MQTTService";

    private MQTTCallBack mCallBack;
    private MqttConnectOptions options;
    @SuppressLint("StaticFieldLeak")
    private static MqttAndroidClient client;

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();
        String channelID = "MqttService";
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel;
        Notification notification;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(channelID, TAG, NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);

            notification = new Notification.Builder(getApplicationContext(), channelID).build();
        }else{
            notification = new Notification.Builder(getApplicationContext()).build();
        }
        startForeground(1, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        //Log.d(getClass().getName(), "MQTT Service onBind");
        init();
        return new CustomBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Utils.createNotification(getApplicationContext(), "MQTT Service 已经启动");
        return super.onStartCommand(intent, flags, startId);
    }

    private void init() {
        //Map<String,String> settings = Utils.getBroker(getApplicationContext());
        Broker.get();
        if(TextUtils.isEmpty(Broker.host)){
            Utils.showToast(getApplicationContext(), "没有选择连接服务器");
            return;
        }

        String serverUrl = Broker.getUrl();
        // 服务器地址（协议+地址+端口号）
        Log.i(TAG, "Broker >>" + serverUrl);
        Log.i(TAG, "clientId >> " + Broker.clientId);

        if(TextUtils.isEmpty(Broker.host)){
            Utils.showToast(getApplicationContext(), "没有设置MQTT服务器地址");
            return;
        }

        client = new MqttAndroidClient(getApplicationContext(), serverUrl, Broker.clientId, Ack.AUTO_ACK);
        // 设置MQTT监听并且接受消息
        client.setCallback(mqttCallback);

        options = new MqttConnectOptions();
        // 设置超时时间，单位：秒
        options.setConnectionTimeout(Broker.timeout);
        // 心跳包发送间隔，单位：秒
        options.setKeepAliveInterval(Broker.alive);
        // 用户名
        options.setUserName(Broker.username);
        // 密码
        options.setPassword(Broker.password.toCharArray());     //将字符串转换为字符串数组
        // 自动重连
        options.setAutomaticReconnect(Broker.auto);
        // 设置清空Session，false表示服务器会保留客户端的连接记录，true表示每次以新的身份连接到服务器
        options.setCleanSession(Broker.session);

        boolean doConnect = true;
        // last will message
        //int qos = 0;
        //boolean retained = false;
        // 最后的遗嘱
        // MQTT本身就是为信号不稳定的网络设计的，所以难免一些客户端会无故的和Broker断开连接。
        //当客户端连接到Broker时，可以指定LWT，Broker会定期检测客户端是否有异常。
        //当客户端异常掉线时，Broker就往连接时指定的topic里推送当时指定的LWT消息。

        try {
            if(!TextUtils.isEmpty(Broker.message)){
                options.setWill(Broker.topic, Broker.message.getBytes(), Broker.qos, Broker.retained);
            }
        } catch (Exception e) {
            Log.i(TAG, "Exception Occured", e);
            doConnect = false;
            iMqttActionListener.onFailure(null, e);
        }

        if (doConnect) {
            doClientConnection();
        }

    }

    public static boolean isConnected(){
        return client != null && client.isConnected();
    }

    public static void publish(String msg){
        publish(Broker.topic + "/set", msg);
    }
    public static void publish(String topic, String msg){
        publish(topic,msg,0,false);
    }
    public static void publish(String topic, String msg, int qos, boolean retained){
        if(isConnected()){
            client.publish(topic, msg.getBytes(), qos, retained);
            Log.i(TAG, "MQTT <- ["+topic+"] "+msg);
        }else{
            Log.e(TAG,"发布失败：没有连接到broker.");
            Utils.showToast(client.getContext(), "发布失败：没有连接到broker.");
        }
    }

    public static void subscribe(String topic){
        subscribe(topic, 0);
    }
    public static void subscribe(String topic,int qos){
        if (isConnected()){
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
            Utils.showToast(client.getContext(), "订阅失败：没有连接到broker.");
        }
    }

    /** 连接MQTT服务器 */
    private void doClientConnection() {
        if (!client.isConnected() && isConnectIsNormal()) {
            client.connect(options, null, iMqttActionListener);
        }
    }

    // MQTT是否连接成功
    private final IMqttActionListener iMqttActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken arg0) {
            Log.v(TAG, "MQTT连接成功 ");
            if(mCallBack != null) {
                mCallBack.onSuccess();
            }
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            //arg1.printStackTrace();
            Utils.showToast(getApplicationContext(),"MQTT连接失败");
            Log.e(TAG, Objects.requireNonNull(arg1.getMessage()));
            // 连接失败，重连
            if(mCallBack != null) {
                mCallBack.onFailure();
            }
        }
    };

    // MQTT监听并且接受消息
    private final MqttCallbackExtended mqttCallback = new MqttCallbackExtended() {
        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            Log.v(TAG,"MQTT成功连接至 " + serverURI);
            Utils.createNotification(getApplicationContext(), "MQTT成功连接至 " + serverURI);
            // 订阅myTopic话题
            if(!TextUtils.isEmpty(Broker.topic))client.subscribe(Broker.topic,1);
            if (mCallBack != null && reconnect){
                mCallBack.onSuccess();
            }
        }

        @Override
        public void messageArrived(String topic, MqttMessage message)  {
            String payload = new String(message.getPayload());
            String str2 = topic + ";qos:" + message.getQos() + ";retained:" + message.isRetained();
            Log.i(TAG, str2);
            Log.i(TAG, "MQTT -> [" + topic + "] " + payload);
            if (mCallBack != null){
                mCallBack.onMessageArrived(topic,payload);
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {
            try {
                Log.i(TAG, "Message delivered: " + arg0.getMessage());
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void connectionLost(Throwable arg0) {
            Utils.createNotification(getApplicationContext(),"MQTT连接丢失");
            Utils.showToast(getApplicationContext(), "MQTT连接丢失");
            Log.i(TAG,"MQTT连接丢失");
            if(mCallBack != null)  mCallBack.onLost();
        }
    };

    /** 判断网络是否连接 */
    private boolean isConnectIsNormal() {
        Context context = getApplicationContext();
        if (isNetworkAvailable(context)) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            assert networkInfo != null;
            String name = networkInfo.getSubtypeName();
            Log.i(TAG, "MQTT当前网络名称：" + name);
            return true;
        } else {
            /*没有可用网络的时候，延迟3秒再尝试重连*/
            new Handler(getMainLooper()).postDelayed(this::doClientConnection, 3000);
            Utils.createNotification(getApplicationContext(),"没有可用网络");
            Utils.showToast(getApplicationContext(), "没有可用网络, 3秒后会再尝试连接");
            Log.e(TAG, "MQTT 没有可用网络, 3秒后会再尝试连接");
            return false;
        }
    }

    public boolean isNetworkAvailable(Context context) {
        if(context == null)  return false;

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return true;
                    }  else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)){
                        return true;
                    }
                }
            } else {
                try {
                    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                    if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                        Log.i("update_status", "Network is available : true");
                        return true;
                    }
                } catch (Exception e) {
                    Log.i("update_status", "" + e.getMessage());
                }
            }
        }
        Log.i("update_status","Network is available : FALSE ");
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
        //断开连接
        try {
            if(client != null){
                //client.disconnect();
                client.unregisterResources();
                client.close();
                client = null;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            Log.w(TAG, e);
        }
        Log.w(TAG, "MQTT服务被终止！");
    }

    public void setCallBack(MQTTCallBack callBack){
        mCallBack = callBack;
    }

    public class CustomBinder extends Binder {
        public MQTTService getService(){
            return MQTTService.this;
        }
    }

    public interface MQTTCallBack {
        void onSuccess();
        void onFailure();
        void onLost();
        void onMessageArrived(String topic, String message);
    }
}