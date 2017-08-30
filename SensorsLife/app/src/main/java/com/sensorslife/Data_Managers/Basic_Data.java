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
 * 1、基本信息，即：设备的型号、版本等信息。创建实体
 * 2、对基本的增删改查的操作。创建数据库操作函数
 */

public class Basic_Data extends ContentProvider {

    //
    public static final int DATABASE_VERSION = 7;

    //将这个类授权给share核心类，还是获取核心大类的权利认证
    public static String AUTHORITY = "com.sensorslife.provider.basic";
    //设置编号，这里基本上不会用到插件
    private static final int DEVICE_INFO = 1;
    private static final int DEVICE_INFO_ID = 2;
    private static final int SETTING = 3;
    private static final int SETTING_ID = 4;

    //安装了本程序的设备的信息
    public static final class Share_Device implements BaseColumns {
        private Share_Device() {    };

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Basic_Data.AUTHORITY + "/basic_device");
        //这两个是什么鬼？？？内容类型（dir多列数据），和内容项的类型（item单列数据）
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.sensorslife.device";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.sensorslife.device";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String BOARD = "board";
        public static final String BRAND = "brand";
        public static final String DEVICE = "device";
        public static final String BUILD_ID = "build_id";
        public static final String HARDWARE = "hardware";
        public static final String MANUFACTURER = "manufacturer";
        public static final String MODEL = "model";
        public static final String PRODUCT = "product";
        public static final String SERIAL = "serial";
        public static final String RELEASE = "release";
        public static final String RELEASE_TYPE = "release_type";
        public static final String SDK = "sdk";
        public static final String LABEL = "label";
    }

    //有关软件相关设置的信息
    public static final class Share_Settings implements BaseColumns {
        private Share_Settings() {      };

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Basic_Data.AUTHORITY + "/basic_settings");
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.sensorslife.settings";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.sensorslife.settings";

        public static final String SETTING_ID = "_id";
        public static final String SETTING_KEY = "key";
        public static final String SETTING_VALUE = "value";
        public static final String SETTING_PACKAGE_NAME = "package_name";
    }

    // 定义数据库的名称，并包含了存储路径
    public static String DATABASE_NAME = Environment
            .getExternalStorageDirectory() + "/sensorslife/" + "basic.db";
    //定义数据库中存储的几个表名,
    // 怎么基本的设备信息都需要设置信息？？？？？
    public static final String[] DATABASE_TABLES = { "basic_device",
            "basic_settings" };
    //定义数据表的列表项的名称，及其相应的数据类型和默认值
    public static final String[] TABLES_FIELDS =
            {
                    //关于设备的信息           第一个是主键
                    Share_Device._ID + " integer primary key autoincrement,"
                            + Share_Device.TIMESTAMP + " real default 0,"
                            + Share_Device.DEVICE_ID + " text default '',"
                            + Share_Device.BOARD + " text default '',"
                            + Share_Device.BRAND + " text default '',"
                            + Share_Device.DEVICE + " text default '',"
                            + Share_Device.BUILD_ID + " text default '',"
                            + Share_Device.HARDWARE + " text default '',"
                            + Share_Device.MANUFACTURER + " text default '',"
                            + Share_Device.MODEL + " text default '',"
                            + Share_Device.PRODUCT + " text default '',"
                            + Share_Device.SERIAL + " text default '',"
                            + Share_Device.RELEASE + " text default '',"
                            + Share_Device.RELEASE_TYPE + " text default '',"
                            + Share_Device.SDK + " integer default 0,"
                            + Share_Device.LABEL + " text default '',"
                            //最后这里定义主键？？？还是必须为不重复的，唯一性
                            + "UNIQUE (" + Share_Device.TIMESTAMP + ","
                            + Share_Device.DEVICE_ID + ")"
                    ,

                    // 关于软件的设置信息，及其数据类型和默认值
                    Share_Settings.SETTING_ID + " integer primary key autoincrement,"
                            + Share_Settings.SETTING_KEY + " text default '',"
                            + Share_Settings.SETTING_VALUE + " text default '',"
                            + Share_Settings.SETTING_PACKAGE_NAME + " text default ''"

                    //下面删除了 关于插件的信息

            };

    private static UriMatcher sUriMatcher = null;
    private static HashMap<String, String> deviceMap = null;
    private static HashMap<String, String> settingsMap = null;
//删除关于插件的哈希图表，类似上面

    private static DatabaseConnect databaseConnect = null;
    private static SQLiteDatabase database = null;

    //初始化数据库，包括上面刚声明的  数据库名称，版本，表名，列名
    private boolean initializeDB()
    {
        if (databaseConnect == null) {
            databaseConnect = new DatabaseConnect( getContext(),
                    DATABASE_NAME, null, DATABASE_VERSION,
                    DATABASE_TABLES, TABLES_FIELDS );
        }
        if( databaseConnect != null && ( database == null || ! database.isOpen() )) {
            //要是数据库辅助类不为空，以及数据库为空或未打开，
            // 那就获取数据库的读写权限
            database = databaseConnect.getWritableDatabase();
        }
        return( database != null && databaseConnect != null);
    }

    //根据条件来删除相应的数据，并返回删除数据的条数
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {

        if( ! initializeDB() ) {
            Log.w("Basic_Data","Delete unavailable...");
            return 0;         }

        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case DEVICE_INFO:
                database.beginTransaction();    //主要利用事务型数据库操作
                count = database.delete(DATABASE_TABLES[0], selection,
                        selectionArgs);
                database.setTransactionSuccessful();
                database.endTransaction();
                break;
            case SETTING:
                database.beginTransaction();
                count = database.delete(DATABASE_TABLES[1], selection,
                        selectionArgs);
                database.setTransactionSuccessful();
                database.endTransaction();
                break;
//            case PLUGIN:  //要是插件呢？

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    /**获取URI相应的内容类型
     //获取URI相应的内容类型？？？
     //
     这个在哪里有调用？？？*/
    @Override
    public String getType(Uri uri)
    {
        switch (sUriMatcher.match(uri)) {
            case DEVICE_INFO:
                return Share_Device.CONTENT_TYPE;
            case DEVICE_INFO_ID:
                return Share_Device.CONTENT_ITEM_TYPE;
            case SETTING:
                return Share_Settings.CONTENT_TYPE;
            case SETTING_ID:
                return Share_Settings.CONTENT_ITEM_TYPE;
//            case PLUGIN:
//                return Aware_Plugins.CONTENT_TYPE;
//            case PLUGIN_ID:
//                return Aware_Plugins.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    //向数据库中添加数据
    @Override
    public Uri insert(Uri uri, ContentValues initialValues)
    {
        //首先都要验证数据库的可用性
        if( ! initializeDB() ) {
            Log.w("Basic_Data","Insert unavailable...");
            return null;
        }

        ContentValues values = (initialValues != null) ? new ContentValues(
                initialValues) : new ContentValues();

        switch (sUriMatcher.match(uri)) {
            case DEVICE_INFO:
                database.beginTransaction();
                long dev_id = database.insertWithOnConflict(DATABASE_TABLES[0],
                        Share_Device.DEVICE_ID, values,
                        SQLiteDatabase.CONFLICT_IGNORE);    //忽略数据库的冲突
                database.setTransactionSuccessful();
                database.endTransaction();
                if (dev_id > 0) {
                    Uri devUri = ContentUris.withAppendedId(
                            Share_Device.CONTENT_URI, dev_id);
                    getContext().getContentResolver().notifyChange(devUri, null);
                    return devUri;
                }
                throw new SQLException("Failed to insert row into " + uri);

            case SETTING:
                database.beginTransaction();
                long sett_id = database.insertWithOnConflict(DATABASE_TABLES[1],
                        Share_Settings.SETTING_KEY, values, SQLiteDatabase.CONFLICT_IGNORE);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (sett_id > 0) {
                    Uri settUri = ContentUris.withAppendedId(
                            Share_Settings.CONTENT_URI, sett_id);
                    getContext().getContentResolver().notifyChange(settUri, null);
                    return settUri;
                }
                throw new SQLException("Failed to insert row into " + uri);

                //case PLUGIN:

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    //本类的刚开始加载的回调方法，即：创造回调
    @Override
    public boolean onCreate()
    {
        AUTHORITY = getContext().getPackageName() + ".provider.basic";

        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Basic_Data.AUTHORITY, DATABASE_TABLES[0], DEVICE_INFO);
        sUriMatcher.addURI(Basic_Data.AUTHORITY, DATABASE_TABLES[0] + "/#", DEVICE_INFO_ID);
        sUriMatcher.addURI(Basic_Data.AUTHORITY, DATABASE_TABLES[1], SETTING);
        sUriMatcher.addURI(Basic_Data.AUTHORITY, DATABASE_TABLES[1] + "/#", SETTING_ID);
//        sUriMatcher.addURI(Aware_Provider.AUTHORITY, DATABASE_TABLES[2], PLUGIN);
//        sUriMatcher.addURI(Aware_Provider.AUTHORITY, DATABASE_TABLES[2] + "/#", PLUGIN_ID);

        deviceMap = new HashMap<String, String>();
        deviceMap.put(Share_Device._ID, Share_Device._ID);
        deviceMap.put(Share_Device.TIMESTAMP, Share_Device.TIMESTAMP);
        deviceMap.put(Share_Device.DEVICE_ID, Share_Device.DEVICE_ID);
        deviceMap.put(Share_Device.BOARD, Share_Device.BOARD);
        deviceMap.put(Share_Device.BRAND, Share_Device.BRAND);
        deviceMap.put(Share_Device.DEVICE, Share_Device.DEVICE);
        deviceMap.put(Share_Device.BUILD_ID, Share_Device.BUILD_ID);
        deviceMap.put(Share_Device.HARDWARE, Share_Device.HARDWARE);
        deviceMap.put(Share_Device.MANUFACTURER, Share_Device.MANUFACTURER);
        deviceMap.put(Share_Device.MODEL, Share_Device.MODEL);
        deviceMap.put(Share_Device.PRODUCT, Share_Device.PRODUCT);
        deviceMap.put(Share_Device.SERIAL, Share_Device.SERIAL);
        deviceMap.put(Share_Device.RELEASE, Share_Device.RELEASE);
        deviceMap.put(Share_Device.RELEASE_TYPE, Share_Device.RELEASE_TYPE);
        deviceMap.put(Share_Device.SDK, Share_Device.SDK);
        deviceMap.put(Share_Device.LABEL, Share_Device.LABEL);

        settingsMap = new HashMap<String, String>();
        settingsMap.put(Share_Settings.SETTING_ID, Share_Settings.SETTING_ID);
        settingsMap.put(Share_Settings.SETTING_KEY, Share_Settings.SETTING_KEY);
        settingsMap.put(Share_Settings.SETTING_VALUE, Share_Settings.SETTING_VALUE);
        settingsMap.put(Share_Settings.SETTING_PACKAGE_NAME, Share_Settings.SETTING_PACKAGE_NAME);

//        pluginsMap = new HashMap<String, String>();

        return true;
    }


    //查询数据
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder)
    {

        if( ! initializeDB() ) {
            Log.w("Basic_Data","Query unavailable...");
            return null;
        }
        //先指定是要查询哪张表
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (sUriMatcher.match(uri)) {
            case DEVICE_INFO:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(deviceMap);
                break;
            case SETTING:
                qb.setTables(DATABASE_TABLES[1]);
                qb.setProjectionMap(settingsMap);
                break;
//            case PLUGIN:

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        try {
            Cursor c = qb.query(database, projection, selection, selectionArgs,
                    null, null, sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        }
        catch (IllegalStateException e) {
            //调用核心类的日志显示
            if (Core_Activity.DEBUG)
                Log.e(Core_Activity.TAG, e.getMessage());
            return null;
        }
    }

    //修改数据，或称为：更新数据
    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs)
    {

        if( ! initializeDB() ) {
            Log.w("Basic_Data","Update unavailable...");
            return 0;
        }

        int count = 0;  //用于记录修改数据的条数，并返回该值
        switch (sUriMatcher.match(uri)) {
            case DEVICE_INFO:
                /**
                 * 这里没有捕获数据库的异常情况
                 * */
                database.beginTransaction();
                count = database.update(DATABASE_TABLES[0], values, selection,
                        selectionArgs);
                database.setTransactionSuccessful();
                database.endTransaction();
                break;
            case SETTING:
                database.beginTransaction();
                count = database.update(DATABASE_TABLES[1], values, selection,
                        selectionArgs);
                database.setTransactionSuccessful();
                database.endTransaction();
                break;
//            case PLUGIN:

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
