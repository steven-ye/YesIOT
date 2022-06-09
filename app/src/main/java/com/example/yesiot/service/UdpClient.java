package com.example.yesiot.service;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.example.yesiot.IApplication;
import com.example.yesiot.object.Constants;
import com.example.yesiot.util.IPUtils;
import com.example.yesiot.util.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by wujn on 2019/2/15.
 * Version : v1.0
 * Function: udp client 64k限制
 */
public class UdpClient {
    final String TAG = "UdpClient";
    final int ON_ERROR = -1;
    final int ON_START = 0;
    final int ON_DATA = 1;
    final int ON_STOP = 2;
    final String BROAD_IP="255.255.255.255";

    private DatagramSocket mSocket=null;
    private ReceiveThread mReceiveThread;
    private boolean connected = false;//thread flag

    private int mPort;
    private int mTimeout = 0;

    private void initSocket() throws SocketException {
        if(!isConnect()){
            mSocket = new DatagramSocket(null);
            mSocket.setReuseAddress(true);
            mSocket.bind(new InetSocketAddress(mPort));
            mSocket.setSoTimeout(mTimeout);
        }
    }

    private class SendThread extends Thread{
        private final String ip;
        private final int port;
        private final String message;

        public SendThread(String ip, int port, String message){
            this.ip = ip;
            this.port = port;
            this.message = message;
        }
        @Override
        public void run() {
            try {
                sleep(100); //ensure ReceiveThread is running;
                initSocket();
                InetAddress ipAddress = InetAddress.getByName(ip);
                byte[] mBuffer = message.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(mBuffer, mBuffer.length, ipAddress, port);
                mSocket.send(sendPacket);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                uiHandler.sendEmptyMessage(ON_ERROR);
            }
        }
    }

    /**
     * 128 - 数据按照最长接收，一次性
     * */
    private class ReceiveThread extends Thread{
        @Override
        public void run() {
            Log.w(TAG,"UDP SocketThread start receiving");
            uiHandler.sendEmptyMessage(ON_START);
            long start = System.currentTimeMillis();
            connected = true;
            try {
                initSocket();
                //if(mSocket==null) mSocket = new DatagramSocket(port);
                //if(timeout>0)mSocket.setSoTimeout(timeout);
                while(connected){
                    byte[] preBuffer = new byte[1024]; //预存buffer
                    //接受
                    DatagramPacket receivePacket = new DatagramPacket(preBuffer, preBuffer.length);
                    receivePacket.setData(preBuffer);
                    mSocket.receive(receivePacket);
                    if (receivePacket.getData() == null) return;
                    String ip = receivePacket.getAddress().getHostAddress();

                    int size = receivePacket.getLength();     //此为获取后的有效长度，一次最多读64k，预存小的话可能分包
                    Log.d(TAG, "pre data size = "+ receivePacket.getData().length + ", value data size = "+size);
                    //byte[] dataBuffer = Arrays.copyOf(preBuffer, size);
                    String message = new String(preBuffer,0,size);
                    int port = receivePacket.getPort();
                    if (size > 0) {
                        Message msg = new Message();
                        msg.what = ON_DATA;
                        Bundle bundle = new Bundle();
                        //bundle.putByteArray("data",dataBuffer);
                        bundle.putInt("port",port);
                        bundle.putString("ip",ip);
                        bundle.putString("message",message);
                        msg.setData(bundle);
                        uiHandler.sendMessage(msg);
                    }
                }
            } catch (IOException e) {
                //e.printStackTrace();
                Log.e(TAG, e.getMessage());
                uiHandler.sendEmptyMessage(ON_ERROR);
            }
            if(mSocket != null)mSocket.close();

            long runtime = (System.currentTimeMillis() - start)/1000;
            Log.w(TAG, "UDP runtime " + runtime + " seconds");
            uiHandler.sendEmptyMessage(ON_STOP);
        }
    }

    //==============================socket connect============================
    /**
     * socket is connect
     * */
    public boolean isConnect(){
        boolean flag = false;
        if (mSocket != null) {
            flag = mSocket.isConnected();
        }
        return flag;
    }
    /**
     * socket disconnect
     * */
    public void disconnect() {
        connected = false;
        if (mSocket != null) {
            mSocket.close();
            mSocket = null;
        }
        if (mReceiveThread != null) {
            mReceiveThread.interrupt();//not intime destory thread,so need a flag
        }
    }

    public void listen(int port, int timeout) {
        mPort = port;
        mTimeout = timeout;
        mReceiveThread = new ReceiveThread();
        mReceiveThread.start();
    }

    public void send(String ip, int port, String message){
        SendThread sendThread = new SendThread(ip,port,message);
        sendThread.start();
    }

    public void broadcast(int port, String message){
        send(BROAD_IP, port, message);
    }

    Handler uiHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            switch(msg.what){
                case ON_ERROR:
                    //disconnect();
                    //String error = bundle.getString("message");
                    //Log.w(TAG, error);
                    break;
                case ON_START:
                    if (null != mListener) {
                        mListener.onStart();
                    }
                    break;
                case ON_DATA: //receive data
                    //Bundle bundle = msg.getData();
                    int port = bundle.getInt("port");
                    //String message =new String(buffer,StandardCharsets.UTF_8);
                    String ip = bundle.getString("ip");
                    String message = bundle.getString("message");
                    Log.v(TAG, "Message from "+ ip + ":" + port);
                    if (null != mListener) {
                        mListener.onDataReceive(ip, port, message);
                    }
                    break;
                case ON_STOP:
                    if (null != mListener) {
                        mListener.onStop();
                    }
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
        void onStart();
        void onStop();
        void onDataReceive(String ip, int port, String message);
    }
}