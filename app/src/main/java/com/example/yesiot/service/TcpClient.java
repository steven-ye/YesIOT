package com.example.yesiot.service;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Created by wujn on 2019/2/15.
 * Version : v1.0
 * Function: tcp client  长度无限制
 */
public class TcpClient {
    final String TAG = "TcpClient";
    /**
     * single instance TcpClient
     * */
    private volatile static TcpClient mSocketClient = null;

    public static TcpClient getInstance(){
        if(mSocketClient == null){
            synchronized (TcpClient.class) {
                if(mSocketClient == null){
                    mSocketClient = new TcpClient();
                }
            }
        }
        return mSocketClient;
    }

    private Socket mSocket;

    private OutputStream mOutputStream;
    private InputStream mInputStream;

    private SocketThread mSocketThread;
    private boolean isStop = false;//thread flag

    /**
     * 128 - 数据按照最长接收，一次性
     * */
    private class SocketThread extends Thread {

        private final String ip;
        private final int port;
        private int mTimeOut = 0;
        public SocketThread(String ip, int port){
            this.ip = ip;
            this.port = port;
        }
        public void setTimeOut(int timeout){
            mTimeOut = timeout;
        }
        @Override
        public void run() {
            Log.d(TAG,"SocketThread start at "+ ip+":"+port);
            super.run();

            //connect ...
            try {
                if (mSocket != null) {
                    mSocket.close();
                    mSocket = null;
                }

                InetAddress ipAddress = InetAddress.getByName(ip);
                mSocket = new Socket(ipAddress, port);
                if (mTimeOut>0) mSocket.setSoTimeout(mTimeOut);//超时3秒

                /*
                mSocket = new Socket();
                SocketAddress socAddress = new InetSocketAddress(ipAddress, port);
                if (mTimeOut>0){
                    mSocket.connect(socAddress,mTimeOut); //超时3秒
                }else{
                    mSocket.connect(socAddress);
                }
                */

                //设置不延时发送
                //mSocket.setTcpNoDelay(true);
                //设置输入输出缓冲流大小
                //mSocket.setSendBufferSize(8*1024);
                //mSocket.setReceiveBufferSize(8*1024);

                if(isConnect()){
                    mOutputStream = mSocket.getOutputStream();
                    mInputStream = mSocket.getInputStream();
                    isStop = false;
                    //uiHandler.sendEmptyMessage(1);
                    sendEmptyMessage(1);
                }
                /* 此处这样做意义不大，真正的socket未连接还是靠心跳发送，等待服务端回应比较好，一段时间内未回应，则socket未连接成功 */
                else{
                    //uiHandler.sendEmptyMessage(-1);
                    sendEmptyMessage(-1);
                    Log.e(TAG,"SocketThread connect failed");
                    return;
                }

            }
            catch (IOException e) {
                //uiHandler.sendEmptyMessage(-1);
                sendEmptyMessage(-1);
                Log.e(TAG,"SocketThread connect io exception = "+e.getMessage());
                //e.printStackTrace();
                return;
            }
            Log.d(TAG,"SocketThread connect over ");

            //read ...
            while (isConnect() && !isStop && !isInterrupted()) {
                int size;
                try {
                    byte[] buffer = new byte[1024];
                    if (mInputStream == null) return;
                    size = mInputStream.read(buffer);//null data -1 , zrd serial rule size default 10
                    if (size > 0) {
                        byte[] res = new byte[size];
                        System.arraycopy(buffer, 0, res, 0, size);

                        Bundle bundle = new Bundle();
                        bundle.putByteArray("data",res);
                        bundle.putInt("size",size);
                        bundle.putInt("requestCode",requestCode);
                        sendMessage(bundle,200);
                    }
                    Log.i(TAG, "SocketThread read listening");
                    //Thread.sleep(100);//log eof
                }
                catch (IOException e) {
                    //uiHandler.sendEmptyMessage(-1);
                    sendEmptyMessage(-1);
                    Log.e(TAG,"SocketThread read io exception = "+e.getMessage());
                    e.printStackTrace();
                    return;
                }
            }
        }

        private void sendMessage(Bundle bundle,int what){
            Message msg = new Message();
            msg.what = what;
            bundle.putString("ip",ip);
            bundle.putInt("port",port);
            msg.setData(bundle);
            uiHandler.sendMessage(msg);
        }

        private void sendEmptyMessage(int what){
            Bundle bundle = new Bundle();
            sendMessage(bundle, what);
        }
    }


    private class SendThread extends Thread{
        byte[] mBuffer;
        public SendThread(byte[] buffer){
            mBuffer = buffer;
        }
        @Override
        public void run() {
            try {
                if (mOutputStream != null) {
                    mOutputStream.write(mBuffer);
                    mOutputStream.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //==============================socket connect============================
    /**
     * connect socket in thread
     * Exception : android.os.NetworkOnMainThreadException
     * */
    public void connect(String ip, int port){
        mSocketThread = new SocketThread(ip, port);
        mSocketThread.start();
    }

    public void connect(String ip, int port, int timeOut){
        mSocketThread = new SocketThread(ip, port);
        mSocketThread.setTimeOut(timeOut);
        mSocketThread.start();
    }

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
        isStop = true;
        try {
            if (mOutputStream != null) {
                mOutputStream.close();
            }

            if (mInputStream != null) {
                mInputStream.close();
            }

            if (mSocket != null) {
                mSocket.close();
                mSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mSocketThread != null) {
            mSocketThread.interrupt();//not intime destory thread,so need a flag
        }
    }

    public void send(String cmd){
        sendStrCmd(cmd, 1001);
    }

    public void send(String cmd,int requestCode){
        sendStrCmd(cmd, requestCode);
    }

    /**
     * send byte[] cmd
     * Exception : android.os.NetworkOnMainThreadException
     * */
    public void sendByteCmd(final byte[] mBuffer,int requestCode) {
        this.requestCode = requestCode;
        new SendThread(mBuffer).start();
    }

    /**
     * send string cmd to serial
     */
    public void sendStrCmd(String cmd, int requestCode) {
        byte[] mBuffer = cmd.getBytes();
        sendByteCmd(mBuffer,requestCode);
    }

    /**
     * send prt content cmd to serial
     */
    public void sendChsPrtCmd(String content, int requestCode) {
        try {
            byte[] mBuffer = content.getBytes("GB2312");
            sendByteCmd(mBuffer,requestCode);
        }
        catch (UnsupportedEncodingException e1){
            e1.printStackTrace();
        }
    }


    Handler uiHandler = new Handler(Looper.getMainLooper()) {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            String ip = bundle.getString("ip");
            int port  = bundle.getInt("port");
            switch(msg.what){
                //connect error
                case -1:
                    if (null != tcpCallback) {
                        tcpCallback.onConnectFail(ip,port);
                        disconnect();
                    }
                    break;

                //connect success
                case 1:
                    if (null != tcpCallback) {
                        tcpCallback.onConnectSuccess(ip,port);
                    }
                    break;

                //receive data
                case 200:
                    byte[] buffer = bundle.getByteArray("data");
                    int requestCode = bundle.getInt("requestCode");
                    String message = new String(buffer, StandardCharsets.UTF_8);
                    Log.i(TAG, "TCP >> " + message);
                    if (null != tcpCallback) {
                        tcpCallback.onDataReceived(message,requestCode);
                    }
                    break;
            }
        }
    };


    /**
     * socket response data listener
     * */
    private TcpCallback tcpCallback;
    private int requestCode = -1;
    public interface TcpCallback {
        void onConnectSuccess(String ip, int port);
        void onConnectFail(String ip, int port);
        void onDataReceived(String message, int requestCode);
        //void onDataReceived(byte[] buffer, int size, int requestCode);
    }
    public void setCallback(TcpCallback callback) {
        tcpCallback = callback;
    }
}