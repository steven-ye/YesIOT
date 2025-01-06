package com.example.yesiot.helper;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.NonNull;

import com.example.yesiot.object.BlueButton;

import java.util.ArrayList;
import java.util.List;

public class BlueButtonHelper extends AbstractHelper {
    final static String table = "blue_button";

    public static BlueButton get(int id) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        BlueButton button = null;
        Cursor cursor = dbHelper.select(table, null, "id=?", new String[]{id+""}, null);
        if(cursor.moveToFirst()){
            button = getItem(cursor);
        }
        cursor.close();
        dbHelper.close();
        return button;
    }

    public static BlueButton get(String service_uuid, String uuid) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        BlueButton blueButton = null;
        Cursor cursor = dbHelper.select(table, null, "service_uuid=? AND uuid=?", new String[]{ service_uuid, uuid }, null);
        if(cursor.moveToFirst()){
            blueButton = getItem(cursor);
        }
        cursor.close();
        dbHelper.close();
        return blueButton;
    }

    public static BlueButton get(String selection, String[] args){
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        BlueButton blueButton = null;
        Cursor cursor = dbHelper.select(table, null, selection, args, null);
        if(cursor.moveToFirst()){
            blueButton = getItem(cursor);
        }
        cursor.close();
        dbHelper.close();
        return blueButton;
    }

    public static boolean save(BlueButton blueButton) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        ContentValues cv = getContentValues(blueButton);
        int num;
        if(blueButton.id>0){
            num = dbHelper.update(table, cv, blueButton.id);
        }else{
            num = dbHelper.insert(table, cv);
            if(num>0)blueButton.id = num;
        }
        return num>0;
    }

    private static @NonNull ContentValues getContentValues(BlueButton button) {
        ContentValues cv = new ContentValues();
        cv.put("uuid", button.uuid);
        cv.put("name", button.name);
        cv.put("caption", button.caption);
        cv.put("title", button.title);
        cv.put("unit", button.unit);
        cv.put("service_uuid", button.service_uuid);
        cv.put("image", button.image);
        cv.put("type", button.type);
        cv.put("cmd_on", button.cmd_on);
        cv.put("cmd_off", button.cmd_off);
        cv.put("payload", button.payload);
        cv.put("weight", button.weight);
        cv.put("extra", button.extra);
        return cv;
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

    public static List<BlueButton> getList(String service_uuid){
        List<BlueButton> list = new ArrayList<>();
        if(service_uuid.length() < 8)return list;
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        //Cursor cursor = dbHelper.rawQuery("SELECT * FROM "+table+" WHERE user_id=?",new String[]{userId+""});
        Cursor cursor = dbHelper.query("SELECT * FROM "+table + " WHERE service_uuid=? ORDER BY weight asc, id asc",new String[]{ service_uuid});
        while(cursor.moveToNext()){
            list.add(getItem(cursor));
        }
        cursor.close();
        dbHelper.close();
        return list;
    }

    public static List<BlueButton> getList()
    {
        List<BlueButton> list = new ArrayList<>();
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        Cursor cursor = dbHelper.select(table, null, null, null, "weight desc, id asc");
        while(cursor.moveToNext()){
            list.add(getItem(cursor));
        }
        cursor.close();
        dbHelper.close();
        return list;
    }


    public static boolean has(BlueButton blueButton){
        return has(blueButton.service_uuid, blueButton.uuid);
    }

    public static boolean has(String service_uuid, String uuid){
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        Cursor cursor = dbHelper.select(table, null, "service_uuid=? AND uuid=?",new String[]{ service_uuid, uuid });
        boolean has = cursor.moveToNext();
        cursor.close();
        dbHelper.close();
        return has;
    }

    private static BlueButton getItem(Cursor cursor){
        BlueButton button = new BlueButton();
        button.id = getColumnInt(cursor,"id");
        button.uuid = getColumn(cursor,"uuid");
        button.name = getColumn(cursor,"name");
        button.caption = getColumn(cursor,"caption");
        button.title = getColumn(cursor,"title");
        button.unit = getColumn(cursor,"unit");
        button.type = getColumnInt(cursor,"type");
        button.image = getColumn(cursor,"image");
        button.service_uuid = getColumn(cursor,"service_uuid");
        button.payload = getColumn(cursor,"payload");
        button.weight = getColumnInt(cursor,"weight");
        button.extra = getColumn(cursor,"extra");

        return button;
    }
}