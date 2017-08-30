package com.sensorslife.CommonUtils;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by Administrator on 2017/8/25.
 *
 * * 数据库链接类，确保拥有最新版本的数据库。只负责数据库的访问，不涉及数据库具体操作
 * 函数有：  本类实例化DatabaseConnect(context,DBname,CursorFactory,DBVersion,Tables,Fields)，并创建了一个文件夹
 *          初始化onCreate(SqliteDatabase)、
 *          数据库升级onUpgrade(SqliteDatabase,oldVersion,new)
 *          获取数据库写入权限getWritableDatabase()
 *          获取数据库读取权限getReadableDatabase()
 */

public class DatabaseConnect extends SQLiteOpenHelper {

    //用于写入日志中，调试及其标签
    private final boolean DEBUG = true;
    private final String TAG = "DatabaseConnect";

    private final String database_name;
    private final String[] database_tables;
    private final String[] table_fields;
    private final int new_version;

    private SQLiteDatabase database = null;

    public DatabaseConnect(Context context,
                           String database_name,
                           SQLiteDatabase.CursorFactory cursor_factory,
                           int database_version,
                           String[] database_tables, String[] table_fields)
    {
        super(context, database_name, cursor_factory, database_version);

        this.database_name = database_name;
        this.database_tables = database_tables;
        this.table_fields = table_fields;
        this.new_version = database_version;

        //创建一个文件夹，用于存放所有的数据库文件
        // 怎么需要存放到外存储设备，况且还是有那么多手机用户，并没有扩展存储卡
        // 还是只是不存在系统文件系统中
        /**测试中，获取外部存储有困难，
         * 先进行直接保存在系统默认的数据库中Environment.getDataDirectory()
         * 其实，这个获取的外存储，就是手机自带的存储，只不过，不与系统保存在一起
         * */
        File folders = new File(Environment.getExternalStorageDirectory()
                .toString()+"/sensorslife/");
        folders.mkdirs();//文件夹执行生成指令
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if(DEBUG) Log.w(TAG, "Database in use: " + db.getPath());

        for (int i=0; i < database_tables.length;i++)
        {
            db.execSQL("CREATE TABLE IF NOT EXISTS "+database_tables[i] +" ("+table_fields[i]+");");
        }
        db.setVersion(new_version);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(DEBUG) Log.w(TAG, "Upgrading database: " + db.getPath());
        //备份数据操作，先删除再创建，是MySQL语句
        for (int i=0; i < database_tables.length;i++)
        {
            db.execSQL("DROP TABLE IF EXISTS "+database_tables[i]);
        }
        onCreate(db);
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        if( database != null ) {
            if( ! database.isOpen() )
                database = null;
            else if ( ! database.isReadOnly() )
                //说明数据库已经打开了，而且也并非只读权限。
                // 为了执行效率，不用再操作了
                return database;
        }

        //也许没有数据库文件
        // 所以要尝试创建一个数据库文件
        //不过，要检查创建的这个版本是不是最新的，要不是，就调用上面的函数进行升级操作
        File database_file = new File(database_name);
        try {
            SQLiteDatabase current_database;
            current_database = SQLiteDatabase.openDatabase(database_file.getPath(),
                    null, SQLiteDatabase.CREATE_IF_NECESSARY);
            int current_version = current_database.getVersion();

            if( current_version != new_version ) {
                current_database.beginTransaction();
                try {
                    if( current_version == 0 )
                        onCreate(current_database);
                    else {
                        onUpgrade(current_database, current_version, new_version);
                    }
                    current_database.setVersion(new_version);
                    current_database.setTransactionSuccessful();
                }finally {
                    current_database.endTransaction();
                }
            }
            onOpen(current_database);
            database = current_database;
            return database;
        } catch (SQLException e ) {
            return null;
        }
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        if( database != null ) {
            if( ! database.isOpen() )
                database = null;
            else if ( ! database.isReadOnly() )
                return database;
        }

        try {
            return getWritableDatabase();
        } catch( SQLException e ) {
            //尝试以只读的方式去测试数据库
        }

        //要是没有数据库文件的话，就创建一个吧
        File database_file = new File(database_name);
        SQLiteDatabase current_database = SQLiteDatabase.openDatabase(
                database_file.getPath(), null, SQLiteDatabase.OPEN_READONLY);
        onOpen(current_database);
        database = current_database;
        return database;
    }
}
