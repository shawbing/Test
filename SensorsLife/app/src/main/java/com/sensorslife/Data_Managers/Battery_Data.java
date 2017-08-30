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
 * Created by Administrator on 2017/8/27.
 */

public class Battery_Data extends ContentProvider {
    //
    private static final int DATABASE_VERSION = 3;

    public static String AUTHORITY = "com.sensorslife.provider.battery";
    public static String Tag="Battery Database: ";
    //内容查询通道
    private static final int BATTERY = 1;
    private static final int BATTERY_ID = 2;
    private static final int BATTERY_DISCHARGE = 3;
    private static final int BATTERY_DISCHARGE_ID = 4;
    private static final int BATTERY_CHARGE = 5;
    private static final int BATTERY_CHARGE_ID = 6;

//电池的基本信息
    public static final class BatteryData implements BaseColumns
    {
        private BatteryData() {
        };

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Battery_Data.AUTHORITY + "/battery");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sensorslife.battery";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.sensorslife.battery";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String STATUS = "battery_status";//状态
        public static final String LEVEL = "battery_level";//电池容量
        public static final String SCALE = "battery_scale";//最大值
        public static final String VOLTAGE = "battery_voltage";//伏特，电池的电压
        public static final String TEMPERATURE = "battery_temperature";//温度，0.1度单位。例如表示197的时候，意思为19.7度
        public static final String PLUG_ADAPTOR = "battery_adaptor";//连接的电源插座
        public static final String HEALTH = "battery_health";//健康状态
        public static final String TECHNOLOGY = "battery_technology";//电池类型，例如，Li-ion等等
    }

//未充电状态下的电池信息表
    public static final class Battery_Discharges implements BaseColumns
    {
        private Battery_Discharges() {
        };

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Battery_Data.AUTHORITY + "/battery_discharges");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware.sensorslife.discharges";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware.sensorslife.discharges";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String BATTERY_START = "battery_start";//开始的电量
        public static final String BATTERY_END = "battery_end";
        public static final String END_TIMESTAMP = "double_end_timestamp";
    }
//充电状态下的电池信息表
    public static final class Battery_Charges implements BaseColumns
    {
        private Battery_Charges() { };

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Battery_Data.AUTHORITY + "/battery_charges");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sensorslife.battery.charges";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.sensorslife.battery.charges";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String BATTERY_START = "battery_start";//开始充电的电量
        public static final String BATTERY_END = "battery_end";
        public static final String END_TIMESTAMP = "double_end_timestamp";
    }

//定义数据库的保存路径，及其文件名
    public static String DATABASE_NAME = Environment
            .getExternalStorageDirectory() + "/sensorslife/" + "battery.db";
    public static final String[] DATABASE_TABLES = { "battery",
            "battery_discharges", "battery_charges" };
    public static final String[] TABLES_FIELDS = {
            //电池的基本信息
            BatteryData._ID + " integer primary key autoincrement,"
                    + BatteryData.TIMESTAMP + " real default 0,"
                    + BatteryData.DEVICE_ID + " text default '',"
                    + BatteryData.STATUS + " integer default 0,"
                    + BatteryData.LEVEL + " integer default 0,"
                    + BatteryData.SCALE + " integer default 0,"
                    + BatteryData.VOLTAGE + " integer default 0,"
                    + BatteryData.TEMPERATURE + " integer default 0,"
                    + BatteryData.PLUG_ADAPTOR + " integer default 0,"
                    + BatteryData.HEALTH + " integer default 0,"
                    + BatteryData.TECHNOLOGY + " text default '',"
                    + "UNIQUE (" + BatteryData.TIMESTAMP + ","
                    + BatteryData.DEVICE_ID + ")",//定义时间戳和设备ID为唯一标识值
            // 还没充电中的电池信息
            Battery_Discharges._ID + " integer primary key autoincrement,"
                    + Battery_Discharges.TIMESTAMP + " real default 0,"
                    + Battery_Discharges.DEVICE_ID + " text default '',"
                    + Battery_Discharges.BATTERY_START + " integer default 0,"
                    + Battery_Discharges.BATTERY_END + " integer default 0,"
                    + Battery_Discharges.END_TIMESTAMP + " real default 0,"
                    + "UNIQUE (" + Battery_Discharges.TIMESTAMP + ","
                    + Battery_Discharges.DEVICE_ID + ")",
            // 充电状态中的电池信息
            Battery_Charges._ID + " integer primary key autoincrement,"
                    + Battery_Charges.TIMESTAMP + " real default 0,"
                    + Battery_Charges.DEVICE_ID + " text default '',"
                    + Battery_Charges.BATTERY_START + " integer default 0,"
                    + Battery_Charges.BATTERY_END + " integer default 0,"
                    + Battery_Charges.END_TIMESTAMP + " real default 0,"
                    + "UNIQUE (" + Battery_Charges.TIMESTAMP + ","
                    + Battery_Charges.DEVICE_ID + ")" };

    private static UriMatcher sUriMatcher = null;
    private static HashMap<String, String> batteryProjectionMap = null;
    private static HashMap<String, String> batteryDischargesMap = null;
    private static HashMap<String, String> batteryChargesMap = null;
    private static DatabaseConnect connect = null;
    private static SQLiteDatabase database = null;

    private boolean initializeDB() {
        if (connect == null) {
            connect = new DatabaseConnect( getContext(), DATABASE_NAME, null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS );
        }
        if( connect != null && ( database == null || ! database.isOpen() )) {
            database = connect.getWritableDatabase();
        }
        return( database != null && connect != null);
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {

        if( ! initializeDB() ) {
            Log.w(Tag,"Delete unavailable...");
            return 0;
        }

        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case BATTERY:
                database.beginTransaction();
                count = database.delete(DATABASE_TABLES[0], selection,
                        selectionArgs);
                database.setTransactionSuccessful();
                database.endTransaction();
                break;
            case BATTERY_DISCHARGE:
                database.beginTransaction();
                count = database.delete(DATABASE_TABLES[1], selection,
                        selectionArgs);
                database.setTransactionSuccessful();
                database.endTransaction();
                break;
            case BATTERY_CHARGE:
                database.beginTransaction();
                count = database.delete(DATABASE_TABLES[2], selection,
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

    @Override
    public String getType(Uri uri)
    {
        switch (sUriMatcher.match(uri)) {
            case BATTERY:
                return BatteryData.CONTENT_TYPE;
            case BATTERY_ID:
                return BatteryData.CONTENT_ITEM_TYPE;
            case BATTERY_DISCHARGE:
                return Battery_Discharges.CONTENT_TYPE;
            case BATTERY_DISCHARGE_ID:
                return Battery_Discharges.CONTENT_ITEM_TYPE;
            case BATTERY_CHARGE:
                return Battery_Charges.CONTENT_TYPE;
            case BATTERY_CHARGE_ID:
                return Battery_Charges.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues)
    {

        if( ! initializeDB() ) {
            Log.w(Tag,"Insert unavailable...");
            return null;
        }

        ContentValues values = (initialValues != null) ? new ContentValues(
                initialValues) : new ContentValues();

        switch (sUriMatcher.match(uri)) {
            case BATTERY:
                database.beginTransaction();
                long battery_id = database.insertWithOnConflict(DATABASE_TABLES[0], BatteryData.TECHNOLOGY, values, SQLiteDatabase.CONFLICT_IGNORE);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (battery_id > 0) {
                    Uri batteryUri = ContentUris.withAppendedId(BatteryData.CONTENT_URI, battery_id);
                    getContext().getContentResolver().notifyChange(batteryUri, null);
                    return batteryUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            case BATTERY_DISCHARGE:
                database.beginTransaction();
                long battery_d_id = database.insertWithOnConflict(DATABASE_TABLES[1], Battery_Discharges.DEVICE_ID, values, SQLiteDatabase.CONFLICT_IGNORE);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (battery_d_id > 0) {
                    Uri batteryUri = ContentUris.withAppendedId(
                            Battery_Discharges.CONTENT_URI, battery_d_id);
                    getContext().getContentResolver()
                            .notifyChange(batteryUri, null);
                    return batteryUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            case BATTERY_CHARGE:
                database.beginTransaction();
                long battery_c_id = database.insertWithOnConflict(DATABASE_TABLES[2],
                        Battery_Charges.DEVICE_ID, values, SQLiteDatabase.CONFLICT_IGNORE);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (battery_c_id > 0) {
                    Uri batteryUri = ContentUris.withAppendedId(
                            Battery_Charges.CONTENT_URI, battery_c_id);
                    getContext().getContentResolver()
                            .notifyChange(batteryUri, null);
                    return batteryUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public boolean onCreate()
    {
        AUTHORITY = getContext().getPackageName() + ".provider.battery";

        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Battery_Data.AUTHORITY, DATABASE_TABLES[0],
                BATTERY);
        sUriMatcher.addURI(Battery_Data.AUTHORITY, DATABASE_TABLES[0]
                + "/#", BATTERY_ID);
        sUriMatcher.addURI(Battery_Data.AUTHORITY, DATABASE_TABLES[1],
                BATTERY_DISCHARGE);
        sUriMatcher.addURI(Battery_Data.AUTHORITY, DATABASE_TABLES[1]
                + "/#", BATTERY_DISCHARGE_ID);
        sUriMatcher.addURI(Battery_Data.AUTHORITY, DATABASE_TABLES[2],
                BATTERY_CHARGE);
        sUriMatcher.addURI(Battery_Data.AUTHORITY, DATABASE_TABLES[2]
                + "/#", BATTERY_CHARGE_ID);

        batteryProjectionMap = new HashMap<String, String>();
        batteryProjectionMap.put(BatteryData._ID, BatteryData._ID);
        batteryProjectionMap
                .put(BatteryData.TIMESTAMP, BatteryData.TIMESTAMP);
        batteryProjectionMap
                .put(BatteryData.DEVICE_ID, BatteryData.DEVICE_ID);
        batteryProjectionMap.put(BatteryData.STATUS, BatteryData.STATUS);
        batteryProjectionMap.put(BatteryData.LEVEL, BatteryData.LEVEL);
        batteryProjectionMap.put(BatteryData.SCALE, BatteryData.SCALE);
        batteryProjectionMap.put(BatteryData.VOLTAGE, BatteryData.VOLTAGE);
        batteryProjectionMap.put(BatteryData.TEMPERATURE,
                BatteryData.TEMPERATURE);
        batteryProjectionMap.put(BatteryData.PLUG_ADAPTOR,
                BatteryData.PLUG_ADAPTOR);
        batteryProjectionMap.put(BatteryData.HEALTH, BatteryData.HEALTH);
        batteryProjectionMap.put(BatteryData.TECHNOLOGY,
                BatteryData.TECHNOLOGY);

        batteryDischargesMap = new HashMap<String, String>();
        batteryDischargesMap
                .put(Battery_Discharges._ID, Battery_Discharges._ID);
        batteryDischargesMap.put(Battery_Discharges.TIMESTAMP,
                Battery_Discharges.TIMESTAMP);
        batteryDischargesMap.put(Battery_Discharges.DEVICE_ID,
                Battery_Discharges.DEVICE_ID);
        batteryDischargesMap.put(Battery_Discharges.BATTERY_START,
                Battery_Discharges.BATTERY_START);
        batteryDischargesMap.put(Battery_Discharges.BATTERY_END,
                Battery_Discharges.BATTERY_END);
        batteryDischargesMap.put(Battery_Discharges.END_TIMESTAMP,
                Battery_Discharges.END_TIMESTAMP);

        batteryChargesMap = new HashMap<String, String>();
        batteryChargesMap.put(Battery_Charges._ID, Battery_Charges._ID);
        batteryChargesMap.put(Battery_Charges.TIMESTAMP,
                Battery_Charges.TIMESTAMP);
        batteryChargesMap.put(Battery_Charges.DEVICE_ID,
                Battery_Charges.DEVICE_ID);
        batteryChargesMap.put(Battery_Charges.BATTERY_START,
                Battery_Charges.BATTERY_START);
        batteryChargesMap.put(Battery_Charges.BATTERY_END,
                Battery_Charges.BATTERY_END);
        batteryChargesMap.put(Battery_Charges.END_TIMESTAMP,
                Battery_Charges.END_TIMESTAMP);

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder)
    {

        if( ! initializeDB() ) {
            Log.w(Tag,"Query unavailable...");
            return null;
        }

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (sUriMatcher.match(uri)) {
            case BATTERY:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(batteryProjectionMap);
                break;
            case BATTERY_DISCHARGE:
                qb.setTables(DATABASE_TABLES[1]);
                qb.setProjectionMap(batteryDischargesMap);
                break;
            case BATTERY_CHARGE:
                qb.setTables(DATABASE_TABLES[2]);
                qb.setProjectionMap(batteryChargesMap);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        try {
            Cursor c = qb.query(database, projection, selection, selectionArgs,
                    null, null, sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        } catch (IllegalStateException e) {
            if (Core_Activity.DEBUG)
                Log.e(Core_Activity.TAG, e.getMessage());
            return null;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs)
    {

        if( ! initializeDB() ) {
            Log.w(Tag,"Update unavailable...");
            return 0;
        }

        int count;
        switch (sUriMatcher.match(uri)) {
            case BATTERY:
                database.beginTransaction();
                count = database.update(DATABASE_TABLES[0], values, selection,
                        selectionArgs);
                database.setTransactionSuccessful();
                database.endTransaction();
                break;
            case BATTERY_DISCHARGE:
                database.beginTransaction();
                count = database.update(DATABASE_TABLES[1], values, selection,
                        selectionArgs);
                database.setTransactionSuccessful();
                database.endTransaction();
                break;
            case BATTERY_CHARGE:
                database.beginTransaction();
                count = database.update(DATABASE_TABLES[2], values, selection,
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
