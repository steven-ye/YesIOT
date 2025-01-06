package com.example.yesiot.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Created by Steven Ye on 2024/2/22.
 * Version : v1.0
 * Function: Bluetooth Socket SPP
 */
public class BlueSocketClient {
    private static final String TAG = "BleSocketClient";
    private static final int ON_ERROR = -1;
    private static final int ON_CONNECT = 0;
    private static final int ON_DATA = 1;
    private static final int ON_FAIL = 2;
    private static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    private BluetoothAdapter mAdapter;
    private ConnectThread mThread = null;
    private boolean connected = false;

    public BlueSocketClient() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * socket is connect
     * */
    public boolean isConnected(){
        return connected;
    }

    /**
     * socket disconnect
     * */
    public void disconnect() {
        connected = false;
        if (mThread != null) {
            mThread.cancel();
            mThread.interrupt();//not in-time destroy thread,so need a flag
        }
    }

    public void connect(BluetoothDevice device) {
        if(connected) disconnect();
        mThread = new ConnectThread(device);
        mThread.start();
    }

    public void connect(BluetoothDevice device, Listener listener) {
        mListener = listener;
        connect(device);
    }

    public void send(String message){
        try {
            send(message.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            //e.printStackTrace();
            send(message.getBytes());
        }
    }

    public void send(byte[] message){
        if(connected){
            mThread.write(message);
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket socket;
        private InputStream inputStream = null;
        private OutputStream outputStream = null;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            try {
                tmp = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
                //tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
            } catch (IOException e) {
                Log.d(TAG, "create rfcomm socket: ", e);
                //e.printStackTrace();
                uiHandler.sendEmptyMessage(-1);
                try {
                    Method method = BluetoothDevice.class.getDeclaredMethod("createRfcommSocket", new Class[]{ int.class });
                    tmp = (BluetoothSocket) method.invoke(device, 1);
                } catch (Exception ex) {
                    //ex.printStackTrace();
                    Log.e(TAG, "create rfcomm socket: ", ex);
                }
            }
            socket = tmp;
        }

        public void run() {
            Log.i(TAG, "Begin ConnectThread");
            connected = false;
            if(mAdapter.isDiscovering()){
                mAdapter.cancelDiscovery();
            }
            try {
                socket.connect();
                connected = true;
                Log.i(TAG, "Connected");
                uiHandler.sendEmptyMessage(ON_CONNECT);

                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                connected = false;
                Log.e(TAG, "Socket", e);
                uiHandler.sendEmptyMessage(ON_FAIL);
                // 关闭 socket
                try {
                    socket.close();
                    //socket = null;
                } catch (Exception e2) {
                    //TODO: handle exception
                    Log.e(TAG, "Socket", e2);
                }
                return;
            }

            byte[] buffer = new byte[1024];
            while(true) {
                try{
                    int size = inputStream.read(buffer);
                    if(size > 0) {
                        byte[] result = new byte[size];
                        System.arraycopy(buffer, 0, result, 0, size);
                        Log.d(TAG, "Received => " + new String(result, StandardCharsets.UTF_8));
                        //uiHandler.obtainMessage(3, size, -1, buffer).sendToTarget();
                        Bundle bundle = new Bundle();
                        bundle.putByteArray("data", result);
                        Message message = new Message();
                        message.what = ON_DATA;
                        message.setData(bundle);
                        uiHandler.sendMessage(message);
                    }else{
                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.i(TAG, "ConnectThread read listening");
                    }
                }catch (IOException e){
                    //e.printStackTrace();
                    Log.e(TAG, "连接已断开", e);
                    uiHandler.sendEmptyMessage(ON_ERROR);
                    break;
                }
            }

            connected = false;
            // 重置ConnectThread
            //synchronized (BluetoothService.this) {
            //ConnectThread = null;
            //}
        }

        public void cancel() {
            try {
                if(null != inputStream) inputStream.close();
                if(null != outputStream) outputStream.close();
                if(null != socket) socket.close();
                //socket = null;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                connected = false;
            }
        }

        /**
         * 发送
         * @param data 内容
         */
        void write(byte[] data) {
            try {
                //mySocket.getOutputStream().write(bytes);
                //OutputStream outputStream = socket.getOutputStream();//获取输出流
                if (outputStream != null) {//判断输出流是否为空
                    // outputStream.write(message.getBytes("UTF-8"));
                    outputStream.write(data);
                    //outputStream.flush();//将输出流的数据强制提交
                    //outputStream.close();//关闭输出流
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    Handler uiHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            switch(msg.what){
                case ON_ERROR:
                    //disconnect();
                    if (null != mListener)  mListener.onError();
                    break;
                case ON_CONNECT:
                    if (null != mListener) mListener.onConnectSuccess();
                    break;
                case ON_DATA: //receive data
                    byte[] bytes = bundle.getByteArray("data");
                    String message =new String(bytes,StandardCharsets.UTF_8);
                    //Log.d(TAG, "Received => " + message);
                    if (null != mListener)  mListener.onDataReceive(bytes);
                    break;
                case ON_FAIL:
                    if (null != mListener)  mListener.onConnectFail();
                    break;
            }
        }
    };

    /**
     * socket response data listener
     * */
    private Listener mListener = null;
    public void setListener(Listener listener) {
        mListener = listener;
    }

    public interface Listener {
        void onConnectSuccess();
        void onConnectFail();
        void onError();
        void onDataReceive(byte[] message);
    }
}