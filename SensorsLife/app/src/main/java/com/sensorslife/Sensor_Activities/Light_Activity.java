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
import android.widget.Toast;

import com.sensorslife.CommonUtils.Extend_Core;
import com.sensorslife.Core_Activity;
import com.sensorslife.Data_Managers.Light_Data;
import com.sensorslife.Data_Managers.Light_Data.Light_Device;
import com.sensorslife.Data_Managers.Light_Data.LightData;
import com.sensorslife.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/8/25.
 */

public class Light_Activity extends Extend_Core implements SensorEventListener {
    //
//    final EditTextPreference current_light = (EditTextPreference) findPreference(MainActivity.CURRENT_LIGHT);

    private static String TAG = "Light_Activity";

    //光感数据的更新频率
    private static int SAMPLING_RATE = 200000;

    private static Context fullContext;
    private static SensorManager mSensorManager;
    private static Sensor mLight;
    private static HandlerThread sensorThread = null;
    private static Handler sensorHandler = null;
    private static PowerManager powerManager = null;
    private static PowerManager.WakeLock wakeLock = null;

    //定义广播通知的标记，表示有新的数据产生
    public static final String ACTION_LIGHT = "ACTION_LIGHT";
    public static final String EXTRA_DATA = "data";
    public static final String EXTRA_SENSOR = "sensor";

    public static final String ACTION_LIGHT_LABEL = "ACTION_LIGHT_LABEL";
    public static final String EXTRA_LABEL = "label";

    //直到现在，没有任何一个手机的频率超过208赫兹
    private static ContentValues[] Light_ContentValue;
    private static List<ContentValues> Light_List = new ArrayList<ContentValues>();

    private static String LABEL = "";
    //用label来接收光感变化的数据
    private static DataLabel dataLabeler = new DataLabel();
    public static class DataLabel extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            if( intent.getAction().equals(ACTION_LIGHT_LABEL)) {
                LABEL = intent.getStringExtra(EXTRA_LABEL);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //关于精度的变化，用日志打印出来
        Log.w(TAG,"AccuracyChanged to:"+accuracy);
    }

    /**当传感器数据变化时，回调此方法
     * 光感的数据从此处开始
     *
     * /**这里并没有加入  记录频率 的影响因素
     */
    @Override
    public void onSensorChanged(SensorEvent event)
    {
        ContentValues rowData = new ContentValues();
        rowData.put(LightData.DEVICE_ID, Core_Activity.getSetting(
                getApplicationContext(), MainActivity.DEVICE_ID));
        rowData.put(LightData.TIMESTAMP, System.currentTimeMillis());
        rowData.put(LightData.LIGHT_LUX, event.values[0]);
        rowData.put(LightData.ACCURACY, event.accuracy);
        rowData.put(LightData.LABEL, LABEL);

//利用Light_List来积累一定量的数据，最后统一批量存入数据库
// 这就是为什么一直都是这个整数倍的数据条数
        if( Light_List.size() < 250 ) {

            Light_List.add(rowData);

            Intent lightData = new Intent(ACTION_LIGHT);
            lightData.putExtra(EXTRA_DATA, rowData);
            sendBroadcast(lightData);

            if( Core_Activity.DEBUG ) Log.d(TAG, "Light:"+ rowData.toString());

            return;
        }

        Light_ContentValue = new ContentValues[Light_List.size()];
        Light_List.toArray(Light_ContentValue);

        try {
            if( Core_Activity.getSetting(getApplicationContext(),
                    MainActivity.DEBUG_DB_SLOW).equals("false") )
            {

                Toast.makeText(fullContext,"Light_Activity.onSensorChanged:\n     " +
                                " 开始调用批量数据处理：" +
                                "\n   AsyncStore().execute "
                                +" \n数据量为Light_ContentValue：" +
                                Light_ContentValue.length
                        , Toast.LENGTH_LONG).show();

                new AsyncStore().execute(Light_ContentValue);
            }


        }catch( SQLiteException e ) {
            if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
        }catch( SQLException e ) {
            if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
        }
        Light_List.clear();//最后清除数据，释放内存
    }

    /**由于此方法耗时比较长，需要启动新的线程来进行输入输出操作,
     * 当上面积累到250条数据时，就调用此方法，进行批量插入数据*/
    private class AsyncStore extends AsyncTask<ContentValues[], Void, Void>
    {
        @Override
        protected Void doInBackground(ContentValues[]... data) {
            getContentResolver().bulkInsert(LightData.CONTENT_URI, data[0]);
            return null;
        }
    }

    //计算统计之前收集到的数据，以频率的形式进行。
    public static int getFrequency(Context context)
    {
        int hz = 0;
        String[] columns = new String[]{ "count(*) as frequency", "datetime("+ LightData.TIMESTAMP+"/1000, 'unixepoch','localtime') as sample_time" };
        Cursor qry = context.getContentResolver().query(LightData.CONTENT_URI, columns, "1) group by (sample_time", null, "sample_time DESC LIMIT 1 OFFSET 2");
        if( qry != null && qry.moveToFirst() ) {
            hz = qry.getInt(0);
        }
        if( qry != null && ! qry.isClosed() ) qry.close();
        return hz;
    }

    private void saveSensorDevice(Sensor sensor)
    {
        Cursor sensorInfo = getContentResolver().query(Light_Device.CONTENT_URI, null, null, null, null);
        if( sensorInfo == null || ! sensorInfo.moveToFirst() ) {
            ContentValues rowData = new ContentValues();
            rowData.put(Light_Device.DEVICE_ID, Core_Activity.getSetting(
                    getApplicationContext(), MainActivity.DEVICE_ID));
            rowData.put(Light_Device.TIMESTAMP, System.currentTimeMillis());
            rowData.put(Light_Device.MAXIMUM_RANGE, sensor.getMaximumRange());//最大范围
            rowData.put(Light_Device.MINIMUM_DELAY, sensor.getMinDelay());//
            rowData.put(Light_Device.NAME, sensor.getName());//名称
            rowData.put(Light_Device.POWER_MA, sensor.getPower());//耗电量
            rowData.put(Light_Device.RESOLUTION, sensor.getResolution());//精度值
            rowData.put(Light_Device.TYPE, sensor.getType());//类型
            rowData.put(Light_Device.VENDOR, sensor.getVendor());//供应商
            rowData.put(Light_Device.VERSION, sensor.getVersion());//版本

//测试通过到这里
//            Toast.makeText(this, "Light_Activity.saveSensorDevice():准备存储光感数据\n"


            try {
                if( Core_Activity.getSetting(getApplicationContext(),
                        MainActivity.DEBUG_DB_SLOW).equals("false") )
                {
                    //这里直接插入数据，会不会调用Light_Data
                    Toast.makeText(fullContext,"Light_Activity.saveSensorDevice:\n      if(DEBUG_DB_SLOW==false) " +
                            "\n  开始调用数据插入语句", Toast.LENGTH_LONG).show();
/**没有执行到这里来，没有直接执行插入操作，可能是上面的判断问题*/
                    getContentResolver().insert(Light_Device.CONTENT_URI, rowData);
                }
                else
                {
                    Toast.makeText(fullContext,"Light_Activity.saveSensorDevice:\n     " +
                                    " if(DEBUG_DB_SLOW).equals(false) " +
                                    "\n  此判断的值："+
                                    Core_Activity.getSetting(getApplicationContext(),
                                            MainActivity.DEBUG_DB_SLOW)
                            , Toast.LENGTH_LONG).show();
                }

                Intent light_dev = new Intent(ACTION_LIGHT);
                light_dev.putExtra(EXTRA_SENSOR, rowData);

                Toast.makeText(fullContext,"Light_Activity.saveSensorDevice:\n " +
                                "开始发送广播通知: " +
                                "\n  sendBroadcast(light_dev), 其中包含rowData数据"
                        , Toast.LENGTH_LONG).show();

                sendBroadcast(light_dev);

                if( Core_Activity.DEBUG ) Log.d(TAG, "Light sensor info: "+ rowData.toString());
            }catch( SQLiteException e ) {
                if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());

                Toast.makeText(fullContext,"Light_Activity.saveSensorDevice:\n " +
                                "捕捉到:SQLiteException " +
                                "\n  手机数据库有误：\n"+e.getMessage()
                        , Toast.LENGTH_LONG).show();

            }catch( SQLException e ) {

                Toast.makeText(fullContext,"Light_Activity.saveSensorDevice:\n " +
                                "SQLException " +
                                "\n  数据库执行有误：\n"+e.getMessage()
                        , Toast.LENGTH_LONG).show();

                if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
            }
        }else
        {
            Toast.makeText(fullContext,"Light_Activity.saveSensorDevice:\n " +
                            "   if(DEBUG_DB_SLOW==false)        else::::::::: " +
                            "\n  开始关闭sensorInfo："
                            +sensorInfo.toString()
                    , Toast.LENGTH_LONG).show();

            sensorInfo.close();
        }
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        fullContext=getApplicationContext();

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);

        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        TAG = Core_Activity.getSetting(getApplicationContext(),
                MainActivity.DEBUG_TAG).length()>0
                ? Core_Activity.getSetting(getApplicationContext(),
                MainActivity.DEBUG_TAG)
                :TAG;
        if(Core_Activity.getSetting(this, MainActivity.FREQUENCY_LIGHT).length() > 0 ) {
            SAMPLING_RATE = Integer.parseInt(Core_Activity.getSetting(
                    getApplicationContext(), MainActivity.FREQUENCY_LIGHT));
        } else {
            Core_Activity.setSetting(getApplicationContext(),
                    MainActivity.FREQUENCY_LIGHT, SAMPLING_RATE);
        }

        sensorThread = new HandlerThread(TAG);
        sensorThread.start();

        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wakeLock.acquire();

        sensorHandler = new Handler(sensorThread.getLooper());
        mSensorManager.registerListener(this, mLight, SAMPLING_RATE, sensorHandler);

        DATABASE_TABLES = Light_Data.DATABASE_TABLES;
        TABLES_FIELDS = Light_Data.TABLES_FIELDS;
        CONTEXT_URIS = new Uri[]{ Light_Device.CONTENT_URI, LightData.CONTENT_URI };

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_LIGHT_LABEL);
        registerReceiver(dataLabeler, filter);

        if(mLight == null) {
            if(Core_Activity.DEBUG) Log.w(TAG,"This device does not have a light sensor!");
            Toast.makeText(fullContext,"In Light_Activity.onCreate:if(mLight==null)   Not have a light sensor!", Toast.LENGTH_LONG).show();

            Core_Activity.setSetting(this, MainActivity.STATUS_LIGHT, false);
            stopSelf();
            return;
        }
        else {

            Toast.makeText(fullContext, "In Light_Activity.onCreate:开始调用saveSensorDevice。其值为："+mLight.getResolution(), Toast.LENGTH_LONG).show();
/**在这里将初始化得到的整个传感器数据传到这个函数执行*/
            saveSensorDevice(mLight);
        }
        if(Core_Activity.DEBUG) {
            Log.d(TAG, "Light service created!");
            Toast.makeText(fullContext, "In Light_Activity.onCreate:Light service created!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        sensorHandler.removeCallbacksAndMessages(null);
        mSensorManager.unregisterListener(this, mLight);
        sensorThread.quit();

        wakeLock.release();

        unregisterReceiver(dataLabeler);

        if(Core_Activity.DEBUG) Log.d(TAG,"Light service terminated...");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {

        TAG = Core_Activity.getSetting(getApplicationContext(),
                MainActivity.DEBUG_TAG).length()>0
                ? Core_Activity.getSetting(getApplicationContext(),
                MainActivity.DEBUG_TAG)
                :TAG;

        if( Core_Activity.getSetting(this,
                MainActivity.FREQUENCY_LIGHT).length() == 0 ) {
            Core_Activity.setSetting(this,
                    MainActivity.FREQUENCY_LIGHT, SAMPLING_RATE);
        }

        if(SAMPLING_RATE != Integer.parseInt(Core_Activity.getSetting(
                getApplicationContext(), MainActivity.FREQUENCY_LIGHT))) {
            SAMPLING_RATE = Integer.parseInt(Core_Activity.getSetting(
                    getApplicationContext(), MainActivity.FREQUENCY_LIGHT));
            sensorHandler.removeCallbacksAndMessages(null);
            mSensorManager.unregisterListener(this, mLight);
            mSensorManager.registerListener(this, mLight, SAMPLING_RATE, sensorHandler);
        }
        if(Core_Activity.DEBUG) Log.d(TAG,"Light service active...");

        return START_STICKY;
    }

    //将本服务单例化
    private static Light_Activity lightSrv = Light_Activity.getService();

    //获取单例化的光感服务
    public static Light_Activity getService()
    {
        if( lightSrv == null ) lightSrv = new Light_Activity();
        return lightSrv;
    }

    private final IBinder serviceBinder = new ServiceBinder();
    public class ServiceBinder extends Binder
    {
        Light_Activity getService() {
            return Light_Activity.getService();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

}
