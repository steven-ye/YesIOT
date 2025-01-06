package com.example.yesiot.helper;

import android.annotation.SuppressLint;
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

public class DeviceHelper extends AbstractHelper {
    DatabaseHelper dbHelper;
    final static String table = "devices";

    public DeviceHelper() {
        dbHelper = DatabaseHelper.getInstance();
    }

    public static Device get(int id) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        Device device = new Device();
        Cursor cursor = dbHelper.select(table, null, "id=?", new String[]{id+""});
        if(cursor.moveToFirst()){
            device = getItem(cursor);
        }
        cursor.close();
        dbHelper.close();
        return device;
    }

    public static Device get(String selection, String[] args){
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        Device device = new Device();
        Cursor cursor = dbHelper.select(table, null, selection, args);
        if(cursor.moveToFirst()){
            device = getItem(cursor);
        }
        cursor.close();
        dbHelper.close();
        return device;
    }

    public static boolean save(Device device) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
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
        cv.put("weight",device.getWeight());
        cv.put("broker_id",device.getBrokerId());
        int id = device.getId();
        int num;
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

    public static boolean has(Device device){
        Device dev = get("code=? and ip=?", new String[]{device.getCode(),device.getIp()});
        return dev.getId() > 0;
    }

    public static List<Device> getList(int brokerId){
        List<Device> list = new ArrayList<>();
        if(brokerId<1)return list;
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        //Cursor cursor = db.rawQuery("SELECT * FROM "+table+" WHERE user_id=?",new String[]{userId+""});
        Cursor cursor = dbHelper.query("SELECT a.*,b.name AS broker FROM "+table + " a, brokers b WHERE a.broker_id=b.id AND a.broker_id=? ORDER BY a.weight asc, a.id asc",new String[]{brokerId+""});
        while(cursor.moveToNext()){
            list.add(getItem(cursor));
        }
        cursor.close();
        dbHelper.close();
        return list;
    }

    public static List<Device> getList(){
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        List<Device> list = new ArrayList<>();
        Cursor cursor = dbHelper.query("SELECT a.*,b.name AS broker FROM "+table + " a, brokers b WHERE a.broker_id=b.id ORDER BY weight desc, id asc",null);
        while(cursor.moveToNext()){
            list.add(getItem(cursor));
        }
        cursor.close();
        dbHelper.close();
        return list;
    }

    public static boolean has(int brokerId, String code){
        SQLiteDatabase db = DatabaseHelper.getInstance().getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+table+" WHERE broker_id=? AND code=?",new String[]{brokerId+"", code});
        boolean has = cursor.moveToNext();
        cursor.close();
        db.close();
        return has;
    }

    private static Device getItem(Cursor cursor){
        Device device = new Device();
        device.setId(getColumnInt(cursor,"id"));
        device.setBrokerId(getColumnInt(cursor,"broker_id"));
        device.setName(getColumn(cursor,"name"));
        device.setCode(getColumn(cursor,"code"));
        device.setTheme(getColumn(cursor,"theme"));
        device.setImage(getColumn(cursor,"image"));
        device.setIp(getColumn(cursor,"ip"));
        device.setPort(getColumnInt(cursor,"port"));
        device.setSub(getColumn(cursor,"sub"));
        device.setTopic(getColumn(cursor,"topic"));
        device.setPayload(getColumn(cursor,"payload"));
        device.setWeight(getColumnInt(cursor,"weight"));
        device.setBrokerId(getColumnInt(cursor,"broker_id"));
        JsonElement jsonElement = JsonParser.parseString(getColumn(cursor,"pins"));
        device.setPins(new Gson().fromJson(jsonElement, new TypeToken<List<String>>() {}.getType()));

        return device;
    }

    public static boolean hasDevice(int brokerId){
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        //Cursor cursor = db.rawQuery("SELECT * FROM "+table+" WHERE user_id=?",new String[]{userId+""});
        Cursor cursor = dbHelper.select(table, null, "broker_id=?", new String[]{brokerId+""});
        boolean has = cursor.moveToNext();
        cursor.close();
        dbHelper.close();
        return has;
    }


    protected static int getMaxID(){
        int id = 0;
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        Cursor cursor = dbHelper.query("SELECT MAX(id) as id FROM " + table, null);
        if(cursor.moveToNext()){
            id = cursor.getInt(0);
        }
        cursor.close();
        dbHelper.close();
        return id;
    }
}