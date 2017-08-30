package com.sensorslife;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sensorslife.UI_Managers.Current_Information;

/**
 * Created by Administrator on 2017/8/29.
 */

public class Main_Screen extends ActionBarActivity {

    //
    public Activity activity;
    public Context fullContext;
    Toolbar toolbar;

    CardView card_data;
    CardView card_settings;
    @Override
    public void onCreate(Bundle savedInstanceValue)
    {
        super.onCreate(savedInstanceValue);
        setContentView(R.layout.main_screen);

        activity=this;
        fullContext=getApplicationContext();


        //设置的卡片，跳转至主设置界面
        card_data=(CardView) findViewById(R.id.card_data);
        card_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_data=new Intent(Main_Screen.this,Current_Information.class);
                startActivity(intent_data);
            }
        });

//设置的卡片，跳转至主设置界面
        card_settings=(CardView) findViewById(R.id.card_settings);
        card_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_settings=new Intent(Main_Screen.this,MainActivity.class);
                startActivity(intent_settings);
            }
        });
    }

    /**设置标题栏*/
    @Override
    public void setContentView(int layoutResID)
    {

        ViewGroup contentView = (ViewGroup) LayoutInflater.from(this).inflate(
                layoutResID,
                (ViewGroup) getWindow().getDecorView().getRootView(),
                false
        );

        toolbar = (Toolbar) contentView.findViewById(R.id.share_toolbar);
    //去掉左上角的返回图标
        toolbar.setNavigationIcon(null);

        setContentView(contentView);
    }
}
