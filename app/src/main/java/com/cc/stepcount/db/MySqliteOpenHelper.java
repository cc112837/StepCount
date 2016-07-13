package com.cc.stepcount.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cc.stepcount.FinalValue;

/**
 * 类描述：
 * 创建人：吴聪聪
 * 邮箱：cc112837@163.com
 * 创建时间：2016/4/21 14:28
 * 修改人：Administrator
 * 修改时间：2016/4/21 14:28
 * 修改备注：
 */
public class MySqliteOpenHelper extends SQLiteOpenHelper{
    public MySqliteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "+ FinalValue.TB_STEP+"(" +
                "_id integer primary key autoincrement," +
                "title varchar,"+"day varchar,"+"time varchar)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
