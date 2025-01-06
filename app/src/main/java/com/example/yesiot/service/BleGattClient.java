package com.example.yesiot.service;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.yesiot.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BleGattClient {
    private static final String TAG = "BleGattClient";
    private final String NOTIFY_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb";
    private String mServiceUUID = "0000fff0-0000-1000-8000-00805f9b34fb";
    Context mContext;
    BluetoothAdapter mAdapter;
    BluetoothDevice mDevice;
    BluetoothGatt mBluetoothGatt;
    BluetoothGattCharacteristic sendGattCharacteristic;
    GattListener mGattListener;
    boolean autoConnect = false;
    final int GattConnectMax = 3;
    int gattConnectTime = 3;

    public interface GattListener {
        void onConnectSuccess(BluetoothGatt gatt);
        void onDisconnect(BluetoothGatt gatt);
        void onConnectFail(BluetoothGatt gatt);
        void onServicesDiscovered(BluetoothGatt gatt);
        void onReceiveData(byte[] data);
        void onSendSuccess(byte[] data);
        void onNotifySuccess(BluetoothGatt gatt);
    }

    @SuppressLint("MissingPermission")
    public BleGattClient(Context context){
        mContext = context;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mAdapter != null){
            if(!mAdapter.isEnabled()) mAdapter.enable();
        }
    }
    public BleGattClient(Context context, GattListener listener){
        this(context);
        mGattListener = listener;
    }

    public void setAutoConnect(boolean auto){
        autoConnect = auto;
    }

    public void setListener(GattListener listener){
        mGattListener = listener;
    }

    public void setServiceUUID(String uuid){
        mServiceUUID = uuid.toLowerCase();
    }

    public BluetoothGattService getService(String uuid){
        if(mBluetoothGatt == null) return null;
        return mBluetoothGatt.getService(UUID.fromString(uuid));
    }
    public void setSendCharacteristic(BluetoothGattCharacteristic characteristic){
        sendGattCharacteristic = characteristic;
    }

    //连接设备
    @SuppressLint("MissingPermission")
    public boolean connect() {
        //连接之前把扫描关闭
        if (mAdapter.isDiscovering()){
            mAdapter.cancelDiscovery();
        }
        //蓝牙设备不存在，返回 false
        if(mDevice == null)  return false;
        //连接蓝牙设备
        gattConnectTime = GattConnectMax;
        Log.d(TAG, "开始连接 " + mDevice.getAddress());
        disconnect();
        mBluetoothGatt = mDevice.connectGatt(mContext, autoConnect, gattCallback);
        //蓝牙设备存在，返回 true
        return true;
    }

    public boolean connect(BluetoothDevice device){
        mDevice = device;
        return connect();
    }

    public boolean connect(String address) {
        mDevice = mAdapter.getRemoteDevice(address);
        return connect();
    }

    @SuppressLint("MissingPermission")
    public void disconnect() {
        if(mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
    }

    @SuppressLint("MissingPermission")
    public void close() {
        if(mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }

    @SuppressLint("MissingPermission")
    public void send(byte[] data) {
        if(sendGattCharacteristic != null) {
            Log.d(TAG, "send data " +  data);
            sendGattCharacteristic.setValue(data);
            mBluetoothGatt.writeCharacteristic(sendGattCharacteristic);
        }
    }

    @SuppressLint("MissingPermission")
    public void send(String s) {
        if(sendGattCharacteristic != null) {
            Log.d(TAG, "send data " + s);
            sendGattCharacteristic.setValue(s);
            mBluetoothGatt.writeCharacteristic(sendGattCharacteristic);
        }
    }

    @SuppressLint("MissingPermission")
    public void prepareNotify(BluetoothGattCharacteristic characteristic) {
        int charaProp = characteristic.getProperties();
        //判断属性是否支持消息通知
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            Log.e(TAG, "Notify CharacteristicUUID:" + characteristic.getUuid().toString());
            if (mBluetoothGatt.setCharacteristicNotification(characteristic, true)) {
                Log.d(TAG, "onServicesDiscovered--注册通知成功=--" + characteristic.getUuid());
                //获取特征值其对应的通知Descriptor
                BluetoothGattDescriptor notifyDescriptor =
                        characteristic.getDescriptor(UUID.fromString(NOTIFY_DESCRIPTOR_UUID));
                if (notifyDescriptor != null) {
                    Log.d(TAG, "onServicesDiscovered--写入通知描述=--" + characteristic.getUuid());
                    //写入你需要传递给外设的特征的描述值（即传递给外设的信息）
                    notifyDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    //通过GATT实体类将，特征值写入到外设中。成功则在 onDescriptorWrite 回调
                    mBluetoothGatt.writeDescriptor(notifyDescriptor);
                }
            }
        }
    }

    /**
     * 获取属性
     */
    public List<String> getProperties(BluetoothGattCharacteristic characteristic) {
        int property = characteristic.getProperties();
        return getProperties(property);
    }

    @SuppressLint("MissingPermission")
    public List<String> getProperties(int property) {
        List<String> properties = new ArrayList<>();
        for (int i=0; i < 8; i++) {
            switch (property & (1 << i)) {
                case 0x01:
                    properties.add("broadcast");
                    break;
                case 0x02:
                    properties.add("read");
                    break;
                case 0x04:
                    properties.add("write_no_response");
                    break;
                case 0x08:
                    properties.add("write");
                    break;
                case 0x10:
                    properties.add("notify");
                    break;
                case 0x20:
                    properties.add("indicate");
                    break;
                case 0x40:
                    properties.add("authenticated_signed_writes");
                    break;
                case 0x80:
                    properties.add("extended_properties");
                    break;
            }
        }
        return properties;
    }

    @SuppressLint("MissingPermission")
    BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            //当前为子线程
            switch (status){
                case BluetoothGatt.GATT_SUCCESS:
                    switch (newState) {
                        case BluetoothProfile.STATE_CONNECTED:
                            Log.d("提示", "蓝牙连接成功");
                            uiHandler.sendEmptyMessage(1);
                            //发现服务
                            //gatt.discoverServices();
                            Log.i(TAG, "Attempting to start service discovery:" +
                                    gatt.discoverServices());
                            break;
                        case BluetoothProfile.STATE_DISCONNECTED:
                            //uiHandler.sendEmptyMessage(GATT_STATE_DISCONNECTED);
                            uiHandler.sendEmptyMessage(0);
                            mBluetoothGatt.close();
                            mBluetoothGatt = null;
                            break;
                        default:
                            Log.e(TAG, "----> newState = " + newState);
                    }
                    break;
                case 133:
                    Log.e("提示", "连接失败： " + status);
                    Log.d(TAG, "发生设备初始连接133情况，需要重新扫描连接设备");
                    //1.需要清除Gatt缓存  2.断开连接  3.关闭Gatt  4.重新连接
                    //doing something...
                    //iPhone 和 某些Android手机作为旁支会出现蓝牙初始连接就是133，此情况下立刻重试
                    mBluetoothGatt.close();
                    //！！！需要去增加代码进行重新扫描重连
                    gattConnectTime--;
                    if(gattConnectTime > 0) {
                        gatt.connect();
                    }else{
                        uiHandler.sendEmptyMessage(-1);
                    }
                    break;
                default:
                    Log.e("提示", "连接失败： " + status);
                    gatt.close();
                    uiHandler.sendEmptyMessage(-1);
            }
        }

        //发现服务成功后，会触发该回调方法。status：远程设备探索是否成功
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onServicesDiscovered-->" + "status:" + status + "操作成功");
                //根据指定的服务uuid获取指定的服务
                //BluetoothGattService gattService = gatt.getService(UUID.fromString(mServiceUUID));

                //根据指定特征值uuid获取指定的特征值A
                //BluetoothGattCharacteristic mGattCharacteristicA = gattService.getCharacteristic(UUID.fromString(mCharUUID));

                //设置特征A通知,即设备的值有变化时会通知该特征A，即回调方法onCharacteristicChanged会有该通知
                //gatt.setCharacteristicNotification(mGattCharacteristicA , true);

                //获取特征值其对应的通知Descriptor
                //BluetoothGattDescriptor descriptor = mGattCharacteristicA.getDescriptor(NOTIFY_DESCRIPTOR_UUID);
                //写入你需要传递给外设的特征的描述值（即传递给外设的信息）
                //descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                //通过GATT实体类将，特征值写入到外设中。成功则在 onDescriptorWrite 回调
                //gatt.writeDescriptor(descriptor);

                uiHandler.sendEmptyMessage(2);
            }
        }

        /*
        //发现服务成功后，会触发该回调方法。status：远程设备探索是否成功
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onServicesDiscovered--" + "ACTION_GATT_SERVICES_DISCOVERED");
                //发现服务是可以在这里查找支持的所有服务
                //BluetoothGattService bluetoothGattService = gatt.getService(UUID.randomUUID());
                List<BluetoothGattService> bluetoothGattServices = gatt.getServices();
                for (BluetoothGattService bluetoothGattService : bluetoothGattServices) {
                    String serviceUUID = bluetoothGattService.getUuid().toString();
                    Log.d(TAG, "onServicesDiscovered--服务 uuid=" + serviceUUID);

                    List<BluetoothGattCharacteristic> bluetoothGattCharacteristics = bluetoothGattService.getCharacteristics();
                    Log.d(TAG, "onServicesDiscovered--遍历特征值=");
                    //*获取指定服务uuid的特征值*
                    //BluetoothGattCharacteristic mBluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(uuid);
                    //gatt.readCharacteristic(mBluetoothGattCharacteristic);
                    for (BluetoothGattCharacteristic gattCharacteristic : bluetoothGattCharacteristics) {
                        Log.d(TAG, "onServicesDiscovered--特征值 uuid=" + gattCharacteristic.getUuid());
                        //final int charaProp = gattCharacteristic.getProperties();
                        //gattCharacteristic.getWriteType()==BluetoothGattCharacteristic.PROPERTY_READ
                        //Log.d(TAG, "WriteType = " + gattCharacteristic.getWriteType());
                        List<String> props = getProperties(gattCharacteristic);
                        Log.d(TAG, "权限 = " + TextUtils.join(", ", props));
                        //如果该字符串可读
                        if (props.contains("read")) {
                            Log.d(TAG, "onServicesDiscovered--字符串可读--");
                            gatt.readCharacteristic(gattCharacteristic);
                            byte[] charaValue = gattCharacteristic.getValue();
                            Log.d(TAG, "onServicesDiscovered--收到值 = " + Utils.bytesToHex(charaValue));
                        }
                        if (props.contains("write_no_response") || props.contains("write"))
                        {
                            Log.d(TAG, "onServicesDiscovered--字符串可写--");
                            //byte[] value = new byte[20];
                            //gattCharacteristic.setValue(value[0], BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                            //String writeBytes = "YESIOT";
                            //gattCharacteristic.setValue(writeBytes.getBytes());
                            //gatt.writeCharacteristic(gattCharacteristic);
                        }

                        /*
                        if (props.contains("notify")) {
                            //经过测试，在本子线程内设置无效，所以放主线程内运行
                            uiHandler.postDelayed(() -> {
                                if (gatt.setCharacteristicNotification(gattCharacteristic, true)) {
                                    Log.d(TAG, "onServicesDiscovered--设置通知成功=--" + gattCharacteristic.getUuid());
                                    //获取特征值其对应的通知Descriptor
                                    BluetoothGattDescriptor notifyDescriptor = gattCharacteristic.getDescriptor(UUID.fromString(NOTIFY_DESCRIPTOR_UUID));
                                    if (notifyDescriptor != null) {
                                        Log.d(TAG, "onServicesDiscovered--写入通知描述=--" + gattCharacteristic.getUuid());
                                        //写入你需要传递给外设的特征的描述值（即传递给外设的信息）
                                        notifyDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                        //通过GATT实体类将，特征值写入到外设中。成功则在 onDescriptorWrite 回调
                                        gatt.writeDescriptor(notifyDescriptor);
                                    }
                                }
                            }, 10);
                        }
                         */
			/*3.再从指定的Characteristic中，我们可以通过getDescriptor()方法来获取该特征所包含的descriptor
				以上的BluetoothGattService、BluetoothGattCharacteristic、BluetoothGattDescriptor。
				我们都可以通过其getUuid()方法，来获取其对应的Uuid，从而判断是否是自己需要的。* /
                        List<BluetoothGattDescriptor> gattDescriptors = gattCharacteristic.getDescriptors();
                        Log.d(TAG, "onServicesDiscovered--遍历Descriptor=");
                        for (BluetoothGattDescriptor gattDescriptor : gattDescriptors) {
                            Log.d(TAG, "onServicesDiscovered--Descriptor uuid=" + gattDescriptor.getUuid());
                            byte[] descValue = gattDescriptor.getValue();
                            Log.d(TAG, "onServicesDiscovered--Descriptor 收到值 =" + Utils.bytesToHex(descValue));
                        }
                    }
                }
                uiHandler.sendEmptyMessage(2);
            }
        }
        */
        //设置Descriptor后回调
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onDescriptorWrite-->" + "描述符写入操作成功，蓝牙连接成功并且通信桥梁成功打通！" );
                //如果设置特征通知是成功的，手机发送数据的特征也是成功的，那么就可以互相成功发送数据了,那么到这里就连接到通信整个过程都已完成，可以互相收发数据了
                //等待个200ms，使其通信通道更稳定
                //doing...
                uiHandler.sendEmptyMessage(5);
            }
        }

        //设备的值有变化时会主动返回
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.d(TAG, "onCharacteristicChanged-->" + characteristic.getUuid());
            //过滤，判断是否是目标特征值
            //if (!MY_UUID.equals(characteristic.getUuid().toString())) return;
            byte[] data = characteristic.getValue();
            //Log.d(TAG, "收到数据：" + byte2Hex(data));
            //通过自己写的一个接口回调传出去
            uiHandler.sendMessage(uiHandler.obtainMessage(3, data));
        }
        //发送数据后的回调，可以在此检测发送的数据包是否有异常
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                byte[] data = characteristic.getValue();
                Log.d(TAG, "onCharacteristicWrite:发送数据成功");
                uiHandler.sendMessage(uiHandler.obtainMessage(4, data));
            }
        }
    };

    Handler uiHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch(msg.what) {
                case -1: // error
                    if(mGattListener != null) mGattListener.onConnectFail(mBluetoothGatt);
                    break;
                case 0: // disconnect
                    if(mGattListener != null) mGattListener.onDisconnect(mBluetoothGatt);
                    break;
                case 1: // connnect
                    if(mGattListener != null) mGattListener.onConnectSuccess(mBluetoothGatt);
                    break;
                case 2: //service
                    if(mGattListener != null) mGattListener.onServicesDiscovered(mBluetoothGatt);
                case 3: //收到数据
                    byte[] data = (byte[]) msg.obj;
                    if(mGattListener != null) mGattListener.onReceiveData(data);
                    break;
                case 4: //发送成功
                    byte[] sendData = (byte[]) msg.obj;
                    if(mGattListener != null) mGattListener.onSendSuccess(sendData);
                    break;
                case 5: //写描述成功
                    if(mGattListener != null) mGattListener.onNotifySuccess(mBluetoothGatt);
                    break;
            }
        }
    };
}
