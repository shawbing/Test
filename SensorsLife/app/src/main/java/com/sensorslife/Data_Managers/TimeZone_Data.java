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

public class TimeZone_Data extends ContentProvider {

    public static final int DATABASE_VERSION = 6;

    public static String AUTHORITY = "com.sensorslife.provider.timezone";

    public static String Tag="TimeZone_Data:";

    private static final int TIMEZONE = 1;
    private static final int TIMEZONE_ID = 2;


    public static final class TimeZoneData implements BaseColumns {
        private TimeZoneData() {
        };

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + TimeZone_Data.AUTHORITY + "/timezone");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sensorslife.timezone";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.sensorslife.timezone";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String TIMEZONE = "timezone";
    }

    public static String DATABASE_NAME = Environment
            .getExternalStorageDirectory() + "/sensorslife/" + "timezone.db";

    public static final String[] DATABASE_TABLES = {"timezone"};

    public static final String[] TABLES_FIELDS = {
            // timezone
            TimeZoneData._ID + " integer primary key autoincrement,"
                    + TimeZoneData.TIMESTAMP + " real default 0,"
                    + TimeZoneData.DEVICE_ID + " text default '',"
                    + TimeZoneData.TIMEZONE + " text default '',"
                    + "UNIQUE(" + TimeZoneData.TIMESTAMP + "," + TimeZoneData.DEVICE_ID + ")" };

    private static UriMatcher sUriMatcher = null;
    private static HashMap<String, String> timeZoneMap = null;
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
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if( ! initializeDB() ) {
            Log.w(Tag,"Delete unavailable...");
            return 0;
        }

        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case TIMEZONE:
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
            case TIMEZONE:
                return TimeZoneData.CONTENT_TYPE;
            case TIMEZONE_ID:
                return TimeZoneData.CONTENT_ITEM_TYPE;
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
            case TIMEZONE:
                database.beginTransaction();
                long timezone_id = database.insertWithOnConflict(DATABASE_TABLES[0],
                        TimeZoneData.TIMEZONE, values, SQLiteDatabase.CONFLICT_IGNORE);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (timezone_id > 0) {
                    Uri tele_uri = ContentUris.withAppendedId(
                            TimeZoneData.CONTENT_URI, timezone_id);
                    getContext().getContentResolver().notifyChange(tele_uri, null);
                    return tele_uri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:

                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        AUTHORITY = getContext().getPackageName() + ".provider.timezone";

        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(TimeZone_Data.AUTHORITY, DATABASE_TABLES[0],
                TIMEZONE);
        sUriMatcher.addURI(TimeZone_Data.AUTHORITY, DATABASE_TABLES[0]
                + "/#", TIMEZONE_ID);

        timeZoneMap = new HashMap<String, String>();
        timeZoneMap.put(TimeZoneData._ID, TimeZoneData._ID);
        timeZoneMap.put(TimeZoneData.TIMESTAMP, TimeZoneData.TIMESTAMP);
        timeZoneMap.put(TimeZoneData.DEVICE_ID, TimeZoneData.DEVICE_ID);
        timeZoneMap.put(TimeZoneData.TIMEZONE, TimeZoneData.TIMEZONE);

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
            case TIMEZONE:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(timeZoneMap);
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
            case TIMEZONE:
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
