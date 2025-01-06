package com.example.yesiot.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class BlueSocketService extends Service {
    private final IBinder mBinder = new LocalBinder();
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mBluetoothSocket;
    private InputStream mInputStream;
    private boolean mReadingData = false;
    String mBleAddress = "";

    // 定义一个UUID，用于标识蓝牙服务
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public class LocalBinder extends Binder {
        BlueSocketService getService() {
            return BlueSocketService.this;
        }
    }

    public BlueSocketService(){
        super();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public IBinder onBind(Intent intent) {
        //mBleAddress = intent.getStringExtra("address");
        initBluetooth();
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //mBleAddress = intent.getStringExtra("address");
        initBluetooth();
        return super.onStartCommand(intent, flags, startId);
    }

    // 初始化蓝牙连接
    public boolean initBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            return false; // 设备不支持蓝牙
        }
        return true;
    }

    public boolean connect(String address){
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mBleAddress);
        try {
            mBluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID);
            mBluetoothSocket.connect();
            mInputStream = mBluetoothSocket.getInputStream();
            return true;
        } catch (IOException e) {
            return false; // 连接失败
        }
    }


    // 开始读取数据
    public void startReadingData() {
        mReadingData = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[1024];
                int bytes;

                while (mReadingData) {
                    try {
                        bytes = mInputStream.read(buffer);
                        // 处理从蓝牙设备读取的数据
                        // 发送数据给Activity或者做其他处理
                        byte[] result = new byte[bytes];
                        System.arraycopy(buffer, 0, result, 0, bytes);
                        if(mListener != null) mListener.onReceiveData(result);
                    } catch (IOException e) {
                        break;
                    }
                }
            }
        }).start();
    }

    // 停止读取数据
    public void stopReadingData() {
        mReadingData = false;
    }

    // 断开蓝牙连接
    public void disconnectBluetooth() {
        if (mBluetoothSocket != null) {
            try {
                mReadingData = false;
                mInputStream.close();
                mBluetoothSocket.close();
            } catch (IOException e) {
                // 处理异常
            }
        }
    }

    Listener mListener = null;
    public void setListener(Listener listener){
        mListener = listener;
    }

    public interface Listener {
        void onConnectSuccess();
        void onConnectFail();
        void onReceiveData(byte[] data);
    }
}