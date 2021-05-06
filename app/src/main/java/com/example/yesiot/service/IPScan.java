package com.example.yesiot.service;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.example.yesiot.object.Constants;
import com.example.yesiot.util.IPUtils;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

public class IPScan {
    final String TAG = "IPSacn";
    // Singleton
    private static IPScan instance;

    private IPScan() {
        this.scanThreads = new ArrayList<>();
    }
    public static IPScan getInstance() {
        if (instance == null) {
            instance = new IPScan();
        }
        return instance;
    }

    // Constants
    private static final int IP_SEGMENT_LEN = 4;
    private static final int IP_SEGMENT_MAX = 256;

    public static final int MSG_GOT_EQUIP = 1001;
    public static final int MSG_START_SCANNING = 1002;
    public static final int MSG_PROGRESS_REPORT = 1003;
    public static final int MSG_SCANNING_STOPPED = 1004;


    // Fields
    private final List<IPScanThread> scanThreads;
    private String ipPrefix;// 局域网IP地址头,如：192.168.1.
    private int ipCurrent;
    private Message msg;

    public void startScanning(Context context){
        // 本机IP地址-完整
        //String devAddress = getHostIP();// 获取本机IP地址
        String devAddress = IPUtils.getIPAdress(context);
        Log.e(TAG, "开始扫描设备,本机Ip为：" + devAddress);
        startScanning(devAddress);
    }

    // Public functions
    public void startScanning(String devAddress) {
        if (this.isStillScanning() || isNotAIp(devAddress)) {
            return;
        }
        Log.i(TAG, "Current IP is "+ipPrefix+ipCurrent);
        msg = new Message();
        msg.what = MSG_START_SCANNING;
        msg.arg1 = Constants.SCANNING_THREADS_COUNT;
        handler.sendMessage(msg);

        scanThreads.clear();
        int step = IP_SEGMENT_MAX / Constants.SCANNING_THREADS_COUNT;
        int start = 0;
        int stop = step;
        for (int i = 0; i < Constants.SCANNING_THREADS_COUNT; i++) {
            IPScanThread thread = new IPScanThread(handler, ipPrefix, ipCurrent);
            thread.setCounter(i);
            thread.setStartIp(start);
            thread.setStopIp(stop);
            thread.start();
            scanThreads.add(thread);
            start += step;
            stop = i == Constants.SCANNING_THREADS_COUNT - 1 ? IP_SEGMENT_MAX : stop + step;
        }
    }
    public void stopScanning() {
        if (!this.isStillScanning()) {
            return;
        }

        for (int i = 0; i < scanThreads.size(); i++) {
            try {
                scanThreads.get(i).setDemandToStop(true);
                scanThreads.get(i).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        msg = new Message();
        msg.what = MSG_SCANNING_STOPPED;
        msg.arg1 = -1;
        handler.sendMessage(msg);
    }

    // Private functions
    private boolean isStillScanning(int idx) {
        if (this.scanThreads.size() <= idx) {
            return false;
        }
        IPScanThread thread = this.scanThreads.get(idx);
        return thread.isAlive();
    }
    private boolean isStillScanning() {
        for (int i = 0; i < this.scanThreads.size(); i++) {
            if (this.isStillScanning(i)) {
                return true;
            }
        }
        return false;
    }
    private boolean isNotAIp(String ip) {
        String[] ipSegments = ip.split("\\.");
        if (ipSegments.length != IP_SEGMENT_LEN) {
            return true;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ipSegments.length; i++) {
            try {
                int ii = Integer.parseInt(ipSegments[i]);
                if (i != IP_SEGMENT_LEN - 1) {
                    sb.append(ii);
                    sb.append('.');
                }
                this.ipCurrent = ii;
            } catch (NumberFormatException ex) {
                return true;
            }
        }
        this.ipPrefix = sb.toString();
        return false;
    }

    /**
     * 获取ip地址
     * @return
     */
    public String getHostIP() {

        String hostIp = null;
        try {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            Log.e(TAG, "SocketException");
            e.printStackTrace();
        }
        return hostIp;
    }

    /**
     * TODO<获取本机IP前缀>
     *
     * @param devAddress
     *            // 本机IP地址
     * @return String
     */
    private String getLocAddrIndex(String devAddress) {
        if (!devAddress.equals("")) {
            return devAddress.substring(0, devAddress.lastIndexOf(".") + 1);
        }
        return null;
    }

    Handler handler = new Handler(Looper.getMainLooper()){
        private int[] progress;
        private List<String> ipList;
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_START_SCANNING:
                    this.progress = new int[msg.arg1];
                    this.ipList = new ArrayList<>();
                case MSG_GOT_EQUIP:
                    Bundle bundle = msg.getData();
                    String ip = bundle.getString("ip");
                    if(!TextUtils.isEmpty(ip)){
                        ipList.add(ip);
                        if(listener != null){
                            listener.onPingSuccess(ip);
                        }
                    }
                    break;
                case MSG_PROGRESS_REPORT:
                    this.progress[msg.arg1] = msg.arg2;
                    break;
                case MSG_SCANNING_STOPPED:
                    if (msg.arg1 > -1 && msg.arg1 < this.progress.length) {
                        this.progress[msg.arg1] = 100;
                    } else if (msg.arg1 == -1) {
                        Arrays.fill(this.progress, 100);
                    }
                    int progress = this.getAverageProgress();
                    if(listener != null){
                        if(progress == -1){
                            listener.onScanningDone(ipList);
                        }else{
                            listener.onScanning(progress);
                        }
                    }
                    break;
            }
        }

        private int getAverageProgress() {
            if (this.progress == null || this.progress.length == 0) {
                return 0;
            }
            boolean allDone = true;
            int len = this.progress.length;
            int sum = 0;
            for (int p : this.progress) {
                if (p != 100) {
                    allDone = false;
                }
                sum += p;
            }
            return allDone ? -1 : sum / len;
        }
    };

    private OnScanListener listener;
    public void setOnScanListener(OnScanListener scanListener){
        listener = scanListener;
    }

    public interface OnScanListener{
        void onPingSuccess(String ip);
        void onScanning(int progress);
        void onScanningDone(List<String> ipList);
    }
}