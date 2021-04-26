package com.example.yesiot.service;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class TcpConnection implements ServiceConnection {

    private TcpService service;
    private TcpClient.TcpCallback tcpCallback;

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        service = ((TcpService.CustomBinder)iBinder).getService();
        service.setTcpCallback(tcpCallback);
        Log.v("TcpConnection","Socket Service connected");
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Log.e("TcpConnection","Socket Service disconnected");
        service = null;
    }

    public TcpService getService(){
        return service;
    }

    public void setTcpCallback(TcpClient.TcpCallback callback){
        tcpCallback = callback;
    }
}
