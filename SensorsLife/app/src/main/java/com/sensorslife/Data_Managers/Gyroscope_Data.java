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

public class Gyroscope_Data extends ContentProvider {

    public static final int DATABASE_VERSION = 2;

    public static String AUTHORITY = "com.sensorslife.provider.gyroscope";

    public static String Tag="Gyroscope_Data:";

    private static final int GYRO_DEV = 1;
    private static final int GYRO_DEV_ID = 2;
    private static final int GYRO_DATA = 3;
    private static final int GYRO_DATA_ID = 4;

///陀螺仪设备的信息
    public static final class Gyroscope_Device implements BaseColumns
    {
        private Gyroscope_Device() {
        };

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Gyroscope_Data.AUTHORITY + "/gyroscope_device");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sensorslife.gyroscope.sensor";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.sensorslife.gyroscope.sensor";

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
//陀螺仪的数据
    public static final class GyroscopeData implements BaseColumns
    {
        private GyroscopeData() {
        };

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Gyroscope_Data.AUTHORITY + "/gyroscope");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sensorslife.gyroscope.data";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.sensorslife.gyroscope.data";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String VALUES_0 = "axis_x";
        public static final String VALUES_1 = "axis_y";
        public static final String VALUES_2 = "axis_z";
        public static final String ACCURACY = "accuracy";
        public static final String LABEL = "label";
    }

    public static String DATABASE_NAME = Environment
            .getExternalStorageDirectory() + "/sensorslife/gyroscope.db";
    public static final String[] DATABASE_TABLES = { "gyroscope_device",
            "gyroscope" };
    public static final String[] TABLES_FIELDS = {
            // gyroscope device information
            Gyroscope_Device._ID + " integer primary key autoincrement,"
                    + Gyroscope_Device.TIMESTAMP + " real default 0,"
                    + Gyroscope_Device.DEVICE_ID + " text default '',"
                    + Gyroscope_Device.MAXIMUM_RANGE + " real default 0,"
                    + Gyroscope_Device.MINIMUM_DELAY + " real default 0,"
                    + Gyroscope_Device.NAME + " text default '',"
                    + Gyroscope_Device.POWER_MA + " real default 0,"
                    + Gyroscope_Device.RESOLUTION + " real default 0,"
                    + Gyroscope_Device.TYPE + " text default '',"
                    + Gyroscope_Device.VENDOR + " text default '',"
                    + Gyroscope_Device.VERSION + " text default '',"
                    + "UNIQUE(" + Gyroscope_Device.TIMESTAMP + ","
                    + Gyroscope_Device.DEVICE_ID + ")",
            // gyroscope data
            GyroscopeData._ID + " integer primary key autoincrement,"
                    + GyroscopeData.TIMESTAMP + " real default 0,"
                    + GyroscopeData.DEVICE_ID + " text default '',"
                    + GyroscopeData.VALUES_0 + " real default 0,"
                    + GyroscopeData.VALUES_1 + " real default 0,"
                    + GyroscopeData.VALUES_2 + " real default 0,"
                    + GyroscopeData.ACCURACY + " integer default 0,"
                    + GyroscopeData.LABEL + " text default ''," + "UNIQUE("
                    + GyroscopeData.TIMESTAMP + "," + GyroscopeData.DEVICE_ID
                    + ")" };

    private static UriMatcher sUriMatcher = null;
    private static HashMap<String, String> gyroDeviceMap = null;
    private static HashMap<String, String> gyroDataMap = null;
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
            case GYRO_DEV:
                database.beginTransaction();
                count = database.delete(DATABASE_TABLES[0], selection,
                        selectionArgs);
                database.setTransactionSuccessful();
                database.endTransaction();
                break;
            case GYRO_DATA:
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
            case GYRO_DEV:
                return Gyroscope_Device.CONTENT_TYPE;
            case GYRO_DEV_ID:
                return Gyroscope_Device.CONTENT_ITEM_TYPE;
            case GYRO_DATA:
                return GyroscopeData.CONTENT_TYPE;
            case GYRO_DATA_ID:
                return GyroscopeData.CONTENT_ITEM_TYPE;
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
            case GYRO_DEV:
                database.beginTransaction();
                long gyro_id = database.insertWithOnConflict(DATABASE_TABLES[0],
                        Gyroscope_Device.DEVICE_ID, values, SQLiteDatabase.CONFLICT_IGNORE);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (gyro_id > 0) {
                    Uri gyroUri = ContentUris.withAppendedId(
                            Gyroscope_Device.CONTENT_URI, gyro_id);
                    getContext().getContentResolver().notifyChange(gyroUri, null);
                    return gyroUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            case GYRO_DATA:
                database.beginTransaction();
                long gyroData_id = database.insertWithOnConflict(DATABASE_TABLES[1],
                        GyroscopeData.DEVICE_ID, values, SQLiteDatabase.CONFLICT_IGNORE);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (gyroData_id > 0) {
                    Uri gyroDataUri = ContentUris.withAppendedId(
                            GyroscopeData.CONTENT_URI, gyroData_id);
                    getContext().getContentResolver().notifyChange(gyroDataUri,
                            null);
                    return gyroDataUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:

                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values)
    {
        if( ! initializeDB() ) {
            Log.w(Tag,"BulkInsert unavailable...");
            return 0;
        }

        int count = 0;
        switch ( sUriMatcher.match(uri) ) {
            case GYRO_DEV:
                database.beginTransaction();
                for (ContentValues v : values) {
                    long id;
                    try {
                        id = database.insertOrThrow( DATABASE_TABLES[0], Gyroscope_Device.DEVICE_ID, v );
                    } catch ( SQLException e ) {
                        id = database.replace( DATABASE_TABLES[0], Gyroscope_Device.DEVICE_ID, v );
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
            case GYRO_DATA:
                database.beginTransaction();
                for (ContentValues v : values) {
                    long id;
                    try {
                        id = database.insertOrThrow( DATABASE_TABLES[1], GyroscopeData.DEVICE_ID, v );
                    } catch ( SQLException e ) {
                        id = database.replace( DATABASE_TABLES[1], GyroscopeData.DEVICE_ID, v );
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
        AUTHORITY = getContext().getPackageName() + ".provider.gyroscope";

        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Gyroscope_Data.AUTHORITY, DATABASE_TABLES[0],
                GYRO_DEV);
        sUriMatcher.addURI(Gyroscope_Data.AUTHORITY, DATABASE_TABLES[0]
                + "/#", GYRO_DEV_ID);
        sUriMatcher.addURI(Gyroscope_Data.AUTHORITY, DATABASE_TABLES[1],
                GYRO_DATA);
        sUriMatcher.addURI(Gyroscope_Data.AUTHORITY, DATABASE_TABLES[1]
                + "/#", GYRO_DATA_ID);

        gyroDeviceMap = new HashMap<String, String>();
        gyroDeviceMap.put(Gyroscope_Device._ID, Gyroscope_Device._ID);
        gyroDeviceMap.put(Gyroscope_Device.TIMESTAMP,
                Gyroscope_Device.TIMESTAMP);
        gyroDeviceMap.put(Gyroscope_Device.DEVICE_ID,
                Gyroscope_Device.DEVICE_ID);
        gyroDeviceMap.put(Gyroscope_Device.MAXIMUM_RANGE,
                Gyroscope_Device.MAXIMUM_RANGE);
        gyroDeviceMap.put(Gyroscope_Device.MINIMUM_DELAY,
                Gyroscope_Device.MINIMUM_DELAY);
        gyroDeviceMap.put(Gyroscope_Device.NAME, Gyroscope_Device.NAME);
        gyroDeviceMap.put(Gyroscope_Device.POWER_MA, Gyroscope_Device.POWER_MA);
        gyroDeviceMap.put(Gyroscope_Device.RESOLUTION,
                Gyroscope_Device.RESOLUTION);
        gyroDeviceMap.put(Gyroscope_Device.TYPE, Gyroscope_Device.TYPE);
        gyroDeviceMap.put(Gyroscope_Device.VENDOR, Gyroscope_Device.VENDOR);
        gyroDeviceMap.put(Gyroscope_Device.VERSION, Gyroscope_Device.VERSION);

        gyroDataMap = new HashMap<String, String>();
        gyroDataMap.put(GyroscopeData._ID, GyroscopeData._ID);
        gyroDataMap.put(GyroscopeData.TIMESTAMP, GyroscopeData.TIMESTAMP);
        gyroDataMap.put(GyroscopeData.DEVICE_ID, GyroscopeData.DEVICE_ID);
        gyroDataMap.put(GyroscopeData.VALUES_0, GyroscopeData.VALUES_0);
        gyroDataMap.put(GyroscopeData.VALUES_1, GyroscopeData.VALUES_1);
        gyroDataMap.put(GyroscopeData.VALUES_2, GyroscopeData.VALUES_2);
        gyroDataMap.put(GyroscopeData.ACCURACY, GyroscopeData.ACCURACY);
        gyroDataMap.put(GyroscopeData.LABEL, GyroscopeData.LABEL);

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
            case GYRO_DEV:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(gyroDeviceMap);
                break;
            case GYRO_DATA:
                qb.setTables(DATABASE_TABLES[1]);
                qb.setProjectionMap(gyroDataMap);
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
            case GYRO_DEV:
                database.beginTransaction();
                count = database.update(DATABASE_TABLES[0], values, selection,
                        selectionArgs);
                database.setTransactionSuccessful();
                database.endTransaction();
                break;
            case GYRO_DATA:
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
