package com.sensorslife.UI_Managers;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.sensorslife.R;

/**
 * Created by Administrator on 2017/8/25.
 * 链接到关于我们的网络，或直接用文本框显示出团队的信息
 * 以及 软件的信息
 */

public class About_Activity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_about);

        TextView about_title=(TextView) findViewById(R.id.about_title);
        CharSequence sequence=getTitle();
        about_title.setText(sequence);

        WebView about_us = (WebView)findViewById(R.id.about_us);
        WebSettings settings = about_us.getSettings();
        settings.setJavaScriptEnabled(true);
        about_us.loadUrl("http://www.github.com/");
    }
}
