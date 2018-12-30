package com.horizon.dbtest.db;

import android.database.sqlite.SQLiteDatabase;

import com.horizon.dbtest.DBTestApp;
import com.horizon.dbtest.util.ResUtil;

public class UserDBOpenHelper extends BaseDBOpenHelper {
    private static final int DATABASE_VERSION = 1;

    public UserDBOpenHelper(String name) {
        super(DBTestApp.getAppContext(), name, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String text = ResUtil.getAssetsString("db_v1.txt");
        String[] sqlArray = text.split("---");
        for(String sql : sqlArray){
            db.execSQL(sql);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
