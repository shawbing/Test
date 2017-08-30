package com.sensorslife.Sensor_Activities;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.sensorslife.CommonUtils.Extend_Core;
import com.sensorslife.Core_Activity;
import com.sensorslife.Data_Managers.Battery_Data;
import com.sensorslife.Data_Managers.Battery_Data.BatteryData;
import com.sensorslife.Data_Managers.Battery_Data.Battery_Charges;
import com.sensorslife.Data_Managers.Battery_Data.Battery_Discharges;
import com.sensorslife.MainActivity;

/**
 * Created by Administrator on 2017/8/27.
 */

public class Battery_Activity extends Extend_Core {

    public static String TAG = "Battery_Activity:";

//定义广播通知：电池的变化活动
    public static final String ACTION_AWARE_BATTERY_CHANGED = "ACTION_AWARE_BATTERY_CHANGED";

//定义广播通知：用户才开始充电
    public static final String ACTION_AWARE_BATTERY_CHARGING = "ACTION_AWARE_BATTERY_CHARGING";

//定义广播通知：充电环境为交流电
    public static final String ACTION_AWARE_BATTERY_CHARGING_AC = "ACTION_AWARE_BATTERY_CHARGING_AC";

//定义广播通知：充电工具：USB数据线
    public static final String ACTION_AWARE_BATTERY_CHARGING_USB = "ACTION_AWARE_BATTERY_CHARGING_USB";

//定义广播通知：开始拔掉电源，进入使用电池的状态
    public static final String ACTION_AWARE_BATTERY_DISCHARGING = "ACTION_AWARE_BATTERY_DISCHARGING";

//定义广播通知：电池已充满点
    public static final String ACTION_AWARE_BATTERY_FULL = "ACTION_AWARE_BATTERY_FULL";

//定义广播通知：低电量，需要充电
    public static final String ACTION_AWARE_BATTERY_LOW = "ACTION_AWARE_BATTERY_LOW";

//定义广播通知：电量过低，导致手机要自动关机了
    public static final String ACTION_AWARE_PHONE_SHUTDOWN = "ACTION_AWARE_PHONE_SHUTDOWN";

//定义广播通知：手机重启
    public static final String ACTION_AWARE_PHONE_REBOOT = "ACTION_AWARE_PHONE_REBOOT";

//定义手机关机的标记
    public static final int STATUS_PHONE_SHUTDOWN = -1;

//定义手机重启的标记
    public static final int STATUS_PHONE_REBOOT = -2;

//定义广播接收
    public static class Battery_Broadcaster extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {

                Bundle extras = intent.getExtras();
                if(extras == null) return;

                ContentValues rowData = new ContentValues();
                rowData.put(BatteryData.TIMESTAMP, System.currentTimeMillis());
                rowData.put(BatteryData.DEVICE_ID, Core_Activity.getSetting(context, MainActivity.DEVICE_ID));
                rowData.put(BatteryData.STATUS, extras.getInt(BatteryManager.EXTRA_STATUS));
                rowData.put(BatteryData.LEVEL, extras.getInt(BatteryManager.EXTRA_LEVEL));
                rowData.put(BatteryData.SCALE, extras.getInt(BatteryManager.EXTRA_SCALE));
                rowData.put(BatteryData.VOLTAGE, extras.getInt(BatteryManager.EXTRA_VOLTAGE));
                rowData.put(BatteryData.TEMPERATURE, extras.getInt(BatteryManager.EXTRA_TEMPERATURE)/10);
                rowData.put(BatteryData.PLUG_ADAPTOR, extras.getInt(BatteryManager.EXTRA_PLUGGED));
                rowData.put(BatteryData.HEALTH, extras.getInt(BatteryManager.EXTRA_HEALTH));
                rowData.put(BatteryData.TECHNOLOGY, extras.getString(BatteryManager.EXTRA_TECHNOLOGY));

                try{
                    if( Core_Activity.DEBUG ) Log.d(TAG,"Battery:" + rowData.toString());
                    context.getContentResolver().insert(BatteryData.CONTENT_URI, rowData);
                }catch( SQLiteException e ) {
                    if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
                }catch( SQLException e ) {
                    if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
                }

                if(extras.getInt(BatteryManager.EXTRA_PLUGGED) == BatteryManager.BATTERY_PLUGGED_AC) {
                    if(Core_Activity.DEBUG) Log.d(TAG,ACTION_AWARE_BATTERY_CHARGING_AC);
                    Intent battChargeAC = new Intent(ACTION_AWARE_BATTERY_CHARGING_AC);
                    context.sendBroadcast(battChargeAC);
                }

                if(extras.getInt(BatteryManager.EXTRA_PLUGGED) == BatteryManager.BATTERY_PLUGGED_USB) {
                    if(Core_Activity.DEBUG) Log.d(TAG,ACTION_AWARE_BATTERY_CHARGING_USB);
                    Intent battChargeUSB = new Intent(ACTION_AWARE_BATTERY_CHARGING_USB);
                    context.sendBroadcast(battChargeUSB);
                }

                if(extras.getInt(BatteryManager.EXTRA_STATUS) == BatteryManager.BATTERY_STATUS_FULL) {
                    if(Core_Activity.DEBUG) Log.d(TAG,ACTION_AWARE_BATTERY_FULL);
                    Intent battFull = new Intent(ACTION_AWARE_BATTERY_FULL);
                    context.sendBroadcast(battFull);
                }

                if(Core_Activity.DEBUG) Log.d(TAG,ACTION_AWARE_BATTERY_CHANGED);
                Intent battChanged = new Intent(ACTION_AWARE_BATTERY_CHANGED);
                context.sendBroadcast(battChanged);
            }

            if(intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
                Cursor lastBattery = context.getContentResolver().query(BatteryData.CONTENT_URI, null, null, null, BatteryData.TIMESTAMP + " DESC LIMIT 1");

                Cursor lastDischarge = context.getContentResolver().query(Battery_Discharges.CONTENT_URI, null, Battery_Discharges.END_TIMESTAMP + "=0", null, Battery_Discharges.TIMESTAMP + " DESC LIMIT 1");
                if( lastDischarge != null && lastDischarge.moveToFirst()) {
                    if( lastBattery != null && lastBattery.moveToFirst() ) {
                        ContentValues rowData = new ContentValues();
                        rowData.put(Battery_Discharges.BATTERY_END, lastBattery.getInt(lastBattery.getColumnIndex(BatteryData.LEVEL)));
                        rowData.put(Battery_Discharges.END_TIMESTAMP, System.currentTimeMillis());
                        context.getContentResolver().update(Battery_Discharges.CONTENT_URI, rowData, Battery_Discharges._ID+"="+ lastDischarge.getInt(lastDischarge.getColumnIndex(Battery_Discharges._ID)), null );
                    }
                }
                if( lastDischarge != null && ! lastDischarge.isClosed() ) lastDischarge.close();

                if( lastBattery != null && lastBattery.moveToFirst() ) {
                    ContentValues rowData = new ContentValues();
                    rowData.put(Battery_Charges.TIMESTAMP, System.currentTimeMillis());
                    rowData.put(Battery_Charges.DEVICE_ID, Core_Activity.getSetting(context, MainActivity.DEVICE_ID));
                    rowData.put(Battery_Charges.BATTERY_START, lastBattery.getInt(lastBattery.getColumnIndex(BatteryData.LEVEL)));
                    context.getContentResolver().insert(Battery_Charges.CONTENT_URI, rowData);
                }
                if( lastBattery != null && ! lastBattery.isClosed() ) lastBattery.close();

                if(Core_Activity.DEBUG) Log.d(TAG,ACTION_AWARE_BATTERY_CHARGING);
                Intent battChanged = new Intent(ACTION_AWARE_BATTERY_CHARGING);
                context.sendBroadcast(battChanged);
            }

            if(intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
                Cursor lastBattery = context.getContentResolver().query(BatteryData.CONTENT_URI, null, null, null, BatteryData.TIMESTAMP + " DESC LIMIT 1");

                Cursor lastCharge = context.getContentResolver().query(Battery_Charges.CONTENT_URI, null, Battery_Charges.END_TIMESTAMP + "=0", null, Battery_Charges.TIMESTAMP + " DESC LIMIT 1");
                if( lastCharge != null && lastCharge.moveToFirst()) {
                    if( lastBattery != null && lastBattery.moveToFirst() ) {
                        ContentValues rowData = new ContentValues();
                        rowData.put(Battery_Charges.BATTERY_END, lastBattery.getInt(lastBattery.getColumnIndex(BatteryData.LEVEL)));
                        rowData.put(Battery_Charges.END_TIMESTAMP, System.currentTimeMillis());
                        context.getContentResolver().update(Battery_Charges.CONTENT_URI, rowData, Battery_Charges._ID+"="+ lastCharge.getInt(lastCharge.getColumnIndex(Battery_Charges._ID)), null );
                    }
                }
                if( lastCharge != null && ! lastCharge.isClosed() ) lastCharge.close();

                if( lastBattery != null && lastBattery.moveToFirst() ) {
                    ContentValues rowData = new ContentValues();
                    rowData.put(Battery_Discharges.TIMESTAMP, System.currentTimeMillis());
                    rowData.put(Battery_Discharges.DEVICE_ID, Core_Activity.getSetting(context, MainActivity.DEVICE_ID));
                    rowData.put(Battery_Discharges.BATTERY_START, lastBattery.getInt(lastBattery.getColumnIndex(BatteryData.LEVEL)));
                    context.getContentResolver().insert(Battery_Discharges.CONTENT_URI, rowData);
                }
                if( lastBattery != null && ! lastBattery.isClosed() ) lastBattery.close();

                if(Core_Activity.DEBUG) Log.d(TAG,ACTION_AWARE_BATTERY_DISCHARGING);
                Intent battChanged = new Intent(ACTION_AWARE_BATTERY_DISCHARGING);
                context.sendBroadcast(battChanged);
            }

            if(intent.getAction().equals(Intent.ACTION_BATTERY_LOW)) {
                if(Core_Activity.DEBUG) Log.d(TAG,ACTION_AWARE_BATTERY_LOW);
                Intent battChanged = new Intent(ACTION_AWARE_BATTERY_LOW);
                context.sendBroadcast(battChanged);
            }

            if(intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
                Cursor lastBattery = context.getContentResolver().query(BatteryData.CONTENT_URI, null, null, null, BatteryData.TIMESTAMP + " DESC LIMIT 1");
                if(lastBattery != null && lastBattery.moveToFirst()) {
                    ContentValues rowData = new ContentValues();
                    rowData.put(BatteryData.TIMESTAMP, System.currentTimeMillis());
                    rowData.put(BatteryData.DEVICE_ID, Core_Activity.getSetting(context, MainActivity.DEVICE_ID));
                    rowData.put(BatteryData.STATUS, STATUS_PHONE_SHUTDOWN);
                    rowData.put(BatteryData.LEVEL, lastBattery.getInt(lastBattery.getColumnIndex(BatteryData.LEVEL)));
                    rowData.put(BatteryData.SCALE, lastBattery.getInt(lastBattery.getColumnIndex(BatteryData.SCALE)));
                    rowData.put(BatteryData.VOLTAGE, lastBattery.getInt(lastBattery.getColumnIndex(BatteryData.VOLTAGE)));
                    rowData.put(BatteryData.TEMPERATURE, lastBattery.getInt(lastBattery.getColumnIndex(BatteryData.TEMPERATURE)));
                    rowData.put(BatteryData.PLUG_ADAPTOR, lastBattery.getInt(lastBattery.getColumnIndex(BatteryData.PLUG_ADAPTOR)));
                    rowData.put(BatteryData.HEALTH, lastBattery.getInt(lastBattery.getColumnIndex(BatteryData.HEALTH)));
                    rowData.put(BatteryData.TECHNOLOGY, lastBattery.getString(lastBattery.getColumnIndex(BatteryData.TECHNOLOGY)));

                    try {
                        if( Core_Activity.DEBUG ) Log.d(TAG,"Battery:" + rowData.toString());
                        context.getContentResolver().insert(BatteryData.CONTENT_URI, rowData);
                    }catch( SQLiteException e ) {
                        if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
                    }catch( SQLException e ) {
                        if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
                    }
                }
                if( lastBattery != null && ! lastBattery.isClosed() ) lastBattery.close();

                if(Core_Activity.DEBUG) Log.d(TAG, ACTION_AWARE_PHONE_SHUTDOWN);
                Intent battChanged = new Intent(ACTION_AWARE_PHONE_SHUTDOWN);
                context.sendBroadcast(battChanged);
            }

            if(intent.getAction().equals(Intent.ACTION_REBOOT)) {
                Cursor lastBattery = context.getContentResolver().query(BatteryData.CONTENT_URI, null, null, null, BatteryData.TIMESTAMP + " DESC LIMIT 1");
                if( lastBattery != null && lastBattery.moveToFirst() ) {
                    ContentValues rowData = new ContentValues();
                    rowData.put(BatteryData.TIMESTAMP, System.currentTimeMillis());
                    rowData.put(BatteryData.DEVICE_ID, Core_Activity.getSetting(context, MainActivity.DEVICE_ID));
                    rowData.put(BatteryData.STATUS, STATUS_PHONE_REBOOT);
                    rowData.put(BatteryData.LEVEL, lastBattery.getInt(lastBattery.getColumnIndex(BatteryData.LEVEL)));
                    rowData.put(BatteryData.SCALE, lastBattery.getInt(lastBattery.getColumnIndex(BatteryData.SCALE)));
                    rowData.put(BatteryData.VOLTAGE, lastBattery.getInt(lastBattery.getColumnIndex(BatteryData.VOLTAGE)));
                    rowData.put(BatteryData.TEMPERATURE, lastBattery.getInt(lastBattery.getColumnIndex(BatteryData.TEMPERATURE)));
                    rowData.put(BatteryData.PLUG_ADAPTOR, lastBattery.getInt(lastBattery.getColumnIndex(BatteryData.PLUG_ADAPTOR)));
                    rowData.put(BatteryData.HEALTH, lastBattery.getInt(lastBattery.getColumnIndex(BatteryData.HEALTH)));
                    rowData.put(BatteryData.TECHNOLOGY, lastBattery.getString(lastBattery.getColumnIndex(BatteryData.TECHNOLOGY)));

                    try {
                        if( Core_Activity.DEBUG ) Log.d(TAG,"Battery:" + rowData.toString());
                        context.getContentResolver().insert(BatteryData.CONTENT_URI, rowData);
                    }catch( SQLiteException e ) {
                        if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
                    }catch( SQLException e ) {
                        if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
                    }
                }
                if( lastBattery != null && ! lastBattery.isClosed() ) lastBattery.close();

                if(Core_Activity.DEBUG) Log.d(TAG, ACTION_AWARE_PHONE_REBOOT);
                Intent battChanged = new Intent(ACTION_AWARE_PHONE_REBOOT);
                context.sendBroadcast(battChanged);
            }
        }
    }
    private static final Battery_Broadcaster batteryMonitor = new Battery_Broadcaster();

//绑定服务
    private final IBinder serviceBinder = new ServiceBinder();
    public class ServiceBinder extends Binder {
        Battery_Activity getService() {
            return Battery_Activity.getService();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    private static Battery_Activity batterySrv = Battery_Activity.getService();
//单例化
    public static Battery_Activity getService() {
        if( batterySrv == null ) batterySrv = new Battery_Activity();
        return batterySrv;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        TAG = Core_Activity.getSetting(getApplicationContext(), MainActivity.DEBUG_TAG).length()>0? Core_Activity.getSetting(getApplicationContext(), MainActivity.DEBUG_TAG):TAG;

        DATABASE_TABLES = Battery_Data.DATABASE_TABLES;
        TABLES_FIELDS = Battery_Data.TABLES_FIELDS;
        CONTEXT_URIS = new Uri[]{ BatteryData.CONTENT_URI, Battery_Discharges.CONTENT_URI, Battery_Charges.CONTENT_URI };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_BATTERY_LOW);
        filter.addAction(Intent.ACTION_SHUTDOWN);
        filter.addAction(Intent.ACTION_REBOOT);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(batteryMonitor, filter);

        if(Core_Activity.DEBUG) Log.d(TAG, "Battery service created!");
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        unregisterReceiver(batteryMonitor);

        if(Core_Activity.DEBUG) Log.d(TAG,"Battery service terminated...");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {

        TAG = Core_Activity.getSetting(getApplicationContext(), MainActivity.DEBUG_TAG).length()>0? Core_Activity.getSetting(getApplicationContext(), MainActivity.DEBUG_TAG):TAG;

        if(Core_Activity.DEBUG) Log.d(TAG, "Battery service active...");

        return START_STICKY;
    }
}
