package com.sensorslife.Sensor_Activities;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.sensorslife.CommonUtils.Extend_Core;
import com.sensorslife.Core_Activity;
import com.sensorslife.Data_Managers.Acceleration_Data;
import com.sensorslife.Data_Managers.Acceleration_Data.Acceleration_Device;
import com.sensorslife.Data_Managers.Acceleration_Data.AccelerationData;
import com.sensorslife.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/8/27.
 */

public class Acceleration_Activity extends Extend_Core implements SensorEventListener {


    public static String TAG = "Acceleration_Activity:";


    private static int SAMPLING_RATE = 200000;

    private static SensorManager mSensorManager;
    private static Sensor mAccelerometer;
    private static HandlerThread sensorThread = null;
    private static Handler sensorHandler = null;
    private static PowerManager powerManager = null;
    private static PowerManager.WakeLock wakeLock = null;
    private static String LABEL = "";

//广播通知：加速度的值，
// 额外的：还有   传感器，数据
    public static final String ACTION_ACCELEROMETER = "ACTION_ACCELEROMETER";
    public static final String EXTRA_SENSOR = "sensor";
    public static final String EXTRA_DATA = "data";

    public static final String ACTION_ACCELEROMETER_LABEL = "ACTION_ACCELEROMETER_LABEL";
    public static final String EXTRA_LABEL = "label";

    /**
     * Until today, no available Android phone samples higher than 208Hz (Nexus 7).
     * http://ilessendata.blogspot.com/2012/11/android-accelerometer-sampling-rates.html
     */
    private static ContentValues[] data_buffer;
    private static List<ContentValues> data_values = new ArrayList<ContentValues>();

    private static DataLabel dataLabeler = new DataLabel();
    public static class DataLabel extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if( intent.getAction().equals(ACTION_ACCELEROMETER_LABEL)) {
                LABEL = intent.getStringExtra(EXTRA_LABEL);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //We log current accuracy on the sensor changed event
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        ContentValues rowData = new ContentValues();
        rowData.put(AccelerationData.DEVICE_ID, Core_Activity.getSetting(getApplicationContext(), MainActivity.DEVICE_ID));
        rowData.put(AccelerationData.TIMESTAMP, System.currentTimeMillis());
        rowData.put(AccelerationData.VALUES_0, event.values[0]);
        rowData.put(AccelerationData.VALUES_1, event.values[1]);
        rowData.put(AccelerationData.VALUES_2, event.values[2]);
        rowData.put(AccelerationData.ACCURACY, event.accuracy);
        rowData.put(AccelerationData.LABEL, LABEL);

        if( data_values.size() < 250 ) {
            data_values.add(rowData);

            Intent accelData = new Intent(ACTION_ACCELEROMETER);
            accelData.putExtra(EXTRA_DATA, rowData);
            sendBroadcast(accelData);

            if( Core_Activity.DEBUG ) Log.d(TAG, "Acceleration_Activity: "+ rowData.toString());

            return;
        }

        data_buffer = new ContentValues[data_values.size()];
        data_values.toArray(data_buffer);

        try {
            if( Core_Activity.getSetting(getApplicationContext(), MainActivity.DEBUG_DB_SLOW).equals("false") ) {
                new AsyncStore().execute(data_buffer);
            }
        }catch( SQLiteException e ) {
            if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
        }catch( SQLException e ) {
            if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
        }
        data_values.clear();
    }

    /**
     * Database I/O on different thread
     */
    private class AsyncStore extends AsyncTask<ContentValues[], Void, Void> {
        @Override
        protected Void doInBackground(ContentValues[]... data) {
            getContentResolver().bulkInsert(AccelerationData.CONTENT_URI, data[0]);
            return null;
        }
    }

    /**
     * Calculates the sampling rate in Hz (i.e., how many samples did we collect in the past second)
     * @param context
     * @return hz
     */
    public static int getFrequency(Context context) {
        int hz = 0;
        String[] columns = new String[]{ "count(*) as frequency", "datetime("+ AccelerationData.TIMESTAMP+"/1000, 'unixepoch','localtime') as sample_time" };
        Cursor qry = context.getContentResolver().query(AccelerationData.CONTENT_URI, columns, "1) group by (sample_time", null, "sample_time DESC LIMIT 1 OFFSET 2");
        if( qry != null && qry.moveToFirst() ) {
            hz = qry.getInt(0);
        }
        if( qry != null && ! qry.isClosed() ) qry.close();
        return hz;
    }

    private void saveAccelerometerDevice(Sensor acc) {
        Cursor accelInfo = getContentResolver().query(Acceleration_Device.CONTENT_URI, null, null, null, null);
        if( accelInfo == null || ! accelInfo.moveToFirst() ) {
            ContentValues rowData = new ContentValues();
            rowData.put(Acceleration_Device.DEVICE_ID, Core_Activity.getSetting(getApplicationContext(), MainActivity.DEVICE_ID));
            rowData.put(Acceleration_Device.TIMESTAMP, System.currentTimeMillis());
            rowData.put(Acceleration_Device.MAXIMUM_RANGE, acc.getMaximumRange());
            rowData.put(Acceleration_Device.MINIMUM_DELAY, acc.getMinDelay());
            rowData.put(Acceleration_Device.NAME, acc.getName());
            rowData.put(Acceleration_Device.POWER_MA, acc.getPower());
            rowData.put(Acceleration_Device.RESOLUTION, acc.getResolution());
            rowData.put(Acceleration_Device.TYPE, acc.getType());
            rowData.put(Acceleration_Device.VENDOR, acc.getVendor());
            rowData.put(Acceleration_Device.VERSION, acc.getVersion());

            try {
                getContentResolver().insert(Acceleration_Device.CONTENT_URI, rowData);

                Intent accel_dev = new Intent(ACTION_ACCELEROMETER);
                accel_dev.putExtra(EXTRA_SENSOR, rowData);
                sendBroadcast(accel_dev);

                if( Core_Activity.DEBUG ) Log.d(TAG, "Acceleration_Activity device:"+ rowData.toString());
            }catch( SQLiteException e ) {
                if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
            }catch( SQLException e ) {
                if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
            }
        } else accelInfo.close();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        TAG = Core_Activity.getSetting(getApplicationContext(), MainActivity.DEBUG_TAG).length()>0? Core_Activity.getSetting(getApplicationContext(), MainActivity.DEBUG_TAG):TAG;

        if( Core_Activity.getSetting(this, MainActivity.FREQUENCY_ACCELEROMETER).length() > 0 ) {
            SAMPLING_RATE = Integer.parseInt(Core_Activity.getSetting(getApplicationContext(), MainActivity.FREQUENCY_ACCELEROMETER));
        } else {
            Core_Activity.setSetting(this, MainActivity.FREQUENCY_ACCELEROMETER, SAMPLING_RATE);
        }

        DATABASE_TABLES = Acceleration_Data.DATABASE_TABLES;
        TABLES_FIELDS = Acceleration_Data.TABLES_FIELDS;
        CONTEXT_URIS = new Uri[]{ Acceleration_Device.CONTENT_URI, AccelerationData.CONTENT_URI };

        sensorThread = new HandlerThread(TAG);
        sensorThread.start();

        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wakeLock.acquire();

        sensorHandler = new Handler(sensorThread.getLooper());
        mSensorManager.registerListener(this, mAccelerometer, SAMPLING_RATE, sensorHandler);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_ACCELEROMETER_LABEL);
        registerReceiver(dataLabeler, filter);

        if( mAccelerometer == null ) {
            if(Core_Activity.DEBUG) Log.w(TAG,"This device does not have an accelerometer!");
            Core_Activity.setSetting(this, MainActivity.STATUS_ACCELEROMETER, false);
            stopSelf();
            return;
        } else {
            saveAccelerometerDevice(mAccelerometer);
        }

        if(Core_Activity.DEBUG) Log.d(TAG,"Acceleration_Activity service created!");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        sensorHandler.removeCallbacksAndMessages(null);
        mSensorManager.unregisterListener(this, mAccelerometer);
        sensorThread.quit();

        wakeLock.release();

        unregisterReceiver(dataLabeler);

        if(Core_Activity.DEBUG) Log.d(TAG,"Acceleration_Activity service terminated...");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        TAG = Core_Activity.getSetting(getApplicationContext(), MainActivity.DEBUG_TAG).length()>0? Core_Activity.getSetting(getApplicationContext(), MainActivity.DEBUG_TAG):TAG;

        if( Core_Activity.getSetting(this, MainActivity.FREQUENCY_ACCELEROMETER).length() == 0 ) {
            Core_Activity.setSetting(this, MainActivity.FREQUENCY_ACCELEROMETER, SAMPLING_RATE);
        }

        if( SAMPLING_RATE != Integer.parseInt(Core_Activity.getSetting(getApplicationContext(), MainActivity.FREQUENCY_ACCELEROMETER)) ) { //changed parameters
            SAMPLING_RATE = Integer.parseInt(Core_Activity.getSetting(getApplicationContext(), MainActivity.FREQUENCY_ACCELEROMETER));
            sensorHandler.removeCallbacksAndMessages(null);
            mSensorManager.unregisterListener(this, mAccelerometer);
            mSensorManager.registerListener(this, mAccelerometer, SAMPLING_RATE, sensorHandler);
        }

        if(Core_Activity.DEBUG) Log.d(TAG,"Acceleration_Activity service active at " + SAMPLING_RATE + " microseconds...");

        return START_STICKY;
    }

    //Singleton instance of this service
    private static Acceleration_Activity accelerometerSrv = Acceleration_Activity.getService();

    /**
     * Get singleton instance to Accelerometer service
     * @return Accelerometer obj
     */
    public static Acceleration_Activity getService() {
        if( accelerometerSrv == null ) accelerometerSrv = new Acceleration_Activity();
        return accelerometerSrv;
    }

    private final IBinder serviceBinder = new ServiceBinder();
    public class ServiceBinder extends Binder {
        Acceleration_Activity getService() {
            return Acceleration_Activity.getService();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }
}
