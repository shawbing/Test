package com.sensorslife.CommonUtils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import com.sensorslife.Core_Activity;
import com.sensorslife.MainActivity;

/**
 * Created by Administrator on 2017/8/25.
 *
 * 核心类Core_Activity的扩展，用于整合软件的框架结构
 */

public class Extend_Core extends Service {

    //
    //
//为这个传感器设定一个调试标签
    public static String TAG = "Extend_Core";

    //为这个传感器设定 调试标记
    public static boolean DEBUG = false;

    public ContextProducer CONTEXT_PRODUCER = null;

    //传感器的数据表 用一个字符串组来表示
    public String[] DATABASE_TABLES = null;

    //数据表的列名
    public String[] TABLES_FIELDS = null;

    //信息内容提供者的统一标记（字符）
    public Uri[] CONTEXT_URIS = null;

    //设定传感器不能用的值为0
    public static final int STATUS_SENSOR_OFF = 0;

    //用 1 来表示传感器处于可用的状态
    public static final int STATUS_SENSOR_ON = 1;

    /**
     * 定义一个接口，将内容分享给其他应用和插件（addons）
     * 一定要在这里把内容广播出去
     */
    public interface ContextProducer {   public void onContext();    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        TAG = Core_Activity.getSetting(getApplicationContext(),
                MainActivity.DEBUG_TAG).length()>0
                ? Core_Activity.getSetting(getApplicationContext(),
                MainActivity.DEBUG_TAG)
                :TAG;
        DEBUG = Core_Activity.getSetting(getApplicationContext(),
                MainActivity.DEBUG_FLAG).equals("true")
                ? true
                : false;

        if( DEBUG ) Log.d(TAG, TAG + " sensor created!");

        //注册内容广播器，这个广播器中包含如下动作
        IntentFilter filter = new IntentFilter();
        filter.addAction(Core_Activity.ACTION_SHARE_CURRENT_CONTEXT);//分享当前内容状态信息

        filter.addAction(Core_Activity.ACTION_SHARE_CLEAR_DATA);///清理数据
        filter.addAction(Core_Activity.ACTION_SHARE_STOP_SENSORS);//停止传感器服务
        filter.addAction(Core_Activity.ACTION_SHARE_SPACE_MAINTENANCE);//空间整理（维护）
        registerReceiver(contextBroadcaster, filter);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        //取消广播注册
        unregisterReceiver(contextBroadcaster);
        //并注明本传感器已经结束了
        if(DEBUG) Log.d(TAG, TAG + " sensor terminated...");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        TAG = Core_Activity.getSetting(getApplicationContext(),
                MainActivity.DEBUG_TAG).length()>0
                ? Core_Activity.getSetting(getApplicationContext(),
                MainActivity.DEBUG_TAG)
                :TAG;
        DEBUG = Core_Activity.getSetting(getApplicationContext(),
                MainActivity.DEBUG_FLAG).equals("true")
                ?true
                :false;
        if(DEBUG) Log.d(TAG, TAG + " sensor active...");
        return START_STICKY;
    }

    /**
     * 本软件的内容广播 接收器，用于接收从其他类传出的广播
     *  ACTION_AWARE_CURRENT_CONTEXT: 返回当前插件的环境参数
     * 不需要 ACTION_AWARE_SYNC_DATA:        远程将 内容提供者的数据 上传
     * 不需要 ACTION_AWARE_CLEAR_DATA:       清理本地和远程数据库
     *  ACTION_AWARE_STOP_SENSORS:     将传感器服务停止
     * 不需要 ACTION_AWARE_SPACE_MAINTENANCE: 清理内容提供者的历史数据
     */
    public class ContextBroadcaster extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            if( intent.getAction().equals(Core_Activity.ACTION_SHARE_CURRENT_CONTEXT) ) {
                if( CONTEXT_PRODUCER != null )
                    CONTEXT_PRODUCER.onContext();
            }
//            if( intent.getAction().equals(Share.ACTION_SHARE_SYNC_DATA)

//            if( intent.getAction().equals(Share.ACTION_SHARE_CLEAR_DATA))

            if(intent.getAction().equals(Core_Activity.ACTION_SHARE_STOP_SENSORS)) {
                if( Core_Activity.DEBUG ) Log.d(TAG, TAG + " stopped");
                stopSelf();
            }

//            String frequency_old = Aware.getSetting(getApplicationContext(), Aware_Preferences.FREQUENCY_CLEAN_OLD_DATA);
        }
    }
    private ContextBroadcaster contextBroadcaster = new ContextBroadcaster();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
