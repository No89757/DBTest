package com.horizon.dbtest;

import android.app.Application;
import android.content.Context;

public class DBTestApp extends Application {

    private static Context appContext;

    public static Context getAppContext(){
        return appContext;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
       // appContext = getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
    }
}
