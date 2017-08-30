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

public class Processor_Data extends ContentProvider {

    public static final int DATABASE_VERSION = 3;

    public static String AUTHORITY = "com.sensorslife.provider.processor";

    public static String Tag="Processor_Data:";

    private static final int PROCESSOR = 1;
    private static final int PROCESSOR_ID = 2;


    public static final class ProcessorData implements BaseColumns {
        private ProcessorData() {
        };

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Processor_Data.AUTHORITY + "/processor");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sensorslife.processor";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.sensorslife.processor";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String LAST_USER = "double_last_user";
        public static final String LAST_SYSTEM = "double_last_system";
        public static final String LAST_IDLE = "double_last_idle";
        public static final String USER_LOAD = "double_user_load";
        public static final String SYSTEM_LOAD = "double_system_load";
        public static final String IDLE_LOAD = "double_idle_load";
    }

    public static String DATABASE_NAME = Environment
            .getExternalStorageDirectory() + "/sensorslife/" + "processor.db";
    public static final String[] DATABASE_TABLES = { "processor" };

    public static final String[] TABLES_FIELDS = {
            // processor
            ProcessorData._ID + " integer primary key autoincrement,"
                    + ProcessorData.TIMESTAMP + " real default 0,"
                    + ProcessorData.DEVICE_ID + " text default '',"
                    + ProcessorData.LAST_USER + " real default 0,"
                    + ProcessorData.LAST_SYSTEM + " real default 0,"
                    + ProcessorData.LAST_IDLE + " real default 0,"
                    + ProcessorData.USER_LOAD + " real default 0,"
                    + ProcessorData.SYSTEM_LOAD + " real default 0,"
                    + ProcessorData.IDLE_LOAD + " real default 0," + "UNIQUE("
                    + ProcessorData.TIMESTAMP + "," + ProcessorData.DEVICE_ID + ")" };

    private static UriMatcher sUriMatcher = null;
    private static HashMap<String, String> processorProjectionMap = null;
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
            case PROCESSOR:
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
            case PROCESSOR:
                return ProcessorData.CONTENT_TYPE;
            case PROCESSOR_ID:
                return ProcessorData.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    /**
     * Insert 这里的插入函数，刚开始有点不一样了，没有日志的记录
     */
    @Override
    public Uri insert(Uri uri, ContentValues initialValues)
    {
        if (database == null || !database.isOpen())
            database = connect.getWritableDatabase();

        ContentValues values = (initialValues != null) ? new ContentValues(
                initialValues) : new ContentValues();

        switch (sUriMatcher.match(uri)) {
            case PROCESSOR:
                database.beginTransaction();
                long processor_id = database.insertWithOnConflict(DATABASE_TABLES[0],
                        ProcessorData.DEVICE_ID, values, SQLiteDatabase.CONFLICT_IGNORE);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (processor_id > 0) {
                    Uri processorUri = ContentUris.withAppendedId(
                            ProcessorData.CONTENT_URI, processor_id);
                    getContext().getContentResolver().notifyChange(processorUri,
                            null);
                    return processorUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:

                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public boolean onCreate()
    {
        AUTHORITY = getContext().getPackageName() + ".provider.processor";

        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Processor_Data.AUTHORITY, DATABASE_TABLES[0],
                PROCESSOR);
        sUriMatcher.addURI(Processor_Data.AUTHORITY, DATABASE_TABLES[0]
                + "/#", PROCESSOR_ID);

        processorProjectionMap = new HashMap<String, String>();
        processorProjectionMap.put(ProcessorData._ID, ProcessorData._ID);
        processorProjectionMap.put(ProcessorData.TIMESTAMP,
                ProcessorData.TIMESTAMP);
        processorProjectionMap.put(ProcessorData.DEVICE_ID,
                ProcessorData.DEVICE_ID);
        processorProjectionMap.put(ProcessorData.LAST_USER,
                ProcessorData.LAST_USER);
        processorProjectionMap.put(ProcessorData.LAST_SYSTEM,
                ProcessorData.LAST_SYSTEM);
        processorProjectionMap.put(ProcessorData.LAST_IDLE,
                ProcessorData.LAST_IDLE);
        processorProjectionMap.put(ProcessorData.USER_LOAD,
                ProcessorData.USER_LOAD);
        processorProjectionMap.put(ProcessorData.SYSTEM_LOAD,
                ProcessorData.SYSTEM_LOAD);
        processorProjectionMap.put(ProcessorData.IDLE_LOAD,
                ProcessorData.IDLE_LOAD);

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
            case PROCESSOR:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(processorProjectionMap);
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
            case PROCESSOR:
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
