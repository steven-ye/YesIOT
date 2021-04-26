package com.example.yesiot.helper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.yesiot.object.Device;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class DeviceHelper {
    DatabaseHelper dbHelper;
    final static String table = "devices";

    public DeviceHelper() {
        dbHelper = DatabaseHelper.getInstance();
    }

    public static Device get(int id) {
        SQLiteDatabase db = DatabaseHelper.getInstance().getReadableDatabase();
        Device device = new Device();
        Cursor cursor = db.rawQuery("SELECT * FROM "+table+" WHERE id=?", new String[]{id+""});
        if(cursor.moveToFirst()){
            device = getDevice(cursor);
        }
        cursor.close();
        db.close();
        return device;
    }

    public static Device get(String selection, String[] args){
        SQLiteDatabase db = DatabaseHelper.getInstance().getReadableDatabase();
        Device device = new Device();
        Cursor cursor = db.query(table, null, selection, args, null, null, null);
        if(cursor.moveToFirst()){
            device = getDevice(cursor);
        }
        cursor.close();
        db.close();
        return device;
    }

    public static boolean save(Device device) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        int num;
        ContentValues cv = new ContentValues();
        cv.put("name",device.getName());
        cv.put("code",device.getCode());
        cv.put("theme", device.getTheme());
        cv.put("image",device.getImage());
        cv.put("ip",device.getIp());
        cv.put("pins",new Gson().toJson(device.getPins()));
        cv.put("sub",device.getSub());
        cv.put("topic",device.getTopic());
        cv.put("payload",device.getPayload());
        int id = device.getId();
        if(id>0){
            num = dbHelper.update(table,cv, id);
        }else{
            num = dbHelper.insert(table,cv);
            if(num>0)device.setId(num);
        }
        return num>0;
    }

    public static boolean remove(int id){
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        int num = dbHelper.delete(table,"id=?", new String[]{id+""});
        return num>0;
    }

    public static boolean find(Device device){
        Device dev = get("code=? and ip=?", new String[]{device.getCode(),device.getIp()});
        return dev.getId() > 0;
    }

    public static List<Device> getList(int userId){
        SQLiteDatabase db = DatabaseHelper.getInstance().getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+table+" WHERE user_id=?",new String[]{userId+""});
        List<Device> list = new ArrayList<>();
        while(cursor.moveToNext()){
            list.add(getDevice(cursor));
        }
        cursor.close();
        db.close();
        return list;
    }

    public static List<Device> getList(){
        SQLiteDatabase db = DatabaseHelper.getInstance().getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+table,null);
        List<Device> list = new ArrayList<>();
        while(cursor.moveToNext()){
            list.add(getDevice(cursor));
        }
        cursor.close();
        db.close();
        return list;
    }

    private static Device getDevice(Cursor cursor){
        Device device = new Device();
        device.setId(getIntColumn(cursor,"id"));
        device.setName(getColumn(cursor,"name"));
        device.setCode(getColumn(cursor,"code"));
        device.setTheme(getColumn(cursor,"theme"));
        device.setImage(getColumn(cursor,"image"));
        device.setIp(getColumn(cursor,"ip"));
        device.setSub(getColumn(cursor,"sub"));
        device.setTopic(getColumn(cursor,"topic"));
        device.setPayload(getColumn(cursor,"payload"));
        JsonElement jsonElement = JsonParser.parseString(getColumn(cursor,"pins"));
        device.setPins(new Gson().fromJson(jsonElement, new TypeToken<List<String>>() {}.getType()));
        return device;
    }

    private static String getColumn(Cursor cursor, String name){
        return cursor.getString(cursor.getColumnIndex(name));
    }
    private static int getIntColumn(Cursor cursor, String name){
        return cursor.getInt(cursor.getColumnIndex(name));
    }
}