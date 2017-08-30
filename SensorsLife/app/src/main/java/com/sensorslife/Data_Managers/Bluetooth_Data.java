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

public class Bluetooth_Data extends ContentProvider {

    private static final int DATABASE_VERSION = 2;

    public static String AUTHORITY = "com.sensorslife.provider.bluetooth";

    public static String Tag="Bluetooth_Data:";

    // ContentProvider query paths
    private static final int BT_DEV = 1;
    private static final int BT_DEV_ID = 2;
    private static final int BT_DATA = 3;
    private static final int BT_DATA_ID = 4;

//蓝牙设备信息
    public static final class Bluetooth_Device implements BaseColumns {
        private Bluetooth_Device() {
        };

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Bluetooth_Data.AUTHORITY + "/bluetooth_device");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sensorslife.bluetooth.device";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.sensorslife.bluetooth.device";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String BT_ADDRESS = "bt_address";
        public static final String BT_NAME = "bt_name";
    }

    public static final class BluetoothData implements BaseColumns {
        private BluetoothData() {
        };

        public static final Uri CONTENT_URI = Uri.parse( "content://" + Bluetooth_Data.AUTHORITY + "/bluetooth" );
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sensorslife.bluetooth.data";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.sensorslife.bluetooth.data";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String BT_ADDRESS = "bt_address";
        public static final String BT_NAME = "bt_name";
        public static final String BT_RSSI = "bt_rssi";
        public static final String BT_LABEL = "label";
    }

    public static String DATABASE_NAME = Environment
            .getExternalStorageDirectory() + "/sensorslife/" + "bluetooth.db";
    public static final String[] DATABASE_TABLES = { "bluetooth_device",
            "bluetooth" };
    public static final String[] TABLES_FIELDS = {
            // device
            Bluetooth_Device._ID + " integer primary key autoincrement,"
                    + Bluetooth_Device.TIMESTAMP + " real default 0,"
                    + Bluetooth_Device.DEVICE_ID + " text default '',"
                    + Bluetooth_Device.BT_ADDRESS + " text default '',"
                    + Bluetooth_Device.BT_NAME + " text default '',"
                    + "UNIQUE (" + Bluetooth_Device.TIMESTAMP + ","
                    + Bluetooth_Device.DEVICE_ID + ")",
            // data
            BluetoothData._ID + " integer primary key autoincrement,"
                    + BluetoothData.TIMESTAMP + " real default 0,"
                    + BluetoothData.DEVICE_ID + " text default '',"
                    + BluetoothData.BT_ADDRESS + " text default '',"
                    + BluetoothData.BT_NAME + " text default '',"
                    + BluetoothData.BT_RSSI + " integer default 0,"
                    + BluetoothData.BT_LABEL + " text default '',"
                    + "UNIQUE (" + BluetoothData.TIMESTAMP + ","
                    + BluetoothData.DEVICE_ID + ","
                    + BluetoothData.BT_ADDRESS + ")" };

    private static UriMatcher sUriMatcher = null;
    private static HashMap<String, String> bluetoothDeviceMap = null;
    private static HashMap<String, String> bluetoothDataMap = null;
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
            Log.w(Tag," Delete unavailable...");
            return 0;
        }

        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case BT_DEV:
                database.beginTransaction();
                count = database.delete(DATABASE_TABLES[0], selection,
                        selectionArgs);
                database.setTransactionSuccessful();
                database.endTransaction();
                break;
            case BT_DATA:
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
            case BT_DEV:
                return Bluetooth_Device.CONTENT_TYPE;
            case BT_DEV_ID:
                return Bluetooth_Device.CONTENT_ITEM_TYPE;
            case BT_DATA:
                return BluetoothData.CONTENT_TYPE;
            case BT_DATA_ID:
                return BluetoothData.CONTENT_ITEM_TYPE;
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
            case BT_DEV:
                database.beginTransaction();
                long rowId = database.insertWithOnConflict(DATABASE_TABLES[0],
                        Bluetooth_Device.BT_NAME, values, SQLiteDatabase.CONFLICT_IGNORE);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (rowId > 0) {
                    Uri bluetoothUri = ContentUris.withAppendedId(
                            Bluetooth_Device.CONTENT_URI, rowId);
                    getContext().getContentResolver().notifyChange(bluetoothUri,
                            null);
                    return bluetoothUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            case BT_DATA:
                database.beginTransaction();
                long btId = database.insertWithOnConflict(DATABASE_TABLES[1],
                        BluetoothData.BT_NAME, values, SQLiteDatabase.CONFLICT_IGNORE);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (btId > 0) {
                    Uri bluetoothUri = ContentUris.withAppendedId(
                            BluetoothData.CONTENT_URI, btId);
                    getContext().getContentResolver().notifyChange(bluetoothUri,
                            null);
                    return bluetoothUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:

                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        AUTHORITY = getContext().getPackageName() + ".provider.bluetooth";

        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Bluetooth_Data.AUTHORITY, DATABASE_TABLES[0],
                BT_DEV);
        sUriMatcher.addURI(Bluetooth_Data.AUTHORITY, DATABASE_TABLES[0]
                + "/#", BT_DEV_ID);
        sUriMatcher.addURI(Bluetooth_Data.AUTHORITY, DATABASE_TABLES[1],
                BT_DATA);
        sUriMatcher.addURI(Bluetooth_Data.AUTHORITY, DATABASE_TABLES[1]
                + "/#", BT_DATA_ID);

        bluetoothDeviceMap = new HashMap<String, String>();
        bluetoothDeviceMap.put(Bluetooth_Device._ID, Bluetooth_Device._ID);
        bluetoothDeviceMap.put(Bluetooth_Device.TIMESTAMP,
                Bluetooth_Device.TIMESTAMP);
        bluetoothDeviceMap.put(Bluetooth_Device.DEVICE_ID,
                Bluetooth_Device.DEVICE_ID);
        bluetoothDeviceMap.put(Bluetooth_Device.BT_ADDRESS,
                Bluetooth_Device.BT_ADDRESS);
        bluetoothDeviceMap.put(Bluetooth_Device.BT_NAME,
                Bluetooth_Device.BT_NAME);

        bluetoothDataMap = new HashMap<String, String>();
        bluetoothDataMap.put(BluetoothData._ID, BluetoothData._ID);
        bluetoothDataMap
                .put(BluetoothData.TIMESTAMP, BluetoothData.TIMESTAMP);
        bluetoothDataMap
                .put(BluetoothData.DEVICE_ID, BluetoothData.DEVICE_ID);
        bluetoothDataMap.put(BluetoothData.BT_ADDRESS,
                BluetoothData.BT_ADDRESS);
        bluetoothDataMap.put(BluetoothData.BT_NAME, BluetoothData.BT_NAME);
        bluetoothDataMap.put(BluetoothData.BT_RSSI, BluetoothData.BT_RSSI);
        bluetoothDataMap.put(BluetoothData.BT_LABEL, BluetoothData.BT_LABEL);

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
            case BT_DEV:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(bluetoothDeviceMap);
                break;
            case BT_DATA:
                qb.setTables(DATABASE_TABLES[1]);
                qb.setProjectionMap(bluetoothDataMap);
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
            case BT_DEV:
                database.beginTransaction();
                count = database.update(DATABASE_TABLES[0], values, selection,
                        selectionArgs);
                database.setTransactionSuccessful();
                database.endTransaction();
                break;
            case BT_DATA:
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
