package com.sensorslife.Data_Managers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
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
 *
 * 存储手机光线传感器的数据
 */

public class Light_Data extends ContentProvider
{
    public static final int DATABASE_VERSION = 2;

    public static Context fullContext;

    public static String AUTHORITY = "com.sensorslife.provider.light";
    public static String TAG="Light_Data";

    // 内容提供者的查询索引
    private static final int SENSOR_DEV = 1;
    private static final int SENSOR_DEV_ID = 2;
    private static final int SENSOR_DATA = 3;
    private static final int SENSOR_DATA_ID = 4;

    //光线传感器所对应的设备信息，一般只有一条记录
    public static final class Light_Device implements BaseColumns
    {
        private Light_Device() {
        };

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Light_Data.AUTHORITY + "/light_device");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sensorslife.light.device";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.sensorslife.light.device";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String MAXIMUM_RANGE = "double_sensor_maximum_range";
        public static final String MINIMUM_DELAY = "double_sensor_minimum_delay";
        public static final String NAME = "sensor_name";
        public static final String POWER_MA = "double_sensor_power_ma";
        public static final String RESOLUTION = "double_sensor_resolution";
        public static final String TYPE = "sensor_type";
        public static final String VENDOR = "sensor_vendor";
        public static final String VERSION = "sensor_version";
    }

    //光感数据的列表项，即表的结构
    public static final class LightData implements BaseColumns
    {
        private LightData() {
        };

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Light_Data.AUTHORITY + "/light_data");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sensorslife.light.data";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.sensorslife.light.data";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String LIGHT_LUX = "double_light_lux";
        public static final String ACCURACY = "accuracy";
        public static final String LABEL = "label";
    }

    public static String DATABASE_NAME = Environment
            .getExternalStorageDirectory() + "/sensorslife/" + "light.db";
    public static final String[] DATABASE_TABLES = { "light_device", "light_data" };
    public static final String[] TABLES_FIELDS = {
//传感器的设备信息
            Light_Device._ID + " integer primary key autoincrement,"
                    + Light_Device.TIMESTAMP + " real default 0,"
                    + Light_Device.DEVICE_ID + " text default '',"
                    + Light_Device.MAXIMUM_RANGE + " real default 0,"
                    + Light_Device.MINIMUM_DELAY + " real default 0,"
                    + Light_Device.NAME + " text default '',"
                    + Light_Device.POWER_MA + " real default 0,"
                    + Light_Device.RESOLUTION + " real default 0,"
                    + Light_Device.TYPE + " text default '',"
                    + Light_Device.VENDOR + " text default '',"
                    + Light_Device.VERSION + " text default ''," + "UNIQUE("
                    + Light_Device.TIMESTAMP + "," + Light_Device.DEVICE_ID
                    + ")",
            // sensor data
            LightData._ID + " integer primary key autoincrement,"
                    + LightData.TIMESTAMP + " real default 0,"
                    + LightData.DEVICE_ID + " text default '',"
                    + LightData.LIGHT_LUX + " real default 0,"
                    + LightData.ACCURACY + " integer default 0,"
                    + LightData.LABEL + " text default ''," + "UNIQUE("
                    + LightData.TIMESTAMP + "," + LightData.DEVICE_ID + ")" };

    private static UriMatcher sUriMatcher = null;
    private static HashMap<String, String> sensorMap = null;
    private static HashMap<String, String> sensorDataMap = null;
    private static DatabaseConnect connect = null;
    private static SQLiteDatabase database = null;

    private boolean initializeDB()
    {
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
            Log.w(TAG,"Database Delete unavailable...");
            return 0;
        }

        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case SENSOR_DEV:
                database.beginTransaction();
                count = database.delete(DATABASE_TABLES[0], selection,
                        selectionArgs);
                database.setTransactionSuccessful();
                database.endTransaction();
                break;
            case SENSOR_DATA:
                database.beginTransaction();
                count = database.delete(DATABASE_TABLES[1], selection,
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
            case SENSOR_DEV:
                return Light_Device.CONTENT_TYPE;
            case SENSOR_DEV_ID:
                return Light_Device.CONTENT_ITEM_TYPE;
            case SENSOR_DATA:
                return LightData.CONTENT_TYPE;
            case SENSOR_DATA_ID:
                return LightData.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }


    @Override
    public Uri insert(Uri uri, ContentValues initialValues)
    {
        if( ! initializeDB() ) {
            Log.w(TAG,"Database Insert unavailable...");
            return null;
        }
//        Toast.makeText(fullContext,"Light_Activity.saveSensorDevice:\n      if(DEBUG_DB_SLOW==false) " +
//                "\n  开始调用数据插入语句", Toast.LENGTH_LONG).show();

        ContentValues values = (initialValues != null) ? new ContentValues(
                initialValues) : new ContentValues();

        switch (sUriMatcher.match(uri)) {
            case SENSOR_DEV:
                database.beginTransaction();
                long accel_id = database.insertWithOnConflict(DATABASE_TABLES[0],
                        Light_Device.DEVICE_ID, values, SQLiteDatabase.CONFLICT_IGNORE);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (accel_id > 0) {
                    Uri accelUri = ContentUris.withAppendedId(
                            Light_Device.CONTENT_URI, accel_id);
                    getContext().getContentResolver().notifyChange(accelUri, null);
                    return accelUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            case SENSOR_DATA:
                database.beginTransaction();
                long accelData_id = database.insertWithOnConflict(DATABASE_TABLES[1],
                        LightData.DEVICE_ID, values, SQLiteDatabase.CONFLICT_IGNORE);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (accelData_id > 0) {
                    Uri accelDataUri = ContentUris.withAppendedId(
                            LightData.CONTENT_URI, accelData_id);
                    getContext().getContentResolver().notifyChange(accelDataUri,
                            null);
                    return accelDataUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:

                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    /**为了数据处理的高效性，高效插入数据，就实行批量数据的插入*/
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values)
    {
        if( ! initializeDB() ) {
            Log.w(TAG,"Database  unavailable...");
            return 0;
        }

        int count = 0;
        switch ( sUriMatcher.match(uri) ) {
            case SENSOR_DEV:
                database.beginTransaction();
                for (ContentValues v : values) {
                    long id;
                    try {
                        id = database.insertOrThrow( DATABASE_TABLES[0], Light_Device.DEVICE_ID, v );
                    } catch ( SQLException e ) {
                        id = database.replace( DATABASE_TABLES[0], Light_Device.DEVICE_ID, v );
                    }
                    if( id <= 0 ) {
/**这里做了修改，将Barometer.TAG，改成TAG*/
                        Log.w(TAG, "Failed to insert/replace row into " + uri);
                    } else {
                        count++;
                    }
                }
                database.setTransactionSuccessful();
                database.endTransaction();
                getContext().getContentResolver().notifyChange(uri, null);
                return count;
            case SENSOR_DATA:
                database.beginTransaction();
                for (ContentValues v : values) {
                    long id;
                    try {
                        id = database.insertOrThrow( DATABASE_TABLES[1], LightData.DEVICE_ID, v );
                    } catch ( SQLException e ) {
                        id = database.replace( DATABASE_TABLES[1], LightData.DEVICE_ID, v );
                    }
                    if( id <= 0 ) {
/**这里做了修改，将Barometer.TAG，改成TAG*/
                        Log.w(TAG, "Failed to insert/replace row into " + uri);
                    } else {
                        count++;
                    }
                }
                database.setTransactionSuccessful();
                database.endTransaction();
                getContext().getContentResolver().notifyChange(uri, null);
                return count;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public boolean onCreate()
    {


        AUTHORITY = getContext().getPackageName() + ".provider.light";

        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Light_Data.AUTHORITY, DATABASE_TABLES[0],
                SENSOR_DEV);
        sUriMatcher.addURI(Light_Data.AUTHORITY, DATABASE_TABLES[0] + "/#",
                SENSOR_DEV_ID);
        sUriMatcher.addURI(Light_Data.AUTHORITY, DATABASE_TABLES[1],
                SENSOR_DATA);
        sUriMatcher.addURI(Light_Data.AUTHORITY, DATABASE_TABLES[1] + "/#",
                SENSOR_DATA_ID);

        sensorMap = new HashMap<String, String>();
        sensorMap.put(Light_Device._ID, Light_Device._ID);
        sensorMap.put(Light_Device.TIMESTAMP, Light_Device.TIMESTAMP);
        sensorMap.put(Light_Device.DEVICE_ID, Light_Device.DEVICE_ID);
        sensorMap.put(Light_Device.MAXIMUM_RANGE, Light_Device.MAXIMUM_RANGE);
        sensorMap.put(Light_Device.MINIMUM_DELAY, Light_Device.MINIMUM_DELAY);
        sensorMap.put(Light_Device.NAME, Light_Device.NAME);
        sensorMap.put(Light_Device.POWER_MA, Light_Device.POWER_MA);
        sensorMap.put(Light_Device.RESOLUTION, Light_Device.RESOLUTION);
        sensorMap.put(Light_Device.TYPE, Light_Device.TYPE);
        sensorMap.put(Light_Device.VENDOR, Light_Device.VENDOR);
        sensorMap.put(Light_Device.VERSION, Light_Device.VERSION);

        sensorDataMap = new HashMap<String, String>();
        sensorDataMap.put(LightData._ID, LightData._ID);
        sensorDataMap.put(LightData.TIMESTAMP, LightData.TIMESTAMP);
        sensorDataMap.put(LightData.DEVICE_ID, LightData.DEVICE_ID);
        sensorDataMap.put(LightData.LIGHT_LUX, LightData.LIGHT_LUX);
        sensorDataMap.put(LightData.ACCURACY, LightData.ACCURACY);
        sensorDataMap.put(LightData.LABEL, LightData.LABEL);

        return true;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder)
    {

        if( ! initializeDB() ) {
            Log.w(TAG,"Database Query unavailable...");
            return null;
        }

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (sUriMatcher.match(uri)) {
            case SENSOR_DEV:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(sensorMap);
                break;
            case SENSOR_DATA:
                qb.setTables(DATABASE_TABLES[1]);
                qb.setProjectionMap(sensorDataMap);
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
            Log.w(TAG,"Database Update unavailable...");
            return 0;
        }

        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case SENSOR_DEV:
                database.beginTransaction();
                count = database.update(DATABASE_TABLES[0], values, selection,
                        selectionArgs);
                database.setTransactionSuccessful();
                database.endTransaction();
                break;
            case SENSOR_DATA:
                database.beginTransaction();
                count = database.update(DATABASE_TABLES[1], values, selection,
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

