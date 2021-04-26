package com.example.yesiot.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.yesiot.IApplication;
import com.example.yesiot.object.Constants;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static DatabaseHelper mInstance = null;
    public synchronized static DatabaseHelper getInstance() {
        if (mInstance == null) {
            Context context = IApplication._getContext();
            mInstance = new DatabaseHelper(context);
        }
        return mInstance;
    }
    public DatabaseHelper(Context context) {
        super(context, Constants.DB_NAME, null, Constants.DB_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        String sql="CREATE TABLE devices(id INTEGER PRIMARY KEY AUTOINCREMENT,name VARCHAR(50),code VARCHAR(50),theme VARCHAR(50),image VARCHAR(50),"
            +"ip VARCHAR(50),pins VARCHAR(255),sub VARCHAR(100),topic VARCHAR(100),payload VARCHAR(200),user_id INTEGER)";
        db.execSQL(sql);
        sql="CREATE TABLE panels(id INTEGER PRIMARY KEY AUTOINCREMENT,name VARCHAR(50),title VARCHAR(50),unit VARCHAR(50),"
            +"image VARCHAR(50),device_id INTEGER,type INGETER,payload VARCHAR(50),cmd_on VARCHAR(50),cmd_off VARCHAR(50),"
            +"sub VARCHAR(50),state VARCHAR(50),width INTEGER,height INGETER,pos VARCHAR(20))";
        db.execSQL(sql);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL("DROP TABLE devices");
        //db.execSQL("DROP TABLE panels");
        //onCreate(db);

        System.out.println("DatabaseHelper: onUpgrade");
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
}
