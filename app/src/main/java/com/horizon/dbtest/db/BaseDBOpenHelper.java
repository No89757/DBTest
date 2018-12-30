package com.horizon.dbtest.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.horizon.dbtest.util.FileUtil;
import com.horizon.dbtest.util.LogUtil;

import java.io.File;
import java.io.IOException;

public abstract class BaseDBOpenHelper extends SQLiteOpenHelper {
    private String name;

    public BaseDBOpenHelper(Context context, String name, int version) {
        super(context, name, null, version);
        this.name = name;
    }

    public SQLiteDatabase getDatabase() {
        return getDatabase(true);
    }

    public SQLiteDatabase getDatabase(boolean enableWAL){
        File file = new File(name);
        try {
            FileUtil.makeFileIfNotExist(file);
        } catch (IOException e) {
            LogUtil.e("DBOpenHelper", e);
        }

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        if(enableWAL){
            sqLiteDatabase.enableWriteAheadLogging();
        }
        return sqLiteDatabase;
    }

}
