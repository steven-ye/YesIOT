package com.example.yesiot.helper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import com.example.yesiot.object.Panel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PanelHelper extends AbstractHelper {
    private static final String table="panels";

    public static Panel get(int id) {
        return getPanel(getMap(table,id));
    }

    public static boolean save(Panel panel) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        int num;
        ContentValues cv = new ContentValues();
        cv.put("name",panel.name);
        cv.put("title",panel.title);
        cv.put("unit",panel.unit);
        cv.put("image",panel.image);
        cv.put("device_id",panel.deviceId);
        cv.put("type",panel.type);
        cv.put("design",panel.design);
        cv.put("width",panel.width);
        cv.put("height",panel.height);
        cv.put("payload",panel.payload);
        cv.put("cmd_on",panel.on);
        cv.put("cmd_off",panel.off);
        cv.put("size",panel.size);
        cv.put("pos",panel.pos);
        cv.put("title_size",panel.title_size);
        cv.put("unit_size",panel.unit_size);
        if(panel.id>0){
            num = dbHelper.update(table,cv,panel.id);
        }else{
            num = dbHelper.insert(table,cv);
            if(num>0)panel.id = num;
        }
        return num>0;
    }

    public static boolean savePos(int id,String pos) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        int num=0;
        ContentValues cv = new ContentValues();
        cv.put("pos",pos);
        if(id>0){
            num = dbHelper.update(table,cv,id);
        }
        return num>0;
    }
    public static boolean hasPanel(int deviceId){
        SQLiteDatabase db = DatabaseHelper.getInstance().getReadableDatabase();
        //Cursor cursor = db.rawQuery("SELECT * FROM "+table+" WHERE user_id=?",new String[]{userId+""});
        Cursor cursor = db.rawQuery("SELECT * FROM "+table+" WHERE device_id=?",new String[]{deviceId+""});
        boolean has = cursor.moveToNext();
        cursor.close();
        db.close();
        return has;
    }
    public static List<Panel> getList(int deviceId){
        List<Panel> panelList = new ArrayList<>();
        List<Map<String,String>> list = getMapList(table,"device_id=?", new String[] {deviceId+""});
        for( Map<String,String> map: list) {
            panelList.add(getPanel(map));
        }
        return panelList;
    }

    private static Panel getPanel(Map<String,String> map){
        Panel panel = new Panel();
        panel.id = map.get("id")==null?0:Integer.parseInt(map.get("id"));
        panel.type = map.get("type")==null?0:Integer.parseInt(map.get("type"));
        panel.design = map.get("design")==null?0:Integer.parseInt(map.get("design"));
        panel.width = map.get("width")==null?0:Integer.parseInt(map.get("width"));
        panel.height = map.get("height")==null?0:Integer.parseInt(map.get("height"));
        panel.deviceId = map.get("device_id")==null?0:Integer.parseInt(map.get("device_id"));
        panel.name = map.get("name")==null?"":map.get("name");
        panel.title = map.get("title")==null?"":map.get("title");
        panel.unit = map.get("unit")==null?"":map.get("unit");
        panel.image = map.get("image")==null?"":map.get("image");
        panel.payload = map.get("payload")==null?"":map.get("payload");
        panel.off = map.get("cmd_off")==null?"":map.get("cmd_off");
        panel.on = map.get("cmd_on")==null?"":map.get("cmd_on");
        panel.size = map.get("pos")==null?"":map.get("size");
        panel.pos = map.get("pos")==null?"":map.get("pos");
        panel.title_size = map.get("title_size")==null?"":map.get("title_size");
        panel.unit_size = map.get("unit_size")==null?"":map.get("unit_size");
        return panel;
    }

    private static Panel getPanel(Cursor cursor){
        Panel panel = new Panel();
        panel.id = getColumnInt(cursor,"id");
        panel.type = getColumnInt(cursor,"type");
        panel.design = getColumnInt(cursor,"design");
        panel.width = getColumnInt(cursor,"width");
        panel.height = getColumnInt(cursor,"height");
        panel.deviceId = getColumnInt(cursor,"device_id");
        panel.name = getColumn(cursor,"name");
        panel.title = getColumn(cursor,"title");
        panel.unit = getColumn(cursor,"unit");
        panel.image = getColumn(cursor,"image");
        panel.payload = getColumn(cursor,"payload");
        panel.off = getColumn(cursor,"cmd_off");
        panel.on = getColumn(cursor,"cmd_on");
        panel.size = getColumn(cursor,"size");
        panel.pos = getColumn(cursor,"pos");
        panel.title_size = getColumn(cursor,"title_size");
        panel.unit_size = getColumn(cursor,"unit_size");
        return panel;
    }

    public static boolean delete(int id){
        return delete(table,id);
    }
    public static boolean remove(int deviceId){
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        int num = dbHelper.delete(table,"device_id=?", new String[]{deviceId+""});
        return num>0;
    }
}