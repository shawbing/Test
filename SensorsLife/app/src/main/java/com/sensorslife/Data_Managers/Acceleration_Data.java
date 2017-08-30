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
import com.sensorslife.Sensor_Activities.Acceleration_Activity;

import java.util.HashMap;

/**
 * Created by Administrator on 2017/8/28.
 */

public class Acceleration_Data extends ContentProvider {

    public static final int DATABASE_VERSION = 2;

    public static String AUTHORITY = "com.sensorslife.provider.accelerometer";

    public static String Tag="Acceleration_Data:";

    private static final int ACCEL_DEV = 1;
    private static final int ACCEL_DEV_ID = 2;
    private static final int ACCEL_DATA = 3;
    private static final int ACCEL_DATA_ID = 4;

//加速度的设备信息
    public static final class Acceleration_Device implements BaseColumns
    {
        private Acceleration_Device() {
        };

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Acceleration_Data.AUTHORITY + "/accelerometer_device");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sensorslife.accelerometer.sensor";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.sensorslife.accelerometer.sensor";

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

//记录加速度信息
    public static final class AccelerationData implements BaseColumns {
        private AccelerationData() {
        };

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Acceleration_Data.AUTHORITY + "/accelerometer");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sensorslife.accelerometer.data";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.sensorslife.accelerometer.data";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String VALUES_0 = "double_values_0";
        public static final String VALUES_1 = "double_values_1";
        public static final String VALUES_2 = "double_values_2";
        public static final String ACCURACY = "accuracy";
        public static final String LABEL = "label";
    }

    public static String DATABASE_NAME = Environment
            .getExternalStorageDirectory() + "/sensorslife/" + "accelerometer.db";
    public static final String[] DATABASE_TABLES = { "acceleration_device",
            "acceleration" };
    public static final String[] TABLES_FIELDS = {
            // accelerometer device information
            Acceleration_Device._ID + " integer primary key autoincrement,"
                    + Acceleration_Device.TIMESTAMP + " real default 0,"
                    + Acceleration_Device.DEVICE_ID + " text default '',"
                    + Acceleration_Device.MAXIMUM_RANGE + " real default 0,"
                    + Acceleration_Device.MINIMUM_DELAY + " real default 0,"
                    + Acceleration_Device.NAME + " text default '',"
                    + Acceleration_Device.POWER_MA + " real default 0,"
                    + Acceleration_Device.RESOLUTION + " real default 0,"
                    + Acceleration_Device.TYPE + " text default '',"
                    + Acceleration_Device.VENDOR + " text default '',"
                    + Acceleration_Device.VERSION + " text default '',"
                    + "UNIQUE (" + Acceleration_Device.TIMESTAMP + ","
                    + Acceleration_Device.DEVICE_ID + ")",

            // accelerometer data
            AccelerationData._ID + " integer primary key autoincrement,"
                    + AccelerationData.TIMESTAMP + " real default 0,"
                    + AccelerationData.DEVICE_ID + " text default '',"
                    + AccelerationData.VALUES_0 + " real default 0,"
                    + AccelerationData.VALUES_1 + " real default 0,"
                    + AccelerationData.VALUES_2 + " real default 0,"
                    + AccelerationData.ACCURACY + " integer default 0,"
                    + AccelerationData.LABEL + " text default '',"
                    + "UNIQUE (" + AccelerationData.TIMESTAMP + ","
                    + AccelerationData.DEVICE_ID + ")" };

    private static UriMatcher sUriMatcher = null;
    private static HashMap<String, String> accelDeviceMap = null;
    private static HashMap<String, String> accelDataMap = null;
    private static DatabaseConnect connect = null;
    private static SQLiteDatabase database = null;

    private boolean initializeDB() {
        if (connect == null) {
            connect = new DatabaseConnect( getContext(), DATABASE_NAME, null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS );
        }
        if(connect != null && ( database == null || ! database.isOpen() )) {
            database = connect.getWritableDatabase();
        }
        return( database != null && connect != null);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        if( ! initializeDB() ) {
            Log.w(Tag,"Delete unavailable...");
            return 0;
        }

        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case ACCEL_DEV:
                database.beginTransaction();
                count = database.delete(DATABASE_TABLES[0], selection,
                        selectionArgs);
                database.setTransactionSuccessful();
                database.endTransaction();
                break;
            case ACCEL_DATA:
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
            case ACCEL_DEV:
                return Acceleration_Device.CONTENT_TYPE;
            case ACCEL_DEV_ID:
                return Acceleration_Device.CONTENT_ITEM_TYPE;
            case ACCEL_DATA:
                return AccelerationData.CONTENT_TYPE;
            case ACCEL_DATA_ID:
                return AccelerationData.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {

        if( ! initializeDB() ) {
            Log.w(Tag,"Insert unavailable...");
            return null;
        }

        ContentValues values = (initialValues != null) ? new ContentValues(
                initialValues) : new ContentValues();

        switch (sUriMatcher.match(uri)) {
            case ACCEL_DEV:
                long accel_id = database.insertWithOnConflict(DATABASE_TABLES[0],
                        Acceleration_Device.DEVICE_ID, values, SQLiteDatabase.CONFLICT_IGNORE);
                if (accel_id > 0) {
                    Uri accelUri = ContentUris.withAppendedId(
                            Acceleration_Device.CONTENT_URI, accel_id);
                    getContext().getContentResolver().notifyChange(accelUri, null);
                    return accelUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            case ACCEL_DATA:
                long accelData_id = database.insertWithOnConflict(DATABASE_TABLES[1],
                        AccelerationData.DEVICE_ID, values, SQLiteDatabase.CONFLICT_IGNORE);
                if (accelData_id > 0) {
                    Uri accelDataUri = ContentUris.withAppendedId(
                            AccelerationData.CONTENT_URI, accelData_id);
                    getContext().getContentResolver().notifyChange(accelDataUri,
                            null);
                    return accelDataUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

//好效率的批量插入数据
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values)
    {
        if( ! initializeDB() ) {
            Log.w(Tag,"BulkInsert unavailable...");
            return 0;
        }

        int count = 0;
        switch ( sUriMatcher.match(uri) ) {
            case ACCEL_DEV:
                database.beginTransaction();
                for (ContentValues v : values) {
                    long id;
                    try {
                        id = database.insertOrThrow( DATABASE_TABLES[0], Acceleration_Device.DEVICE_ID, v );
                    } catch ( SQLException e ) {
                        id = database.replace( DATABASE_TABLES[0], Acceleration_Device.DEVICE_ID, v );
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
            case ACCEL_DATA:
                database.beginTransaction();
                for (ContentValues v : values) {
                    long id;
                    try {
                        id = database.insertOrThrow( DATABASE_TABLES[1], AccelerationData.DEVICE_ID, v );
                    } catch ( SQLException e ) {
                        id = database.replace( DATABASE_TABLES[1], AccelerationData.DEVICE_ID, v );
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
    public boolean onCreate() {
        AUTHORITY = getContext().getPackageName() + ".provider.accelerometer";

        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Acceleration_Data.AUTHORITY,
                DATABASE_TABLES[0], ACCEL_DEV);
        sUriMatcher.addURI(Acceleration_Data.AUTHORITY, DATABASE_TABLES[0]
                + "/#", ACCEL_DEV_ID);
        sUriMatcher.addURI(Acceleration_Data.AUTHORITY,
                DATABASE_TABLES[1], ACCEL_DATA);
        sUriMatcher.addURI(Acceleration_Data.AUTHORITY, DATABASE_TABLES[1]
                + "/#", ACCEL_DATA_ID);

        accelDeviceMap = new HashMap<String, String>();
        accelDeviceMap.put(Acceleration_Device._ID, Acceleration_Device._ID);
        accelDeviceMap.put(Acceleration_Device.TIMESTAMP,
                Acceleration_Device.TIMESTAMP);
        accelDeviceMap.put(Acceleration_Device.DEVICE_ID,
                Acceleration_Device.DEVICE_ID);
        accelDeviceMap.put(Acceleration_Device.MAXIMUM_RANGE,
                Acceleration_Device.MAXIMUM_RANGE);
        accelDeviceMap.put(Acceleration_Device.MINIMUM_DELAY,
                Acceleration_Device.MINIMUM_DELAY);
        accelDeviceMap
                .put(Acceleration_Device.NAME, Acceleration_Device.NAME);
        accelDeviceMap.put(Acceleration_Device.POWER_MA,
                Acceleration_Device.POWER_MA);
        accelDeviceMap.put(Acceleration_Device.RESOLUTION,
                Acceleration_Device.RESOLUTION);
        accelDeviceMap
                .put(Acceleration_Device.TYPE, Acceleration_Device.TYPE);
        accelDeviceMap.put(Acceleration_Device.VENDOR,
                Acceleration_Device.VENDOR);
        accelDeviceMap.put(Acceleration_Device.VERSION,
                Acceleration_Device.VERSION);

        accelDataMap = new HashMap<String, String>();
        accelDataMap.put(AccelerationData._ID, AccelerationData._ID);
        accelDataMap.put(AccelerationData.TIMESTAMP,
                AccelerationData.TIMESTAMP);
        accelDataMap.put(AccelerationData.DEVICE_ID,
                AccelerationData.DEVICE_ID);
        accelDataMap.put(AccelerationData.VALUES_0,
                AccelerationData.VALUES_0);
        accelDataMap.put(AccelerationData.VALUES_1,
                AccelerationData.VALUES_1);
        accelDataMap.put(AccelerationData.VALUES_2,
                AccelerationData.VALUES_2);
        accelDataMap.put(AccelerationData.ACCURACY,
                AccelerationData.ACCURACY);
        accelDataMap.put(AccelerationData.LABEL, AccelerationData.LABEL);

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
            case ACCEL_DEV:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(accelDeviceMap);
                break;
            case ACCEL_DATA:
                qb.setTables(DATABASE_TABLES[1]);
                qb.setProjectionMap(accelDataMap);
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
            case ACCEL_DEV:
                database.beginTransaction();
                count = database.update(DATABASE_TABLES[0], values, selection,
                        selectionArgs);
                database.setTransactionSuccessful();
                database.endTransaction();
                break;
            case ACCEL_DATA:
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
