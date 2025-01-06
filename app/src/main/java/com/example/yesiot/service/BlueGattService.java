package com.example.yesiot.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;

import androidx.core.app.ActivityCompat;

public class BlueGattService extends Service {
    private final IBinder mBinder = new LocalBinder();
    private BluetoothAdapter mBluetoothAdapter;
    BluetoothGatt mBluetoothGatt;
    private BluetoothGattCallback gattCallback;
    String mBleAddress = "";

    // 定义一个UUID，用于标识蓝牙服务
    //private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public class LocalBinder extends Binder {
        BlueGattService getService() {
            return BlueGattService.this;
        }
    }

    public BlueGattService() {
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
        return mBluetoothAdapter != null; // 设备不支持蓝牙
    }

    //开启扫描
    public void startLeScan(ScanCallback scanCallback) {
        BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        scanner.startScan(scanCallback);
    }

    //关闭扫描
    @SuppressLint("MissingPermission")
    public void stopLeScan(ScanCallback scanCallback) {
        BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
        scanner.stopScan(scanCallback);
    }

    //连接蓝牙设备
    public boolean connect(String address) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mBleAddress);
        return connect(device);
    }

    //连接蓝牙设备
    @SuppressLint("MissingPermission")
    public boolean connect(BluetoothDevice device) {
        try {
            mBluetoothGatt = device.connectGatt(getApplicationContext(), false, gattCallback);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false; // 连接失败
        }
    }

    @SuppressLint("MissingPermission")
    public void disconnect() {
        mBluetoothGatt.close();
        mBluetoothGatt = null;
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