package com.sensorslife;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.sensorslife.UI_Managers.Navigation;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 主界面显示，同时是程序的入口
 *
 * 设置页面的实现
 * */

public class MainActivity extends Navigation {
    //

    //查看手机传感器是否可用，如果不能用，就关掉相应的事件
    // 避免软件出错
    // 初始值为false
    private static boolean is_watch = false;

    //这几个怎么没有用到，难道被删掉了
    //  记录程序运行的log日志
    public static final String DEBUG_FLAG = "debug_flag";
    //消除错误的标签？？？？？
    public static final String DEBUG_TAG = "debug_tag";
    //数据库处理缓慢，就将其停止
    public static final String DEBUG_DB_SLOW = "debug_db_slow";
    //定义设备编号的字段的字符串
    public static final String DEVICE_ID="device_id";

/**各种服务的全局字符定义开始*/
//GPS定位的激活状态
    public static final String STATUS_LOCATION_GPS = "status_location_gps";
    //频率默认为180秒
    public static final String FREQUENCY_LOCATION_GPS = "frequency_location_gps";
    //精度默认为150米
    public static final String MIN_LOCATION_GPS_ACCURACY = "min_location_gps_accuracy";

    //网络定位服务的激活状态，是一个勾选框
    public static final String STATUS_LOCATION_NETWORK = "status_location_network";
    //网络定位的频率，默认为300秒，其中0表示服务一直打开
    public static final String FREQUENCY_LOCATION_NETWORK = "frequency_location_network";
    //网络定位的最小精度，默认为1500米
    public static final String MIN_LOCATION_NETWORK_ACCURACY = "min_location_network_accuracy";
    //定位服务取消的时间，默认为300秒
    public static final String LOCATION_EXPIRATION_TIME="location_expiration_time";

//应用程序状态，后台应用程序数据更新频率，安装，通知，崩溃
    public static final String STATUS_APPLICATIONS="status_applications";
    public static final String FREQUENCY_APPLICATIONS = "frequency_applications";
    public static final String STATUS_INSTALLATIONS = "status_installations";
    public static final String STATUS_NOTIFICATIONS = "status_notifications";
    public static final String STATUS_CRASHES = "status_crashes";

    //电池状态信息
    public static final String STATUS_BATTERY="status_battery";

    //光线传感器的状态信息
    public static final String STATUS_LIGHT = "status_light";
    public static final String FREQUENCY_LIGHT = "frequency_light";
    public static final String CURRENT_LIGHT = "current_light";
    //
//加速度状态，频率（默认：20000毫秒）
    public static final String STATUS_ACCELEROMETER = "status_accelerometer";
    public static final String FREQUENCY_ACCELEROMETER = "frequency_accelerometer";

//蓝牙状态，频率（默认：60秒）
    public static final String STATUS_BLUETOOTH = "status_bluetooth";
    public static final String FREQUENCY_BLUETOOTH = "frequency_bluetooth";

//通信交流活动状态，电话，短信
    public static final String STATUS_COMMUNICATION_EVENTS = "status_communication_events";
    public static final String STATUS_CALLS = "status_calls";
    public static final String STATUS_MESSAGES = "status_messages";

//重力状态，频率（默认：20000毫秒）
    public static final String STATUS_GRAVITY = "status_gravity";
    public static final String FREQUENCY_GRAVITY = "frequency_gravity";

//陀螺仪状态，频率（默认：20000毫秒）
    public static final String STATUS_GYROSCOPE = "status_gyroscope";
    public static final String FREQUENCY_GYROSCOPE = "frequency_gyroscope";

//线性加速度状态，频率（默认：20000毫秒）
    public static final String STATUS_LINEAR_ACCELEROMETER = "status_linear_accelerometer";
    public static final String FREQUENCY_LINEAR_ACCELEROMETER = "frequency_linear_accelerometer";

//网络活动状态
    public static final String STATUS_NETWORK_EVENTS = "status_network_events";

//网络流量状态，频率（默认：60秒）
    public static final String STATUS_NETWORK_TRAFFIC = "status_network_traffic";
    public static final String FREQUENCY_NETWORK_TRAFFIC = "frequency_network_traffic";

//磁场状态，频率（默认：20000毫秒）
    public static final String STATUS_MAGNETOMETER = "status_magnetometer";
    public static final String FREQUENCY_MAGNETOMETER = "frequency_magnetometer";


//气压状态，频率（默认：20000毫秒）
    public static final String STATUS_BAROMETER = "status_barometer";
    public static final String FREQUENCY_BAROMETER = "frequency_barometer";

//CPU状态，频率（默认10秒）
    public static final String STATUS_PROCESSOR = "status_processor";
    public static final String FREQUENCY_PROCESSOR = "frequency_processor";

//时区状态，频率
    public static final String STATUS_TIMEZONE = "status_timezone";
    public static final String FREQUENCY_TIMEZONE = "frequency_timezone";

//临近状态，频率
    public static final String STATUS_PROXIMITY = "status_proximity";
    public static final String FREQUENCY_PROXIMITY = "frequency_proximity";

//旋转状态，频率
    public static final String STATUS_ROTATION = "status_rotation";
    public static final String FREQUENCY_ROTATION = "frequency_rotation";

//屏幕的状态
    public static final String STATUS_SCREEN = "status_screen";

//温度，频率
    public static final String STATUS_TEMPERATURE = "status_temperature";
    public static final String FREQUENCY_TEMPERATURE = "frequency_temperature";

//电话通信服务的状态（）
    public static final String STATUS_TELEPHONY = "status_telephony";

//WiFi状态，频率
    public static final String STATUS_WIFI = "status_wifi";
    public static final String FREQUENCY_WIFI = "frequency_wifi";

//网络服务的状态，服务者，是否只用WiFi，服务频率
    public static final String STATUS_WEBSERVICE = "status_webservice";
    public static final String WEBSERVICE_SERVER = "webservice_server";
    public static final String WEBSERVICE_WIFI_ONLY = "webservice_wifi_only";
    public static final String FREQUENCY_WEBSERVICE = "frequency_webservice";

//清理历史数据的频率
    public static final String FREQUENCY_CLEAN_OLD_DATA = "frequency_clean_old_data";






    public static Context sContext;
    public static Activity mainactive;
    public static SensorManager sensorMana;
    public static final Core_Activity coreActivity=Core_Activity.getService();

    public static final int DIALOG_ERROR_ACCESSIBILITY=1;
    public static final int DIALOG_ERROR_MISSING_PARAMETERS=2;
    public static final int DIALOG_ERROR_MISSING_SENSOR=3;

    @Override
    protected Dialog onCreateDialog(int id)
    {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch(id) {
            case DIALOG_ERROR_ACCESSIBILITY:
                builder.setMessage("Please activate aware on the Accessibility Services!");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent accessibilitySettings = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        accessibilitySettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT );
                        startActivity(accessibilitySettings);
                    }
                });
                dialog = builder.create();
                break;
            case DIALOG_ERROR_MISSING_PARAMETERS:
                builder.setMessage("Some parameters are missing...");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog = builder.create();
                break;
            case DIALOG_ERROR_MISSING_SENSOR:
                builder.setMessage("This device is missing this sensor.");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog = builder.create();
                break;
        }
        return dialog;
    }

    public static class SettingsFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.layout_main);

            servicesOptions();

        }

        private void servicesOptions()
        {

            low_data();
            battery();
            locations();
            light();
        }

        private void low_data()
        {
/**勾选内存不足，定义轻量级的输入输出流*/
            final CheckBoxPreference debug_db_slow = (CheckBoxPreference) findPreference(MainActivity.DEBUG_DB_SLOW);
            debug_db_slow.setChecked(Core_Activity.getSetting(sContext, MainActivity.DEBUG_DB_SLOW).equals("true"));
            debug_db_slow.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Core_Activity.setSetting(sContext, MainActivity.DEBUG_DB_SLOW, debug_db_slow.isChecked());
                    return true;
                }
            });
        }

        private void battery()
        {
            final CheckBoxPreference battery = (CheckBoxPreference) findPreference( MainActivity.STATUS_BATTERY );
            battery.setChecked(Core_Activity.getSetting(sContext, MainActivity.STATUS_BATTERY).equals("true"));
            battery.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Core_Activity.setSetting(sContext, MainActivity.STATUS_BATTERY, battery.isChecked());
                    if(battery.isChecked()) {
                        battery.setSummary("记录电池的使用情况       已激活");
                        coreActivity.startBattery();
                    }else {
                        battery.setSummary("记录电池的使用情况       未激活");
                        coreActivity.stopBattery();
                    }
                    return true;
                }
            });
        }

        private void locations()
        {
            final PreferenceScreen locations = (PreferenceScreen) findPreference("locations");
            if( is_watch ) {
                locations.setEnabled(false);
                return;
            }

            final CheckBoxPreference location_gps = (CheckBoxPreference) findPreference("status_location_gps");

            location_gps.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    LocationManager localMng = (LocationManager) sContext.getSystemService(LOCATION_SERVICE);
                    List<String> providers = localMng.getAllProviders();

                    if( ! providers.contains(LocationManager.GPS_PROVIDER) ) {
                        mainactive.showDialog(DIALOG_ERROR_MISSING_SENSOR);
                        location_gps.setChecked(false);
//   这个直接添入数据库
                        coreActivity.setSetting(sContext, "status_location_gps", false);
                        return false;
                    }
                    if(location_gps.isChecked()) {
                        coreActivity.startLocations();   //要是勾选框处于已勾状态，那就打开定位服务
                    }else {
                        coreActivity.stopLocations();   //要是勾选框处于没勾状态，那就关闭定位服务
                    }
                    return true;
                }
            });

            final CheckBoxPreference location_network = (CheckBoxPreference) findPreference("status_location_network");
            location_network.setChecked(Core_Activity.getSetting(sContext, "status_location_network").equals("true"));
            location_network.setChecked(false);
            location_network.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    LocationManager localMng = (LocationManager) sContext.getSystemService(LOCATION_SERVICE);
                    List<String> providers = localMng.getAllProviders();

                    if( ! providers.contains(LocationManager.NETWORK_PROVIDER) ) {
                        mainactive.showDialog(DIALOG_ERROR_MISSING_SENSOR);
                        location_gps.setChecked(false);
//将设置信息添加到数据库中
                        Core_Activity.setSetting(sContext, "status_location_network", false);
                        return false;
                    }
                    if(location_network.isChecked()) {
                        coreActivity.startLocations();
                    }else {
                        coreActivity.stopLocations();
                    }
                    return true;
                }
            });
//GPS定位的频率编辑
            final EditTextPreference gpsInterval = (EditTextPreference) findPreference("frequency_location_gps");

//监听参数设置的变化
            gpsInterval.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Core_Activity.setSetting(sContext, "frequency_location_gps", (String) newValue);
                    gpsInterval.setSummary((String) newValue + " seconds");
                    coreActivity.startLocations();
                    return true;
                }
            });

//网络定位的频率编辑
            final EditTextPreference networkInterval = (EditTextPreference) findPreference("frequency_location_network");

            networkInterval.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Core_Activity.setSetting(sContext, "frequency_location_network", (String) newValue);
                    networkInterval.setSummary((String) newValue + " seconds");

                    return true;
                }
            });
//GPS最小的定位精度
            final EditTextPreference gpsAccuracy = (EditTextPreference) findPreference("min_location_gps_accuracy");

            gpsAccuracy.setText(Core_Activity.getSetting(sContext, "min_location_gps_accuracy"));
            gpsAccuracy.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Core_Activity.setSetting(sContext, "min_location_gps_accuracy", (String) newValue);
                    gpsAccuracy.setSummary((String) newValue + " meters");
                    coreActivity.startLocations();
                    return true;
                }
            });
//网络定位的精度
            final EditTextPreference networkAccuracy = (EditTextPreference) findPreference("min_location_network_accuracy");
            if( Core_Activity.getSetting(sContext, "min_location_network_accuracy").length() > 0 ) {
                networkAccuracy.setSummary(Core_Activity.getSetting(sContext, "min_location_network_accuracy") + " meters");
            }
            networkAccuracy.setText(Core_Activity.getSetting(sContext,"min_location_network_accuracy"));
            networkAccuracy.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Core_Activity.setSetting(sContext, "min_location_network_accuracy", (String) newValue);
                    networkAccuracy.setSummary((String) newValue + " meters");
                    coreActivity.startLocations();
                    return true;
                }
            });

            final EditTextPreference expirateTime = (EditTextPreference) findPreference("location_expiration_time");
            if( Core_Activity.getSetting(sContext, "location_expiration_time").length() > 0 ) {
                expirateTime.setSummary(Core_Activity.getSetting(sContext, "location_expiration_time") + " seconds");
            }
            expirateTime.setText(Core_Activity.getSetting(sContext, "location_expiration_time"));
            expirateTime.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Core_Activity.setSetting(sContext, "location_expiration_time", (String) newValue);
                    expirateTime.setSummary((String) newValue + " seconds");
                    coreActivity.startLocations();
                    return true;
                }
            });
        }

        private void light()
        {
            final PreferenceScreen light_pref = (PreferenceScreen) findPreference("light");
            Sensor temp = sensorMana.getDefaultSensor(Sensor.TYPE_LIGHT);
            if( temp != null ) {
                light_pref.setSummary(light_pref.getSummary().toString().replace("*", " - Power: " + temp.getPower() +" mA"));
            } else {
                light_pref.setSummary(light_pref.getSummary().toString().replace("*", ""));
                light_pref.setEnabled(false);
                return;
            }

            final CheckBoxPreference light = (CheckBoxPreference) findPreference(
                    MainActivity.STATUS_LIGHT);
            light.setChecked(Core_Activity.getSetting(sContext,
                    MainActivity.STATUS_LIGHT).equals("true"));
            light.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if( sensorMana.getDefaultSensor(Sensor.TYPE_LIGHT) == null ) {
                        mainactive.showDialog(DIALOG_ERROR_MISSING_SENSOR);
                        light.setChecked(false);
                        Core_Activity.setSetting(sContext,
                                MainActivity.STATUS_LIGHT, false);
                        return false;
                    }

                    Core_Activity.setSetting(sContext, MainActivity.STATUS_LIGHT,
                            light.isChecked());
                    if(light.isChecked()) {

                        coreActivity.startLight();
                    }else {
                        coreActivity.stopLight();
                    }
                    return true;
                }
            });

            final ListPreference frequency_light = (ListPreference) findPreference(
                    FREQUENCY_LIGHT );
            if( Core_Activity.getSetting(sContext,
                    MainActivity.FREQUENCY_LIGHT).length() > 0 )
            {
                String freq = Core_Activity.getSetting(sContext,
                        MainActivity.FREQUENCY_LIGHT);
                frequency_light.setSummary(freq);
            }
            frequency_light.setDefaultValue(Core_Activity.getSetting(sContext,
                    FREQUENCY_LIGHT));
            frequency_light.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Core_Activity.setSetting(sContext,FREQUENCY_LIGHT, (String) newValue);
                    frequency_light.setSummary( (String)newValue);
                    coreActivity.startLight();
                    return true;
                }
            });

            final EditTextPreference current_light = (EditTextPreference) findPreference(MainActivity.CURRENT_LIGHT);
            current_light.setSummary("光感值:" + Core_Activity.getSetting(sContext, DEVICE_ID));
//            final Sensor currentlight=sensorMana.getDefaultSensor(Sensor.TYPE_LIGHT);
//   //          current_light.setSummary("光感值:" + currentlight.getResolution());
            current_light.setText(Core_Activity.getSetting(sContext, MainActivity.DEVICE_ID));
            current_light.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Core_Activity.setSetting(sContext, MainActivity.DEVICE_ID, (String) newValue);
                    current_light.setSummary("光感值: " + Core_Activity.getSetting(sContext, MainActivity.DEVICE_ID));
//                    current_light.setSummary("光感值:" + currentlight.getResolution());
                    return true;
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        /**测试成功，但必须放在super前面，否则会出现如背景丢失等错误*/
        setTheme(R.style.perference_set_activity);

        super.onCreate(savedInstanceState);


        is_watch=Core_Activity.is_watch(this);
        sContext=getApplicationContext();
        mainactive=this;
        sensorMana=(SensorManager)  getSystemService(SENSOR_SERVICE);

        //开始启动Share主服务
        Intent startAware = new Intent( getApplicationContext(), Core_Activity.class );
        startService(startAware);

        SharedPreferences prefs = getSharedPreferences( getPackageName(), Context.MODE_PRIVATE );

        //如果页面里的设置状态全为空，那就要进行默认值的设置
        if( prefs.getAll().isEmpty() && Core_Activity.getSetting(
                getApplicationContext(), MainActivity.DEVICE_ID).length() == 0 )
        {
            PreferenceManager.setDefaultValues(getApplicationContext(),
                    getPackageName(), Context.MODE_PRIVATE, R.xml.layout_main, true);
            //提交更改
            prefs.edit().commit();
        } else {
            //否则还要用到
            PreferenceManager.setDefaultValues(getApplicationContext(),
                    getPackageName(), Context.MODE_PRIVATE, R.xml.layout_main, false);
        }

        Map<String,?> defaults = prefs.getAll();
        for(Map.Entry<String, ?> entry : defaults.entrySet()) {
            if( Core_Activity.getSetting(getApplicationContext(), entry.getKey()).length() == 0 ) {
                Core_Activity.setSetting(getApplicationContext(), entry.getKey(), entry.getValue());
            }
        }
//要用到生成的设备ID，进行针对性的设置
        if( Core_Activity.getSetting(getApplicationContext(),
                MainActivity.DEVICE_ID).length() == 0 )
        {
            UUID uuid = UUID.randomUUID();
            Core_Activity.setSetting(getApplicationContext(),
                    MainActivity.DEVICE_ID, uuid.toString());
        }

        setContentView(R.layout.activity_main);

        getSupportFragmentManager().executePendingTransactions();
    }

    //这里的不完善，重新启动时的回调
    @Override
    protected void onResume()
    {
        super.onResume();
/*这里是什么鬼？？？？？？？*/
//        if( Share.getSetting( getApplicationContext()
    }
}
