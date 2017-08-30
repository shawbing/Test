package com.sensorslife.Data_Managers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.util.Log;

import com.sensorslife.CommonUtils.DatabaseConnect;
import com.sensorslife.Core_Activity;

import java.util.HashMap;

/**
 * Created by Administrator on 2017/8/25.
 *
 * 定位信息的实体结构，
 * 及其在数据库中的操作函数
 */

public class Location_Data extends ContentProvider {


    //数据库版本
    public static final int DATABASE_VERSION = 2;
    //定位内容提供的端口,
// 为什么本类的操作还要申明认证路径？？？
// 是为了其他类直接调用这个？
    public static String AUTHORITY = "com.sensorslife.provider.locations";

    // 定位提供者的检索路径
    private static final int LOCATIONS = 1;
    private static final int LOCATIONS_ID = 2;

    //
    // 定位信息内容及其存储的表结构的呈现
    public static final class LocationsData implements BaseColumns {
        private LocationsData() {  };

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Location_Data.AUTHORITY + "/locations");

        //接下来这两句有问题
//接下来这两句有问题
        //接下来这两句有问题
        //分别是：内容类型  节点（单个数据项）类型
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sensorslife.locations";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.sensorslife.locations";
        //统一定义列名。包括：主键ID（系统自动给的？？？）
//时间戳，设备ID，纬度，经度，方向，速度，高度，提供者，精度，标签
        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String LATITUDE = "double_latitude";
        public static final String LONGITUDE = "double_longitude";
        public static final String BEARING = "double_bearing";
        public static final String SPEED = "double_speed";
        public static final String ALTITUDE = "double_altitude";
        public static final String PROVIDER = "provider";
        public static final String ACCURACY = "accuracy";
        public static final String LABEL = "label";
    }
    //数据库的连接路径
    public static String DATABASE_NAME = Environment
            .getExternalStorageDirectory() + "/sensorslife/" + "locations.db";
    //数据库的表名，即locations
    public static final String[] DATABASE_TABLES = { "locations" };
    //定位信息表中的各个列名，并设置初始值
    public static final String[] TABLES_FIELDS = {
            LocationsData._ID + " integer primary key autoincrement,"
                    + LocationsData.TIMESTAMP + " real default 0,"
                    + LocationsData.DEVICE_ID + " text default '',"
                    + LocationsData.LATITUDE + " real default 0,"
                    + LocationsData.LONGITUDE + " real default 0,"
                    + LocationsData.BEARING + " real default 0,"
                    + LocationsData.SPEED + " real default 0,"
                    + LocationsData.ALTITUDE + " real default 0,"
                    + LocationsData.PROVIDER + " text default '',"
                    + LocationsData.ACCURACY + " real default 0,"
                    + LocationsData.LABEL + " text default '',"
                    //独一无二的标识：时间+设备编号
                    + "UNIQUE("
                    + LocationsData.TIMESTAMP + "," + LocationsData.DEVICE_ID + ")" };
    //初始化必要的对象
    private static UriMatcher sUriMatcher = null;//用于筛选出具体的数据
    //用于后面，设置数据库中字段的别名
    private static HashMap<String, String> locationsProjectionMap = null;
    private static DatabaseConnect connect = null;
    private static SQLiteDatabase database = null;

    //初始化数据库
    private boolean initializeDB()
    {
        if (connect == null) {
            //相当于新建一个数据库
            connect = new DatabaseConnect( getContext(), DATABASE_NAME
                    , null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS );
        }
        if( connect != null && ( database == null || ! database.isOpen() )) {
            //获取读写数据库的权限
            database = connect.getWritableDatabase();
        }
        return( database != null && connect != null);
    }

    //删除数据库中的数据
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {

        if( ! initializeDB() ) {
            Log.w("location_Database","unavailable");
            return 0;
        }

        int count = 0;

        switch (sUriMatcher.match(uri)) {
            case LOCATIONS:
                database.beginTransaction();
                count = database.delete(DATABASE_TABLES[0], selection,
                        selectionArgs);
                database.setTransactionSuccessful();
                database.endTransaction();
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        //通知数据已经改变
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    //获取类型
    @Override
    public String getType(Uri uri)
    {
        switch (sUriMatcher.match(uri)) {
            case LOCATIONS:
                return LocationsData.CONTENT_TYPE;
            case LOCATIONS_ID:
                return LocationsData.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    //插入数据
    @Override
    public Uri insert(Uri uri, ContentValues initialValues)
    {

        if( ! initializeDB() ) {
            Log.w("location_Database","unavailable");
            return null;
        }
//？？？？？？？为什么有这一步不直接用initialValues，
// 而要判断其值是否为空，
// 如果不为空就将其赋给新同类型的变量
// 若为空，就重新定义一个
        ContentValues values = (initialValues != null) ? new ContentValues(
                initialValues) : new ContentValues();

        switch (sUriMatcher.match(uri)) {
            case LOCATIONS:
                database.beginTransaction();
                long location_id = database.insertWithOnConflict(DATABASE_TABLES[0],
                        LocationsData.PROVIDER, values, SQLiteDatabase.CONFLICT_IGNORE);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (location_id > 0) {
                    Uri locationUri = ContentUris.withAppendedId(
                            LocationsData.CONTENT_URI, location_id);
                    getContext().getContentResolver().notifyChange(locationUri,
                            null);
                    return locationUri;
                }

                throw new SQLException("Failed to insert row into " + uri);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    //这个类的主函数，主要用于实例化对象
    @Override
    public boolean onCreate()
    {
        //主要对象的实例化
        AUTHORITY = getContext().getPackageName() + ".provider.locations";
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Location_Data.AUTHORITY, DATABASE_TABLES[0],
                LOCATIONS);
//ProjectionMap允许字段名及其值 一样‘
// 不过这里重复的好处在哪里？？？？？
        locationsProjectionMap = new HashMap<String, String>();
        locationsProjectionMap.put(LocationsData._ID, LocationsData._ID);
        locationsProjectionMap.put(LocationsData.TIMESTAMP,
                LocationsData.TIMESTAMP);
        locationsProjectionMap.put(LocationsData.DEVICE_ID,
                LocationsData.DEVICE_ID);
        locationsProjectionMap.put(LocationsData.LATITUDE,
                LocationsData.LATITUDE);
        locationsProjectionMap.put(LocationsData.LONGITUDE,
                LocationsData.LONGITUDE);
        locationsProjectionMap.put(LocationsData.BEARING,
                LocationsData.BEARING);
        locationsProjectionMap.put(LocationsData.SPEED, LocationsData.SPEED);
        locationsProjectionMap.put(LocationsData.ALTITUDE,
                LocationsData.ALTITUDE);
        locationsProjectionMap.put(LocationsData.PROVIDER,
                LocationsData.PROVIDER);
        locationsProjectionMap.put(LocationsData.ACCURACY,
                LocationsData.ACCURACY);
        locationsProjectionMap.put(LocationsData.LABEL, LocationsData.LABEL);

        return true;
    }

    //查询数据库中的数据
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder)
    {

        if( ! initializeDB() ) {
            //如果不能初始化数据库，将这个 异常 放入 日志中
            Log.w("location_Database","unavailable");
            return null;
        }

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
            case LOCATIONS:
                //只有一张表
                qb.setTables(DATABASE_TABLES[0]);
                //这是什么？提供一种映射，对象必须是HASHMAP
                // 可设置数据库中字段的别名（有点像注释），
                // 用户自定义的列名
                qb.setProjectionMap(locationsProjectionMap);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        try {
            Cursor c = qb.query(database, projection, selection, selectionArgs,
                    null, null, sortOrder);
            //貌似是通知，已调用相应的Uri，查询了数据
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        } catch (IllegalStateException e) {
            //将 异常 记录到系统日志中
            if (Core_Activity.DEBUG)
                Log.e(Core_Activity.TAG, e.getMessage());

            return null;
        }
    }

    //进入数据库，更新数据
    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs)
    {

        if( ! initializeDB() ) {
            Log.w("location_Database","unavailable");
            return 0;
        }

        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case LOCATIONS:
                database.beginTransaction();
                count = database.update(DATABASE_TABLES[0], values, selection,
                        selectionArgs);
                database.setTransactionSuccessful();
                database.endTransaction();
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

}

