package com.horizon.dbtest.util;

import android.util.Log;

import com.horizon.dbtest.BuildConfig;

public class LogUtil {

    public static void d(String tag, String msg) {
        if(BuildConfig.DEBUG){
            Log.d(tag, msg);
        }
    }

    public static void e(String tag, Throwable tr) {
        Log.e(tag, tr.getMessage(), tr);
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
    }
}
