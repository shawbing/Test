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
 *
 */

public class Network_Data extends ContentProvider {

    public static final int DATABASE_VERSION = 2;

    public static String AUTHORITY = "com.sensorslife.provider.network";

    public static String Tag="Network_Data:";

    private static final int NETWORK = 1;
    private static final int NETWORK_ID = 2;

//
    public static final class NetworkData implements BaseColumns
    {
        private NetworkData() {
        };

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Network_Data.AUTHORITY + "/network");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sensorslife.network";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.sensorslife.network";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String TYPE = "network_type";
        public static final String SUBTYPE = "network_subtype";
        public static final String STATE = "network_state";
    }

    public static String DATABASE_NAME = Environment
            .getExternalStorageDirectory() + "/sensorslife/" + "network.db";
    public static final String[] DATABASE_TABLES = { "network" };
    public static final String[] TABLES_FIELDS = {
            // network
            NetworkData._ID + " integer primary key autoincrement,"
                    + NetworkData.TIMESTAMP + " real default 0,"
                    + NetworkData.DEVICE_ID + " text default ''," + NetworkData.TYPE
                    + " integer default 0," + NetworkData.SUBTYPE
                    + " text default ''," + NetworkData.STATE + " integer default 0,"
                    + "UNIQUE(" + NetworkData.TIMESTAMP + "," + NetworkData.DEVICE_ID
                    + ")" };

    private static UriMatcher sUriMatcher = null;
    private static HashMap<String, String> networkProjectionMap = null;
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
            case NETWORK:
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
    public String getType(Uri uri)
    {
        switch (sUriMatcher.match(uri)) {
            case NETWORK:
                return NetworkData.CONTENT_TYPE;
            case NETWORK_ID:
                return NetworkData.CONTENT_ITEM_TYPE;
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
            case NETWORK:
                database.beginTransaction();
                long network_id = database.insertWithOnConflict(DATABASE_TABLES[0],
                        NetworkData.DEVICE_ID, values, SQLiteDatabase.CONFLICT_IGNORE);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (network_id > 0) {
                    Uri networkUri = ContentUris.withAppendedId(
                            NetworkData.CONTENT_URI, network_id);
                    getContext().getContentResolver()
                            .notifyChange(networkUri, null);
                    return networkUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:

                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public boolean onCreate()
    {
        AUTHORITY = getContext().getPackageName() + ".provider.network";

        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Network_Data.AUTHORITY, DATABASE_TABLES[0],
                NETWORK);
        sUriMatcher.addURI(Network_Data.AUTHORITY, DATABASE_TABLES[0]
                + "/#", NETWORK_ID);

        networkProjectionMap = new HashMap<String, String>();
        networkProjectionMap.put(NetworkData._ID, NetworkData._ID);
        networkProjectionMap
                .put(NetworkData.TIMESTAMP, NetworkData.TIMESTAMP);
        networkProjectionMap
                .put(NetworkData.DEVICE_ID, NetworkData.DEVICE_ID);
        networkProjectionMap.put(NetworkData.TYPE, NetworkData.TYPE);
        networkProjectionMap.put(NetworkData.SUBTYPE, NetworkData.SUBTYPE);
        networkProjectionMap.put(NetworkData.STATE, NetworkData.STATE);

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
            case NETWORK:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(networkProjectionMap);
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
            case NETWORK:
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
