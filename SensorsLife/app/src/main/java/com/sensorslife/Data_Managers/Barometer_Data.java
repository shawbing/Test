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
 * Created by Administrator on 2017/8/28.
 *
 * 定义 气压数据库 的字符
 */

public class Barometer_Data extends ContentProvider {

    public static final int DATABASE_VERSION = 1;
//认证，授权标记
    public static String AUTHORITY = "com.sensorslife.provider.barometer";

    public static String Tag="Barometer_Data:";

    private static final int SENSOR_DEV = 1;
    private static final int SENSOR_DEV_ID = 2;
    private static final int SENSOR_DATA = 3;
    private static final int SENSOR_DATA_ID = 4;

//记录该传感器的设备信息
    public static final class Barometer_Device implements BaseColumns
    {
        private Barometer_Device() {
        };

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Barometer_Data.AUTHORITY + "/barometer_device");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sensorslife.barometer.sensor";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.sensorslife.barometer.sensor";

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

    public static final class BarometerData implements BaseColumns
    {
        private BarometerData() {
        };

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Barometer_Data.AUTHORITY + "/barometer");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sensorslife.barometer.data";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.sensorslife.barometer.data";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String AMBIENT_PRESSURE = "double_values_0";
        public static final String ACCURACY = "accuracy";
        public static final String LABEL = "label";
    }

    public static String DATABASE_NAME = Environment
            .getExternalStorageDirectory() + "/sensorslife/" + "barometer.db";
    public static final String[] DATABASE_TABLES = { "barometer_device",
            "barometer" };
    public static final String[] TABLES_FIELDS = {

            Barometer_Device._ID + " integer primary key autoincrement,"
                    + Barometer_Device.TIMESTAMP + " real default 0,"
                    + Barometer_Device.DEVICE_ID + " text default '',"
                    + Barometer_Device.MAXIMUM_RANGE + " real default 0,"
                    + Barometer_Device.MINIMUM_DELAY + " real default 0,"
                    + Barometer_Device.NAME + " text default '',"
                    + Barometer_Device.POWER_MA + " real default 0,"
                    + Barometer_Device.RESOLUTION + " real default 0,"
                    + Barometer_Device.TYPE + " text default '',"
                    + Barometer_Device.VENDOR + " text default '',"
                    + Barometer_Device.VERSION + " text default '',"
                    + "UNIQUE(" + Barometer_Device.TIMESTAMP + ","
                    + Barometer_Device.DEVICE_ID + ")",
            //定义气压传感器的数据
            BarometerData._ID + " integer primary key autoincrement,"
                    + BarometerData.TIMESTAMP + " real default 0,"
                    + BarometerData.DEVICE_ID + " text default '',"
                    + BarometerData.AMBIENT_PRESSURE + " real default 0,"
                    + BarometerData.ACCURACY + " integer default 0,"
                    + BarometerData.LABEL + " text default ''," + "UNIQUE("
                    + BarometerData.TIMESTAMP + "," + BarometerData.DEVICE_ID
                    + ")" };

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
            Log.w(Tag,"Delete unavailable...");
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
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case SENSOR_DEV:
                return Barometer_Device.CONTENT_TYPE;
            case SENSOR_DEV_ID:
                return Barometer_Device.CONTENT_ITEM_TYPE;
            case SENSOR_DATA:
                return BarometerData.CONTENT_TYPE;
            case SENSOR_DATA_ID:
                return BarometerData.CONTENT_ITEM_TYPE;
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
            case SENSOR_DEV:
                database.beginTransaction();
                long accel_id = database.insertWithOnConflict(DATABASE_TABLES[0],
                        Barometer_Device.DEVICE_ID, values, SQLiteDatabase.CONFLICT_IGNORE);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (accel_id > 0) {
                    Uri accelUri = ContentUris.withAppendedId(
                            Barometer_Device.CONTENT_URI, accel_id);
                    getContext().getContentResolver().notifyChange(accelUri, null);
                    return accelUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            case SENSOR_DATA:
                database.beginTransaction();
                long accelData_id = database.insertWithOnConflict(DATABASE_TABLES[1],
                        BarometerData.DEVICE_ID, values, SQLiteDatabase.CONFLICT_IGNORE);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (accelData_id > 0) {
                    Uri accelDataUri = ContentUris.withAppendedId(
                            BarometerData.CONTENT_URI, accelData_id);
                    getContext().getContentResolver().notifyChange(accelDataUri,
                            null);
                    return accelDataUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:

                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

//批量插入
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values)
    {
        if( ! initializeDB() ) {
            Log.w(Tag,"BulkInsert unavailable...");
            return 0;
        }

        int count = 0;
        switch ( sUriMatcher.match(uri) ) {
            case SENSOR_DEV:
                database.beginTransaction();
                for (ContentValues v : values) {
                    long id;
                    try {
                        id = database.insertOrThrow( DATABASE_TABLES[0], Barometer_Device.DEVICE_ID, v );
                    } catch ( SQLException e ) {
                        id = database.replace( DATABASE_TABLES[0], Barometer_Device.DEVICE_ID, v );
                    }
                    if( id <= 0 ) {
                        Log.w(Tag, "Failed to insert/replace row into " + uri);
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
                        id = database.insertOrThrow( DATABASE_TABLES[1], BarometerData.DEVICE_ID, v );
                    } catch ( SQLException e ) {
                        id = database.replace( DATABASE_TABLES[1], BarometerData.DEVICE_ID, v );
                    }
                    if( id <= 0 ) {
                        Log.w(Tag, "Failed to insert/replace row into " + uri);
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
        AUTHORITY = getContext().getPackageName() + ".provider.barometer";

        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Barometer_Data.AUTHORITY, DATABASE_TABLES[0],
                SENSOR_DEV);
        sUriMatcher.addURI(Barometer_Data.AUTHORITY, DATABASE_TABLES[0]
                + "/#", SENSOR_DEV_ID);
        sUriMatcher.addURI(Barometer_Data.AUTHORITY, DATABASE_TABLES[1],
                SENSOR_DATA);
        sUriMatcher.addURI(Barometer_Data.AUTHORITY, DATABASE_TABLES[1]
                + "/#", SENSOR_DATA_ID);

        sensorMap = new HashMap<String, String>();
        sensorMap.put(Barometer_Device._ID, Barometer_Device._ID);
        sensorMap.put(Barometer_Device.TIMESTAMP, Barometer_Device.TIMESTAMP);
        sensorMap.put(Barometer_Device.DEVICE_ID, Barometer_Device.DEVICE_ID);
        sensorMap.put(Barometer_Device.MAXIMUM_RANGE,
                Barometer_Device.MAXIMUM_RANGE);
        sensorMap.put(Barometer_Device.MINIMUM_DELAY,
                Barometer_Device.MINIMUM_DELAY);
        sensorMap.put(Barometer_Device.NAME, Barometer_Device.NAME);
        sensorMap.put(Barometer_Device.POWER_MA, Barometer_Device.POWER_MA);
        sensorMap.put(Barometer_Device.RESOLUTION, Barometer_Device.RESOLUTION);
        sensorMap.put(Barometer_Device.TYPE, Barometer_Device.TYPE);
        sensorMap.put(Barometer_Device.VENDOR, Barometer_Device.VENDOR);
        sensorMap.put(Barometer_Device.VERSION, Barometer_Device.VERSION);

        sensorDataMap = new HashMap<String, String>();
        sensorDataMap.put(BarometerData._ID, BarometerData._ID);
        sensorDataMap.put(BarometerData.TIMESTAMP, BarometerData.TIMESTAMP);
        sensorDataMap.put(BarometerData.DEVICE_ID, BarometerData.DEVICE_ID);
        sensorDataMap.put(BarometerData.AMBIENT_PRESSURE,
                BarometerData.AMBIENT_PRESSURE);
        sensorDataMap.put(BarometerData.ACCURACY, BarometerData.ACCURACY);
        sensorDataMap.put(BarometerData.LABEL, BarometerData.LABEL);

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
            Log.w(Tag,"Update unavailable...");
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
