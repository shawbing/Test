package com.sensorslife.Sensor_Activities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.sensorslife.CommonUtils.Extend_Core;
import com.sensorslife.Core_Activity;
import com.sensorslife.Data_Managers.Location_Data;
import com.sensorslife.Data_Managers.Location_Data.LocationsData;
import com.sensorslife.MainActivity;

/**
 * Created by Administrator on 2017/8/25.
 *
 * 处理逻辑：监听定位信息的变化，比较哪个定位更优，获取数据的服务
 */

public class Location_Activity extends Extend_Core implements LocationListener {
    //

    private static LocationManager locationManager = null;

    //这个监听主要用于跟踪 定位获取失败
    private final GpsStatus.Listener gps_status_listener = new GpsStatus.Listener()
    {
        @Override
        public void onGpsStatusChanged(int event) {
            switch(event) {
                case GpsStatus.GPS_EVENT_FIRST_FIX://第一次修正
                    break;
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS://卫星状态
                    break;
                case GpsStatus.GPS_EVENT_STARTED://刚开始
                    break;
                case GpsStatus.GPS_EVENT_STOPPED://停止
                    //保存定位信息，这个信息可能来自GPS，也有可能来自网络
                    //这同时包含了：GPS服务停止运行 和 定位信息没得到修正 这两种情况
                    Location lastGPS = locationManager.getLastKnownLocation(
                            LocationManager.GPS_PROVIDER);

                    Location lastNetwork = null;
                    //对网络提供服务，进行简单快速的检查
                    if( locationManager.getProvider(LocationManager.NETWORK_PROVIDER) != null)
                    {
                        locationManager.requestSingleUpdate(
                                LocationManager.NETWORK_PROVIDER,
                                Location_Activity.this, getMainLooper());
                        lastNetwork = locationManager.getLastKnownLocation(
                                LocationManager.NETWORK_PROVIDER);
                    }
                    //找出定位最好的定位信息
                    Location bestLocation = null;
                    if(isBetterLocation(lastNetwork, lastGPS)) {
                        bestLocation = lastNetwork;
                    }else{
                        bestLocation = lastGPS;
                    }

                    ContentValues rowData = new ContentValues();
                    rowData.put(LocationsData.TIMESTAMP, System.currentTimeMillis());
                    rowData.put(LocationsData.DEVICE_ID,
                            Core_Activity.getSetting(getApplicationContext(),
                                    MainActivity.DEVICE_ID));
                    rowData.put(LocationsData.LATITUDE, bestLocation.getLatitude());
                    rowData.put(LocationsData.LONGITUDE, bestLocation.getLongitude());
                    rowData.put(LocationsData.BEARING, bestLocation.getBearing());
                    rowData.put(LocationsData.SPEED, bestLocation.getSpeed());
                    rowData.put(LocationsData.ALTITUDE, bestLocation.getAltitude());
                    rowData.put(LocationsData.PROVIDER, bestLocation.getProvider());
                    rowData.put(LocationsData.ACCURACY, bestLocation.getAccuracy());

                    try {
                        getContentResolver().insert(LocationsData.CONTENT_URI, rowData);
                    }catch( SQLiteException e ) {
                        if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
                    }catch( SQLException e ) {
                        if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
                    }

                    Intent locationEvent = new Intent(ACTION_SHARE_LOCATIONS);
                    sendBroadcast(locationEvent);

                    break;
            }
        }
    };

    //广播通知：你有新的定位信息可用
    public static final String ACTION_SHARE_LOCATIONS = "ACTION_SHARE_LOCATIONS";

    //广播通知：GPS定位服务可以用了
    public static final String ACTION_SHARE_GPS_LOCATION_ENABLED = "ACTION_SHARE_GPS_LOCATION_ENABLED";

    //广播通知：网路定位服务可以用了
    public static final String ACTION_SHARE_NETWORK_LOCATION_ENABLED = "ACTION_SHARE_NETWORK_LOCATION_ENABLED";

    //广播通知：GPS定位服务不能用
    public static final String ACTION_SHARE_GPS_LOCATION_DISABLED = "ACTION_SHARE_GPS_LOCATION_DISABLED";

    //广播通知：网络定位服务不能用
    public static final String ACTION_SHARE_NETWORK_LOCATION_DISABLED = "ACTION_SHARE_NETWORK_LOCATION_DISABLED";

    //默认每隔3分钟更新一次GPS定位信息，而0表示进行实时更新
    public static int UPDATE_TIME_GPS = 180;

    //默认每隔5分钟更新一次网络定位信息，同样0表示实时更新
    public static int UPDATE_TIME_NETWORK = 300;

    //GPS定位的最小精度为150米
    public static int UPDATE_DISTANCE_GPS = 150;

    //网络定位的最小精度为1500米
    public static int UPDATE_DISTANCE_NETWORK = 1500;

    //最好的定位信息 默认在5分钟以内是有效的
    public static int EXPIRATION_TIME = 300;

    private static Location_Activity locationSrv = Location_Activity.getService();
    //将定位服务设置为单例模式。。其返回值为：定位这个对象
//将定位服务设置为单例模式。。其返回值为：定位这个对象
// 这种设置方式有点不懂？？？？？？？？？？？
    public static Location_Activity getService()
    {
        if(locationSrv == null) locationSrv = new Location_Activity();
        return locationSrv;
    }
    //绑定服务
    private LocationBinder locationBinder = new LocationBinder();
    public class LocationBinder extends Binder {
        public Location_Activity getService() {
            return Location_Activity.getService();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return locationBinder;
    }

    //评估新获取到的定位信息，比较上次的定位和新的定位信息，哪个更好
    private boolean isBetterLocation(Location newLocation, Location lastLocation)
    {
        if( newLocation != null && lastLocation == null) {
            return true;
        }

        if( lastLocation != null && newLocation == null ) {
            return false;
        }
//先比较时间的先后
        long timeDelta = newLocation.getTime() - lastLocation.getTime();
        //典型的新的定位信息
        boolean isSignificantlyNewer = timeDelta > 1000 * EXPIRATION_TIME;
        //典型的旧的定位信息
        boolean isSignificantlyOlder = timeDelta < -( 1000 * EXPIRATION_TIME );
        boolean isNewer = timeDelta > 0;

        if (isSignificantlyNewer) {
            return true;
        } else if (isSignificantlyOlder) {
            return false;
        }
//再比较精确度的大小
        int accuracyDelta = (int) (newLocation.getAccuracy() - lastLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;
        boolean isFromSameProvider = isSameProvider(newLocation.getProvider(),lastLocation.getProvider());

        if (isMoreAccurate) {
            return true;    //非典型的新定位，就需要结合精度判断
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    // 检查两个提供者是否一样


    private static boolean isSameProvider(String provider1, String provider2)
    {
        if (provider1 == null) {
            return provider2 == null;       //返回的是一个判断
        }
        return provider1.equals(provider2);
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        TAG = Core_Activity.getSetting(getApplicationContext(),
                MainActivity.DEBUG_TAG).length()>0
                ? Core_Activity.getSetting(getApplicationContext(), MainActivity.DEBUG_TAG)
                :"SHARE::Locations";

        if(Core_Activity.getSetting(getApplicationContext(),
                MainActivity.FREQUENCY_LOCATION_GPS).length() > 0 ) {
            UPDATE_TIME_GPS = Integer.parseInt(Core_Activity.getSetting(getApplicationContext(),
                    MainActivity.FREQUENCY_LOCATION_GPS));
        }
        if(Core_Activity.getSetting(getApplicationContext(),
                MainActivity.FREQUENCY_LOCATION_NETWORK).length() > 0 ) {
            UPDATE_TIME_NETWORK = Integer.parseInt(
                    Core_Activity.getSetting(getApplicationContext(),
                            MainActivity.FREQUENCY_LOCATION_NETWORK));
        }
        if( Core_Activity.getSetting(getApplicationContext(),
                MainActivity.MIN_LOCATION_GPS_ACCURACY).length() > 0 ) {
            UPDATE_DISTANCE_GPS = Integer.parseInt(
                    Core_Activity.getSetting(getApplicationContext(),
                            MainActivity.MIN_LOCATION_GPS_ACCURACY));
        }
        if( Core_Activity.getSetting(getApplicationContext(),
                MainActivity.MIN_LOCATION_NETWORK_ACCURACY).length() > 0 ){
            UPDATE_DISTANCE_NETWORK = Integer.parseInt(
                    Core_Activity.getSetting(getApplicationContext(),
                            MainActivity.MIN_LOCATION_NETWORK_ACCURACY));
        }
        if( Core_Activity.getSetting(getApplicationContext(),
                MainActivity.LOCATION_EXPIRATION_TIME).length() > 0 ) {
            EXPIRATION_TIME = Integer.parseInt( Core_Activity.getSetting(getApplicationContext(),
                    MainActivity.LOCATION_EXPIRATION_TIME));
        }

        DATABASE_TABLES = Location_Data.DATABASE_TABLES;
        TABLES_FIELDS = Location_Data.TABLES_FIELDS;
        CONTEXT_URIS = new Uri[]{ LocationsData.CONTENT_URI };
//查看GPS定位服务是否打开？？？？？？
        if( Core_Activity.getSetting(getApplicationContext(),
                MainActivity.STATUS_LOCATION_GPS).equals("true") ) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, UPDATE_TIME_GPS * 1000,
                    UPDATE_DISTANCE_GPS, this);
            locationManager.addGpsStatusListener(gps_status_listener);
            if( Core_Activity.DEBUG ) Log.d(TAG,"Locations tracking with GPS is active");
        }
        //要是网络定位服务打开的话，就马上请求位置更新
        if( Core_Activity.getSetting(getApplicationContext(),
                MainActivity.STATUS_LOCATION_NETWORK).equals("true") ){
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, UPDATE_TIME_NETWORK * 1000,
                    UPDATE_DISTANCE_NETWORK, this);
            if( Core_Activity.DEBUG ) Log.d(TAG,"Locations tracking with Network is active");
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
///本服务销毁时，回调该方法，把相关方法进行销毁，释放资源
        locationManager.removeUpdates(this);
        locationManager.removeGpsStatusListener(gps_status_listener);

        if(Core_Activity.DEBUG) Log.d(TAG, "Locations service terminated...");
    }

    //在本服务重新生成时，调用此类
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);

        TAG = Core_Activity.getSetting(getApplicationContext(),
                MainActivity.DEBUG_TAG).length()>0
                ? Core_Activity.getSetting(getApplicationContext(), MainActivity.DEBUG_TAG)
                :"SHARE::Locations";
//要是GPS的定位频率有值的话，那就将值给 GPS定位更新时间
        if(Core_Activity.getSetting(getApplicationContext(),
                MainActivity.FREQUENCY_LOCATION_GPS).length() > 0 )
        {
            UPDATE_TIME_GPS = Integer.parseInt(Core_Activity.getSetting(getApplicationContext(),
                    MainActivity.FREQUENCY_LOCATION_GPS));
        }
        //网络定位的频率
        if(Core_Activity.getSetting(getApplicationContext(),
                MainActivity.FREQUENCY_LOCATION_NETWORK).length() > 0 ) {
            UPDATE_TIME_NETWORK = Integer.parseInt(
                    Core_Activity.getSetting(getApplicationContext(),
                            MainActivity.FREQUENCY_LOCATION_NETWORK));
        }
        //GPS定位精度
        if( Core_Activity.getSetting(getApplicationContext(),
                MainActivity.MIN_LOCATION_GPS_ACCURACY).length() > 0 ) {
            UPDATE_DISTANCE_GPS = Integer.parseInt(Core_Activity.getSetting(
                    getApplicationContext(),
                    MainActivity.MIN_LOCATION_GPS_ACCURACY));
        }
        //网络定位精度
        if( Core_Activity.getSetting(getApplicationContext(),
                MainActivity.MIN_LOCATION_NETWORK_ACCURACY).length() > 0 ) {
            UPDATE_DISTANCE_NETWORK = Integer.parseInt(Core_Activity.getSetting(
                    getApplicationContext(),
                    MainActivity.MIN_LOCATION_NETWORK_ACCURACY));
        }
        //定位实验的时间
        if( Core_Activity.getSetting(getApplicationContext(),
                MainActivity.LOCATION_EXPIRATION_TIME).length() > 0 ) {
            EXPIRATION_TIME = Integer.parseInt(Core_Activity.getSetting(getApplicationContext(),
                    MainActivity.LOCATION_EXPIRATION_TIME));
        }

        locationManager.removeUpdates(this);
        if( Core_Activity.getSetting(getApplicationContext(),
                MainActivity.STATUS_LOCATION_GPS).equals("true") ) {
            //请求定位更新
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    UPDATE_TIME_GPS * 1000, UPDATE_DISTANCE_GPS, this);

            if( Core_Activity.DEBUG ) Log.d(TAG,"Locations tracking with GPS is active");
        }
        if( Core_Activity.getSetting(getApplicationContext(),
                MainActivity.STATUS_LOCATION_NETWORK).equals("true") ) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    UPDATE_TIME_NETWORK * 1000, UPDATE_DISTANCE_NETWORK, this);

            if( Core_Activity.DEBUG ) Log.d(TAG,"Locations tracking with Network is active");
        }

        return START_STICKY;
    }

    //监控定位信息的改变
    @Override
    public void onLocationChanged(Location newLocation)
    {
        Location bestLocation = null;

        //要是GPS和网络定位都打开了，那就要看是否还有更好的定位信息
        // 要是不能获取到更好的定位，那就一直保持着上一次的
        if( Core_Activity.getSetting(getApplicationContext(),
                MainActivity.STATUS_LOCATION_GPS).equals("true")
                && Core_Activity.getSetting(getApplicationContext(),
                MainActivity.STATUS_LOCATION_NETWORK).equals("true"))
        {
            //获取上一次GPS的定位信息
            Location lastGPS = locationManager.getLastKnownLocation(
                    LocationManager.GPS_PROVIDER);
            //获取上一次网络定位信息
            Location lastNetwork = locationManager.getLastKnownLocation(
                    LocationManager.NETWORK_PROVIDER);

            if(isBetterLocation(lastNetwork, lastGPS)) {
                if(isBetterLocation(newLocation, lastNetwork)) {
                    bestLocation = newLocation;
                }else{
                    bestLocation = lastNetwork;
                }
            }else{
                if(isBetterLocation(newLocation, lastGPS)){
                    bestLocation = newLocation;
                }else{
                    bestLocation = lastGPS;
                }
            }
        } else {
            bestLocation = newLocation;
        }

        ContentValues rowData = new ContentValues();
        rowData.put(LocationsData.TIMESTAMP, System.currentTimeMillis());
        rowData.put(LocationsData.DEVICE_ID, Core_Activity.getSetting(getApplicationContext(),
                MainActivity.DEVICE_ID));
        rowData.put(LocationsData.LATITUDE, bestLocation.getLatitude());
        rowData.put(LocationsData.LONGITUDE, bestLocation.getLongitude());
        rowData.put(LocationsData.BEARING, bestLocation.getBearing());
        rowData.put(LocationsData.SPEED, bestLocation.getSpeed());
        rowData.put(LocationsData.ALTITUDE, bestLocation.getAltitude());
        rowData.put(LocationsData.PROVIDER, bestLocation.getProvider());
        rowData.put(LocationsData.ACCURACY, bestLocation.getAccuracy());

        try {
            getContentResolver().insert(LocationsData.CONTENT_URI, rowData);
        }catch( SQLiteException e ) {
            if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
        }catch( SQLException e ) {
            if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
        }

        Intent locationEvent = new Intent(ACTION_SHARE_LOCATIONS);
        sendBroadcast(locationEvent);
    }
    //用接下来的两个回调函数来进行 判断 和 检测，然后发出广播通告，这个能不能用
    @Override
    public void onProviderDisabled(String provider)
    {
        if(provider.equals(LocationManager.GPS_PROVIDER)) {
            if(Core_Activity.DEBUG) Log.d(TAG,ACTION_SHARE_GPS_LOCATION_DISABLED);
            Intent gps = new Intent(ACTION_SHARE_GPS_LOCATION_DISABLED);
            sendBroadcast(gps);
        }
        if(provider.equals(LocationManager.NETWORK_PROVIDER)) {
            if(Core_Activity.DEBUG) Log.d(TAG,ACTION_SHARE_NETWORK_LOCATION_DISABLED);
            Intent network = new Intent(ACTION_SHARE_NETWORK_LOCATION_DISABLED);
            sendBroadcast(network);
        }
    }
    @Override
    public void onProviderEnabled(String provider)
    {
        if(provider.equals(LocationManager.GPS_PROVIDER)) {
            if(Core_Activity.DEBUG) Log.d(TAG,ACTION_SHARE_GPS_LOCATION_ENABLED);
            Intent gps = new Intent(ACTION_SHARE_GPS_LOCATION_ENABLED);
            sendBroadcast(gps);
        }
        if(provider.equals(LocationManager.NETWORK_PROVIDER)) {
            if(Core_Activity.DEBUG) Log.d(TAG,ACTION_SHARE_NETWORK_LOCATION_ENABLED);
            Intent network = new Intent(ACTION_SHARE_NETWORK_LOCATION_ENABLED);
            sendBroadcast(network);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        if(Core_Activity.DEBUG) Log.d(TAG,"onStatusChanged: "+provider
                + " Status:"+status+" Extras:"+extras.toString());

        //保存最优的定位信息，这个信息可能来自GPS。也可能来自网络
        //不过，这涵盖了 当GPS关闭时无法得到定位修正 的情况
        Location lastGPS = locationManager.getLastKnownLocation(
                LocationManager.GPS_PROVIDER);

        Location lastNetwork = null;
        //对网络提供者进行一次快速的检查，并获取上一次的网络定位信息
        if( locationManager.getProvider(LocationManager.NETWORK_PROVIDER) != null) {
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,
                    Location_Activity.this, getMainLooper());
            lastNetwork = locationManager.getLastKnownLocation(
                    LocationManager.NETWORK_PROVIDER);
        }

        Location bestLocation = null;
        if(isBetterLocation(lastNetwork, lastGPS)) {
            bestLocation = lastNetwork;
        }else{
            bestLocation = lastGPS;
        }

        ContentValues rowData = new ContentValues();
        rowData.put(LocationsData.TIMESTAMP, System.currentTimeMillis());
        rowData.put(LocationsData.DEVICE_ID, Core_Activity.getSetting(getApplicationContext(),
                MainActivity.DEVICE_ID));
        rowData.put(LocationsData.LATITUDE, bestLocation.getLatitude());
        rowData.put(LocationsData.LONGITUDE, bestLocation.getLongitude());
        rowData.put(LocationsData.BEARING, bestLocation.getBearing());
        rowData.put(LocationsData.SPEED, bestLocation.getSpeed());
        rowData.put(LocationsData.ALTITUDE, bestLocation.getAltitude());
        rowData.put(LocationsData.PROVIDER, bestLocation.getProvider());
        rowData.put(LocationsData.ACCURACY, bestLocation.getAccuracy());

        try {
            getContentResolver().insert(LocationsData.CONTENT_URI, rowData);
        }catch( SQLiteException e ) {
            if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
        }catch( SQLException e ) {
            if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
        }

        Intent locationEvent = new Intent(ACTION_SHARE_LOCATIONS);
        sendBroadcast(locationEvent);
    }

}

