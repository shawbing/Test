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

public class Traffic_Data extends ContentProvider {

    public static final int DATABASE_VERSION = 4;

    public static String AUTHORITY = "com.sensorslife.provider.traffic";

    public static String Tag="Traffic_Data:";

    private static final int TRAFFIC = 1;
    private static final int TRAFFIC_ID = 2;


    public static final class TrafficData implements BaseColumns
    {
        private TrafficData() {
        };

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Traffic_Data.AUTHORITY + "/network_traffic");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sensorslife.network.traffic";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.sensorslife.network.traffic";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String NETWORK_TYPE = "network_type";
        public static final String RECEIVED_BYTES = "double_received_bytes";
        public static final String SENT_BYTES = "double_sent_bytes";
        public static final String RECEIVED_PACKETS = "double_received_packets";
        public static final String SENT_PACKETS = "double_sent_packets";
    }

    public static String DATABASE_NAME = Environment
            .getExternalStorageDirectory() + "/sensorslife/" + "network_traffic.db";

    public static final String[] DATABASE_TABLES = { "network_traffic" };

    public static final String[] TABLES_FIELDS = { TrafficData._ID
            + " integer primary key autoincrement," + TrafficData.TIMESTAMP
            + " real default 0," + TrafficData.DEVICE_ID + " text default '',"
            + TrafficData.NETWORK_TYPE + " integer default 0,"
            + TrafficData.RECEIVED_BYTES + " real default 0,"
            + TrafficData.SENT_BYTES + " real default 0,"
            + TrafficData.RECEIVED_PACKETS + " real default 0,"
            + TrafficData.SENT_PACKETS + " real default 0," + "UNIQUE("
            + TrafficData.TIMESTAMP + "," + TrafficData.DEVICE_ID + ")" };

    private static UriMatcher sUriMatcher = null;
    private static HashMap<String, String> trafficProjectionMap = null;
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
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        if( ! initializeDB() ) {
            Log.w(Tag,"Delete unavailable...");
            return 0;
        }

        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case TRAFFIC:
                database.beginTransaction();
                count = database.delete(DATABASE_TABLES[0], selection,
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
            case TRAFFIC:
                return TrafficData.CONTENT_TYPE;
            case TRAFFIC_ID:
                return TrafficData.CONTENT_ITEM_TYPE;
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
            case TRAFFIC:
                database.beginTransaction();
                long traffic_id = database.insertWithOnConflict(DATABASE_TABLES[0],
                        TrafficData.DEVICE_ID, values, SQLiteDatabase.CONFLICT_IGNORE);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (traffic_id > 0) {
                    Uri trafficUri = ContentUris.withAppendedId(
                            TrafficData.CONTENT_URI, traffic_id);
                    getContext().getContentResolver()
                            .notifyChange(trafficUri, null);
                    return trafficUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:

                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public boolean onCreate()
    {
        AUTHORITY = getContext().getPackageName() + ".provider.traffic";

        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Traffic_Data.AUTHORITY, DATABASE_TABLES[0],
                TRAFFIC);
        sUriMatcher.addURI(Traffic_Data.AUTHORITY, DATABASE_TABLES[0]
                + "/#", TRAFFIC_ID);

        trafficProjectionMap = new HashMap<String, String>();
        trafficProjectionMap.put(TrafficData._ID, TrafficData._ID);
        trafficProjectionMap
                .put(TrafficData.TIMESTAMP, TrafficData.TIMESTAMP);
        trafficProjectionMap
                .put(TrafficData.DEVICE_ID, TrafficData.DEVICE_ID);
        trafficProjectionMap.put(TrafficData.NETWORK_TYPE,
                TrafficData.NETWORK_TYPE);
        trafficProjectionMap.put(TrafficData.RECEIVED_BYTES,
                TrafficData.RECEIVED_BYTES);
        trafficProjectionMap.put(TrafficData.SENT_BYTES,
                TrafficData.SENT_BYTES);
        trafficProjectionMap.put(TrafficData.RECEIVED_PACKETS,
                TrafficData.RECEIVED_PACKETS);
        trafficProjectionMap.put(TrafficData.SENT_PACKETS,
                TrafficData.SENT_PACKETS);

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
            case TRAFFIC:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(trafficProjectionMap);
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
            case TRAFFIC:
                database.beginTransaction();
                count = database.update(DATABASE_TABLES[0], values, selection,
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
