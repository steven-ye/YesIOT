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
        cv.put("port",device.getPort());
        cv.put("pins",new Gson().toJson(device.getPins()));
        cv.put("sub",device.getSub());
        cv.put("topic",device.getTopic());
        cv.put("payload",device.getPayload());
        cv.put("broker_id",device.getBrokerId());
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

    public static List<Device> getList(int brokerId){
        List<Device> list = new ArrayList<>();
        if(brokerId<1)return list;
        SQLiteDatabase db = DatabaseHelper.getInstance().getReadableDatabase();
        //Cursor cursor = db.rawQuery("SELECT * FROM "+table+" WHERE user_id=?",new String[]{userId+""});
        Cursor cursor = db.rawQuery("SELECT a.*,b.name AS broker FROM "+table + " a, brokers b WHERE a.broker_id=b.id AND broker_id=?",new String[]{brokerId+""});
        while(cursor.moveToNext()){
            list.add(getDevice(cursor));
        }
        cursor.close();
        db.close();
        return list;
    }

    public static List<Device> getList(){
        List<Device> list = new ArrayList<>();
        SQLiteDatabase db = DatabaseHelper.getInstance().getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT a.*,b.name AS broker FROM "+table + " a, brokers b WHERE a.broker_id=b.id",null);
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
        device.setBrokerId(getIntColumn(cursor,"broker_id"));
        device.setName(getColumn(cursor,"name"));
        device.setCode(getColumn(cursor,"code"));
        device.setTheme(getColumn(cursor,"theme"));
        device.setImage(getColumn(cursor,"image"));
        device.setIp(getColumn(cursor,"ip"));
        device.setPort(getIntColumn(cursor,"port"));
        device.setSub(getColumn(cursor,"sub"));
        device.setTopic(getColumn(cursor,"topic"));
        device.setPayload(getColumn(cursor,"payload"));
        JsonElement jsonElement = JsonParser.parseString(getColumn(cursor,"pins"));
        device.setPins(new Gson().fromJson(jsonElement, new TypeToken<List<String>>() {}.getType()));

        device.setBroker(getColumn(cursor,"broker"));

        return device;
    }

    public static boolean hasDevice(int brokerId){
        SQLiteDatabase db = DatabaseHelper.getInstance().getReadableDatabase();
        //Cursor cursor = db.rawQuery("SELECT * FROM "+table+" WHERE user_id=?",new String[]{userId+""});
        Cursor cursor = db.rawQuery("SELECT * FROM "+table+" WHERE broker_id=?",new String[]{brokerId+""});
        boolean has = cursor.moveToNext();
        cursor.close();
        db.close();
        return has;
    }

    private static String getColumn(Cursor cursor, String name){
        if(cursor.getColumnIndex(name)>0){
            return cursor.getString(cursor.getColumnIndex(name));
        }
        return "";
    }
    private static int getIntColumn(Cursor cursor, String name){
        return cursor.getInt(cursor.getColumnIndex(name));
    }
}