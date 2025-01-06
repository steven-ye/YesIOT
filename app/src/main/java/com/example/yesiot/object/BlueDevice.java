package com.example.yesiot.object;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.os.ParcelUuid;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BlueDevice implements Serializable
{
    public int id = 0;
    public int userId = 0;
    public int type = 0;
    public int rssi = 0;
    public String mac;
    public String name = "";
    public String alias = "";
    public String service_uuid;
    public String notify_uuid="";
    public String image = "";
    public String weight = "0";
    public String status = "";
    public String extra = "";
    public boolean bonded = false;
    public ParcelUuid[] uuids;

    public BlueDevice(){}
    public BlueDevice(String mac, String name){
        this.name = name;
        this.mac = mac;
    }

    @SuppressLint("MissingPermission")
    public BlueDevice(BluetoothDevice device){
        this(device.getAddress(), device.getName());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            alias = device.getAlias();
        }
        type = device.getType();
        uuids = device.getUuids();
        bonded = device.getBondState() == BluetoothDevice.BOND_BONDED;
    }

    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }
    public void setMac(String mac) {
        this.mac = mac;
    }
    public String getMac() {
        return mac;
    }
    public void setAddress(String address) {
        this.mac = address;
    }
    public String getAddress() {
        return mac;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public void setAlias(String name) {
        alias = name;
    }
    public String getAlias() {
        return alias;
    }
    public void setServiceUuid(String uuid) {
        service_uuid = uuid;
    }
    public String getServiceUuid() {
        return service_uuid;
    }
    public void setNotifyUuid(String uuid) {
        notify_uuid = uuid;
    }
    public String getNotifyUuid() {
        return notify_uuid;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public int getUserId() {
        return userId;
    }
    public void setImage(String image) {
        this.image = image;
    }
    public String getImage() {
        return image;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getStatus() {
        return status;
    }
    public void setWeight(String weight) {
        this.weight = weight;
    }
    public void setWeight(int weight) {
        this.weight = weight+"";
    }
    public int getWeight() {
        return Integer.parseInt(weight);
    }
    public void setExtra(String extra) {
        this.extra = extra;
    }
    public String getExtra() {
        return extra;
    }
    public int getRssi() {
        return rssi;
    }
    public void setRssi(int rssi) {
        this.rssi = rssi;
    }
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public boolean isBonded() {
        return bonded;
    }
    public void setBonded(boolean bonded) {
        this.bonded = bonded;
    }
    @NonNull
    public String toString()
    {
        return "name:"+name+",mac:"+mac+",service uuid:"+service_uuid+",notify uuid=" + notify_uuid;
    }
}
