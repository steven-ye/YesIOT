package com.example.yesiot.service;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;

public class IPScanThread extends Thread {
    final String TAG = "IPScanThread";
    // Constants
    private static final int CONTROLLING_BOUND = 6;
    // -c -1: times to send ping command
    // -w -3: time to wait for respond
    private static final String CMD_PING = "ping -c 1 -w 3 %s%d";

    // Fields
    private final Handler handler;
    private final String ipPrefix;
    private final int currentIp;
    private int startIp;
    private int stopIp;
    private int counter;
    private boolean demandToStop;

    // Constructors
    public IPScanThread(Handler handler, String ipPrefix, int currentIp) {
        this.handler = handler;
        this.ipPrefix = ipPrefix;
        this.currentIp = currentIp;
    }

    // Override functions
    @Override
    public void run() {
        if (startIp >= stopIp) {
            return;
        }
        Message msg;
        for (int ip = startIp; ip < stopIp; ip++) {
            if (this.isDemandToStop()) {
                break;
            }

            if (ip == this.currentIp) {
                continue;
            }

            String command = String.format(CMD_PING, this.ipPrefix, ip);
            //Log.i(TAG,"About to execute: " + command);
            Process process = null;
            try {
                process = Runtime.getRuntime().exec(command);
                int result = process.waitFor();
                if (result == 0) {
                    msg = new Message();
                    msg.what = IPScan.MSG_GOT_EQUIP;
                    Bundle bundle = new Bundle();
                    bundle.putString("ip", this.ipPrefix + ip);
                    msg.setData(bundle);
                    this.sendMessage(msg);
                }
                msg = new Message();
                msg.what = IPScan.MSG_PROGRESS_REPORT;
                msg.arg1 = this.counter;
                msg.arg2 = (ip - startIp) * 100 / (stopIp - startIp);
                this.sendMessage(msg);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (process != null) {
                    process.destroy();
                }
            }
        }

        msg = new Message();
        msg.what = IPScan.MSG_SCANNING_STOPPED;
        msg.arg1 = this.counter;
        this.sendMessage(msg);
    }

    // private functions
    private synchronized void sendMessage(Message msg) {
        handler.sendMessage(msg);
    }

    // Generated functions for properties
    public int getCounter() { return counter; }
    public void setCounter(int counter) { this.counter = counter; }
    public boolean isDemandToStop() { return demandToStop; }
    public void setDemandToStop(boolean demandToStop) { this.demandToStop = demandToStop; }
    public int getStartIp() { return startIp; }
    public void setStartIp(int startIp) { this.startIp = startIp; }
    public int getStopIp() { return stopIp; }
    public void setStopIp(int stopIp) { this.stopIp = stopIp; }
}