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

public class WiFi_Data extends ContentProvider {

    public static final int DATABASE_VERSION = 4;

    public static String AUTHORITY = "com.sensorslife.provider.wifi";

    public static String Tag="WiFi_Data:";

    private static final int WIFI_DATA = 1;
    private static final int WIFI_DATA_ID = 2;
    private static final int WIFI_DEV = 3;
    private static final int WIFI_DEV_ID = 4;


    public static final class WiFi_Device implements BaseColumns {
        private WiFi_Device() {
        };

        public static final Uri CONTENT_URI = Uri.parse("content://"+ WiFi_Data.AUTHORITY + "/wifi_device");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sensorslife.wifi.device";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.sensorslife.wifi.device";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String MAC_ADDRESS = "mac_address";
        public static final String BSSID = "bssid";
        public static final String SSID = "ssid";
    }

    /**
     * Logged WiFi data
     *
     * @author df
     *
     */
    public static final class WiFiData implements BaseColumns {
        private WiFiData() {
        };

        public static final Uri CONTENT_URI = Uri.parse("content://"+ WiFi_Data.AUTHORITY + "/wifi");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sensorslife.wifi.data";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.sensorslife.wifi.data";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String BSSID = "bssid";
        public static final String SSID = "ssid";
        public static final String SECURITY = "security";
        public static final String FREQUENCY = "frequency";
        public static final String RSSI = "rssi";
        public static final String LABEL = "label";
    }

    public static String DATABASE_NAME = Environment
            .getExternalStorageDirectory() + "/sensorslife/" + "wifi.db";
    public static final String[] DATABASE_TABLES = { "wifi", "wifi_device" };

    public static final String[] TABLES_FIELDS = {
            // data
            WiFiData._ID + " integer primary key autoincrement,"
                    + WiFiData.TIMESTAMP + " real default 0,"
                    + WiFiData.DEVICE_ID + " text default '',"
                    + WiFiData.BSSID + " text default '',"
                    + WiFiData.SSID + " text default '',"
                    + WiFiData.SECURITY + " text default '',"
                    + WiFiData.FREQUENCY + " integer default 0,"
                    + WiFiData.RSSI + " integer default 0,"
                    + WiFiData.LABEL+ " text default '',"
                    + "UNIQUE(" + WiFiData.TIMESTAMP + "," + WiFiData.DEVICE_ID + "," + WiFiData.BSSID + ")",
            // device
            WiFi_Device._ID + " integer primary key autoincrement,"
                    + WiFi_Device.TIMESTAMP + " real default 0,"
                    + WiFi_Device.DEVICE_ID + " text default '',"
                    + WiFi_Device.MAC_ADDRESS + " text default '',"
                    + WiFiData.SSID + " text default '',"
                    + WiFiData.BSSID + " text default '',"
                    + "UNIQUE("+ WiFi_Device.TIMESTAMP + "," + WiFi_Device.DEVICE_ID + ")" };

    private static DatabaseConnect connect = null;
    private static SQLiteDatabase database = null;

    private static UriMatcher sUriMatcher = null;
    private static HashMap<String, String> wifiDataMap = null;
    private static HashMap<String, String> wifiDeviceMap = null;

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
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if( ! initializeDB() ) {
            Log.w(Tag,"Delete unavailable...");
            return 0;
        }

        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case WIFI_DATA:
                database.beginTransaction();
                count = database.delete(DATABASE_TABLES[0], selection,
                        selectionArgs);
                database.setTransactionSuccessful();
                database.endTransaction();
                break;
            case WIFI_DEV:
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
            case WIFI_DATA:
                return WiFiData.CONTENT_TYPE;
            case WIFI_DATA_ID:
                return WiFiData.CONTENT_ITEM_TYPE;
            case WIFI_DEV:
                return WiFiData.CONTENT_TYPE;
            case WIFI_DEV_ID:
                return WiFiData.CONTENT_ITEM_TYPE;
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
            case WIFI_DATA:
                database.beginTransaction();
                long wifiID = database.insertWithOnConflict(DATABASE_TABLES[0],
                        WiFiData.DEVICE_ID, values, SQLiteDatabase.CONFLICT_IGNORE);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (wifiID > 0) {
                    Uri wifiUri = ContentUris.withAppendedId(WiFiData.CONTENT_URI,
                            wifiID);
                    getContext().getContentResolver().notifyChange(wifiUri, null);
                    return wifiUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            case WIFI_DEV:
                database.beginTransaction();
                long wifiDevID = database.insertWithOnConflict(DATABASE_TABLES[1],
                        WiFi_Device.DEVICE_ID, values, SQLiteDatabase.CONFLICT_IGNORE);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (wifiDevID > 0) {
                    Uri wifiUri = ContentUris.withAppendedId(
                            WiFi_Device.CONTENT_URI, wifiDevID);
                    getContext().getContentResolver().notifyChange(wifiUri, null);
                    return wifiUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:

                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        AUTHORITY = getContext().getPackageName() + ".provider.wifi";

        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(WiFi_Data.AUTHORITY, DATABASE_TABLES[0],
                WIFI_DATA);
        sUriMatcher.addURI(WiFi_Data.AUTHORITY, DATABASE_TABLES[0] + "/#",
                WIFI_DATA_ID);
        sUriMatcher.addURI(WiFi_Data.AUTHORITY, DATABASE_TABLES[1],
                WIFI_DEV);
        sUriMatcher.addURI(WiFi_Data.AUTHORITY, DATABASE_TABLES[1] + "/#",
                WIFI_DEV_ID);

        wifiDataMap = new HashMap<String, String>();
        wifiDataMap.put(WiFiData._ID, WiFiData._ID);
        wifiDataMap.put(WiFiData.TIMESTAMP, WiFiData.TIMESTAMP);
        wifiDataMap.put(WiFiData.DEVICE_ID, WiFiData.DEVICE_ID);
        wifiDataMap.put(WiFiData.BSSID, WiFiData.BSSID);
        wifiDataMap.put(WiFiData.SSID, WiFiData.SSID);
        wifiDataMap.put(WiFiData.SECURITY, WiFiData.SECURITY);
        wifiDataMap.put(WiFiData.FREQUENCY, WiFiData.FREQUENCY);
        wifiDataMap.put(WiFiData.RSSI, WiFiData.RSSI);
        wifiDataMap.put(WiFiData.LABEL, WiFiData.LABEL);

        wifiDeviceMap = new HashMap<String, String>();
        wifiDeviceMap.put(WiFi_Device._ID, WiFi_Device._ID);
        wifiDeviceMap.put(WiFi_Device.DEVICE_ID, WiFi_Device.DEVICE_ID);
        wifiDeviceMap.put(WiFi_Device.TIMESTAMP, WiFi_Device.TIMESTAMP);
        wifiDeviceMap.put(WiFi_Device.MAC_ADDRESS, WiFi_Device.MAC_ADDRESS);
        wifiDeviceMap.put(WiFi_Device.BSSID, WiFi_Device.BSSID);
        wifiDeviceMap.put(WiFi_Device.SSID, WiFi_Device.SSID);

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        if( ! initializeDB() ) {
            Log.w(Tag,"Query unavailable...");
            return null;
        }

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
            case WIFI_DATA:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(wifiDataMap);
                break;
            case WIFI_DEV:
                qb.setTables(DATABASE_TABLES[1]);
                qb.setProjectionMap(wifiDeviceMap);
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
                      String[] selectionArgs) {

        if( ! initializeDB() ) {
            Log.w(Tag,"Update unavailable...");
            return 0;
        }

        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case WIFI_DATA:
                database.beginTransaction();
                count = database.update(DATABASE_TABLES[0], values, selection,
                        selectionArgs);
                database.setTransactionSuccessful();
                database.endTransaction();
                break;
            case WIFI_DEV:
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
