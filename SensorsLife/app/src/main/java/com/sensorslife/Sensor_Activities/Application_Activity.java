package com.sensorslife.Sensor_Activities;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import com.sensorslife.CommonUtils.Encrypter;
import com.sensorslife.Core_Activity;
import com.sensorslife.Data_Managers.Applications_Data;
import com.sensorslife.Data_Managers.Applications_Data.Applications_Foreground;
import com.sensorslife.Data_Managers.Applications_Data.Applications_History;
import com.sensorslife.Data_Managers.Applications_Data.Applications_Notifications;
import com.sensorslife.Data_Managers.Applications_Data.Applications_Crashes;
import com.sensorslife.MainActivity;
import com.sensorslife.R;

import java.util.List;

/**
 * Created by Administrator on 2017/8/27.
 */

public class Application_Activity extends AccessibilityService {

    private static String TAG = "Applications_Activity";

    private static AlarmManager alarmManager = null;
    private static Intent updateApps = null;
    private static PendingIntent repeatingIntent = null;
    public static final int ACCESSIBILITY_NOTIFICATION_ID = 42;

//广播：有新的应用程序运行在前台
    public static final String ACTION_AWARE_APPLICATIONS_FOREGROUND = "ACTION_AWARE_APPLICATIONS_FOREGROUND";

//广播：有新的应用程序或进程 运行在前台或后台
    public static final String ACTION_AWARE_APPLICATIONS_HISTORY = "ACTION_AWARE_APPLICATIONS_HISTORY";

//广播：有新的应用通知
    public static final String ACTION_AWARE_APPLICATIONS_NOTIFICATIONS = "ACTION_AWARE_APPLICATIONS_NOTIFICATIONS";

//广播：有新的应用程序崩溃了
    public static final String ACTION_AWARE_APPLICATIONS_CRASHES = "ACTION_AWARE_APPLICATIONS_CRASHES";

//根据包名，获取设备的应用程序标签
    private String getApplicationName(String package_name ) {
        PackageManager packageManager = getPackageManager();
        ApplicationInfo appInfo = null;
        try {
            appInfo = packageManager.getApplicationInfo(package_name, PackageManager.GET_ACTIVITIES);
        } catch( final PackageManager.NameNotFoundException e ) {
            appInfo = null;
        }
        String appName = ( appInfo != null ) ? (String) packageManager.getApplicationLabel(appInfo):"";
        return appName;
    }

//监听活动：界面的变化，通知的变化
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if( Core_Activity.getSetting(getApplicationContext(), MainActivity.STATUS_NOTIFICATIONS).equals("true") && event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED ) {

            Notification notificationDetails = (Notification) event.getParcelableData();

            if( notificationDetails != null ) {
                ContentValues rowData = new ContentValues();
                rowData.put(Applications_Notifications.DEVICE_ID, Core_Activity.getSetting(getApplicationContext(), MainActivity.DEVICE_ID));
                rowData.put(Applications_Notifications.TIMESTAMP, System.currentTimeMillis());
                rowData.put(Applications_Notifications.PACKAGE_NAME, event.getPackageName().toString());
                rowData.put(Applications_Notifications.APPLICATION_NAME, getApplicationName(event.getPackageName().toString()));
                rowData.put(Applications_Notifications.TEXT, Encrypter.hashSHA1(event.getText().toString()));
                rowData.put(Applications_Notifications.SOUND, (( notificationDetails.sound != null ) ? notificationDetails.sound.toString() : "") );
                rowData.put(Applications_Notifications.VIBRATE, (( notificationDetails.vibrate != null) ? notificationDetails.vibrate.toString() : "") );
                rowData.put(Applications_Notifications.DEFAULTS, notificationDetails.defaults );
                rowData.put(Applications_Notifications.FLAGS, notificationDetails.flags );

                if(Core_Activity.DEBUG) Log.d(TAG, "New notification:" + rowData.toString() );

                getContentResolver().insert(Applications_Notifications.CONTENT_URI, rowData);
                Intent notification = new Intent(ACTION_AWARE_APPLICATIONS_NOTIFICATIONS);
                sendBroadcast(notification);
            }
        }

        if( Core_Activity.getSetting(getApplicationContext(), MainActivity.STATUS_APPLICATIONS).equals("true") && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ) {
            if( updateApps == null ) {
                updateApps = new Intent(getApplicationContext(), BackgroundService.class);
                updateApps.setAction(ACTION_AWARE_APPLICATIONS_HISTORY);
                repeatingIntent = PendingIntent.getService(getApplicationContext(), 0, updateApps, 0);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+1000, Integer.parseInt(Core_Activity.getSetting(getApplicationContext(), MainActivity.FREQUENCY_APPLICATIONS)) * 1000, repeatingIntent);
            }

            PackageManager packageManager = getPackageManager();

            ApplicationInfo appInfo;
            try {
                appInfo = packageManager.getApplicationInfo(event.getPackageName().toString(), PackageManager.GET_ACTIVITIES);
            } catch( final PackageManager.NameNotFoundException e ) {
                appInfo = null;
            }

            PackageInfo pkgInfo;
            try {
                pkgInfo = packageManager.getPackageInfo(event.getPackageName().toString(), PackageManager.GET_META_DATA);
            } catch (PackageManager.NameNotFoundException e ) {
                pkgInfo = null;
            }

            String appName = ( appInfo != null ) ? (String) packageManager.getApplicationLabel(appInfo):"";

            ContentValues rowData = new ContentValues();
            rowData.put(Applications_Foreground.TIMESTAMP, System.currentTimeMillis());
            rowData.put(Applications_Foreground.DEVICE_ID, Core_Activity.getSetting(getApplicationContext(), MainActivity.DEVICE_ID));
            rowData.put(Applications_Foreground.PACKAGE_NAME, event.getPackageName().toString());
            rowData.put(Applications_Foreground.APPLICATION_NAME, appName);
            rowData.put(Applications_Foreground.IS_SYSTEM_APP, ( pkgInfo != null ) ? isSystemPackage(pkgInfo) : false );

            if( Core_Activity.DEBUG ) Log.d(Core_Activity.TAG, "FOREGROUND: " + rowData.toString());

            try{
                getContentResolver().insert(Applications_Foreground.CONTENT_URI, rowData);
            }catch( SQLiteException e ) {
                if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
            }catch( SQLException e ) {
                if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
            }

            Intent newForeground = new Intent(ACTION_AWARE_APPLICATIONS_FOREGROUND);
            sendBroadcast(newForeground);

            if( Core_Activity.getSetting(getApplicationContext(), MainActivity.STATUS_CRASHES).equals("true") ) {
                //Check if there is a crashed application
                ActivityManager activityMng = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                List<ActivityManager.ProcessErrorStateInfo> errors = activityMng.getProcessesInErrorState();
                if(errors != null ) {
                    for(ActivityManager.ProcessErrorStateInfo error : errors ) {

                        try {
                            pkgInfo = packageManager.getPackageInfo(error.processName, PackageManager.GET_META_DATA);
                            appInfo = packageManager.getApplicationInfo(event.getPackageName().toString(), PackageManager.GET_ACTIVITIES);
                            appName = ( appInfo != null ) ? (String) packageManager.getApplicationLabel(appInfo):"";

                            ContentValues crashData = new ContentValues();
                            crashData.put(Applications_Crashes.TIMESTAMP, System.currentTimeMillis());
                            crashData.put(Applications_Crashes.DEVICE_ID, Core_Activity.getSetting(getApplicationContext(), MainActivity.DEVICE_ID));
                            crashData.put(Applications_Crashes.PACKAGE_NAME, error.processName);
                            crashData.put(Applications_Crashes.APPLICATION_NAME, appName);
                            crashData.put(Applications_Crashes.APPLICATION_VERSION, pkgInfo.versionCode);
                            crashData.put(Applications_Crashes.ERROR_SHORT, error.shortMsg);
                            crashData.put(Applications_Crashes.ERROR_LONG, error.longMsg);
                            crashData.put(Applications_Crashes.ERROR_CONDITION, error.condition);
                            crashData.put(Applications_Crashes.IS_SYSTEM_APP, isSystemPackage(pkgInfo));

                            getContentResolver().insert(Applications_Crashes.CONTENT_URI, crashData);

                            if( Core_Activity.DEBUG ) Log.d(Core_Activity.TAG, "Crashed: " + crashData.toString());

                            Intent crashed = new Intent(ACTION_AWARE_APPLICATIONS_CRASHES);
                            sendBroadcast(crashed);
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            Intent backgroundService = new Intent(this, BackgroundService.class);
            backgroundService.setAction(ACTION_AWARE_APPLICATIONS_HISTORY);
            startService(backgroundService);
        }

//这里不记录键盘的使用情况
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        if( Core_Activity.DEBUG ) Log.d("Core_Activity","Core service connected to accessibility services...");

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        TAG = Core_Activity.getSetting(getApplicationContext(), MainActivity.DEBUG_TAG).length()>0? Core_Activity.getSetting(getApplicationContext(), MainActivity.DEBUG_TAG):TAG;
/*
        IntentFilter filter = new IntentFilter();
        filter.addAction(Core_Activity.ACTION_AWARE_SYNC_DATA);
        filter.addAction(Core_Activity.ACTION_AWARE_CLEAR_DATA);
        registerReceiver(awareMonitor, filter);
*/
        if( Core_Activity.getSetting(getApplicationContext(), MainActivity.FREQUENCY_APPLICATIONS).length() == 0 ) {
            Core_Activity.setSetting(getApplicationContext(), MainActivity.FREQUENCY_APPLICATIONS, 30);
        }

        if( Core_Activity.getSetting(getApplicationContext(), MainActivity.STATUS_APPLICATIONS).equals("true") ) {
            updateApps = new Intent(getApplicationContext(), BackgroundService.class);
            updateApps.setAction(ACTION_AWARE_APPLICATIONS_HISTORY);
            repeatingIntent = PendingIntent.getService(getApplicationContext(), 0, updateApps, 0);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+1000, Integer.parseInt(Core_Activity.getSetting(getApplicationContext(), MainActivity.FREQUENCY_APPLICATIONS)) * 1000, repeatingIntent);
        }

        //重启核心服务，即主服务
        Intent aware = new Intent(this, Core_Activity.class);
        startService(aware);
    }

    @Override
    public void onInterrupt() {
        Log.e(TAG,"Accessibility Service has been interrupted...");
    }

    @Override
    public void onCreate() {
        super.onCreate();
/*
        IntentFilter filter = new IntentFilter();
        filter.addAction(Core_Activity.ACTION_AWARE_SYNC_DATA);
        filter.addAction(Core_Activity.ACTION_AWARE_CLEAR_DATA);
        registerReceiver(awareMonitor, filter);
*/
        if( Core_Activity.getSetting(getApplicationContext(), MainActivity.FREQUENCY_APPLICATIONS).length() == 0 ) {
            Core_Activity.setSetting(getApplicationContext(), MainActivity.FREQUENCY_APPLICATIONS, 30);
        }

        //重启核心服务，即主服务
        Intent aware = new Intent(this, Core_Activity.class);
        startService(aware);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        if( Core_Activity.getSetting(getApplicationContext(), MainActivity.FREQUENCY_APPLICATIONS).length() == 0 ) {
            Core_Activity.setSetting(getApplicationContext(), MainActivity.FREQUENCY_APPLICATIONS, 30);
        }

        if( Core_Activity.getSetting(getApplicationContext(), MainActivity.STATUS_APPLICATIONS).equals("true") ) {
            updateApps = new Intent(getApplicationContext(), BackgroundService.class);
            updateApps.setAction(ACTION_AWARE_APPLICATIONS_HISTORY);
            repeatingIntent = PendingIntent.getService(getApplicationContext(), 0, updateApps, 0);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+1000, Integer.parseInt(Core_Activity.getSetting(getApplicationContext(), MainActivity.FREQUENCY_APPLICATIONS)) * 1000, repeatingIntent);
        }

        if( ! isAccessibilityServiceActive( getApplicationContext() ) ) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
            mBuilder.setSmallIcon(R.drawable.accessibility);
            mBuilder.setContentTitle("传感器生活的配置");
            mBuilder.setContentText("在辅助服务中激活本软件");
            mBuilder.setDefaults(Notification.DEFAULT_ALL);
            mBuilder.setAutoCancel(true);

            Intent accessibilitySettings = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
            accessibilitySettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent clickIntent = PendingIntent.getActivity(getApplicationContext(), 0, accessibilitySettings, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(clickIntent);
            NotificationManager notManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notManager.notify(Application_Activity.ACCESSIBILITY_NOTIFICATION_ID, mBuilder.build());
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Core_Activity.setSetting(getApplicationContext(), MainActivity.STATUS_APPLICATIONS, false);
        alarmManager.cancel(repeatingIntent);
        //unregisterReceiver(awareMonitor);
    }

//检查 额外服务 是否已启用
    public static boolean isAccessibilityServiceActive(Context c) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) c.getSystemService(ACCESSIBILITY_SERVICE);
        if( accessibilityManager.isEnabled() ) {
            List<ServiceInfo> accessibilityServices = accessibilityManager.getAccessibilityServiceList();
            for( ServiceInfo s : accessibilityServices ) {
                if( s.name.equalsIgnoreCase("com.sensorslife.Sensor_Activities.Application_Activity") ) {
                    return true;
                }
            }
        }
        return false;
    }

//接收软件的广播：清理数据，同步数据
//    private final Applications_Broadcaster awareMonitor = new Applications_Broadcaster();

    /**
、、后台服务：更新运行程序或服务的数据，将数据上传至网络

     */
    public static class BackgroundService extends IntentService {
        public BackgroundService() {
            super(TAG+" background service");
        }

        @Override
        protected void onHandleIntent(Intent intent) {

            //更新正运行的程序或服务在数据库中的数据
            if( Core_Activity.getSetting(getApplicationContext(), MainActivity.STATUS_APPLICATIONS ).equals("true") && intent.getAction().equals(ACTION_AWARE_APPLICATIONS_HISTORY) ) {

                ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                PackageManager packageManager = (PackageManager) getPackageManager();
                List<ActivityManager.RunningAppProcessInfo> runningApps = activityManager.getRunningAppProcesses();

                if(Core_Activity.DEBUG) Log.d(TAG,"Running " + runningApps.size() + " applications");

                for( ActivityManager.RunningAppProcessInfo app : runningApps ) {

                    Cursor appUnclosed = null;

                    try {
                        PackageInfo appPkg = packageManager.getPackageInfo(app.processName, PackageManager.GET_META_DATA);
                        ApplicationInfo appInfo = packageManager.getApplicationInfo(app.processName, PackageManager.GET_ACTIVITIES);

                        String appName = ( appInfo != null ) ? (String) packageManager.getApplicationLabel(appInfo):"";

                        appUnclosed = getContentResolver().query(Applications_History.CONTENT_URI, null, Applications_History.PACKAGE_NAME + " LIKE '%"+app.processName+"%' AND "+ Applications_History.PROCESS_ID + "=" +app.pid + " AND " + Applications_History.END_TIMESTAMP +"=0", null, null);
                        if( appUnclosed == null || appUnclosed.moveToFirst() == false ) {
                            ContentValues rowData = new ContentValues();
                            rowData.put(Applications_History.TIMESTAMP, System.currentTimeMillis());
                            rowData.put(Applications_History.DEVICE_ID, Core_Activity.getSetting(getApplicationContext(), MainActivity.DEVICE_ID));
                            rowData.put(Applications_History.PACKAGE_NAME, app.processName);
                            rowData.put(Applications_History.APPLICATION_NAME, appName);
                            rowData.put(Applications_History.PROCESS_IMPORTANCE, app.importance);
                            rowData.put(Applications_History.PROCESS_ID, app.pid);
                            rowData.put(Applications_History.END_TIMESTAMP, 0);
                            rowData.put(Applications_History.IS_SYSTEM_APP, isSystemPackage(appPkg));
                            try {
                                getContentResolver().insert(Applications_History.CONTENT_URI, rowData);
                            }catch( SQLiteException e ) {
                                if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
                            }catch( SQLException e ) {
                                if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
                            }
                        } else if( appUnclosed.getInt(appUnclosed.getColumnIndex(Applications_History.PROCESS_IMPORTANCE)) != app.importance ) {
                            //Close last importance
                            ContentValues rowData = new ContentValues();
                            rowData.put(Applications_History.END_TIMESTAMP, System.currentTimeMillis());
                            try {
                                getContentResolver().update(Applications_History.CONTENT_URI, rowData, Applications_History._ID + "="+ appUnclosed.getInt(appUnclosed.getColumnIndex(Applications_History._ID)), null);
                            }catch( SQLiteException e ) {
                                if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
                            }catch( SQLException e) {
                                if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
                            }

                            if( appUnclosed != null && ! appUnclosed.isClosed() ) appUnclosed.close();

                            //Insert new importance
                            rowData = new ContentValues();
                            rowData.put(Applications_History.TIMESTAMP, System.currentTimeMillis());
                            rowData.put(Applications_History.DEVICE_ID, Core_Activity.getSetting(getApplicationContext(), MainActivity.DEVICE_ID));
                            rowData.put(Applications_History.PACKAGE_NAME, app.processName);
                            rowData.put(Applications_History.APPLICATION_NAME, appName);
                            rowData.put(Applications_History.PROCESS_IMPORTANCE, app.importance);
                            rowData.put(Applications_History.PROCESS_ID, app.pid);
                            rowData.put(Applications_History.END_TIMESTAMP, 0);
                            rowData.put(Applications_History.IS_SYSTEM_APP, isSystemPackage(appPkg));
                            try {
                                getContentResolver().insert(Applications_History.CONTENT_URI, rowData);
                            }catch( SQLiteException e ) {
                                if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
                            }catch( SQLException e ) {
                                if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
                            }
                        }
                    }catch(PackageManager.NameNotFoundException e) {
                    }catch( IllegalStateException e ) {
                    } finally {
                        if( appUnclosed != null && ! appUnclosed.isClosed() ) appUnclosed.close();
                    }
                }

                //关闭那些没有运行的程序
                Cursor appsOpened = getContentResolver().query(Applications_History.CONTENT_URI, null, Applications_History.END_TIMESTAMP+"=0", null, null);
                try {
                    if(appsOpened != null && appsOpened.moveToFirst() ) {
                        do{
                            if( exists(runningApps, appsOpened) == false ) {
                                ContentValues rowData = new ContentValues();
                                rowData.put(Applications_History.END_TIMESTAMP, System.currentTimeMillis());
                                try {
                                    getContentResolver().update(Applications_History.CONTENT_URI, rowData, Applications_History._ID + "="+ appsOpened.getInt(appsOpened.getColumnIndex(Applications_History._ID)), null);
                                }catch( SQLiteException e ) {
                                    if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
                                }catch( SQLException e) {
                                    if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
                                }
                            }
                        } while(appsOpened.moveToNext());
                    }
                }catch(IllegalStateException e) {
                    if(Core_Activity.DEBUG) Log.e(TAG,e.toString());
                }finally{
                    if(appsOpened != null && ! appsOpened.isClosed() ) appsOpened.close();
                }

                Intent statsUpdated = new Intent(ACTION_AWARE_APPLICATIONS_HISTORY);
                sendBroadcast(statsUpdated);
            }
        }

//检查数据库中的应用程序，是否还在运行
        private boolean exists(List<ActivityManager.RunningAppProcessInfo> running, Cursor dbApp)
        {
            for( ActivityManager.RunningAppProcessInfo app : running ) {
                if(dbApp.getString(dbApp.getColumnIndexOrThrow(Applications_History.PACKAGE_NAME)).equals(app.processName) &&
                        dbApp.getInt(dbApp.getColumnIndexOrThrow(Applications_History.PROCESS_IMPORTANCE)) == app.importance &&
                        dbApp.getInt(dbApp.getColumnIndexOrThrow(Applications_History.PROCESS_ID)) == app.pid) {
                    return true;
                }
            }
            return false;
        }
    }

//检查一个应用程序是否是系统自带的
    private static boolean isSystemPackage(PackageInfo pkgInfo)
    {
        if( pkgInfo == null ) return false;
        return ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1);
    }
}
