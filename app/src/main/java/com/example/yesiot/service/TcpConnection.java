package com.example.yesiot.service;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class TcpConnection implements ServiceConnection {

    private TcpService tcpService;
    private TcpService.TcpCallback tcpCallback;

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        tcpService = ((TcpService.CustomBinder)iBinder).getService();
        tcpService.setTcpCallback(tcpCallback);
        Log.v("TcpConnection","Socket Service connected");
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Log.e("TcpConnection","Socket Service disconnected");
        tcpService = null;
    }

    public TcpService getTcpService(){
        return tcpService;
    }

    public void setTcpCallback(TcpService.TcpCallback callback){
        tcpCallback = callback;
    }
}
