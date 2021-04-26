package com.example.yesiot.helper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class AbstractHelper {
    public static Map<String, String> getMap(String table, int id){
        SQLiteDatabase db = DatabaseHelper.getInstance().getReadableDatabase();
        Map<String, String> map = new HashMap<>();
        if(id>0){
            Cursor cursor = db.rawQuery("select * from "+table+" where id=?",new String[]{id+""});
            if(cursor.moveToFirst()){
               map = getMap(cursor);
            }
            cursor.close();
        }
        db.close();
        return map;
    }

    private static Map<String, String> getMap(Cursor cursor){
        Map<String, String> map = new HashMap<>();
        for(String name: cursor.getColumnNames()){
            map.put(name,getColumn(cursor,name));
        }
        return map;
    }

    public static List<Map<String, String>> getMapList(String table, String selection, String[] args){
        SQLiteDatabase db = DatabaseHelper.getInstance().getReadableDatabase();
        String sql = "select * from "+table;
        if(!TextUtils.isEmpty(selection)){
            sql += " where "+selection;
        }
        List<Map<String, String>> list = new ArrayList<>();
        if(!db.isOpen()) {
            return list;
        };
        Cursor cursor = db.rawQuery(sql,args);
        //Cursor cursor = db.query(table,null, selection, args, null, null, null);
        while(cursor.moveToNext()){
            list.add(getMap(cursor));
        }
        cursor.close();
        db.close();
        return list;
    }

    public static boolean remove(String table, int id){
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        int num = dbHelper.delete(table,"id=?",new String[]{id+""});
        return num>0;
    }

    public static boolean remove(String table,String whereClause, String[] args){
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        int num = dbHelper.delete(table,whereClause,args);
        return num>0;
    }

    public static String getColumn(Cursor cursor, String name){
        return cursor.getString(cursor.getColumnIndex(name));
    }
    public static int getColumnInt(Cursor cursor, String name){
        return cursor.getInt(cursor.getColumnIndex(name));
    }
}