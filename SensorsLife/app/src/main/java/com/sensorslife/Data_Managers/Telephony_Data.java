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
 * 注意“”“”“”随着通信技术的发展，这里涵盖的通信种类不是那么全面
 *
 *          还需要进行添加………………………………………………
 *
 */

public class Telephony_Data extends ContentProvider {

    public static final int DATABASE_VERSION = 8;

    public static String AUTHORITY = "com.sensorslife.provider.telephony";

    public static String Tag="Telephony_Data:";

    private static final int TELEPHONY = 1;
    private static final int TELEPHONY_ID = 2;
    private static final int GSM = 3;
    private static final int GSM_ID = 4;
    private static final int NEIGHBOR = 5;
    private static final int NEIGHBOR_ID = 6;
    private static final int CDMA = 7;
    private static final int CDMA_ID = 8;//通信信号等级，通信类型


    public static final class TelephonyData implements BaseColumns {
        private TelephonyData() {
        };

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Telephony_Data.AUTHORITY + "/telephony");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sensorslife.telephony";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.sensorslife.telephony";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String DATA_ENABLED = "data_enabled";
        public static final String IMEI_MEID_ESN = "imei_meid_esn";
        public static final String SOFTWARE_VERSION = "software_version";
        public static final String LINE_NUMBER = "line_number";
        public static final String NETWORK_COUNTRY_ISO_MCC = "network_country_iso_mcc";
        public static final String NETWORK_OPERATOR_CODE = "network_operator_code";
        public static final String NETWORK_OPERATOR_NAME = "network_operator_name";
        public static final String NETWORK_TYPE = "network_type";
        public static final String PHONE_TYPE = "phone_type";
        public static final String SIM_STATE = "sim_state";
        public static final String SIM_OPERATOR_CODE = "sim_operator_code";
        public static final String SIM_OPERATOR_NAME = "sim_operator_name";
        public static final String SIM_SERIAL = "sim_serial";
        public static final String SUBSCRIBER_ID = "subscriber_id";
    }

//GSM 通用2G网络的数据项
    public static final class GSM_Data implements BaseColumns
    {
        private GSM_Data() {
        };

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Telephony_Data.AUTHORITY + "/gsm");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sensorslife.gsm";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.sensorslife.gsm";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String CID = "cid";
        public static final String LAC = "lac";
        public static final String PSC = "psc";
        public static final String SIGNAL_STRENGTH = "signal_strength";
        public static final String GSM_BER = "bit_error_rate";
    }

/**
 *
 * GSM_Neighbors 通用2G网络的数据项
 *
 * 这倒是是什么鬼？？？？？？？？？？？？？？？？？？？？？？？？？？？？
 * */
    public static final class GSM_Neighbors_Data implements BaseColumns
    {
        private GSM_Neighbors_Data() {
        };

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Telephony_Data.AUTHORITY + "/gsm_neighbor");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sensorslife.gsm.neighbor";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.sensorslife.gsm.neighbor";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String CID = "cid";
        public static final String LAC = "lac";
        public static final String PSC = "psc";
        public static final String SIGNAL_STRENGTH = "signal_strength";
    }

//CDMA 电信3G网络
    public static final class CDMA_Data implements BaseColumns
    {
        private CDMA_Data() {
        };

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + Telephony_Data.AUTHORITY + "/cdma");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sensorslife.cdma";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.sensorslife.cdma";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String BASE_STATION_ID = "base_station_id";
        public static final String BASE_STATION_LATITUDE = "double_base_station_latitude";
        public static final String BASE_STATION_LONGITUDE = "double_base_station_longitude";
        public static final String NETWORK_ID = "network_id";
        public static final String SYSTEM_ID = "system_id";
        public static final String SIGNAL_STRENGTH = "signal_strength";
        public static final String CDMA_ECIO = "cdma_ecio";
        public static final String EVDO_DBM = "evdo_dbm";
        public static final String EVDO_ECIO = "evdo_ecio";
        public static final String EVDO_SNR = "evdo_snr";
    }

    public static String DATABASE_NAME = Environment
            .getExternalStorageDirectory() + "/sensorslife/" + "telephony.db";

    public static final String[] DATABASE_TABLES = { "telephony", "gsm",
            "gsm_neighbor", "cdma" };

    public static final String[] TABLES_FIELDS = {
            // telephony
            TelephonyData._ID + " integer primary key autoincrement,"
                    + TelephonyData.TIMESTAMP + " real default 0,"
                    + TelephonyData.DEVICE_ID + " text default '',"
                    + TelephonyData.DATA_ENABLED + " integer default 0,"
                    + TelephonyData.IMEI_MEID_ESN + " text default '',"
                    + TelephonyData.SOFTWARE_VERSION + " text default '',"
                    + TelephonyData.LINE_NUMBER + " text default '',"
                    + TelephonyData.NETWORK_COUNTRY_ISO_MCC
                    + " text default '',"
                    + TelephonyData.NETWORK_OPERATOR_CODE
                    + " text default '',"
                    + TelephonyData.NETWORK_OPERATOR_NAME
                    + " text default ''," + TelephonyData.NETWORK_TYPE
                    + " integer default 0," + TelephonyData.PHONE_TYPE
                    + " integer default 0," + TelephonyData.SIM_STATE
                    + " integer default 0," + TelephonyData.SIM_OPERATOR_CODE
                    + " text default ''," + TelephonyData.SIM_OPERATOR_NAME
                    + " text default ''," + TelephonyData.SIM_SERIAL
                    + " text default ''," + TelephonyData.SUBSCRIBER_ID
                    + " text default '',"
                    + "UNIQUE(" + TelephonyData.TIMESTAMP + "," + TelephonyData.DEVICE_ID + ")",
            // GSM data
            GSM_Data._ID + " integer primary key autoincrement,"
                    + GSM_Data.TIMESTAMP + " real default 0,"
                    + GSM_Data.DEVICE_ID + " text default ''," + GSM_Data.CID
                    + " integer default -1," + GSM_Data.LAC
                    + " integer default -1," + GSM_Data.PSC
                    + " integer default 0," + GSM_Data.SIGNAL_STRENGTH
                    + " integer default -1," + GSM_Data.GSM_BER
                    + " integer default -1," + "UNIQUE(" + GSM_Data.TIMESTAMP
                    + "," + GSM_Data.DEVICE_ID + ")",
            // GSM neighbors data
            GSM_Neighbors_Data._ID + " integer primary key autoincrement,"
                    + GSM_Neighbors_Data.TIMESTAMP + " real default 0,"
                    + GSM_Neighbors_Data.DEVICE_ID + " text default '',"
                    + GSM_Neighbors_Data.CID + " integer default -1,"
                    + GSM_Neighbors_Data.LAC + " integer default -1,"
                    + GSM_Neighbors_Data.PSC + " integer default -1,"
                    + GSM_Neighbors_Data.SIGNAL_STRENGTH + " integer default 0",
            // CDMA data
            CDMA_Data._ID + " integer primary key autoincrement,"
                    + CDMA_Data.TIMESTAMP + " real default 0,"
                    + CDMA_Data.DEVICE_ID + " text default '',"
                    + CDMA_Data.BASE_STATION_ID + " integer default 0,"
                    + CDMA_Data.BASE_STATION_LATITUDE + " real default 0,"
                    + CDMA_Data.BASE_STATION_LONGITUDE + " real default 0,"
                    + CDMA_Data.NETWORK_ID + " integer default 0,"
                    + CDMA_Data.SYSTEM_ID + " integer default 0,"
                    + CDMA_Data.SIGNAL_STRENGTH + " integer default -1,"
                    + CDMA_Data.CDMA_ECIO + " integer default -1,"
                    + CDMA_Data.EVDO_DBM + " integer default -1,"
                    + CDMA_Data.EVDO_ECIO + " integer default -1,"
                    + CDMA_Data.EVDO_SNR + " integer default -1," + "UNIQUE("
                    + CDMA_Data.TIMESTAMP + "," + CDMA_Data.DEVICE_ID + ")" };

    private static UriMatcher sUriMatcher = null;
    private static HashMap<String, String> telephonyMap = null;
    private static HashMap<String, String> gsmMap = null;
    private static HashMap<String, String> gsmNeighborsMap = null;
    private static HashMap<String, String> cdmaMap = null;
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
        if( ! initializeDB() )
        {
            Log.w(Tag,"Delete unavailable...");
            return 0;
        }

        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case TELEPHONY:
                database.beginTransaction();
                count = database.delete(DATABASE_TABLES[0], selection,
                        selectionArgs);
                database.setTransactionSuccessful();
                database.endTransaction();
                break;
            case GSM:
                database.beginTransaction();
                count = database.delete(DATABASE_TABLES[1], selection,
                        selectionArgs);
                database.setTransactionSuccessful();
                database.endTransaction();
                break;
            case NEIGHBOR:
                database.beginTransaction();
                count = database.delete(DATABASE_TABLES[2], selection,
                        selectionArgs);
                database.setTransactionSuccessful();
                database.endTransaction();
                break;
            case CDMA:
                database.beginTransaction();
                count = database.delete(DATABASE_TABLES[3], selection,
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
            case TELEPHONY:
                return TelephonyData.CONTENT_TYPE;
            case TELEPHONY_ID:
                return TelephonyData.CONTENT_ITEM_TYPE;
            case GSM:
                return GSM_Data.CONTENT_TYPE;
            case GSM_ID:
                return GSM_Data.CONTENT_ITEM_TYPE;
            case NEIGHBOR:
                return GSM_Neighbors_Data.CONTENT_TYPE;
            case NEIGHBOR_ID:
                return GSM_Neighbors_Data.CONTENT_ITEM_TYPE;
            case CDMA:
                return CDMA_Data.CONTENT_TYPE;
            case CDMA_ID:
                return CDMA_Data.CONTENT_ITEM_TYPE;
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
            case TELEPHONY:
                database.beginTransaction();
                long tele_id = database.insertWithOnConflict(DATABASE_TABLES[0],
                        TelephonyData.DEVICE_ID, values, SQLiteDatabase.CONFLICT_IGNORE);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (tele_id > 0) {
                    Uri tele_uri = ContentUris.withAppendedId(
                            TelephonyData.CONTENT_URI, tele_id);
                    getContext().getContentResolver().notifyChange(tele_uri, null);
                    return tele_uri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            case GSM:
                database.beginTransaction();
                long gsm_id = database.insertWithOnConflict(DATABASE_TABLES[1],
                        GSM_Data.DEVICE_ID, values, SQLiteDatabase.CONFLICT_IGNORE);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (gsm_id > 0) {
                    Uri gsm_uri = ContentUris.withAppendedId(GSM_Data.CONTENT_URI,
                            gsm_id);
                    getContext().getContentResolver().notifyChange(gsm_uri, null);
                    return gsm_uri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            case NEIGHBOR:
                database.beginTransaction();
                long neighbor_id = database.insertWithOnConflict(DATABASE_TABLES[2],
                        GSM_Neighbors_Data.DEVICE_ID, values, SQLiteDatabase.CONFLICT_IGNORE);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (neighbor_id > 0) {
                    Uri neighbor_uri = ContentUris.withAppendedId(
                            GSM_Neighbors_Data.CONTENT_URI, neighbor_id);
                    getContext().getContentResolver().notifyChange(neighbor_uri,
                            null);
                    return neighbor_uri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            case CDMA:
                database.beginTransaction();
                long cdma_id = database.insertWithOnConflict(DATABASE_TABLES[3],
                        CDMA_Data.DEVICE_ID, values, SQLiteDatabase.CONFLICT_IGNORE);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (cdma_id > 0) {
                    Uri cdma_uri = ContentUris.withAppendedId(
                            CDMA_Data.CONTENT_URI, cdma_id);
                    getContext().getContentResolver().notifyChange(cdma_uri, null);
                    return cdma_uri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:

                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public boolean onCreate()
    {
        AUTHORITY = getContext().getPackageName() + ".provider.telephony";

        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Telephony_Data.AUTHORITY, DATABASE_TABLES[0],
                TELEPHONY);
        sUriMatcher.addURI(Telephony_Data.AUTHORITY, DATABASE_TABLES[0]
                + "/#", TELEPHONY_ID);

        sUriMatcher.addURI(Telephony_Data.AUTHORITY, DATABASE_TABLES[1],
                GSM);
        sUriMatcher.addURI(Telephony_Data.AUTHORITY, DATABASE_TABLES[1]
                + "/#", GSM_ID);

        sUriMatcher.addURI(Telephony_Data.AUTHORITY, DATABASE_TABLES[2],
                NEIGHBOR);
        sUriMatcher.addURI(Telephony_Data.AUTHORITY, DATABASE_TABLES[2]
                + "/#", NEIGHBOR_ID);

        sUriMatcher.addURI(Telephony_Data.AUTHORITY, DATABASE_TABLES[3],
                CDMA);
        sUriMatcher.addURI(Telephony_Data.AUTHORITY, DATABASE_TABLES[3]
                + "/#", CDMA_ID);

        telephonyMap = new HashMap<String, String>();
        telephonyMap.put(TelephonyData._ID, TelephonyData._ID);
        telephonyMap.put(TelephonyData.TIMESTAMP, TelephonyData.TIMESTAMP);
        telephonyMap.put(TelephonyData.DEVICE_ID, TelephonyData.DEVICE_ID);
        telephonyMap.put(TelephonyData.DATA_ENABLED,
                TelephonyData.DATA_ENABLED);
        telephonyMap.put(TelephonyData.IMEI_MEID_ESN,
                TelephonyData.IMEI_MEID_ESN);
        telephonyMap.put(TelephonyData.SOFTWARE_VERSION,
                TelephonyData.SOFTWARE_VERSION);
        telephonyMap
                .put(TelephonyData.LINE_NUMBER, TelephonyData.LINE_NUMBER);
        telephonyMap.put(TelephonyData.NETWORK_COUNTRY_ISO_MCC,
                TelephonyData.NETWORK_COUNTRY_ISO_MCC);
        telephonyMap.put(TelephonyData.NETWORK_OPERATOR_CODE,
                TelephonyData.NETWORK_OPERATOR_CODE);
        telephonyMap.put(TelephonyData.NETWORK_OPERATOR_NAME,
                TelephonyData.NETWORK_OPERATOR_NAME);
        telephonyMap.put(TelephonyData.NETWORK_TYPE,
                TelephonyData.NETWORK_TYPE);
        telephonyMap.put(TelephonyData.PHONE_TYPE, TelephonyData.PHONE_TYPE);
        telephonyMap.put(TelephonyData.SIM_STATE, TelephonyData.SIM_STATE);
        telephonyMap.put(TelephonyData.SIM_OPERATOR_CODE,
                TelephonyData.SIM_OPERATOR_CODE);
        telephonyMap.put(TelephonyData.SIM_OPERATOR_NAME,
                TelephonyData.SIM_OPERATOR_NAME);
        telephonyMap.put(TelephonyData.SIM_SERIAL, TelephonyData.SIM_SERIAL);
        telephonyMap.put(TelephonyData.SUBSCRIBER_ID,
                TelephonyData.SUBSCRIBER_ID);

        gsmMap = new HashMap<String, String>();
        gsmMap.put(GSM_Data._ID, GSM_Data._ID);
        gsmMap.put(GSM_Data.TIMESTAMP, GSM_Data.TIMESTAMP);
        gsmMap.put(GSM_Data.DEVICE_ID, GSM_Data.DEVICE_ID);
        gsmMap.put(GSM_Data.CID, GSM_Data.CID);
        gsmMap.put(GSM_Data.LAC, GSM_Data.LAC);
        gsmMap.put(GSM_Data.PSC, GSM_Data.PSC);
        gsmMap.put(GSM_Data.SIGNAL_STRENGTH, GSM_Data.SIGNAL_STRENGTH);
        gsmMap.put(GSM_Data.GSM_BER, GSM_Data.GSM_BER);

        gsmNeighborsMap = new HashMap<String, String>();
        gsmNeighborsMap.put(GSM_Neighbors_Data._ID, GSM_Neighbors_Data._ID);
        gsmNeighborsMap.put(GSM_Neighbors_Data.TIMESTAMP,
                GSM_Neighbors_Data.TIMESTAMP);
        gsmNeighborsMap.put(GSM_Neighbors_Data.DEVICE_ID,
                GSM_Neighbors_Data.DEVICE_ID);
        gsmNeighborsMap.put(GSM_Neighbors_Data.CID, GSM_Neighbors_Data.CID);
        gsmNeighborsMap.put(GSM_Neighbors_Data.LAC, GSM_Neighbors_Data.LAC);
        gsmNeighborsMap.put(GSM_Neighbors_Data.PSC, GSM_Neighbors_Data.PSC);
        gsmNeighborsMap.put(GSM_Neighbors_Data.SIGNAL_STRENGTH,
                GSM_Neighbors_Data.SIGNAL_STRENGTH);

        cdmaMap = new HashMap<String, String>();
        cdmaMap.put(CDMA_Data._ID, CDMA_Data._ID);
        cdmaMap.put(CDMA_Data.TIMESTAMP, CDMA_Data.TIMESTAMP);
        cdmaMap.put(CDMA_Data.DEVICE_ID, CDMA_Data.DEVICE_ID);
        cdmaMap.put(CDMA_Data.BASE_STATION_ID, CDMA_Data.BASE_STATION_ID);
        cdmaMap.put(CDMA_Data.BASE_STATION_LATITUDE,
                CDMA_Data.BASE_STATION_LATITUDE);
        cdmaMap.put(CDMA_Data.BASE_STATION_LONGITUDE,
                CDMA_Data.BASE_STATION_LONGITUDE);
        cdmaMap.put(CDMA_Data.NETWORK_ID, CDMA_Data.NETWORK_ID);
        cdmaMap.put(CDMA_Data.SYSTEM_ID, CDMA_Data.SYSTEM_ID);
        cdmaMap.put(CDMA_Data.SIGNAL_STRENGTH, CDMA_Data.SIGNAL_STRENGTH);
        cdmaMap.put(CDMA_Data.CDMA_ECIO, CDMA_Data.CDMA_ECIO);
        cdmaMap.put(CDMA_Data.EVDO_DBM, CDMA_Data.EVDO_DBM);
        cdmaMap.put(CDMA_Data.EVDO_ECIO, CDMA_Data.EVDO_ECIO);
        cdmaMap.put(CDMA_Data.EVDO_SNR, CDMA_Data.EVDO_SNR);

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
            case TELEPHONY:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(telephonyMap);
                break;
            case GSM:
                qb.setTables(DATABASE_TABLES[1]);
                qb.setProjectionMap(gsmMap);
                break;
            case NEIGHBOR:
                qb.setTables(DATABASE_TABLES[2]);
                qb.setProjectionMap(gsmNeighborsMap);
                break;
            case CDMA:
                qb.setTables(DATABASE_TABLES[3]);
                qb.setProjectionMap(cdmaMap);
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
            case TELEPHONY:
                database.beginTransaction();
                count = database.update(DATABASE_TABLES[0], values, selection,
                        selectionArgs);
                database.setTransactionSuccessful();
                database.endTransaction();
                break;
            case GSM:
                database.beginTransaction();
                count = database.update(DATABASE_TABLES[1], values, selection,
                        selectionArgs);
                database.setTransactionSuccessful();
                database.endTransaction();
                break;
            case NEIGHBOR:
                database.beginTransaction();
                count = database.update(DATABASE_TABLES[2], values, selection,
                        selectionArgs);
                database.setTransactionSuccessful();
                database.endTransaction();
                break;
            case CDMA:
                database.beginTransaction();
                count = database.update(DATABASE_TABLES[3], values, selection,
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