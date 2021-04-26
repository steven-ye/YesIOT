package com.example.yesiot.helper;

import android.content.ContentValues;
import android.database.Cursor;


import com.example.yesiot.object.Panel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PanelHelper extends AbstractHelper {
    private static String table="panels";

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
        cv.put("width",panel.width);
        cv.put("height",panel.height);
        cv.put("sub",panel.sub);
        cv.put("payload",panel.payload);
        cv.put("cmd_on",panel.on);
        cv.put("cmd_off",panel.off);
        cv.put("pos",panel.pos);
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
        panel.width = map.get("width")==null?0:Integer.parseInt(map.get("width"));
        panel.height = map.get("height")==null?0:Integer.parseInt(map.get("height"));
        panel.deviceId = map.get("device_id")==null?0:Integer.parseInt(map.get("device_id"));
        panel.name = map.get("name")==null?"":map.get("name");
        panel.title = map.get("title")==null?"":map.get("title");
        panel.unit = map.get("unit")==null?"":map.get("unit");
        panel.image = map.get("image")==null?"":map.get("image");
        panel.payload = map.get("payload")==null?"":map.get("payload");
        panel.sub = map.get("sub")==null?"":map.get("sub");
        panel.off = map.get("cmd_off")==null?"":map.get("cmd_off");
        panel.on = map.get("cmd_on")==null?"":map.get("cmd_on");
        panel.pos = map.get("pos")==null?"":map.get("pos");
        return panel;
    }

    private static Panel getPanel(Cursor cursor){
        Panel panel = new Panel();
        panel.id = getColumnInt(cursor,"id");
        panel.type = getColumnInt(cursor,"type");
        panel.width = getColumnInt(cursor,"width");
        panel.height = getColumnInt(cursor,"height");
        panel.deviceId = getColumnInt(cursor,"device_id");
        panel.name = getColumn(cursor,"name");
        panel.title = getColumn(cursor,"title");
        panel.unit = getColumn(cursor,"unit");
        panel.image = getColumn(cursor,"image");
        panel.sub = getColumn(cursor,"sub");
        panel.payload = getColumn(cursor,"payload");
        panel.off = getColumn(cursor,"cmd_off");
        panel.on = getColumn(cursor,"cmd_on");
        panel.pos = getColumn(cursor,"pos");
        return panel;
    }

    public static boolean remove(int id){
        return remove(table,id);
    }
}