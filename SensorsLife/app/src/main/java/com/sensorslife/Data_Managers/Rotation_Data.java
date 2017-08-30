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

public class Rotation_Data extends ContentProvider {

    public static final int DATABASE_VERSION = 3;

    public static String AUTHORITY = "com.sensorslife.provider.rotation";

    public static String Tag="Rotation_Data:";

    private static final int SENSOR_DEV = 1;
    private static final int SENSOR_DEV_ID = 2;
    private static final int SENSOR_DATA = 3;
    private static final int SENSOR_DATA_ID = 4;

//旋转传感设备的信息
    public static final class Rotation_Device implements BaseColumns
    {
        private Rotation_Device() {
        };

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Rotation_Data.AUTHORITY + "/rotation_device");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sensorslife.rotation.device";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.sensorslife.rotation.device";

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

    public static final class RotationData implements BaseColumns
    {
        private RotationData() {
        };

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Rotation_Data.AUTHORITY + "/rotation");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sensorslife.rotation.data";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.sensorslife.rotation.data";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String VALUES_0 = "double_values_0";
        public static final String VALUES_1 = "double_values_1";
        public static final String VALUES_2 = "double_values_2";
        public static final String VALUES_3 = "double_values_3";
        public static final String ACCURACY = "accuracy";
        public static final String LABEL = "label";
    }

    public static String DATABASE_NAME = Environment
            .getExternalStorageDirectory() + "/sensorslife/" + "rotation.db";
    public static final String[] DATABASE_TABLES = { "rotation_device",
            "rotation" };
    public static final String[] TABLES_FIELDS = {
            // sensor device information
            Rotation_Device._ID + " integer primary key autoincrement,"
                    + Rotation_Device.TIMESTAMP + " real default 0,"
                    + Rotation_Device.DEVICE_ID + " text default '',"
                    + Rotation_Device.MAXIMUM_RANGE + " real default 0,"
                    + Rotation_Device.MINIMUM_DELAY + " real default 0,"
                    + Rotation_Device.NAME + " text default '',"
                    + Rotation_Device.POWER_MA + " real default 0,"
                    + Rotation_Device.RESOLUTION + " real default 0,"
                    + Rotation_Device.TYPE + " text default '',"
                    + Rotation_Device.VENDOR + " text default '',"
                    + Rotation_Device.VERSION + " text default ''," + "UNIQUE("
                    + Rotation_Device.TIMESTAMP + ","
                    + Rotation_Device.DEVICE_ID + ")",
            // sensor data
            RotationData._ID + " integer primary key autoincrement,"
                    + RotationData.TIMESTAMP + " real default 0,"
                    + RotationData.DEVICE_ID + " text default '',"
                    + RotationData.VALUES_0 + " real default 0,"
                    + RotationData.VALUES_1 + " real default 0,"
                    + RotationData.VALUES_2 + " real default 0,"
                    + RotationData.VALUES_3 + " real default 0,"
                    + RotationData.ACCURACY + " integer default 0,"
                    + RotationData.LABEL + " text default ''," + "UNIQUE("
                    + RotationData.TIMESTAMP + "," + RotationData.DEVICE_ID
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
    public String getType(Uri uri)
    {
        switch (sUriMatcher.match(uri)) {
            case SENSOR_DEV:
                return Rotation_Device.CONTENT_TYPE;
            case SENSOR_DEV_ID:
                return Rotation_Device.CONTENT_ITEM_TYPE;
            case SENSOR_DATA:
                return RotationData.CONTENT_TYPE;
            case SENSOR_DATA_ID:
                return RotationData.CONTENT_ITEM_TYPE;
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
                        Rotation_Device.DEVICE_ID, values, SQLiteDatabase.CONFLICT_IGNORE);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (accel_id > 0) {
                    Uri accelUri = ContentUris.withAppendedId(
                            Rotation_Device.CONTENT_URI, accel_id);
                    getContext().getContentResolver().notifyChange(accelUri, null);
                    return accelUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            case SENSOR_DATA:
                database.beginTransaction();
                long accelData_id = database.insertWithOnConflict(DATABASE_TABLES[1],
                        RotationData.DEVICE_ID, values, SQLiteDatabase.CONFLICT_IGNORE);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (accelData_id > 0) {
                    Uri accelDataUri = ContentUris.withAppendedId(
                            RotationData.CONTENT_URI, accelData_id);
                    getContext().getContentResolver().notifyChange(accelDataUri,
                            null);
                    return accelDataUri;
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
            case SENSOR_DEV:
                database.beginTransaction();
                for (ContentValues v : values) {
                    long id;
                    try {
                        id = database.insertOrThrow( DATABASE_TABLES[0], Rotation_Device.DEVICE_ID, v );
                    } catch ( SQLException e ) {
                        id = database.replace( DATABASE_TABLES[0], Rotation_Device.DEVICE_ID, v );
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
                        id = database.insertOrThrow( DATABASE_TABLES[1], RotationData.DEVICE_ID, v );
                    } catch ( SQLException e ) {
                        id = database.replace( DATABASE_TABLES[1], RotationData.DEVICE_ID, v );
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
        AUTHORITY = getContext().getPackageName() + ".provider.rotation";

        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Rotation_Data.AUTHORITY, DATABASE_TABLES[0],
                SENSOR_DEV);
        sUriMatcher.addURI(Rotation_Data.AUTHORITY, DATABASE_TABLES[0]
                + "/#", SENSOR_DEV_ID);
        sUriMatcher.addURI(Rotation_Data.AUTHORITY, DATABASE_TABLES[1],
                SENSOR_DATA);
        sUriMatcher.addURI(Rotation_Data.AUTHORITY, DATABASE_TABLES[1]
                + "/#", SENSOR_DATA_ID);

        sensorMap = new HashMap<String, String>();
        sensorMap.put(Rotation_Device._ID, Rotation_Device._ID);
        sensorMap.put(Rotation_Device.TIMESTAMP, Rotation_Device.TIMESTAMP);
        sensorMap.put(Rotation_Device.DEVICE_ID, Rotation_Device.DEVICE_ID);
        sensorMap.put(Rotation_Device.MAXIMUM_RANGE,
                Rotation_Device.MAXIMUM_RANGE);
        sensorMap.put(Rotation_Device.MINIMUM_DELAY,
                Rotation_Device.MINIMUM_DELAY);
        sensorMap.put(Rotation_Device.NAME, Rotation_Device.NAME);
        sensorMap.put(Rotation_Device.POWER_MA, Rotation_Device.POWER_MA);
        sensorMap.put(Rotation_Device.RESOLUTION, Rotation_Device.RESOLUTION);
        sensorMap.put(Rotation_Device.TYPE, Rotation_Device.TYPE);
        sensorMap.put(Rotation_Device.VENDOR, Rotation_Device.VENDOR);
        sensorMap.put(Rotation_Device.VERSION, Rotation_Device.VERSION);

        sensorDataMap = new HashMap<String, String>();
        sensorDataMap.put(RotationData._ID, RotationData._ID);
        sensorDataMap.put(RotationData.TIMESTAMP, RotationData.TIMESTAMP);
        sensorDataMap.put(RotationData.DEVICE_ID, RotationData.DEVICE_ID);
        sensorDataMap.put(RotationData.VALUES_0, RotationData.VALUES_0);
        sensorDataMap.put(RotationData.VALUES_1, RotationData.VALUES_1);
        sensorDataMap.put(RotationData.VALUES_2, RotationData.VALUES_2);
        sensorDataMap.put(RotationData.VALUES_3, RotationData.VALUES_3);
        sensorDataMap.put(RotationData.ACCURACY, RotationData.ACCURACY);
        sensorDataMap.put(RotationData.LABEL, RotationData.LABEL);

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
