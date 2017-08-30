package com.sensorslife.UI_Managers;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.sensorslife.MainActivity;
import com.sensorslife.R;

/**
 * Created by Administrator on 2017/8/25.
 * 显示导航栏，及其点击事件的处理（实现页面跳转）
 */

public class Navigation extends ActionBarActivity {
    //

    private DrawerLayout navigationDrawer;
    private ListView navigationList;
    private ActionBarDrawerToggle navigationToggle;
    public static Toolbar toolbar;

    @Override
    public void setContentView(int layoutResID)
    {
    //设置标题
        ViewGroup contentView=(ViewGroup) LayoutInflater.from(this).inflate(
                layoutResID,
                (ViewGroup) getWindow().getDecorView().getRootView(),
                false
        );

        toolbar=(Toolbar) contentView.findViewById(R.id.share_toolbar);
//        CharSequence sequence=getTitle();
//        toolbar.setTitle("设置");
//        setSupportActionBar(toolbar);

        //测试导航栏
        navigationDrawer = (DrawerLayout) contentView.findViewById(R.id.share_ui_main);
        navigationList = (ListView) contentView.findViewById(R.id.share_navigation);

        navigationToggle = new ActionBarDrawerToggle( Navigation.this
                , navigationDrawer, toolbar, R.string.drawer_open
                , R.string.drawer_close );
        navigationDrawer.setDrawerListener(navigationToggle);

        String[] options = {"传感器信息", "信息设置", "检查更新", "关于我们"};
        NavigationAdapter nav_adapter = new NavigationAdapter( getApplicationContext(), options);
        navigationList.setAdapter(nav_adapter);
        navigationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                LinearLayout item_container = (LinearLayout) view.findViewById(R.id.nav_container);
                item_container.setBackgroundColor(Color.DKGRAY);

                for( int i=0; i< navigationList.getChildCount(); i++ ) {
                    if( i != position ) {
                        LinearLayout other = (LinearLayout) navigationList.getChildAt(i);
                        LinearLayout other_item = (LinearLayout) other.findViewById(R.id.nav_container);
                        other_item.setBackgroundColor(Color.TRANSPARENT);
                    }
                }

                ActivityOptionsCompat options =ActivityOptionsCompat
                        .makeCustomAnimation(Navigation.this,
                                R.anim.slide_in_left,
                                R.anim.slide_out_left);
                Bundle animations = options.toBundle();

                switch( position ) {
                    case 0: //传感器信息
                        Intent sensors_info = new Intent( Navigation.this, Current_Information.class );
                        ActivityCompat.startActivity(Navigation.this,sensors_info,animations);
                        break;
                    case 1: //信息设置
                        Intent sensors_ui = new Intent( Navigation.this, MainActivity.class );
                        ActivityCompat.startActivity(Navigation.this,sensors_ui,animations);
                        break;
                    case 2: //检查更新
                        //切换到Plugins界面
                        break;
                    case 3: //关于我们
                        Intent about_us = new Intent(Navigation.this, About_Activity.class);
                        startActivity(about_us);
                        break;
                }
                navigationDrawer.closeDrawer(navigationList);
            }
        });

        setContentView(contentView);
    }
/*此处菜单的显示，不需要了
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.mainmenu, menu);

        //先检查是否有相机，要是没有，那就将这个功能设为不可用
//            if( Aware.is_watch(this) ) {
//        MenuItem qrcode = menu.findItem(R.id.aware_qrcode);
//        qrcode.setVisible(true);
//            }
        return super.onCreateOptionsMenu(menu);
    }
    //菜单选项 相应的动作，即跳转链接
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if( item != null && item.getTitle() != null ){
            if( item.getTitle().equals(getString(R.string.qrcode)) ) {
                //用消息提示框简单提出来
                Toast.makeText(this,"已点击扫描",Toast.LENGTH_SHORT);
            }
            if( item.getTitle().equals(getString(R.string.team)) ) {
                Intent about_us = new Intent(toolbaractivity.this, AboutActivity.class);
                startActivity(about_us);
//                Toast.makeText(this,"链接到关于我们界面",Toast.LENGTH_SHORT);
            }
        }
        switch (item.getItemId()) {
            case android.R.id.home: onBackPressed(); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }
*/

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        navigationToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        navigationToggle.onConfigurationChanged(newConfig);
    }

    /**
     * 导航栏适配器,加载导航栏的界面
     */
    public class NavigationAdapter extends ArrayAdapter<String> {
        private final String[] items;
        private final LayoutInflater inflater;
        private final Context context;

        public NavigationAdapter(Context context, String[] items) {
            super(context, R.layout.navigation_item, items);
            this.context = context;
            this.items = items;
            this.inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent)
        {
            LinearLayout row = (LinearLayout) inflater.inflate(R.layout.navigation_item, parent, false);
            row.setFocusable(false);
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //界面切换时的动画
                    ActivityOptionsCompat options =
                            ActivityOptionsCompat.makeCustomAnimation(
                                    Navigation.this,
                                    R.anim.slide_in_left,
                                    R.anim.slide_out_left);
                    Bundle animations = options.toBundle();

                    switch( position ) {
                        case 0: //传感器信息
                            Intent sensors_info = new Intent( Navigation.this, Current_Information.class );
                            ActivityCompat.startActivity(Navigation.this,sensors_info,animations);
                            break;
                        case 1: //信息设置
                            Intent sensors_ui = new Intent( Navigation.this, MainActivity.class );
                            ActivityCompat.startActivity(Navigation.this,sensors_ui,animations);//切换到设置界面
                            break;
                        case 2: //检查更新
                            //
                            break;
                        case 3: //关于我们
                            //
                            Intent about_us = new Intent(Navigation.this, About_Activity.class);
                            startActivity(about_us);
                            break;
                    }
                    navigationDrawer.closeDrawer(navigationList);
                }
            });

            //导航栏界面的加载
            ImageView nav_icon = (ImageView) row.findViewById(R.id.nav_placeholder);
            TextView nav_title = (TextView) row.findViewById(R.id.nav_title);

            switch( position ) {
                case 0:
                    nav_icon.setImageResource(R.drawable.sensor_icon);
                    if( context.getClass().getSimpleName().equals("Current_Information") ) {
                        row.setBackgroundColor(Color.DKGRAY);
                    }
                    break;
                case 1:
                    nav_icon.setImageResource(R.drawable.sensors_icon);
                    if( context.getClass().getSimpleName().equals("MainActivity")) {
                        row.setBackgroundColor(Color.DKGRAY);
                    }
                    break;
                case 2:
                    nav_icon.setImageResource(R.drawable.update_icon);
                    if( context.getClass().getSimpleName().equals("检查更新")) {
                        row.setBackgroundColor(Color.DKGRAY);
                    }
                    break;
                case 3:
                    nav_icon.setImageResource(R.drawable.team_icon);
                    if( context.getClass().getSimpleName().equals("About_Activity")) {
                        row.setBackgroundColor(Color.DKGRAY);
                    }
                    break;
            }
            String item = items[position];
            nav_title.setText(item);

            return row;
        }
    }

}
