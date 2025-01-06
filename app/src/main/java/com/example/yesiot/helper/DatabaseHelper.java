package com.example.yesiot.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.yesiot.IApplication;
import com.example.yesiot.object.Constants;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static DatabaseHelper mInstance = null;
    public synchronized static DatabaseHelper getInstance() {
        if (mInstance == null) {
            Context context = IApplication.getContext();
            mInstance = new DatabaseHelper(context);
        }
        return mInstance;
    }
    public DatabaseHelper(Context context) {
        super(context, Constants.DB_NAME, null, Constants.DB_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS brokers(id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR(50),protocol VARCHAR(20),"
                + "host VARCHAR(100), port INTEGER, path VARCHAR(50), clientId VARCHAR(50), username VARCHAR(50),password VARCHAR(50),"
                + "alive INTEGER, timeout INTEGER,topic VARCHAR(50),message VARCHAR(200), auto VARCHAR(5),session VARCHAR(5))";
        db.execSQL(sql);
        sql = "CREATE TABLE IF NOT EXISTS devices(id INTEGER PRIMARY KEY AUTOINCREMENT,name VARCHAR(50),code VARCHAR(50),theme VARCHAR(50),image VARCHAR(50),"
            +"ip VARCHAR(50), port INTEGER, pins VARCHAR(255),sub VARCHAR(100),topic VARCHAR(100),payload VARCHAR(200),broker_id INTEGER,"
            +"user_id INTEGER, weight INTEGER)";
        db.execSQL(sql);
        //设备控制面板
        sql = "CREATE TABLE IF NOT EXISTS panels(id INTEGER PRIMARY KEY AUTOINCREMENT,name VARCHAR(50),title VARCHAR(50),unit VARCHAR(50),device_id INTEGER,"
            +"type INGETER,design INGETER,img_id INTEGER,image VARCHAR(50),payload VARCHAR(50),cmd_on VARCHAR(50),cmd_off VARCHAR(50),"
            +"state VARCHAR(50),width INTEGER,height INGETER,size VARCHAR(20),pos VARCHAR(20),title_size VARCHAR(10),unit_size VARCHAR(10))";
        db.execSQL(sql);
        //蓝牙设备
        sql = "CREATE TABLE IF NOT EXISTS blue_device(id INTEGER PRIMARY KEY AUTOINCREMENT,mac VARCHAR(50),service_uuid VARCHAR(50),name VARCHAR(50),"
                +"title VARCHAR(50),alias VARCHAR(50),image VARCHAR(50),type INTEGER,user_id INTEGER,weight INTEGER,extra VARCHAR(255))";
        db.execSQL(sql);
        //蓝牙设备控制按钮
        sql = "CREATE TABLE IF NOT EXISTS blue_button(id INTEGER PRIMARY KEY AUTOINCREMENT,device_id INTEGER,uuid VARCHAR(150),service_uuid VARCHAR(150),name VARCHAR(50),"
                +"title VARCHAR(50),caption VARCHAR(50),unit VARCHAR(50),type INGETER,design INGETER,img_id INTEGER,image VARCHAR(50),cmd_on VARCHAR(50),cmd_off VARCHAR(50),"
                +"payload VARCHAR(255),status INTEGER,width INTEGER,height INGETER,size VARCHAR(20),weight VARCHAR(20),extra VARCHAR(255))";
        db.execSQL(sql);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL("DROP TABLE brokers");
        //db.execSQL("DROP TABLE devices");
        //db.execSQL("DROP TABLE panels");
        onCreate(db);
        System.out.println("DatabaseHelper: onUpgrade ["+oldVersion+" > "+newVersion+"]");
    }

    public int save(String table,ContentValues cv){
        return insert(table,cv);
    }
    public int save(String table,ContentValues cv,int id){
        return save(table,cv,"id=?", new String[]{id+""});
    }
    public int save(String table,ContentValues cv,String whereClause,String[] args){
        SQLiteDatabase db = this.getWritableDatabase();
        int count = db.update(table, cv, whereClause, args);
        db.close();
        return count;
    }

    public int insert(String table,ContentValues cv){
        SQLiteDatabase db = this.getWritableDatabase();
        long id = db.insert(table, null, cv);
        db.close();
        return (int) id;
    }
    public int update(String table,ContentValues cv,int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] args = {String.valueOf(id)};
        int count = db.update(table, cv, "id=?", args);
        db.close();
        return count;
    }
    public int delete(String table,String whereClause,String[]  whereArgs){
        SQLiteDatabase db = this.getWritableDatabase();
        int count = db.delete(table,whereClause, whereArgs);
        db.close();
        return count;
    }

    public Cursor select(String table, String[] columns, String selection, String[] selectionArgs)
    {
        return select(table, columns, selection, selectionArgs, null);
    }

    public Cursor select(String table, String[] columns, String selection, String[] selectionArgs, String orderBy)
    {
        return select(table, columns, selection, selectionArgs, null, null, orderBy, null);
    }

    public Cursor select(String table, String[] columns, String selection, String[] selectionArgs, String orderBy, String limit)
    {
        return select(table, columns, selection, selectionArgs, null, null, orderBy, limit);
    }

    public Cursor select(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit)
    {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    public Cursor query(String sql, String[] selectionArgs)
    {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(sql, selectionArgs);
    }

    public void close()
    {
        SQLiteDatabase db = getReadableDatabase();
        db.close();
    }
}
