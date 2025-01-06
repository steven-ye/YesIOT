package com.example.yesiot.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.yesiot.IApplication;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SPUtil {
    private final static String SP_TAG = "YES_IOT";

    public static SharedPreferences getSP(){
        return getSP(IApplication.getContext(), SP_TAG);
    }

    public static SharedPreferences getSP(Context context, String tag){
        return context.getSharedPreferences(tag, Context.MODE_PRIVATE);
    }

    public static void putBrokerId(int id){
        putInt("broker_id", id);
    }
    public static int getBrokerId(){
        return getInt("broker_id", 0);
    }

    public static void putInt(String key, int value) {
        SharedPreferences.Editor editor = getSP().edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static int getInt(String key, int defValue) {
        return getSP().getInt(key, defValue);
    }

    public static void putString(String key, String val) {
        SharedPreferences.Editor editor = getSP().edit();
        editor.putString(key, val);
        editor.apply();
    }

    public static String getString(String key) {
        return getSP().getString(key, "");
    }

    public static String getString(String key, String defValue) {
        return getSP().getString(key, defValue);
    }

    public static void putBoolean(String key, boolean val) {
        SharedPreferences.Editor editor = getSP().edit();
        editor.putBoolean(key, val);
        editor.apply();
    }

    public static boolean getBoolean(String key) {
        return getSP().getBoolean(key, false);
    }

    public static boolean getBoolean(String key, boolean defValue) {
        return getSP().getBoolean(key, defValue);
    }

    /**
     * 保存List
     * @param tag String
     * @param datalist List<T>
     */
    public static <T> void putDataList(String tag, List<T> datalist) {
        if (null == datalist || datalist.size() <= 0) return;

        SharedPreferences.Editor editor = getSP().edit();
        Gson gson = new Gson();
        //转换成json数据，再保存
        String strJson = gson.toJson(datalist);
        //editor.clear();
        editor.putString(tag, strJson);
        editor.apply();
    }

    /**
     * 获取List
     * @param tag String
     * @return datalist List<T>
     */
    public static <T> List<T> getDataList(String tag) {
        List<T> datalist=new ArrayList<>();
        String strJson = getSP().getString(tag, null);
        if (null == strJson) {
            return datalist;
        }
        Gson gson = new Gson();
        datalist = gson.fromJson(strJson, new TypeToken<List<T>>() {
        }.getType());
        return datalist;
    }

    /**
     * 存储Map集合
     * @param key 键
     * @param map 存储的集合
     * @param <K> 指定Map的键
     * @param <T> 指定Map的值
     */

    public static <K,T> void putMap(String key , Map<K,T> map){
        if (map == null || map.isEmpty()){
            return;
        } else {
            map.size();
        }

        SharedPreferences.Editor editor = getSP().edit();

        Gson gson = new Gson();
        String strJson  = gson.toJson(map);
        editor.clear();
        editor.putString(key ,strJson);
        editor.apply();
    }

    /**
     * 获取Map集合
     * */
    public static <K,T> Map<K,T> getMap(String key){
        Map<K,T> map = new HashMap<>();
        String strJson = getSP().getString(key,null);
        if (strJson == null){
            return map;
        }
        Gson gson = new Gson();
        map = gson.fromJson(strJson,new TypeToken<Map<K,T>>(){}.getType());
        return map;
    }
}