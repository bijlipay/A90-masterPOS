package com.vanstone.redsysa90prokeypos.tools;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataBaseOpenHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "db_master_pos.db";
    public static final String TABLE_CMI_KEY_INFO = "cmi_key_info";
    public static final String TABLE_CMA_KEY_INFO = "cma_key_info";
    public static final String TABLE_CMTK_KEY_INFO = "cmtk_key_info";
    public static final String TABLE_KEY_VER_INFO = "key_ver_info";
    public static final String TABLE_ZCMK_INFO = "zcmk_info";
    public static final String TABLE_KEYIMPORT_INFO = "keyimport_info";
    public static final String TABLE_KEYEXPORT_INFO = "keyexport_info";

    public static final String KEY_ID = "_id";
    public static final String KEY_KEY_TYPE = "key_type";
    public static final String KEY_CMI_INDEX = "key_cmi_index";
    public static final String KEY_CI_INDEX = "key_ci_index";
    public static final String KEY_CI_VERSION_KCV = "key_ci_version_kcv";
    public static final String KEY_CI_VERSION_INDEX = "key_ci_version_index";
    public static final String KEY_CMA_INDEX = "key_cma_index";
    public static final String KEY_CA_INDEX = "key_ca_index";
    public static final String KEY_CMTK_INDEX = "key_cmtk_index";
    public static final String KEY_CTK_INDEX = "key_ctk_index";
    public static final String KEY_VERSION_KCV = "key_version_kcv";
    public static final String KEY_VERSION = "key_version";
    public static final String KEY_ZCMK_INDEX = "key_zcmk_index";
    public static final String KEY_TMK_INDEX = "key_tmk_index";
    public static final String KEY_KCV = "key_kcv";
    public static final String DEVICE_SERIAL = "device_serial";
    public static final String DATE = "date";
    public static final String TIME = "time";
    public static final String ENCRYPTED_KEY = "encrypted_key";

    public static final String SQL_CREATE_TABLE_CMI_KEY_INFO = "create table "
            + TABLE_CMI_KEY_INFO
            + "(" + KEY_ID
            + " integer primary key autoincrement, "
            + KEY_KEY_TYPE +" integer, "
            + KEY_CMI_INDEX +" integer, "
            + KEY_KCV + " text, "
            + KEY_CI_INDEX + " integer)";

    public static final String SQL_CREATE_TABLE_CMA_KEY_INFO = "create table "
            + TABLE_CMA_KEY_INFO
            + "(" + KEY_ID
            + " integer primary key autoincrement, "
            + KEY_KEY_TYPE +" integer, "
            + KEY_CMA_INDEX +" integer, "
            + KEY_KCV + " text, "
            + KEY_CA_INDEX + " integer)";

    public static final String SQL_CREATE_TABLE_CMTK_KEY_INFO = "create table "
            + TABLE_CMTK_KEY_INFO
            + "(" + KEY_ID
            + " integer primary key autoincrement, "
            + KEY_KEY_TYPE +" integer, "
            + KEY_CMTK_INDEX +" integer, "
            + KEY_KCV + " text, "
            + KEY_CTK_INDEX + " integer)";

    public static final String SQL_CREATE_TABLE_KEY_VER_INFO = "create table "
            + TABLE_KEY_VER_INFO
            + "(" + KEY_ID
            + " integer primary key autoincrement, "
            + KEY_KEY_TYPE +" integer, "
            + KEY_VERSION_KCV + " text, "
            +  KEY_VERSION + " text)";

    public static final String SQL_CREATE_TABLE_ZCMK_INFO = "create table "
            + TABLE_ZCMK_INFO
            + "(" + KEY_ID
            + " integer primary key autoincrement, "
            + KEY_KEY_TYPE +" integer, "
            + KEY_KCV + " text, "
            + KEY_ZCMK_INDEX + " integer)";

    public static final String SQL_CREATE_TABLE_KEYIMPORT_INFO = "create table "
            + TABLE_KEYIMPORT_INFO
            + "(" + KEY_ID
            + " integer primary key autoincrement, "
            + KEY_KEY_TYPE +" integer, "
            + ENCRYPTED_KEY +" text, "
            + KEY_KCV + " text, "
            + DEVICE_SERIAL + " text, "
            + KEY_TMK_INDEX + " integer)";

    public static final String SQL_CREATE_TABLE_KEYEXPORT_INFO = "create table "
            + TABLE_KEYEXPORT_INFO
            + "(" + KEY_ID
            + " integer primary key autoincrement, "
            + KEY_KEY_TYPE +" integer, "
            + KEY_KCV + " text, "
            + DEVICE_SERIAL + " text, "
            + DATE + " text, "
            + TIME + " text, "
            + KEY_TMK_INDEX + " integer)";

    public static final String SQL_ORDER_BY_KEY_ID_ASC ="ORDER BY " +  KEY_ID + " ASC";
    private static Map<String, DataBaseOpenHelper> dbMaps = new HashMap<String, DataBaseOpenHelper>();
    private OnSqliteUpdateListener onSqliteUpdateListener;
    /**
     * 建表语句列表
     */
    private List<String> createTableList;
    private static String nowDbName;
 
    private DataBaseOpenHelper(Context context, String dbName, int dbVersion, List<String> tableSqls) {
        super(context, dbName, null, dbVersion);
        nowDbName = dbName;
        createTableList = new ArrayList<String>();
        createTableList.addAll(tableSqls);
    }
 
    /**
     *
     * @Title: getInstance
     * @Description: 获取数据库实例
     * @param @param context
     * @param @param userId
     * @param @return
     * @return DataBaseOpenHelper
     * @author lihy
     */
    public static DataBaseOpenHelper getInstance(Context context, String dbName, int dbVersion, List<String> tableSqls) {
        DataBaseOpenHelper dataBaseOpenHelper = dbMaps.get(dbName);
        if (dataBaseOpenHelper == null) {
            dataBaseOpenHelper = new DataBaseOpenHelper(context, dbName, dbVersion, tableSqls);
        }
        dbMaps.put(dbName, dataBaseOpenHelper);
        return dataBaseOpenHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String sqlString : createTableList) {
            db.execSQL(sqlString);
        }
    }

    /**
     *
     * @Title: execSQL
     * @Description: Sql写入
     * @param @param sql
     * @param @param bindArgs
     * @return void
     * @author lihy
     */
    public static void execSQL(String sql, Object[] bindArgs) {
        DataBaseOpenHelper dataBaseOpenHelper = dbMaps.get(nowDbName);
        synchronized (dataBaseOpenHelper) {
            SQLiteDatabase database = dataBaseOpenHelper.getWritableDatabase();
            database.execSQL(sql, bindArgs);
        }
    }

    /**
     *
     * @Title: rawQuery
     * @Description:
     * @param @param sql查询
     * @param @param bindArgs
     * @param @return
     * @return Cursor
     * @author lihy
     */
    public static Cursor rawQuery(String sql, String[] bindArgs) {
        DataBaseOpenHelper dataBaseOpenHelper = dbMaps.get(nowDbName);
        synchronized (dataBaseOpenHelper) {
            SQLiteDatabase database = dataBaseOpenHelper.getReadableDatabase();
            Cursor cursor = database.rawQuery(sql, bindArgs);
            return cursor;
        }
    }

    /**
     *
     * @Title: insert
     * @Description: 插入数据
     * @param @param table
     * @param @param contentValues 设定文件
     * @return void 返回类型
     * @author lihy
     * @throws
     */
    public static void insert(String table, ContentValues contentValues) {
        DataBaseOpenHelper dataBaseOpenHelper = dbMaps.get(nowDbName);
        synchronized (dataBaseOpenHelper) {
            SQLiteDatabase database = dataBaseOpenHelper.getWritableDatabase();
            database.insert(table, null, contentValues);
        }
    }

    /**
     *
     * @Title: update
     * @Description: 更新
     * @param @param table
     * @param @param values
     * @param @param whereClause
     * @param @param whereArgs 设定文件
     * @return void 返回类型
     * @throws
     */
    public static void update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        DataBaseOpenHelper dataBaseOpenHelper = dbMaps.get(nowDbName);
        synchronized (dataBaseOpenHelper) {
            SQLiteDatabase database = dataBaseOpenHelper.getWritableDatabase();
            database.update(table, values, whereClause, whereArgs);
        }
    }
    /**
     *
     * @Title: delete
     * @Description:删除
     * @param @param table
     * @param @param whereClause
     * @param @param whereArgs
     * @return void
     * @author lihy
     */
    public static void delete(String table, String whereClause, String[] whereArgs) {
        DataBaseOpenHelper dataBaseOpenHelper = dbMaps.get(nowDbName);
        synchronized (dataBaseOpenHelper) {
            SQLiteDatabase database = dataBaseOpenHelper.getWritableDatabase();
            database.delete(table, whereClause, whereArgs);
        }
    }

    /**
     *
     * @Title: query
     * @Description: 查
     * @param @param table
     * @param @param columns
     * @param @param selection
     * @param @param selectionArgs
     * @param @param groupBy
     * @param @param having
     * @param @param orderBy
     * @return void
     * @author lihy
     */
    public static Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having,
                               String orderBy) {
        DataBaseOpenHelper dataBaseOpenHelper = dbMaps.get(nowDbName);
        synchronized (dataBaseOpenHelper) {
            SQLiteDatabase database = dataBaseOpenHelper.getReadableDatabase();
            // Cursor cursor = database.rawQuery("select * from "
            // + TableName.TABLE_NAME_USER + " where userId =" + userId, null);
            Cursor cursor = database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
            return cursor;
        }
    }
    /**
     *
     * @Description:查
     * @param table
     * @param columns
     * @param selection
     * @param selectionArgs
     * @param groupBy
     * @param having
     * @param orderBy
     * @param limit
     * @return
     * Cursor
     * @exception:
     * @author: lihy
     * @time:2015-4-3 上午9:37:29
     */
    public static Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having,
                               String orderBy, String limit) {
        DataBaseOpenHelper dataBaseOpenHelper = dbMaps.get(nowDbName);
        synchronized (dataBaseOpenHelper) {
            SQLiteDatabase database = dataBaseOpenHelper.getReadableDatabase();
            // Cursor cursor = database.rawQuery("select * from "
            // + TableName.TABLE_NAME_USER + " where userId =" + userId, null);
            Cursor cursor = database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
            return cursor;
        }
    }

    /**
     *
     * @Description 查询，方法重载,table表名，sqlString条件
     * @param @return
     * @return Cursor
     * @author lihy
     */
    public static Cursor query(String tableName, String sqlString) {
        DataBaseOpenHelper dataBaseOpenHelper = dbMaps.get(nowDbName);
        synchronized (dataBaseOpenHelper) {
            SQLiteDatabase database = dataBaseOpenHelper.getReadableDatabase();
            Cursor cursor = database.rawQuery("select * from " + tableName + " " + sqlString, null);

            return cursor;
        }
    }

    /**
     * @see SQLiteOpenHelper#close()
     */
    public static void clear(String tableName) {
        DataBaseOpenHelper dataBaseOpenHelper = dbMaps.get(nowDbName);
        synchronized (dataBaseOpenHelper){
            SQLiteDatabase db = dataBaseOpenHelper.getWritableDatabase();
            db.execSQL("delete from " + tableName);
            db.execSQL("update sqlite_sequence set seq=0 where name='"+ tableName +"'");
        }

    }

    public static void dbClose(){
        DataBaseOpenHelper dataBaseOpenHelper = dbMaps.get(nowDbName);
        dataBaseOpenHelper.close();
    }

    /**
     * onUpgrade()方法在数据库版本每次发生变化时都会把用户手机上的数据库表删除，然后再重新创建。<br/>
     * 一般在实际项目中是不能这样做的，正确的做法是在更新数据库表结构时，还要考虑用户存放于数据库中的数据不会丢失,从版本几更新到版本几。(非
     * Javadoc)
     */
    @Override
    public  void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
        if (onSqliteUpdateListener != null) {
            onSqliteUpdateListener.onSqliteUpdateListener(db, arg1, arg2);
        }
    }

    interface OnSqliteUpdateListener{
        void onSqliteUpdateListener(SQLiteDatabase db, int oldVersion, int newVersion);
    }

    public void setOnSqliteUpdateListener(OnSqliteUpdateListener onSqliteUpdateListener) {
        this.onSqliteUpdateListener = onSqliteUpdateListener;
    }
}