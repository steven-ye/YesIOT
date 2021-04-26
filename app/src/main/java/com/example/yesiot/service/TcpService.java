package com.example.yesiot.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class TcpService extends Service {
    String TAG = "TcpService";

    private TcpClient client;
    private TcpCallback tcpCallback;

    @Override
    public void onCreate() {
        super.onCreate();
        client = TcpClient.getInstance();
        client.setOnDataReceiveListener(dataReceiveListener);
    }

    private void connect(String ip, int port){
        Log.i(TAG,"TCP server is "+ip+":"+port);
        if(isConnectIsNormal()){
            client.connect(ip,port);
        }
    }

    public void send(String msg){
        client.sendStrCmd(msg.trim(),1001);
    }

    public boolean isConnected(){
        return client!=null&&client.isConnect();
    }

    /** 判断网络是否连接 */
    private boolean isConnectIsNormal() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            String name = info.getTypeName();
            Log.v(TAG, "当前网络名称: " + name);
            return true;
        } else {
            Log.v(TAG, "==没有可用网络==");
            return false;
        }
    }

    private final TcpClient.OnDataReceiveListener dataReceiveListener = new TcpClient.OnDataReceiveListener() {
        @Override
        public void onConnectSuccess(String ip, int port) {
            Log.v(TAG, "连接到 >> "+ip+":"+port + " 成功");
            if(tcpCallback != null){
                tcpCallback.onConnectionSuccess(ip,port);
            }
        }

        @Override
        public void onConnectFail(String ip, int port) {
            Log.e(TAG, "连接到 >> "+ip+":"+port + " 失败");
            if(tcpCallback != null){
                tcpCallback.onConnectionFail();
            }
        }

        @Override
        public void onDataReceived(String message, int requestCode) {
            Log.v(TAG, "== 收到消息 == ");
            Log.v(TAG, message);
            if(tcpCallback != null){
                tcpCallback.onDataReceived(message,requestCode);
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String ip = intent.getStringExtra("ip");
        int port = intent.getIntExtra("port",80);
        connect(ip,port);
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "==onBind==");
        String ip = intent.getStringExtra("ip");
        int port = intent.getIntExtra("port",80);
        connect(ip,port);
        return new CustomBinder();
    }

    @Override
    public void onDestroy() {
        if (client != null) {
            client.disconnect();
            client=null;
        }
        super.onDestroy();
    }

    public void setTcpCallback(TcpService.TcpCallback callBack){
        tcpCallback = callBack;
    }

    public class CustomBinder extends Binder {
        public TcpService getService(){
            return TcpService.this;
        }
    }

    public interface TcpCallback {
        void onConnectionSuccess(String ip, int port);
        void onConnectionFail();
        void onDataReceived(String message, int requestCode);
    }
}