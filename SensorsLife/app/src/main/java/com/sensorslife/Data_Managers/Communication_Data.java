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

public class Communication_Data extends ContentProvider {

    public static final int DATABASE_VERSION = 2;

    public static String AUTHORITY = "com.sensorslife.provider.communication";

    public static String Tag="Communication_Data:";

    private static final int CALLS = 1;
    private static final int CALLS_ID = 2;
    private static final int MESSAGES = 3;
    private static final int MESSAGES_ID = 4;

//展示所需采集的电话内容
    public static final class Calls_Data implements BaseColumns
    {
        private Calls_Data() {
        };

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Communication_Data.AUTHORITY + "/calls");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sensorslife.calls";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.sensorslife.calls";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String TYPE = "call_type";
        public static final String DURATION = "call_duration";
        public static final String TRACE = "trace";
    }

//表述 采集短信的哪些内容
    public static final class Messages_Data implements BaseColumns
    {
        private Messages_Data() {
        };

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Communication_Data.AUTHORITY + "/messages");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sensorslife.messages";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.sensorslife.messages";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String TYPE = "message_type";
        public static final String TRACE = "trace";
    }

    public static String DATABASE_NAME = Environment
            .getExternalStorageDirectory() + "/sensorslife/" + "communication.db";

    public static final String[] DATABASE_TABLES = { "calls", "messages" };

    public static final String[] TABLES_FIELDS = {
            // calls
            "_id integer primary key autoincrement,"
                    + "timestamp real default 0,"
                    + "device_id text default '',"
                    + "call_type integer default 0,"
                    + "call_duration integer default 0,"
                    + "trace text default ''," + "UNIQUE ("
                    + Calls_Data.TIMESTAMP + "," + Calls_Data.DEVICE_ID + ")",
            // messages
            "_id integer primary key autoincrement,"
                    + "timestamp real default 0,"
                    + "device_id text default '',"
                    + "message_type integer default 0,"
                    + "trace text default ''," + "UNIQUE ("
                    + Messages_Data.TIMESTAMP + "," + Messages_Data.DEVICE_ID
                    + ")" };

    private static UriMatcher sUriMatcher = null;
    private static HashMap<String, String> callsProjectionMap = null;
    private static HashMap<String, String> messageProjectionMap = null;
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
            case CALLS:
                database.beginTransaction();
                count = database.delete(DATABASE_TABLES[0], selection,
                        selectionArgs);
                database.setTransactionSuccessful();
                database.endTransaction();
                break;
            case MESSAGES:
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
            case CALLS:
                return Calls_Data.CONTENT_TYPE;
            case CALLS_ID:
                return Calls_Data.CONTENT_ITEM_TYPE;
            case MESSAGES:
                return Messages_Data.CONTENT_TYPE;
            case MESSAGES_ID:
                return Messages_Data.CONTENT_ITEM_TYPE;
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
            case CALLS:
                database.beginTransaction();
                long call_id = database.insertWithOnConflict(DATABASE_TABLES[0],
                        Calls_Data.DEVICE_ID, values, SQLiteDatabase.CONFLICT_IGNORE);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (call_id > 0) {
                    Uri callsUri = ContentUris.withAppendedId(
                            Calls_Data.CONTENT_URI, call_id);
                    getContext().getContentResolver().notifyChange(callsUri, null);
                    return callsUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            case MESSAGES:
                database.beginTransaction();
                long message_id = database.insertWithOnConflict(DATABASE_TABLES[1],
                        Messages_Data.DEVICE_ID, values, SQLiteDatabase.CONFLICT_IGNORE);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (message_id > 0) {
                    Uri messagesUri = ContentUris.withAppendedId(
                            Messages_Data.CONTENT_URI, message_id);
                    getContext().getContentResolver().notifyChange(messagesUri,
                            null);
                    return messagesUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:

                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public boolean onCreate()
    {
        AUTHORITY = getContext().getPackageName() + ".provider.communication";

        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Communication_Data.AUTHORITY,
                DATABASE_TABLES[0], CALLS);
        sUriMatcher.addURI(Communication_Data.AUTHORITY, DATABASE_TABLES[0]
                + "/#", CALLS_ID);
        sUriMatcher.addURI(Communication_Data.AUTHORITY,
                DATABASE_TABLES[1], MESSAGES);
        sUriMatcher.addURI(Communication_Data.AUTHORITY, DATABASE_TABLES[1]
                + "/#", MESSAGES_ID);

        callsProjectionMap = new HashMap<String, String>();
        callsProjectionMap.put(Calls_Data._ID, Calls_Data._ID);
        callsProjectionMap.put(Calls_Data.TIMESTAMP, Calls_Data.TIMESTAMP);
        callsProjectionMap.put(Calls_Data.DEVICE_ID, Calls_Data.DEVICE_ID);
        callsProjectionMap.put(Calls_Data.TYPE, Calls_Data.TYPE);
        callsProjectionMap.put(Calls_Data.DURATION, Calls_Data.DURATION);
        callsProjectionMap.put(Calls_Data.TRACE, Calls_Data.TRACE);

        messageProjectionMap = new HashMap<String, String>();
        messageProjectionMap.put(Messages_Data._ID, Messages_Data._ID);
        messageProjectionMap.put(Messages_Data.TIMESTAMP,
                Messages_Data.TIMESTAMP);
        messageProjectionMap.put(Messages_Data.DEVICE_ID,
                Messages_Data.DEVICE_ID);
        messageProjectionMap.put(Messages_Data.TYPE, Messages_Data.TYPE);
        messageProjectionMap.put(Messages_Data.TRACE, Messages_Data.TRACE);

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
            case CALLS:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(callsProjectionMap);
                break;
            case MESSAGES:
                qb.setTables(DATABASE_TABLES[1]);
                qb.setProjectionMap(messageProjectionMap);
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
            case CALLS:
                database.beginTransaction();
                count = database.update(DATABASE_TABLES[0], values, selection,
                        selectionArgs);
                database.setTransactionSuccessful();
                database.endTransaction();
                break;
            case MESSAGES:
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
