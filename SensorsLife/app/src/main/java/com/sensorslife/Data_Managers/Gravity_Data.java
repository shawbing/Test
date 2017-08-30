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
 */

public class Gravity_Data extends ContentProvider {

    private static final int DATABASE_VERSION = 2;

    public static String AUTHORITY = "com.sensorslife.provider.gravity";

    public static String Tag="Gravity_Data:";

    private static final int SENSOR_DEV = 1;
    private static final int SENSOR_DEV_ID = 2;
    private static final int SENSOR_DATA = 3;
    private static final int SENSOR_DATA_ID = 4;

//重力传感器设备的信息
    public static final class Gravity_Device implements BaseColumns {
        private Gravity_Device() {
        };

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Gravity_Data.AUTHORITY + "/gravity_device");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sensorslife.gravity.sensor";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.sensorslife.gravity.sensor";

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

    public static final class GravityData implements BaseColumns {
        private GravityData() {
        };

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Gravity_Data.AUTHORITY + "/gravity");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sensorslife.gravity.data";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.sensorslife.gravity.data";

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
            .getExternalStorageDirectory() + "/sensorslife/" + "gravity.db";
    public static final String[] DATABASE_TABLES = { "gravity_device",
            "gravity" };
    public static final String[] TABLES_FIELDS = {
            // sensor device information
            Gravity_Device._ID + " integer primary key autoincrement,"
                    + Gravity_Device.TIMESTAMP + " real default 0,"
                    + Gravity_Device.DEVICE_ID + " text default '',"
                    + Gravity_Device.MAXIMUM_RANGE + " real default 0,"
                    + Gravity_Device.MINIMUM_DELAY + " real default 0,"
                    + Gravity_Device.NAME + " text default '',"
                    + Gravity_Device.POWER_MA + " real default 0,"
                    + Gravity_Device.RESOLUTION + " real default 0,"
                    + Gravity_Device.TYPE + " text default '',"
                    + Gravity_Device.VENDOR + " text default '',"
                    + Gravity_Device.VERSION + " text default ''," + "UNIQUE ("
                    + Gravity_Device.TIMESTAMP + "," + Gravity_Device.DEVICE_ID
                    + ")",
            // sensor data
            GravityData._ID + " integer primary key autoincrement,"
                    + GravityData.TIMESTAMP + " real default 0,"
                    + GravityData.DEVICE_ID + " text default '',"
                    + GravityData.VALUES_0 + " real default 0,"
                    + GravityData.VALUES_1 + " real default 0,"
                    + GravityData.VALUES_2 + " real default 0,"
                    + GravityData.ACCURACY + " integer default 0,"
                    + GravityData.LABEL + " text default ''," + "UNIQUE ("
                    + GravityData.TIMESTAMP + "," + GravityData.DEVICE_ID
                    + ")" };

    private static UriMatcher sUriMatcher = null;
    private static HashMap<String, String> sensorDeviceMap = null;
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
    public String getType(Uri uri)
    {
        switch (sUriMatcher.match(uri)) {
            case SENSOR_DEV:
                return Gravity_Device.CONTENT_TYPE;
            case SENSOR_DEV_ID:
                return Gravity_Device.CONTENT_ITEM_TYPE;
            case SENSOR_DATA:
                return GravityData.CONTENT_TYPE;
            case SENSOR_DATA_ID:
                return GravityData.CONTENT_ITEM_TYPE;
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
                        Gravity_Device.DEVICE_ID, values, SQLiteDatabase.CONFLICT_IGNORE);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (accel_id > 0) {
                    Uri accelUri = ContentUris.withAppendedId(
                            Gravity_Device.CONTENT_URI, accel_id);
                    getContext().getContentResolver().notifyChange(accelUri, null);
                    return accelUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            case SENSOR_DATA:
                database.beginTransaction();
                long accelData_id = database.insertWithOnConflict(DATABASE_TABLES[1],
                        GravityData.DEVICE_ID, values, SQLiteDatabase.CONFLICT_IGNORE);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (accelData_id > 0) {
                    Uri accelDataUri = ContentUris.withAppendedId(
                            GravityData.CONTENT_URI, accelData_id);
                    getContext().getContentResolver().notifyChange(accelDataUri,
                            null);
                    return accelDataUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:

                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

// 批量插入数据
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
                        id = database.insertOrThrow( DATABASE_TABLES[0], Gravity_Device.DEVICE_ID, v );
                    } catch ( SQLException e ) {
                        id = database.replace( DATABASE_TABLES[0], Gravity_Device.DEVICE_ID, v );
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
                        id = database.insertOrThrow( DATABASE_TABLES[1], GravityData.DEVICE_ID, v );
                    } catch ( SQLException e ) {
                        id = database.replace( DATABASE_TABLES[1], GravityData.DEVICE_ID, v );
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
        AUTHORITY = getContext().getPackageName() + ".provider.gravity";

        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Gravity_Data.AUTHORITY, DATABASE_TABLES[0],
                SENSOR_DEV);
        sUriMatcher.addURI(Gravity_Data.AUTHORITY, DATABASE_TABLES[0]
                + "/#", SENSOR_DEV_ID);
        sUriMatcher.addURI(Gravity_Data.AUTHORITY, DATABASE_TABLES[1],
                SENSOR_DATA);
        sUriMatcher.addURI(Gravity_Data.AUTHORITY, DATABASE_TABLES[1]
                + "/#", SENSOR_DATA_ID);

        sensorDeviceMap = new HashMap<String, String>();
        sensorDeviceMap.put(Gravity_Device._ID, Gravity_Device._ID);
        sensorDeviceMap.put(Gravity_Device.TIMESTAMP, Gravity_Device.TIMESTAMP);
        sensorDeviceMap.put(Gravity_Device.DEVICE_ID, Gravity_Device.DEVICE_ID);
        sensorDeviceMap.put(Gravity_Device.MAXIMUM_RANGE,
                Gravity_Device.MAXIMUM_RANGE);
        sensorDeviceMap.put(Gravity_Device.MINIMUM_DELAY,
                Gravity_Device.MINIMUM_DELAY);
        sensorDeviceMap.put(Gravity_Device.NAME, Gravity_Device.NAME);
        sensorDeviceMap.put(Gravity_Device.POWER_MA, Gravity_Device.POWER_MA);
        sensorDeviceMap.put(Gravity_Device.RESOLUTION,Gravity_Device.RESOLUTION);
        sensorDeviceMap.put(Gravity_Device.TYPE, Gravity_Device.TYPE);
        sensorDeviceMap.put(Gravity_Device.VENDOR, Gravity_Device.VENDOR);
        sensorDeviceMap.put(Gravity_Device.VERSION, Gravity_Device.VERSION);

        sensorDataMap = new HashMap<String, String>();
        sensorDataMap.put(GravityData._ID, GravityData._ID);
        sensorDataMap.put(GravityData.TIMESTAMP, GravityData.TIMESTAMP);
        sensorDataMap.put(GravityData.DEVICE_ID, GravityData.DEVICE_ID);
        sensorDataMap.put(GravityData.VALUES_0, GravityData.VALUES_0);
        sensorDataMap.put(GravityData.VALUES_1, GravityData.VALUES_1);
        sensorDataMap.put(GravityData.VALUES_2, GravityData.VALUES_2);
        sensorDataMap.put(GravityData.ACCURACY, GravityData.ACCURACY);
        sensorDataMap.put(GravityData.LABEL, GravityData.LABEL);

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
                qb.setProjectionMap(sensorDeviceMap);
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
