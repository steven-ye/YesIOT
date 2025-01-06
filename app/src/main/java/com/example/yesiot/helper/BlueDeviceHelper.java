package com.example.yesiot.helper;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.yesiot.object.BlueDevice;

import java.util.ArrayList;
import java.util.List;

public class BlueDeviceHelper extends AbstractHelper {
    final static String table = "blue_device";

    public static BlueDevice get(int id) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        BlueDevice device = null;
        Cursor cursor = dbHelper.select(table, null, "id=?", new String[]{id+""});
        if(cursor.moveToFirst()){
            device = getDevice(cursor);
        }
        cursor.close();
        dbHelper.close();
        return device;
    }

    public static BlueDevice get(String mac) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        BlueDevice device = null;
        Cursor cursor = dbHelper.select(table, null, "mac=?", new String[]{ mac });
        if(cursor.moveToFirst()){
            device = getDevice(cursor);
        }
        cursor.close();
        dbHelper.close();
        return device;
    }

    public static BlueDevice get(String selection, String[] args){
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        BlueDevice device = new BlueDevice();
        Cursor cursor = dbHelper.select(table, null, selection, args, null);
        if(cursor.moveToFirst()){
            device = getDevice(cursor);
        }
        cursor.close();
        dbHelper.close();
        return device;
    }

    public static boolean save(BlueDevice device) {
        int id = 0;
        BlueDevice blueDevice = get(device.getMac());
        if(blueDevice != null) id = blueDevice.getId();
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        ContentValues cv = getContentValues(device);
        int num;
        if(id>0){
            num = dbHelper.update(table, cv, id);
        }else{
            num = dbHelper.insert(table, cv);
            if(num>0)device.setId(num);
        }
        return num>0;
    }

    public static boolean remove(int id){
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        int num = dbHelper.delete(table,"id=?", new String[]{id+""});
        return num>0;
    }

    public static boolean remove(String mac){
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        int num = dbHelper.delete(table,"mac=?", new String[]{ mac });
        return num>0;
    }

    public static List<BlueDevice> getList(int userId){
        List<BlueDevice> list = new ArrayList<>();
        if(userId<1)return list;
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        Cursor cursor = dbHelper.select(table, null,"user_id=?", new String[]{userId+""}, "weight asc, id asc");
        while(cursor.moveToNext()){
            list.add(getDevice(cursor));
        }
        cursor.close();
        dbHelper.close();
        return list;
    }

    public static List<BlueDevice> getList()
    {
        List<BlueDevice> list = new ArrayList<>();
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        Cursor cursor = dbHelper.select(table, null,null, null,"weight desc, id asc");
        while(cursor.moveToNext()){
            list.add(getDevice(cursor));
        }
        cursor.close();
        dbHelper.close();
        return list;
    }

    public static boolean has(String mac){
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        Cursor cursor = dbHelper.select(table, null,"mac=?",new String[]{ mac });
        boolean has = cursor.moveToNext();
        cursor.close();
        dbHelper.close();
        return has;
    }

    private static BlueDevice getDevice(Cursor cursor){
        BlueDevice device = new BlueDevice();
        device.setId(getColumnInt(cursor,"id"));
        device.setName(getColumn(cursor,"name"));
        device.setAlias(getColumn(cursor,"alias"));
        device.setMac(getColumn(cursor,"mac"));
        device.setType(getColumnInt(cursor,"type"));
        device.setImage(getColumn(cursor,"image"));
        device.setServiceUuid(getColumn(cursor,"service_uuid"));
        //device.setNotifyUuid(getColumn(cursor,"notify_uuid"));
        device.setWeight(getColumnInt(cursor,"weight"));
        device.setExtra(getColumn(cursor,"extra"));

        return device;
    }

    private static ContentValues getContentValues(BlueDevice device)
    {
        ContentValues cv = new ContentValues();
        cv.put("name",device.getName());
        cv.put("mac",device.getMac());
        cv.put("image",device.getImage());
        cv.put("alias",device.getAlias());
        cv.put("type",device.getType());
        cv.put("weight",device.getWeight());
        cv.put("service_uuid",device.getServiceUuid());
        //cv.put("notify_uuid",device.getNotifyUuid());
        cv.put("extra",device.getExtra());

        return cv;
    }
}