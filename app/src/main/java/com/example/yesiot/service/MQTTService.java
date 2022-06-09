package com.example.yesiot.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.yesiot.MainActivity;
import com.example.yesiot.R;
import com.example.yesiot.helper.BrokerHelper;
import com.example.yesiot.util.SPUtils;
import com.example.yesiot.util.Utils;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Map;
import java.util.Objects;

public class MQTTService extends Service {
    public static final String TAG = "MQTTService";

    private final int NOTIFICATION_ID = 1001;
    NotificationManager notificationManager;

    private MQTTCallBack mCallBack;
    @SuppressLint("StaticFieldLeak")
    private static MqttAndroidClient client;
    private MqttConnectOptions options;

    private boolean autoReconnect = false;
    private String lastWillTopic = "";      //要订阅的主题

    public static int getBrokerId(){
        return SPUtils.getInstance().getInt("broker_id");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(getClass().getName(), "onBind");
        init();
        return new CustomBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotification();
        return super.onStartCommand(intent, flags, startId);
    }

    private void init() {
        //Map<String,String> settings = Utils.getBroker(getApplicationContext());
        int brokerId = getBrokerId();
        if(!BrokerHelper.has(brokerId)){
            Utils.showToast("没有选择连接服务器");
            return;
        }
        Map<String,String> map = BrokerHelper.get(brokerId);

        String protocol = map.get("protocol");
        String host = map.get("host");
        String port = map.get("port");
        String path = map.get("path");
        if(TextUtils.isEmpty(protocol))protocol="tcp";
        String clientId = map.get("clientId");
        String userName = map.get("username");
        String passWord = map.get("password");
        int alive = Integer.parseInt(map.get("alive"));
        int timeout = Integer.parseInt(map.get("timeout"));
        boolean cleanSession = Objects.equals(map.get("session"),"yes");
        autoReconnect= Objects.equals(map.get("auto"),"yes");
        String lastWill = map.get("message");
        lastWillTopic = map.get("topic");

        if(TextUtils.isEmpty(protocol))protocol="tcp";
        if(TextUtils.isEmpty(port))port="1883";
        if(TextUtils.isEmpty(clientId))clientId=Utils.getRandomString(8);
        if(TextUtils.isEmpty(passWord))passWord="";

        String serverUrl = protocol+"://"+host+":"+port;
        if(!TextUtils.isEmpty(path)){
            serverUrl = serverUrl + path;
        }
        // 服务器地址（协议+地址+端口号）
        Log.i(TAG, "Broker >>" + serverUrl);
        Log.i(TAG, "clientId >> " + clientId);
        if(TextUtils.isEmpty(host)){
            Utils.showToast("没有设置MQTT服务器地址");
            return;
        }

        client = new MqttAndroidClient(getApplicationContext(), serverUrl, clientId);
        // 设置MQTT监听并且接受消息
        client.setCallback(mqttCallback);

        options = new MqttConnectOptions();
        // 设置超时时间，单位：秒
        options.setConnectionTimeout(timeout);
        // 心跳包发送间隔，单位：秒
        options.setKeepAliveInterval(alive);
        // 用户名
        options.setUserName(userName);
        // 密码
        options.setPassword(passWord.toCharArray());     //将字符串转换为字符串数组
        // 自动重连
        options.setAutomaticReconnect(autoReconnect);
        // 设置清空Session，false表示服务器会保留客户端的连接记录，true表示每次以新的身份连接到服务器
        options.setCleanSession(cleanSession);

        boolean doConnect = true;
        // last will message
        int qos = 0;
        boolean retained = false;
        // 最后的遗嘱
        // MQTT本身就是为信号不稳定的网络设计的，所以难免一些客户端会无故的和Broker断开连接。
        //当客户端连接到Broker时，可以指定LWT，Broker会定期检测客户端是否有异常。
        //当客户端异常掉线时，Broker就往连接时指定的topic里推送当时指定的LWT消息。

        try {
            if(!TextUtils.isEmpty(lastWill)){
                options.setWill(lastWillTopic, lastWill.getBytes(), qos, retained);
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
    public static void publish(String topic, String msg){
        publish(topic,msg,0,false);
    }
    public static void publish(String topic, String msg, int qos, boolean retained){
        try {
            if(client.isConnected()){
                client.publish(topic, msg.getBytes(), qos, retained);
                Log.i(TAG, "MQTT <- ["+topic+"] "+msg);
            }else{
                Log.e(TAG,"发布失败：没有连接到broker.");
                Utils.showToast("发布失败：没有连接到broker.");
            }
        } catch (MqttException e) {
            e.printStackTrace();
            Log.e(TAG, "发布失败 >> " + e.getMessage());
        }
    }

    public static void subscribe(String topic){
        subscribe(topic, 2);
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
            Log.e(TAG, "订阅失败 >> " + e.getMessage());
        }
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
            Log.i(TAG, "MQTT连接成功 ");
            if(mCallBack != null) {
                mCallBack.onSuccess();
            }
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            arg1.printStackTrace();
            Utils.showToast(getApplicationContext(),"MQTT连接失败");
            Log.e(TAG, arg1.getMessage());
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
            Log.i(TAG,"MQTT成功连接至 " + serverURI);
            Utils.createNotification(getApplicationContext(), "MQTT成功连接至 " + serverURI);
            try {
                // 订阅myTopic话题
                client.subscribe(lastWillTopic,1);
            } catch (MqttException e) {
                e.printStackTrace();
            }
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
            Utils.showToast("MQTT连接丢失");
            Log.i(TAG,"MQTT连接丢失");
            if(mCallBack != null)  mCallBack.onLost();
        }
    };

    /** 判断网络是否连接 */
    private boolean isConnectIsNormal() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            String name = info.getTypeName();
            Log.i(TAG, "MQTT当前网络名称：" + name);
            return true;
        } else {
            /*没有可用网络的时候，延迟3秒再尝试重连*/
            new Handler(getMainLooper()).postDelayed(this::doClientConnection, 3000);
            Utils.createNotification(getApplicationContext(),"没有可用网络");
            Utils.showToast(getApplicationContext(), "没有可用网络, 3秒后会再尝试连接");
            Log.i(TAG, "MQTT 没有可用网络, 3秒后会再尝试连接");
            return false;
        }
    }

    private void createNotification(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("mqttchannel", "MQTT服务", NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(false);
            channel.setVibrationPattern(new long[]{0});
            channel.enableLights(false);
            channel.setSound(null, null);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
        startForeground(NOTIFICATION_ID, buildNotification());
    }

    private Notification buildNotification() {
        Intent intent=new Intent(getApplicationContext(), MainActivity.class);
        //PendingIntent点击通知后跳转，一参：context 二参：一般为0 三参：intent对象 四参：一般为0
        PendingIntent pendingIntent=PendingIntent.getActivity(getApplicationContext(),1,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("已开启MQTT服务")
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true) //完成跳转自动取消通知
                .setWhen(System.currentTimeMillis())
                .setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE)
                .setVibrate(new long[]{0})
                .setSound(null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("mqttchannel");
        }
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_FOREGROUND_SERVICE;
        return notification;
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
            e.printStackTrace();
        }
        Log.w(TAG, "MQTT服务被终止！");
        if(notificationManager!=null)notificationManager.cancel(NOTIFICATION_ID);
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