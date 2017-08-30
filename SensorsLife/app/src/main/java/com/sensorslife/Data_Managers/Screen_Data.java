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
 * Created by Administrator on 2017/8/27.
 */

public class Screen_Data extends ContentProvider {

    public static final int DATABASE_VERSION = 2;

    public static String AUTHORITY = "com.sensorslife.provider.screen";

    public static String Tag="Screen_Data :";

    private static final int SCREEN = 1;
    private static final int SCREEN_ID = 2;

//
    public static final class ScreenData implements BaseColumns
    {
        private ScreenData() {
        };

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Screen_Data.AUTHORITY + "/screen");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sensorslife.screen";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.sensorslife.screen";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String SCREEN_STATUS = "screen_status";
    }

    public static String DATABASE_NAME = Environment
            .getExternalStorageDirectory() + "/sensorslife/" + "screen.db";
    public static final String[] DATABASE_TABLES = { "screen" };

    public static final String[] TABLES_FIELDS = {
            // screen
            ScreenData._ID + " integer primary key autoincrement,"
                    + ScreenData.TIMESTAMP + " real default 0,"
                    + ScreenData.DEVICE_ID + " text default '',"
                    + ScreenData.SCREEN_STATUS + " integer default 0," + "UNIQUE("
                    + ScreenData.TIMESTAMP + "," + ScreenData.DEVICE_ID + ")" };

    private static UriMatcher sUriMatcher = null;
    private static HashMap<String, String> screenProjectionMap = null;
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
            case SCREEN:
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
            case SCREEN:
                return ScreenData.CONTENT_TYPE;
            case SCREEN_ID:
                return ScreenData.CONTENT_ITEM_TYPE;
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
            case SCREEN:
                database.beginTransaction();
                long screen_id = database.insertWithOnConflict(DATABASE_TABLES[0],
                        ScreenData.DEVICE_ID, values, SQLiteDatabase.CONFLICT_IGNORE);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (screen_id > 0) {
                    Uri screenUri = ContentUris.withAppendedId(
                            ScreenData.CONTENT_URI, screen_id);
                    getContext().getContentResolver().notifyChange(screenUri, null);
                    return screenUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:

                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        AUTHORITY = getContext().getPackageName() + ".provider.screen";

        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Screen_Data.AUTHORITY, DATABASE_TABLES[0],
                SCREEN);
        sUriMatcher.addURI(Screen_Data.AUTHORITY,
                DATABASE_TABLES[0] + "/#", SCREEN_ID);

        screenProjectionMap = new HashMap<String, String>();
        screenProjectionMap.put(ScreenData._ID, ScreenData._ID);
        screenProjectionMap.put(ScreenData.TIMESTAMP, ScreenData.TIMESTAMP);
        screenProjectionMap.put(ScreenData.DEVICE_ID, ScreenData.DEVICE_ID);
        screenProjectionMap.put(ScreenData.SCREEN_STATUS,
                ScreenData.SCREEN_STATUS);

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
            case SCREEN:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(screenProjectionMap);
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
            case SCREEN:
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
