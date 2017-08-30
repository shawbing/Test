package com.sensorslife;

import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.sensorslife.Data_Managers.Basic_Data;
import com.sensorslife.Data_Managers.Basic_Data.Share_Device;
import com.sensorslife.Data_Managers.Basic_Data.Share_Settings;
import com.sensorslife.Sensor_Activities.Acceleration_Activity;
import com.sensorslife.Sensor_Activities.Application_Activity;
import com.sensorslife.Sensor_Activities.Barometer_Activity;
import com.sensorslife.Sensor_Activities.Battery_Activity;
import com.sensorslife.Sensor_Activities.Bluetooth_Activity;
import com.sensorslife.Sensor_Activities.Communication_Activity;
import com.sensorslife.Sensor_Activities.Gravity_Activity;
import com.sensorslife.Sensor_Activities.Gyroscope_Activity;
import com.sensorslife.Sensor_Activities.Installation_Activity;
import com.sensorslife.Sensor_Activities.Light_Activity;
import com.sensorslife.Sensor_Activities.LinearAcceleration_Activity;
import com.sensorslife.Sensor_Activities.Location_Activity;
import com.sensorslife.Sensor_Activities.Magnetic_Activity;
import com.sensorslife.Sensor_Activities.Network_Activity;
import com.sensorslife.Sensor_Activities.Processor_Activity;
import com.sensorslife.Sensor_Activities.Proximity_Activity;
import com.sensorslife.Sensor_Activities.Rotation_Activity;
import com.sensorslife.Sensor_Activities.Screen_Activity;
import com.sensorslife.Sensor_Activities.Telephony_Activity;
import com.sensorslife.Sensor_Activities.Temperature_Activity;
import com.sensorslife.Sensor_Activities.TimeZone_Activity;
import com.sensorslife.Sensor_Activities.Traffic_Activity;
import com.sensorslife.Sensor_Activities.WiFi_Activity;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Administrator on 2017/8/25.
 *
 * 整个软件的数据处理中心，与MainActivity分开，负责逻辑部分
 * 主要包括
 */

public class Core_Activity extends Service {
    //
//消除错误的标记，默认值为否
    public static boolean DEBUG = false;

//消除错误的标签TAG，默认名为SHARE
// 可能这里有误
    public static String TAG = "Core_Activity";

/**开始定义各种  广播通知*/
    //申明 广播动作：可获取到 软件的内容信息。主要是往外广播信息
    public static final String ACTION_SHARE_DEVICE_INFORMATION = "ACTION_SHARE_DEVICE_INFORMATION";

//接收所有模式组件发出的广播
// 通知删除已经收集到关于设备的数据
    public static final String ACTION_SHARE_CLEAR_DATA = "ACTION_SHARE_CLEAR_DATA";

//接收广播通知：刷新界面中活动的传感器
    public static final String ACTION_SHARE_REFRESH = "ACTION_SHARE_REFRESH";

    //接收广播通知：插件一定要应用内容广播接收器
// 去分享当前的状态信息
    public static final String ACTION_SHARE_CURRENT_CONTEXT = "ACTION_SHARE_CURRENT_CONTEXT";

    //停止所有的传感器服务
    public static final String ACTION_SHARE_STOP_SENSORS = "ACTION_SHARE_STOP_SENSORS";

    //接收所有模式的广播通知：清除相应的内容提供者存储的历史数据
    public static final String ACTION_SHARE_SPACE_MAINTENANCE = "ACTION_SHARE_SPACE_MAINTENANCE";

    //只能手表？？？感觉不像是手表的意思
// 通过Wear来了解那个传感器服务处于激活状态，能够与手机同步
// 来进行配置的改变
// 另外还有两个额外的变量：用于设置的 关键字段 和 相应的值
    public static final String ACTION_SHARE_CONFIG_CHANGED = "ACTION_SHARE_CONFIG_CHANGED";
    public static final String EXTRA_CONFIG_SETTING = "extra_config_setting";
    public static final String EXTRA_CONFIG_VALUE = "extra_config_value";

    //用于记录用户所更新下载的，以便能帮助用户进行安装，更新功能
    private static long SHARE_FRAMEWORK_DOWNLOAD_ID = 0;

    //万一有许多插件依赖需要下载安装，
// 需要一个插件的下载队列来统一所有下载，编排顺序
    private static ArrayList<Long> SHARE_PLUGIN_DOWNLOAD_IDS = new ArrayList<Long>();

    //初始化主要的对象
    //第一个alarmmanager是干啥的？？？
    // 百度了一下，原来是闹钟，即：计时器
    private static AlarmManager alarmManager = null;
    //已经封装好的intent，包含动作。看对象就晓得是要“重复
    private static PendingIntent repeatingIntent = null;
    private static Context shareContext = null;

    //用于转换（或是说启动）到网络上传数据的服务
    private static PendingIntent webserviceUploadIntent = null;

/**定义  服务*/
    private static Intent shareStatusMonitor = null;

    private static Intent batteryService=null;
    private static Intent locationsService = null;
    private static Intent LightService=null;

    private static Intent applicationService = null;
    private static Intent accelerometerService = null;
    private static Intent bluetoothService = null;
    private static Intent screenService = null;
    private static Intent networkService = null;
    private static Intent trafficService = null;
    private static Intent communicationService = null;
    private static Intent processorService = null;
    private static Intent gyroService = null;
    private static Intent wifiService = null;
    private static Intent telephonyService = null;
    private static Intent timeZoneService = null;
    private static Intent rotationService = null;
    private static Intent proximityService = null;
    private static Intent magnetoService = null;
    private static Intent barometerService = null;
    private static Intent gravityService = null;
    private static Intent linear_accelService = null;
    private static Intent temperatureService = null;
    private static Intent installationsService = null;

    //上次的监视频率
    private final String PREF_FREQUENCY_WATCHDOG = "frequency_watchdog";
    private final String PREF_LAST_UPDATE = "last_update";
    //上次的同步
    private final String PREF_LAST_SYNC = "last_sync";
    //定义固定频率  为 五分钟
    private final int CONST_FREQUENCY_WATCHDOG = 5 * 60;

    //SharedPreferences这是干啥的？？？？？
    // 很轻量级的存储数据，如应用的一些设置，activity的状态
    private SharedPreferences share_preferences;

    //单例化对象，这里只是一个对象，还没看见单例化的标记singleton
    // 单例模式只允许创建一个对象，因此节省内存，加快对象访问速度，
// 因此对象需要被公用的场合适合使用，如多个模块使用同一个数据源连接对象等等
    private static Core_Activity shareService = Core_Activity.getService();

    //获取到单例化的框架对象
    public static Core_Activity getService()
    {
        if( shareService == null ) shareService = new Core_Activity();
        return shareService;
    }

    //、、绑定本服务，避免销毁
    private final IBinder serviceBinder = new ServiceBinder();
    public class ServiceBinder extends Binder {
        //里面这个就有点不懂了？？？而且还有点问题
        Core_Activity getService() {    return Core_Activity.getService();  } }

    //必须要的回调方法，因为上面已对IBinder初始化
    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    //本类必须回调的初始化方法
    @Override
    public void onCreate()
    {
        super.onCreate();
//实例化主要的对象
        shareContext = getApplicationContext();
        //实例化计时器，闹钟管理器
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//添加动作转接命令接口
//但是这个会转到哪里去？？？
        //用于打开内存卡
        IntentFilter filter = new IntentFilter();
//插入SD卡并且已正确安装（识别）时发出广播：扩展介质被插入，而且已经被挂载。
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        //MOUNTED表示挂载的意思，即外接设备已连接
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");//表明数据的样式
//本类注册存储卡的监听，用于获取信息
        shareContext.registerReceiver(storage_BR, filter);

// 这些清理和更新数据，居然和 下载管理器 绑在一起
// 可能不需要这个
        filter = new IntentFilter();
        filter.addAction(Core_Activity.ACTION_SHARE_CLEAR_DATA);
        filter.addAction(Core_Activity.ACTION_SHARE_REFRESH);

        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        shareContext.registerReceiver(aware_BR, filter);//进行注册

//要是获取不到外接存储器，就直接关掉这个功能
        if( ! Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ) {
            stopSelf();//貌似关掉整个服务
            return;
        }
//双引号里面那个是什么鬼？？？
// 是要加载核心，主要用于保存用户的设置参数，新建一个文件
// 保存格式为xml，其名称为引号里面的，即第一个参数
        share_preferences = getSharedPreferences("share_core_prefs", MODE_PRIVATE);
        if( share_preferences.getAll().isEmpty() ) {
            //检查细节类是否为空
            //就要开始获取编辑器，记录信息
            SharedPreferences.Editor editor = share_preferences.edit();
            editor.putInt(PREF_FREQUENCY_WATCHDOG, CONST_FREQUENCY_WATCHDOG);
            //将上次同步和更新的信息填入，默认值为0
            editor.putLong(PREF_LAST_SYNC, 0);
            editor.putLong(PREF_LAST_UPDATE, 0);
            editor.commit();//提交成功，完成编辑
        }

//为所有插件 进行默认设置
        SharedPreferences prefs = getSharedPreferences(
                getPackageName(), Context.MODE_PRIVATE );
        if( prefs.getAll().isEmpty() &&
                Core_Activity.getSetting(getApplicationContext(),
                        MainActivity.DEVICE_ID).length() == 0 ) {
            //要是所有的节点值没有，那么就为所有 进行默认值 设置
            // 先是获取包名，再则定义为私有模式，加载布局文件
            // 最后用true来表示，启用默认值，也称为缺省值
            PreferenceManager.setDefaultValues(getApplicationContext(),
                    getPackageName(), Context.MODE_PRIVATE,
                    R.xml.layout_main, true);
            //提交更改信息
            prefs.edit().commit();
        }
        else {
            PreferenceManager.setDefaultValues(getApplicationContext(),
                    getPackageName(), Context.MODE_PRIVATE,
                    R.xml.layout_main, false);
        }
//真正开始实行默认值的设置
        Map<String,?> defaults = prefs.getAll();
        for(Map.Entry<String, ?> entry : defaults.entrySet()) {
            if( Core_Activity.getSetting(getApplicationContext(),
                    entry.getKey()).length() == 0 )
            //要是当前该节点设置的长度为零，即为空。不过返回的什么
            {
                //那就进行默认值设置
                Core_Activity.setSetting(getApplicationContext(),
                        entry.getKey(), entry.getValue());
            }
        }
//检测设备设置中的编号有没有，若没有就直接新建一个（一个随机值）
        if( Core_Activity.getSetting(getApplicationContext(),
                MainActivity.DEVICE_ID).length() == 0 )
        {
            UUID uuid = UUID.randomUUID();//然后就对其进行设置
            Core_Activity.setSetting(   getApplicationContext(),
                    MainActivity.DEVICE_ID,
                    uuid.toString()   );
        }
//DEBUG是bool类型的
        DEBUG = Core_Activity.getSetting(shareContext,
                MainActivity.DEBUG_FLAG).equals("true");
//而TAG是String类型的，初始值为 本类名。如果设置中存在，那就用设置中的值
        TAG = Core_Activity.getSetting(shareContext, MainActivity.DEBUG_TAG).length()>0
                ? Core_Activity.getSetting(shareContext,
                MainActivity.DEBUG_TAG)
                :TAG;
//调用函数，获取设备的信息
        get_device_info();

//要是主界面函数的DEBUG_FLAG为true，说明程序运行没有错误，能进入主界面
        if( Core_Activity.DEBUG ) Log.d(TAG,"主框架已经成功创立");

        //onCreate函数到此为止
    }

    /**直接从 Build 里获取移动设备的信息，即时间，ID，平台，版本，设备，表现ID，硬件……
     * 并在此将数据存在数据库中，但在存储之前没有判断
     * */
    private void get_device_info()
    {
        //这里的Share_Device是在providers中的Share_Provider
        Cursor shareContextDevice = shareContext.getContentResolver()
                .query(Share_Device.CONTENT_URI, null, null, null, null);
        //要是查询到设备的信息内容的话
        if( shareContextDevice == null ||
                ! shareContextDevice.moveToFirst() )
        {
            ContentValues rowData = new ContentValues();
            rowData.put("timestamp", System.currentTimeMillis());//时间戳
            //这个还要查询获取数据
            rowData.put("device_id", Core_Activity.getSetting(shareContext,
                    MainActivity.DEVICE_ID));//设备编号
            rowData.put("board", Build.BOARD);//平台
            rowData.put("brand", Build.BRAND);//版本
            rowData.put("device", Build.DEVICE);//设备
            rowData.put("build_id", Build.DISPLAY);//表现id
            rowData.put("hardware", Build.HARDWARE);//硬件
            rowData.put("manufacturer", Build.MANUFACTURER);//制造商
            rowData.put("model", Build.MODEL);//模式
            rowData.put("product", Build.PRODUCT);//产品
            rowData.put("serial", Build.SERIAL);//系列
            rowData.put("release", Build.VERSION.RELEASE);//发布
            rowData.put("release_type", Build.TYPE);//发布的型号
            rowData.put("sdk", Build.VERSION.SDK_INT);//软件开发工具包

            try {
                shareContext.getContentResolver().insert(
                        Share_Device.CONTENT_URI, rowData);
//怎么就进行信息广播了呢???哪里需要这些信息？
                //将设备的基本信息广播出去
                Intent deviceData = new Intent(ACTION_SHARE_DEVICE_INFORMATION);
                sendBroadcast(deviceData);
                //如果操作成功，就编写到数据库日志中，记录软件的操作
//DEBUG的值会在哪里被改变
                if( Core_Activity.DEBUG ) Log.d(TAG, "Device information:"
                        + rowData.toString());
//怎么还会涉及到数据库的操作问题、、
            }catch( SQLiteException e ) {
                if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
            }catch( SQLException e ) {
                if(Core_Activity.DEBUG) Log.d(TAG,e.getMessage());
            }
        }
        //但凡用到数据库操作
        // 都要随时注意到是否关闭的问题，
        // 出于安全和节约资源的考虑
        if( shareContextDevice != null && !shareContextDevice.isClosed())
            shareContextDevice.close();
    }

    //检查获取手机比较隐私的信息的权限   而非传感器的信息
//检查获取手机比较隐私的信息的权限  其他类中也会经常调用的
    public static boolean is_watch(Context c)
    {
        boolean is_watch = false;
        //又要用到查询语句，难道是只限制返回一条数据
        Cursor device = c.getContentResolver().query(
                Share_Device.CONTENT_URI,
                null, null, null, "1 LIMIT 1");
        if( device != null && device.moveToFirst() ) {
//判定的方法就是：是否包含版本号，一般以W,W.1,W.2……来呈现
            is_watch = device.getString(device.getColumnIndex(
                    Share_Device.RELEASE)).contains("W");
        }
        if( device != null && ! device.isClosed() ) device.close();
        return is_watch;
    }

    /**回调“开始命令”,主要是里面涉及到网络和插件
     * 1、检查存储设备是否挂载
     * 2、加载服务和插件
     * 3、检查网络的频率，利用网络，下载版本，同步数据
     * */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        //首先检查存储卡是否成功挂载（能否利用）
        if( Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) )
        {
            //debug在这里受到影响
            DEBUG = Core_Activity.getSetting(shareContext,
                    MainActivity.DEBUG_FLAG).equals("true");
            //要是设置里有标签的话，就用这个标签；没有的话，就用初始值
            TAG = Core_Activity.getSetting(shareContext,
                    MainActivity.DEBUG_TAG).length()>0
                    ? Core_Activity.getSetting(
                    shareContext, MainActivity.DEBUG_TAG)
                    :TAG;
            //若能调试成功，那就记录在日志中
            if( Core_Activity.DEBUG ) Log.d(TAG,"share framework is active...");
            //插件希望也能够打开所有的服务，，这在其设置中已经申明了的
            startAllServices();
        }
        else //如果不能用存储卡，那就关掉所有的服务和插件吧
        {
            //关掉所有服务    、、
            stopAllServices();
        }
        return START_STICKY;//一直处于启动的状态
    }

//给一个包名和类，来检查这个到底存不存在。
// 要是存在，那就返回true
// 这个有用
    private static boolean isClassAvailable(Context context
            , String package_name, String class_name )
    {
        try{
            //创建一个指定包的环境（这个环境忽略安全，包含代码）
            Context package_context = context.createPackageContext(package_name
                    , Context.CONTEXT_IGNORE_SECURITY
                            | Context.CONTEXT_INCLUDE_CODE);
            //然后用包的环境来加载对应的类
            package_context.getClassLoader().loadClass(package_name+"."+class_name);
        }
        catch ( ClassNotFoundException e ) {
            return false;   //没找到类的异常
        }
        catch ( PackageManager.NameNotFoundException e ) {
            return false;   //没找到包名的异常
        }
        return true;
    }

/**查询   根据键（Key）来检索对应的 设置值（setting value）*/
    public static String getSetting(Context context, String key )
    {
        boolean is_restricted_package = true;
//全局，所有的设置信息
        ArrayList<String> global_settings = new ArrayList<String>();
        global_settings.add(MainActivity.DEBUG_FLAG);
        global_settings.add(MainActivity.DEBUG_TAG);

        global_settings.add(MainActivity.DEVICE_ID);

        //要是没有这个键，那么说明，这是个受到限制的包，属于局外的
        // 另外一个关键在于，这个是不是全部
        if( global_settings.contains(key) ) {
            is_restricted_package = false;
        }
//这里的Share_Settings是来自Share_Provider类，
// 里面的第二个方法，定义了基本信息的列
        String value = "";
        Cursor qry = context.getContentResolver().query(
                Share_Settings.CONTENT_URI,
                null,
                Share_Settings.SETTING_KEY + " LIKE '" + key + "'"
//这里这句有点不懂了
//要是之前这个
                        + ( is_restricted_package
                        ?   " AND " + Share_Settings.SETTING_PACKAGE_NAME
                        + " LIKE '" + context.getPackageName() + "'"
                        :   "")
                , null
                , null);
        //要是能够查到有值，那就把这个值给value来返回
        if( qry != null && qry.moveToFirst() )
            value = qry.getString(qry.getColumnIndex(Share_Settings.SETTING_VALUE));

        if( qry != null && ! qry.isClosed() )
            qry.close();    //每一次查询都不忘释放资源

        return value;
    }

/**设置    根据键（key）来设置相应的设置值（value），用对象（object）来定义 值*/
    public static void setSetting(Context context, String key, Object value )
    {

        boolean is_restricted_package = true;

        ArrayList<String> global_settings = new ArrayList<String>();
        global_settings.add(MainActivity.DEBUG_FLAG);
        global_settings.add(MainActivity.DEBUG_TAG);

        global_settings.add(MainActivity.DEVICE_ID);

        if( global_settings.contains(key) ) {
            is_restricted_package = false;
        }

        //要是取得当前设备的编号，却又不能获取相应的包，那就直接返回结束
        if( key.equals(MainActivity.DEVICE_ID) && ! context.getPackageName()
                .equals("com.sensorslife") )
            return;
//方法里面设置三个参数（params）在这里都用到
        ContentValues setting = new ContentValues();
        setting.put(Share_Settings.SETTING_KEY, key);
        setting.put(Share_Settings.SETTING_VALUE, value.toString());
        setting.put(Share_Settings.SETTING_PACKAGE_NAME,
                context.getPackageName());
//查询
        Cursor qry = context.getContentResolver().query(
                Share_Settings.CONTENT_URI, null,
                Share_Settings.SETTING_KEY + " LIKE '" + key + "'"
                        + (is_restricted_package
                        ? " AND " + Share_Settings.SETTING_PACKAGE_NAME
                        + " LIKE '" + context.getPackageName() + "'"
                        : "")
                , null, null);

        //要是有设置值，那就进行 更新 或修改 操作，
        // 不然就执行 插入 操作
        if( qry != null && qry.moveToFirst() ) {
            try {//如果查询到的这个值，跟传入的参数值不相等，才执行修改
                if( ! qry.getString(qry.getColumnIndex(
                        Share_Settings.SETTING_VALUE))
                        .equals(value.toString()) )
                {//可以直接用这个方法 修改数据 或添加
                    context.getContentResolver().update(
                            Share_Settings.CONTENT_URI,
                            setting,    //这就是上面ContentValue的东西
                            Share_Settings.SETTING_ID + "="
                                    + qry.getInt(qry.getColumnIndex(
                                    Share_Settings.SETTING_ID))
                            , null);
                    if( Core_Activity.DEBUG)    //将更新操作记录出来
                        Log.d(Core_Activity.TAG,"Updated: "+key+"="+value);
                }
            }catch( SQLiteException e ) {
                if(Core_Activity.DEBUG)     Log.d(TAG,e.getMessage());
            }catch( SQLException e ) {
                if(Core_Activity.DEBUG)     Log.d(TAG,e.getMessage());
            }

        }
        else //要是没有这个设置值，就执行插入操作。不过，这一步比较简单，只用两个参数
        {
            try {
                context.getContentResolver().insert(Share_Settings.CONTENT_URI, setting);
                if( Core_Activity.DEBUG)
                    Log.d(Core_Activity.TAG,"Added: " + key + "=" + value);
            }catch( SQLiteException e ) {
                if(Core_Activity.DEBUG)         Log.d(TAG,e.getMessage());
            }catch( SQLException e ) {
                if(Core_Activity.DEBUG)         Log.d(TAG,e.getMessage());
            }
        }
        if( qry != null && ! qry.isClosed() )
            qry.close();

//启动广播，通知配置参数的改变
        Intent wearBroadcast = new Intent(ACTION_SHARE_CONFIG_CHANGED);
        wearBroadcast.putExtra(EXTRA_CONFIG_SETTING, key);
        wearBroadcast.putExtra(EXTRA_CONFIG_VALUE, value.toString());
        context.sendBroadcast(wearBroadcast);
    }

    //用于释放所有资源、应用退出时
//用于释放所有资源
//用于释放所有资源。因为有那么多的服务还在使用
    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if( repeatingIntent != null ) alarmManager.cancel(repeatingIntent);
        if( webserviceUploadIntent != null) alarmManager.cancel(webserviceUploadIntent);

        if( aware_BR != null ) shareContext.unregisterReceiver(aware_BR);
        if( storage_BR != null ) shareContext.unregisterReceiver(storage_BR);
    }

    //重新设置 值 的函数
    public static void reset(Context context)
    {
        if( ! context.getPackageName()
                .equals("com.sensorslife"))
            return;     //要不是当前项目的这个包名，那就直接返回，不执行

        String device_id = Core_Activity.getSetting( context,
                MainActivity.DEVICE_ID );

        //先把所有的设置值清理掉
        context.getContentResolver().delete(
                Share_Settings.CONTENT_URI, null, null );

        //查看客户端的默认参数的设置
        SharedPreferences prefs = context.getSharedPreferences(
                context.getPackageName(), Context.MODE_PRIVATE );

        PreferenceManager.setDefaultValues(context,
                context.getPackageName(), Context.MODE_PRIVATE,
                R.xml.layout_main, true);
        prefs.edit().commit();

        //调用setsetting逐个进行设置
        Map<String,?> defaults = prefs.getAll();
        for(Map.Entry<String, ?> entry : defaults.entrySet()) {
            Core_Activity.setSetting(context, entry.getKey(), entry.getValue());
        }

        //但要保持上次的设备编号，不变
        Core_Activity.setSetting(context, MainActivity.DEVICE_ID, device_id);

        //最后再应用刷新操作的广播，来更新界面
        Intent aware_apply = new Intent( Core_Activity.ACTION_SHARE_REFRESH );
        context.sendBroadcast(aware_apply);
    }

    //  广播接收那些 监控软件动作的信息。主要广播定义如下：
    // ACTION_AWARE_SYNC_DATA: 将数据传送到远程的网站服务器端.
    // ACTION_AWARE_CLEAR_DATA: 清理掉设备本地该软件存储的数据库.
    // ACTION_AWARE_REFRESH: 将那些改变应用于 配置.
    // {@link DownloadManager#ACTION_DOWNLOAD_COMPLETE}:就在本软件更新完成下载的时候
    // 其中多次调用到（Share_Provider）Share_Device.CONTENT_URI，
    //     Share_Preferences.STATUS_WEBSERVICE ，      WebserviceHelper
    public static class Share_Broadcaster extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {

            //只用于同步设备的信息，而非软件的设置和插件
            String[] DATABASE_TABLES = Basic_Data.DATABASE_TABLES;
            String[] TABLES_FIELDS = Basic_Data.TABLES_FIELDS;
            Uri[] CONTEXT_URIS = new Uri[]{ Share_Device.CONTENT_URI };

            //要是启动其他服务动作为：同步软件的数据，以及网络服务的状态为true
            //用于启动网络服务
//            if( intent.getAction().equals(Share.ACTION_SHARE_SYNC_DATA)

            //如果动作的名称为：清理数据
            if( intent.getAction().equals(Core_Activity.ACTION_SHARE_CLEAR_DATA) )
            {
                context.getContentResolver().delete(
                        Share_Device.CONTENT_URI, null, null);
                if(Core_Activity.DEBUG ) Log.d(TAG,"Cleared " + CONTEXT_URIS[0]);

                //要是网络服务还可以用时，那就启动网络服务进行远程同步清理
//                if( Share.getSetting(context,
            }

            //要是动作名称为：刷新。那就重新启动一次本服务类
            if( intent.getAction().equals(Core_Activity.ACTION_SHARE_REFRESH))
            {
                Intent refresh = new Intent(context, Core_Activity.class);
                context.startService(refresh);
            }
            //下载管理器的下载完成动作。要是下载完成了，怎么办
//            if( intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE) )

            //这里又要轮到插件的下载完成后，进行的处理。插件的下载编号，是字符串组
//                if( SHARE_PLUGIN_DOWNLOAD_IDS.size() > 0 )
        }
    }
    private static final Share_Broadcaster aware_BR = new Share_Broadcaster();

    /**还要 查看能够否使用存储设备

     * 要是不能利用存储设备，就把软件关掉,
     * 等到能够利用的时候，就又将其打开
     */
    public static class Storage_Broadcaster extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            //要是挂载好了存储设备，那就添入日志：
            if( intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED) )
                if( Core_Activity.DEBUG ) Log.d(TAG,"恢复数据的登录状态：即可用状态");
            //要是没有挂载的存储设备，写入日志：在外存再次可用之前，关闭软件数据的登录状态
            if ( intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED) )
                if( Core_Activity.DEBUG ) Log.w(TAG,
                        "在存储设备还能继续用之前，就把日志停止了");
            //最后再次启动本服务大类.
            Intent aware = new Intent(context, Core_Activity.class);
            context.startService(aware);
        }
    }
    //存储信息的广播？？？
    private static final Storage_Broadcaster storage_BR = new Storage_Broadcaster();

    /**从这里开始的
     //打开所有的服务，其实是事先检查一下用户是否勾选，再打开相应的服务
     // 至于这个服务所对应的网络要求就不清楚了？？？
     // 都不引导用户打开定位所需的条件（GPS或网络）*/
    protected void startAllServices()
    {
//检测电池的设置状态
        if( Core_Activity.getSetting(shareContext, MainActivity.STATUS_BATTERY).equals("true") ) {
            startBattery();
        }else stopBattery();

//如果应用的GPS定位或网络定位勾选状态为真的话，就打开定位服务
// 相反就直接关闭定位服务
        if( Core_Activity.getSetting(shareContext, MainActivity.STATUS_LOCATION_GPS)
                .equals("true")
                || Core_Activity.getSetting(shareContext,
                MainActivity.STATUS_LOCATION_NETWORK).equals("true") ) {
            startLocations();
        }else stopLocations();

//检查光感的设置是否为激活状态，如果是，那就启动服务
        if( Core_Activity.getSetting(shareContext,
                MainActivity.STATUS_LIGHT).equals("true") ) {
            startLight();
        }else stopLight();

        if( Core_Activity.getSetting(shareContext, MainActivity.STATUS_APPLICATIONS).equals("true")) {
            startApplications();
        }else stopApplications();

        if( Core_Activity.getSetting(shareContext, MainActivity.STATUS_ACCELEROMETER).equals("true") ) {
            startAccelerometer();
        }else stopAccelerometer();

        if( Core_Activity.getSetting(shareContext, MainActivity.STATUS_INSTALLATIONS).equals("true")) {
            startInstallations();
        }else stopInstallations();

        if( Core_Activity.getSetting(shareContext, MainActivity.STATUS_BLUETOOTH).equals("true") ) {
            startBluetooth();
        }else stopBluetooth();

        if( Core_Activity.getSetting(shareContext, MainActivity.STATUS_SCREEN).equals("true") ) {
            startScreen();
        }else stopScreen();

        if( Core_Activity.getSetting(shareContext, MainActivity.STATUS_NETWORK_EVENTS).equals("true") ) {
            startNetwork();
        }else stopNetwork();

        if( Core_Activity.getSetting(shareContext, MainActivity.STATUS_NETWORK_TRAFFIC).equals("true") ) {
            startTraffic();
        }else stopTraffic();

        if( Core_Activity.getSetting(shareContext, MainActivity.STATUS_COMMUNICATION_EVENTS).equals("true")
                || Core_Activity.getSetting(shareContext, MainActivity.STATUS_CALLS).equals("true")
                || Core_Activity.getSetting(shareContext, MainActivity.STATUS_MESSAGES).equals("true") ) {
            startCommunication();
        }else stopCommunication();

        if( Core_Activity.getSetting(shareContext, MainActivity.STATUS_PROCESSOR).equals("true") ) {
            startProcessor();
        }else stopProcessor();

        if( Core_Activity.getSetting(shareContext, MainActivity.STATUS_TIMEZONE).equals("true") ) {
            startTimeZone();
        }else stopTimeZone();

        if( Core_Activity.getSetting(shareContext, MainActivity.STATUS_GYROSCOPE).equals("true") ) {
            startGyroscope();
        }else stopGyroscope();

        if( Core_Activity.getSetting(shareContext, MainActivity.STATUS_WIFI).equals("true") ) {
            startWiFi();
        }else stopWiFi();

        if( Core_Activity.getSetting(shareContext, MainActivity.STATUS_TELEPHONY).equals("true") ) {
            startTelephony();
        }else stopTelephony();

        if( Core_Activity.getSetting(shareContext, MainActivity.STATUS_ROTATION).equals("true") ) {
            startRotation();
        }else stopRotation();

        if( Core_Activity.getSetting(shareContext, MainActivity.STATUS_PROXIMITY).equals("true") ) {
            startProximity();
        }else stopProximity();

        if( Core_Activity.getSetting(shareContext, MainActivity.STATUS_MAGNETOMETER).equals("true") ) {
            startMagnetometer();
        }else stopMagnetometer();

        if( Core_Activity.getSetting(shareContext, MainActivity.STATUS_BAROMETER).equals("true") ) {
            startBarometer();
        }else stopBarometer();

        if( Core_Activity.getSetting(shareContext, MainActivity.STATUS_GRAVITY).equals("true") ) {
            startGravity();
        }else stopGravity();

        if( Core_Activity.getSetting(shareContext, MainActivity.STATUS_LINEAR_ACCELEROMETER).equals("true") ) {
            startLinearAccelerometer();
        }else stopLinearAccelerometer();

        if( Core_Activity.getSetting(shareContext, MainActivity.STATUS_TEMPERATURE).equals("true") ) {
            startTemperature();
        }else stopTemperature();
    }

//停止或关闭所有服务
    protected void stopAllServices()
    {
        stopApplications();
        stopAccelerometer();
        stopBattery();
        stopBluetooth();
        stopCommunication();
        stopLocations();
        stopNetwork();
        stopTraffic();
        stopScreen();
        stopProcessor();
        stopGyroscope();
        stopWiFi();
        stopTelephony();
        stopTimeZone();
        stopRotation();
        stopLight();
        stopProximity();
        stopMagnetometer();
        stopBarometer();
        stopGravity();
        stopLinearAccelerometer();
        stopTemperature();
        stopInstallations();
    }

//打开和关闭监听 应用程序 的服务
    protected void startApplications() {
        if( applicationService == null) applicationService = new Intent(shareContext, Application_Activity.class);
        shareContext.startService(applicationService);
    }
    protected void stopApplications() {
        if( applicationService != null) shareContext.stopService(applicationService);
    }

//电池服务
    protected void startBattery() {
        if( batteryService == null) batteryService = new Intent(shareContext, Battery_Activity.class);
        shareContext.startService(batteryService);
    }
    protected void stopBattery() {
        if(batteryService != null) shareContext.stopService(batteryService);
    }

//屏幕服务
    protected void startScreen() {
        if( screenService == null) screenService = new Intent(shareContext, Screen_Activity.class);
        shareContext.startService(screenService);
    }
    protected void stopScreen() {
        if(screenService != null) shareContext.stopService(screenService);
    }

//打开和关闭定位模式，用于打开和关闭相应的服务
    protected void startLocations()
    {
        if( locationsService == null) locationsService = new Intent(shareContext, Location_Activity.class);
        shareContext.startService(locationsService);
    }
    protected void stopLocations()
    {
        if( Core_Activity.getSetting(shareContext, MainActivity.STATUS_LOCATION_GPS).equals("false")
                && Core_Activity.getSetting(shareContext, MainActivity.STATUS_LOCATION_NETWORK).equals("false") )
        {
            if(locationsService != null) shareContext.stopService(locationsService);
        }
    }

    //光感服务的开和关
    protected void startLight()
    {
        if( LightService == null ) LightService = new Intent(shareContext, Light_Activity.class);
        Toast.makeText(shareContext, "In Core_Activity.startLight():准备开启光感服务", Toast.LENGTH_SHORT).show();
        shareContext.startService(LightService);

    }
    protected void stopLight() {
        if( LightService != null ) shareContext.stopService(LightService);
    }

//安装
    protected void startInstallations() {
        if(installationsService == null) installationsService = new Intent(shareContext, Installation_Activity.class);
        shareContext.startService(installationsService);
    }
    protected void stopInstallations() {
        if(installationsService != null) shareContext.stopService(installationsService);
    }

//温度
    protected void startTemperature() {
        if( temperatureService == null ) temperatureService = new Intent(shareContext, Temperature_Activity.class);
        shareContext.startService(temperatureService);
    }
    protected void stopTemperature() {
        if( temperatureService != null ) shareContext.stopService(temperatureService);
    }

//线性加速度
    protected void startLinearAccelerometer() {
        if( linear_accelService == null ) linear_accelService = new Intent(shareContext, LinearAcceleration_Activity.class);
        shareContext.startService(linear_accelService);
    }
    protected void stopLinearAccelerometer() {
        if( linear_accelService != null ) shareContext.stopService(linear_accelService);
    }

//重力
    protected void startGravity() {
        if( gravityService == null ) gravityService = new Intent(shareContext, Gravity_Activity.class);
        shareContext.startService(gravityService);
    }
    protected void stopGravity() {
        if( gravityService != null ) shareContext.stopService(gravityService);
    }

//气压服务
    protected void startBarometer() {
        if( barometerService == null ) barometerService = new Intent(shareContext, Barometer_Activity.class);
        shareContext.startService(barometerService);
    }
    protected void stopBarometer() {
        if( barometerService != null ) shareContext.stopService(barometerService);
    }

//磁场
    protected void startMagnetometer() {
        if( magnetoService == null ) magnetoService = new Intent(shareContext, Magnetic_Activity.class);
        shareContext.startService(magnetoService);
    }
    protected void stopMagnetometer() {
        if( magnetoService != null ) shareContext.stopService(magnetoService);
    }

//临近服务
    protected void startProximity() {
        if( proximityService == null ) proximityService = new Intent(shareContext, Proximity_Activity.class);
        shareContext.startService(proximityService);
    }
    protected void stopProximity() {
        if( proximityService != null ) shareContext.stopService(proximityService);
    }

//旋转方向
    protected void startRotation() {
        if( rotationService == null ) rotationService = new Intent(shareContext, Rotation_Activity.class);
        shareContext.startService(rotationService);
    }
    protected void stopRotation() {
        if( rotationService != null ) shareContext.stopService(rotationService);
    }

//通信服务
    protected void startTelephony() {
        if( telephonyService == null) telephonyService = new Intent(shareContext, Telephony_Activity.class);
        shareContext.startService(telephonyService);
    }
    protected void stopTelephony() {
        if( telephonyService != null ) shareContext.stopService(telephonyService);
    }

//WiFi
    protected void startWiFi() {
        if( wifiService == null ) wifiService = new Intent(shareContext, WiFi_Activity.class);
        shareContext.startService(wifiService);
    }
    protected void stopWiFi() {
        if( wifiService != null ) shareContext.stopService(wifiService);
    }

//陀螺仪
    protected void startGyroscope() {
        if( gyroService == null ) gyroService = new Intent(shareContext, Gyroscope_Activity.class);
        shareContext.startService(gyroService);
    }
    protected void stopGyroscope() {
        if( gyroService != null ) shareContext.stopService(gyroService);
    }

//加速度
    protected void startAccelerometer() {
        if( accelerometerService == null ) accelerometerService = new Intent(shareContext, Acceleration_Activity.class);
        shareContext.startService(accelerometerService);
    }
    protected void stopAccelerometer() {
        if( accelerometerService != null) shareContext.stopService(accelerometerService);
    }

//CPU使用情况
    protected void startProcessor() {
        if( processorService == null) processorService = new Intent(shareContext, Processor_Activity.class);
        shareContext.startService(processorService);
    }
    protected void stopProcessor() {
        if( processorService != null ) shareContext.stopService(processorService);
    }

//蓝牙服务
    protected void startBluetooth() {
        if( bluetoothService == null) bluetoothService = new Intent(shareContext, Bluetooth_Activity.class);
        shareContext.startService(bluetoothService);
    }
    protected void stopBluetooth() {
        if(bluetoothService != null) shareContext.stopService(bluetoothService);
    }

//移动网络
    protected void startNetwork() {
        if( networkService == null ) networkService = new Intent(shareContext, Network_Activity.class);
        shareContext.startService(networkService);
    }
    protected void stopNetwork() {
        if(networkService != null) shareContext.stopService(networkService);
    }

//网络流量
    protected void startTraffic() {
        if(trafficService == null) trafficService = new Intent(shareContext, Traffic_Activity.class);
        shareContext.startService(trafficService);
    }
    protected void stopTraffic() {
        if( Core_Activity.getSetting(shareContext, MainActivity.STATUS_NETWORK_TRAFFIC).equals("false") ) {
            if( trafficService != null ) shareContext.stopService(trafficService);
        }
    }

//时区
    protected void startTimeZone() {
        if(timeZoneService == null) timeZoneService = new Intent(shareContext, TimeZone_Activity.class);
        shareContext.startService(timeZoneService);
    }
    protected void stopTimeZone() {
        if( timeZoneService != null ) shareContext.stopService(timeZoneService);
    }

//手机交流服务（电话，短信的使用情况）
    protected void startCommunication() {
        if( communicationService == null ) communicationService = new Intent(shareContext, Communication_Activity.class);
        shareContext.startService(communicationService);
    }
    protected void stopCommunication() {
        if( Core_Activity.getSetting(shareContext, MainActivity.STATUS_COMMUNICATION_EVENTS).equals("false")
                && Core_Activity.getSetting(shareContext, MainActivity.STATUS_CALLS).equals("false")
                && Core_Activity.getSetting(shareContext, MainActivity.STATUS_MESSAGES).equals("false") ) {
            if(communicationService != null) shareContext.stopService(communicationService);
        }
    }

}

