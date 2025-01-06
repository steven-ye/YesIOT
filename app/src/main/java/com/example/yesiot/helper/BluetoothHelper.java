package com.example.yesiot.helper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BluetoothHelper {
    private static final String TAG = "BluetoothHelper";
    private static BluetoothAdapter mAdapter = null;
    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 0;
    //private static final UUID RFCOMM_UUID = UUID.fromString("00001801-0000-1000-8000-00805F9B34FB");
    //private final BluetoothAdapter mAdapter;//本手机的蓝牙适配器
    private static BluetoothLeScanner mLeScanner;//本手机蓝牙适配器上的扫描硬件
    private static DiscoveryCallback discoveryCallback;

    @SuppressLint("MissingPermission")
    public static void init(Activity activity)
    {
        requirePermission(activity);
        //checkPermission();
        registerReceiver(activity);

        mAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mAdapter != null) {
            if (!mAdapter.isEnabled()) mAdapter.enable();
            mLeScanner = mAdapter.getBluetoothLeScanner();
        }

        turnOnBluetooth(activity);
    }

    public static BluetoothAdapter getAdapter() {
        return mAdapter;
    }

    //打开蓝牙
    public static void turnOnBluetooth(Activity mActivity) {
        if (mAdapter != null && mAdapter.isEnabled()) return;
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            mActivity.startActivityForResult(intent, REQUEST_CODE_ENABLE_BLUETOOTH);
        }
    }

    public static void requirePermission(Activity mActivity) {
        List<String> mPermissionList = new ArrayList<>();
        // Android 版本大于等于 12 时，申请新的蓝牙权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mPermissionList.add(Manifest.permission.BLUETOOTH_SCAN);
            mPermissionList.add(Manifest.permission.BLUETOOTH_ADVERTISE);
            mPermissionList.add(Manifest.permission.BLUETOOTH_CONNECT);
            //根据实际需要申请定位权限
            mPermissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            mPermissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            mPermissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            mPermissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        ActivityCompat.requestPermissions(mActivity, mPermissionList.toArray(new String[0]), 102);
    }

    //关闭蓝牙
    @SuppressLint("MissingPermission")
    public static void turnOffBluetooth() {
        if (mAdapter != null) mAdapter.disable();
    }

    //打开蓝牙可见性
    @SuppressLint("MissingPermission")
    public static void enableVisibility(Context context) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        context.startActivity(intent);
    }

    //查找未绑定的蓝牙设备
    @SuppressLint("MissingPermission")
    public static void startDiscovery(DiscoveryCallback callback) {
        discoveryCallback = callback;
        new Thread(() -> {
            cancelDiscovery();
            mAdapter.startDiscovery();
        }).start();
    }

    @SuppressLint("MissingPermission")
    public static void cancelDiscovery() {
        if (mAdapter.isDiscovering()) {
            mAdapter.cancelDiscovery();
        }
    }

    //
    public static BluetoothDevice getDevice(String address) {
        assert mAdapter != null;
        return mAdapter.getRemoteDevice(address);
    }

    //查看已经绑定的设备列表
    public static List<BluetoothDevice> getBondedDevices(Activity mActivity) {
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return new ArrayList<>();
        }
        return new ArrayList<>(mAdapter.getBondedDevices());
    }

    //开启扫描
    @SuppressLint("MissingPermission")
    public static void startLeScan(ScanCallback scanCallback) {
        mLeScanner.startScan(scanCallback);
    }

    //关闭扫描
    @SuppressLint("MissingPermission")
    public static void stopLeScan(ScanCallback scanCallback) {
        mLeScanner.stopScan(scanCallback);
    }

    private static void registerReceiver(Activity mActivity) {
        //搜索开始的过滤器
        IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        //搜索结束的过滤器
        IntentFilter filter2 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        //寻找到设备的过滤器
        IntentFilter filter3 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        //绑定状态改变
        IntentFilter filter4 = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        //配对请求
        IntentFilter filter5 = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);

        mActivity.registerReceiver(mFindBlueToothReceiver, filter1);
        mActivity.registerReceiver(mFindBlueToothReceiver, filter2);
        mActivity.registerReceiver(mFindBlueToothReceiver, filter3);
        mActivity.registerReceiver(mFindBlueToothReceiver, filter4);
        mActivity.registerReceiver(mFindBlueToothReceiver, filter5);
    }

    public static void onDestroy(Activity activity) {
        activity.unregisterReceiver(mFindBlueToothReceiver);
    }

    /**
     * 配对
     *
     * @param device BluetoothDevice
     */
    public void pair(BluetoothDevice device) {
        Log.i(TAG, "pair device:" + device);
        Method createBondMethod;
        try {
            createBondMethod = BluetoothDevice.class.getMethod("createBond");
            createBondMethod.invoke(device);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 配对蓝牙设备
     */
    @SuppressLint("MissingPermission")
    public void bondDevice(@NonNull BluetoothDevice device) {
        //在配对之前，停止搜索
        //cancelDiscovery();
        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {//没配对才配对
            Log.d(TAG, "开始配对...");
            //如果这个设备取消了配对，则尝试配对
            device.createBond();
        }
    }

    /**
     * 取消配对（取消配对成功与失败通过广播返回 也就是配对失败）
     *
     * @param device BluetoothDevice
     *
     */
    @SuppressLint("MissingPermission")
    public static void unbondDevice(Activity activity, @NonNull BluetoothDevice device) {
        //mActivity.startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
        Log.d(TAG, "attemp to cancel bond:" + device.getName());
        try {
            Method removeBondMethod = device.getClass().getMethod("removeBond");
            Boolean returnValue = (Boolean) removeBondMethod.invoke(device);
            assert returnValue != null;
            if (returnValue) {
                Log.d(TAG, "取消配对成功");
            } else {
                activity.startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            Log.e(TAG, "attemp to cancel bond fail!");
        }
    }

    @SuppressLint("MissingPermission")
    //广播接收器，当远程蓝牙设备被发现时，回调函数onReceiver()会被执行
    private static final BroadcastReceiver mFindBlueToothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            switch (Objects.requireNonNull(action)) {
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    Log.d(TAG, "开始扫描...");
                    if (discoveryCallback != null) discoveryCallback.onScanStarted();
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Log.d(TAG, "结束扫描...");
                    if (discoveryCallback != null) discoveryCallback.onScanFinished();
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                    Log.d(TAG, "发现设备..." + device.getName() + "(" + device.getAddress() + ") RSSI: " + rssi);
                    if (discoveryCallback != null) discoveryCallback.onFoundDevice(device, rssi);
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    Log.d(TAG, "设备绑定状态改变...");
                    if (bondCallback != null) bondCallback.onStateChanged(device);
                    switch (Objects.requireNonNull(device).getBondState()) {
                        case BluetoothDevice.BOND_BONDING:
                            Log.w(TAG, "正在配对......");
                            break;
                        case BluetoothDevice.BOND_BONDED:
                            Log.w(TAG, "配对完成");
                            break;
                        case BluetoothDevice.BOND_NONE:
                            Log.w(TAG, "取消配对");
                        default:
                            break;
                    }
                    break;
            }
        }
    };

    private static BondCallback bondCallback;

    public static void setBondCallback(BondCallback callback) {
        bondCallback = callback;
    }

    public interface DiscoveryCallback {
        void onScanStarted();

        void onScanFinished();

        void onFoundDevice(BluetoothDevice device, int rssi);
    }

    public interface BondCallback {
        void onStateChanged(BluetoothDevice device);
    }
}
