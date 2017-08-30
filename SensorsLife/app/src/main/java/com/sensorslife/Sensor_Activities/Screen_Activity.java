package com.sensorslife.Sensor_Activities;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.sensorslife.CommonUtils.Extend_Core;
import com.sensorslife.Core_Activity;
import com.sensorslife.Data_Managers.Screen_Data;
import com.sensorslife.Data_Managers.Screen_Data.ScreenData;
import com.sensorslife.MainActivity;

/**
 * Created by Administrator on 2017/8/27.
 */

public class Screen_Activity extends Extend_Core {

    private static String TAG = "Screen_Activity:";

//广播：屏幕打开
    public static final String ACTION_AWARE_SCREEN_ON = "ACTION_AWARE_SCREEN_ON";

//广播：屏幕关闭
    public static final String ACTION_AWARE_SCREEN_OFF = "ACTION_AWARE_SCREEN_OFF";

//广播：锁屏
    public static final String ACTION_AWARE_SCREEN_LOCKED = "ACTION_AWARE_SCREEN_LOCKED";

//广播：解开屏幕
    public static final String ACTION_AWARE_SCREEN_UNLOCKED = "ACTION_AWARE_SCREEN_UNLOCKED";

//用0表示屏幕关闭
    public static final int STATUS_SCREEN_OFF = 0;

    public static final int STATUS_SCREEN_ON = 1;

    public static final int STATUS_SCREEN_LOCKED = 2;

    public static final int STATUS_SCREEN_UNLOCKED = 3;


    private final IBinder serviceBinder = new ServiceBinder();
    public class ServiceBinder extends Binder {
        Screen_Activity getService() {
            return Screen_Activity.getService();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    private static Screen_Activity screenSrv = Screen_Activity.getService();

//服务单例化
    public static Screen_Activity getService() {
        if ( screenSrv == null ) screenSrv = new Screen_Activity();
        return screenSrv;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        TAG = "Screen_Activity:";
        TAG = Core_Activity.getSetting(getApplicationContext(), MainActivity.DEBUG_TAG).length()>0
                ? Core_Activity.getSetting(getApplicationContext(), MainActivity.DEBUG_TAG)
                :TAG;

        DATABASE_TABLES = Screen_Data.DATABASE_TABLES;
        TABLES_FIELDS = Screen_Data.TABLES_FIELDS;
        CONTEXT_URIS = new Uri[]{ ScreenData.CONTENT_URI };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(screenMonitor, filter);

        if(Core_Activity.DEBUG) Log.d(TAG, "Screen service created!");
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        unregisterReceiver(screenMonitor);

        if(Core_Activity.DEBUG) Log.d(TAG,"Screen service terminated...");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {

        TAG = Core_Activity.getSetting(getApplicationContext(), MainActivity.DEBUG_TAG).length()>0? Core_Activity.getSetting(getApplicationContext(), MainActivity.DEBUG_TAG):TAG;

        if(Core_Activity.DEBUG) Log.d(TAG, "Screen service active...");

        return START_STICKY;
    }
//屏幕监控器，广播接收屏幕的动作
    public static class ScreenMonitor extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equalsIgnoreCase(Intent.ACTION_SCREEN_ON)) {
                ContentValues rowData = new ContentValues();
                rowData.put(ScreenData.TIMESTAMP, System.currentTimeMillis());
                rowData.put(ScreenData.DEVICE_ID, Core_Activity.getSetting(context, MainActivity.DEVICE_ID));
                rowData.put(ScreenData.SCREEN_STATUS, Screen_Activity.STATUS_SCREEN_ON);
                try {
                    context.getContentResolver().insert(ScreenData.CONTENT_URI, rowData);
                }catch( SQLiteException e ) {
                    if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
                }catch( SQLException e ) {
                    if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
                }

                if(Core_Activity.DEBUG) Log.d(TAG, ACTION_AWARE_SCREEN_ON);
                Intent screenOn = new Intent(ACTION_AWARE_SCREEN_ON);
                context.sendBroadcast(screenOn);
            }
            if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {

                ContentValues rowData = new ContentValues();
                rowData.put(ScreenData.TIMESTAMP, System.currentTimeMillis());
                rowData.put(ScreenData.DEVICE_ID, Core_Activity.getSetting(context, MainActivity.DEVICE_ID));
                rowData.put(ScreenData.SCREEN_STATUS, Screen_Activity.STATUS_SCREEN_OFF);
                try {
                    context.getContentResolver().insert(ScreenData.CONTENT_URI, rowData);
                }catch( SQLiteException e ) {
                    if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
                }catch( SQLException e ) {
                    if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
                }

                if(Core_Activity.DEBUG) Log.d(TAG, ACTION_AWARE_SCREEN_OFF);
                Intent screenOff = new Intent(ACTION_AWARE_SCREEN_OFF);
                context.sendBroadcast(screenOff);

    //虽然屏幕关闭了，但还是要检查是否锁屏，尽管有些用户不清楚，没有使用
                KeyguardManager km = (KeyguardManager) context.getSystemService(KEYGUARD_SERVICE);
                if( km.inKeyguardRestrictedInputMode() ) {
                    rowData = new ContentValues();
                    rowData.put(ScreenData.TIMESTAMP, System.currentTimeMillis());
                    rowData.put(ScreenData.DEVICE_ID, Core_Activity.getSetting(context, MainActivity.DEVICE_ID));
                    rowData.put(ScreenData.SCREEN_STATUS, Screen_Activity.STATUS_SCREEN_LOCKED);
                    try {
                        context.getContentResolver().insert(ScreenData.CONTENT_URI, rowData);
                    }catch( SQLiteException e ) {
                        if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
                    }catch( SQLException e ) {
                        if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
                    }

                    if(Core_Activity.DEBUG) Log.d(TAG, ACTION_AWARE_SCREEN_LOCKED);
                    Intent screenLocked = new Intent(ACTION_AWARE_SCREEN_LOCKED);
                    context.sendBroadcast(screenLocked);
                }
            }
            if(intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                ContentValues rowData = new ContentValues();
                rowData.put(ScreenData.TIMESTAMP, System.currentTimeMillis());
                rowData.put(ScreenData.DEVICE_ID, Core_Activity.getSetting(context, MainActivity.DEVICE_ID));
                rowData.put(ScreenData.SCREEN_STATUS, Screen_Activity.STATUS_SCREEN_UNLOCKED);
                try {
                    context.getContentResolver().insert(ScreenData.CONTENT_URI, rowData);
                }catch( SQLiteException e ) {
                    if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
                }catch( SQLException e ) {
                    if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
                }

                if(Core_Activity.DEBUG) Log.d(TAG, ACTION_AWARE_SCREEN_UNLOCKED);
                Intent screenUnlocked = new Intent(ACTION_AWARE_SCREEN_UNLOCKED);
                context.sendBroadcast(screenUnlocked);
            }
        }
    }
    private static final ScreenMonitor screenMonitor = new ScreenMonitor();
}
