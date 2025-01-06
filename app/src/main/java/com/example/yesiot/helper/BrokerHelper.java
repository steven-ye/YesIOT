package com.example.yesiot.helper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.example.yesiot.object.Device;
import com.example.yesiot.util.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BrokerHelper extends AbstractHelper {
    DatabaseHelper dbHelper;
    final static String table = "brokers";

    public BrokerHelper() {
        dbHelper = DatabaseHelper.getInstance();
    }

    public static Map<String,String> get(int id) {
        return getMap(table,id);
    }

    public static Map<String,String> get(String selection, String[] args){
        SQLiteDatabase db = DatabaseHelper.getInstance().getReadableDatabase();
        Map<String,String> map = new HashMap<>();
        Cursor cursor = db.query(table, null, selection, args, null, null, null);
        if(cursor.moveToFirst()){
            map = getMap(cursor);
        }
        cursor.close();
        db.close();
        return map;
    }

    public static boolean save(Map<String,String> map) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        int num;
        int id = 0;
        if(!TextUtils.isEmpty(map.get("id"))){
            id = Integer.parseInt(map.get("id"));
        }
        ContentValues cv = new ContentValues();
        for(Map.Entry<String,String> entry: map.entrySet()){
            if(entry.getKey().equals("id"))continue;
            cv.put(entry.getKey(),entry.getValue());
        }
        if(id>0){
            num = dbHelper.update(table,cv, id);
        }else{
            num = dbHelper.insert(table,cv);
            if(num>0)map.put("id",num+"");
        }
        return num>0;
    }

    public static boolean remove(int id){
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        int num = dbHelper.delete(table,"id=?", new String[]{id+""});
        return num>0;
    }

    public static List<Map<String,String>> getList(int userId){
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        Cursor cursor = dbHelper.select(table, null, "user_id=?", new String[]{userId+""});
        List<Map<String,String>> list = new ArrayList<>();
        while(cursor.moveToNext()){
            list.add(getMap(cursor));
        }
        cursor.close();
        dbHelper.close();
        return list;
    }

    public static List<Map<String,String>> getList(){
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        Cursor cursor = dbHelper.select(table,null,null,null);
        List<Map<String,String>> list = new ArrayList<>();
        while(cursor.moveToNext()){
            list.add(getMap(cursor));
        }
        cursor.close();
        dbHelper.close();
        return list;
    }

    public static boolean has(int id){
        boolean result;
        if(id<1)return false;
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        Cursor cursor = dbHelper.select(table,null,"id=?", new String[]{ id+"" });
        result = cursor.moveToFirst();
        cursor.close();
        dbHelper.close();
        return result;
    }
}