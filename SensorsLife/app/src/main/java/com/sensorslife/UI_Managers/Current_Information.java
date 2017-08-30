package com.sensorslife.UI_Managers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.sensorslife.Core_Activity;
import com.sensorslife.MainActivity;
import com.sensorslife.R;

import java.text.SimpleDateFormat;

import static android.R.attr.color;
import static android.R.attr.colorError;
import static android.view.Gravity.CENTER;

/**
 * Created by Administrator on 2017/8/25.
 *
 *  * 显示当前手机所有传感器的信息，实时数据的显示，不涉及数据库
 */

public class Current_Information extends ActionBarActivity implements SensorEventListener {
    //
    public static Context fullcontext;
    // 定义Sensor管理器
    private SensorManager mSensorManager;
    private LocationManager locationManager;

    public Activity activity=this;
    public static Toolbar toolbar;
    TextView title;
    EditText showGPS;
    EditText data_UUID;
    EditText etOrientation;
    EditText etGyro;
    EditText etMagnetic;
    EditText etGravity;
    EditText etLinearAcc;
    EditText etTemerature;
    EditText etLight;
    EditText etPressure;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_sensor);

        fullcontext=getApplicationContext();
        // 获取界面上的EditText组件
        title=(TextView) findViewById(R.id.information_title);
        data_UUID=(EditText) findViewById(R.id.data_UUID);
        etLight = (EditText) findViewById(R.id.data_light);
        showGPS=(EditText) findViewById(R.id.data_GPS);
        etOrientation = (EditText) findViewById(R.id.data_orientation);
        etGyro = (EditText) findViewById(R.id.data_Gyro);
        etMagnetic = (EditText) findViewById(R.id.data_Magnetic);
        etGravity = (EditText) findViewById(R.id.data_Gravity);
        etLinearAcc = (EditText) findViewById(R.id.data_Linear);
        etTemerature = (EditText) findViewById(R.id.data_Temperature);
        etPressure = (EditText) findViewById(R.id.data_Pressure);

        //显示当前时间
        //time.setText(""+new java.util.Date());
        //实时监听时间变化
/*       CountDownTimer timer=new CountDownTimer(Integer.MAX_VALUE,1000)
        {
            @Override
            public void onTick(long millisUntilFinished) {
                //每秒更新时间
                //refreshTime();
                SimpleDateFormat dateFormat=new SimpleDateFormat
                        ("yyyy-MM-dd HH:mm:ss");
                String date=dateFormat.format(new java.util.Date());
                time.setText(""+date);
            }

            @Override
            public void onFinish() {
            }
        };
        timer.start();
*/
        //设置页面主标题
        CharSequence sequence=getTitle();
        title.setText(sequence);

        //显示UUID
        String uuid=Core_Activity.getSetting(fullcontext, MainActivity.DEVICE_ID);
        if (Core_Activity.getSetting(fullcontext, MainActivity.DEVICE_ID)=="") {
//获取自定义的颜色资源
            uuid="未生成 UUID";
            data_UUID.setTextColor(ContextCompat.getColor(fullcontext, R.color.colorError));
        }
        else
            data_UUID.setTextColor(ContextCompat.getColor(fullcontext, R.color.colorBack));

        data_UUID.setText(uuid);

        // 获取传感器管理服务
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);  // ①
        locationManager=(LocationManager)getSystemService(LOCATION_SERVICE);
/*        Location location=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        //展现定位信息
        getGPS(location);
        //实时更新数据
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                3000, 8, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        getGPS(location);
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                        getGPS(locationManager
                                .getLastKnownLocation(provider));
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                        getGPS(null);
                    }
                });
   */

    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // 为系统的方向传感器注册监听器
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
        // 为系统的陀螺仪传感器注册监听器
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_GAME);
        // 为系统的磁场传感器注册监听器
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_GAME);
        // 为系统的重力传感器注册监听器
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                SensorManager.SENSOR_DELAY_GAME);
        // 为系统的线性加速度传感器注册监听器
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                SensorManager.SENSOR_DELAY_GAME);
        // 为系统的温度传感器注册监听器
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE),
                SensorManager.SENSOR_DELAY_GAME);
        // 为系统的光传感器注册监听器
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
                SensorManager.SENSOR_DELAY_GAME);
        // 为系统的压力传感器注册监听器
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE),
                SensorManager.SENSOR_DELAY_GAME);
    }
    @Override
    protected void onStop()
    {
        // 程序退出时取消注册传感器监听器
        mSensorManager.unregisterListener(this);
        super.onStop();
    }
    @Override
    protected void onPause()
    {
        // 程序暂停时取消注册传感器监听器
        mSensorManager.unregisterListener(this);
        super.onPause();
    }
    // 以下是实现SensorEventListener接口必须实现的方法
    @Override
    // 当传感器精度改变时回调该方法。
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }
    @Override
    public void onSensorChanged(SensorEvent event)
    {
        float[] values = event.values;
        // 获取触发event的传感器类型
        int sensorType = event.sensor.getType();
        StringBuilder sb = null;

        // 判断是哪个传感器发生改变
        switch (sensorType)
        {
            // 光传感器
            case Sensor.TYPE_LIGHT:
                sb = new StringBuilder();
                sb.append(Math.round(values[0]));
                etLight.setText(sb.toString()+" lux");
                break;
            // 方向传感器
            case Sensor.TYPE_ORIENTATION:
                sb = new StringBuilder();
                sb.append("  绕X轴：");
                sb.append(Math.round(values[1]));
                sb.append("  绕Y轴：");
                sb.append(Math.round(values[2]));
                sb.append("  绕Z轴：");
                sb.append(Math.round(values[0]));
                etOrientation.setText(sb.toString());
                break;
            // 陀螺仪传感器
            case Sensor.TYPE_GYROSCOPE:
                sb = new StringBuilder();
                sb.append("绕X轴：");
                sb.append(Math.round(values[0]));
                sb.append("  绕Y轴：");
                sb.append(Math.round(values[1]));
                sb.append("  绕Z轴：");
                sb.append(Math.round(values[2]));
                etGyro.setText(sb.toString());
                break;
            // 磁场传感器
            case Sensor.TYPE_MAGNETIC_FIELD:
                sb = new StringBuilder();
                sb.append("X轴：");
                sb.append(Math.round(values[0]));
                sb.append("  Y轴：");
                sb.append(Math.round(values[1]));
                sb.append("  Z轴：");
                sb.append(Math.round(values[2]));
                etMagnetic.setText(sb.toString());
                break;
            // 重力传感器
            case Sensor.TYPE_GRAVITY:
                sb = new StringBuilder();
                sb.append("X轴：");
                sb.append(Math.round(values[0]));
                sb.append("  Y轴：");
                sb.append(Math.round(values[1]));
                sb.append("  Z轴：");
                sb.append(Math.round(values[2]));
                etGravity.setText(sb.toString());
                break;
            // 线性加速度传感器
            case Sensor.TYPE_LINEAR_ACCELERATION:
                sb = new StringBuilder();
                sb.append("X轴：");
                sb.append(Math.round(values[0]));
                sb.append("  Y轴：");
                sb.append(Math.round(values[1]));
                sb.append("  Z轴：");
                sb.append(Math.round(values[2]));
                etLinearAcc.setText(sb.toString());
                break;
            // 温度传感器
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                sb = new StringBuilder();
                sb.append(Math.round(values[0]));
                etTemerature.setText(sb.toString()+"  ℃");
                break;

            // 压力传感器
            case Sensor.TYPE_PRESSURE:
                sb = new StringBuilder();
                sb.append(Math.round(values[0]));
                etPressure.setText(sb.toString()+" Pa");
                break;
        }
    }
    //
/**/    //获取GPS数据
    public void getGPS(Location location)
    {
        StringBuilder string=new StringBuilder();
        if (location!=null)
        {
            string.append("经度："+Math.round(location.getLongitude()));
            string.append("  纬度："+Math.round(location.getLatitude()));
            string.append("\n海拔："+Math.round(location.getAltitude()));
            string.append("  精度："+Math.round(location.getAccuracy()));
        }
        else
        {
            //其实这里应该加一个时间相应机制，首先提示“正在获取中”，
            // 接着才是下面这个
            string.append("抱歉，还未获取到您的位置信息");
        }
        showGPS.setText(string);
    }

}
