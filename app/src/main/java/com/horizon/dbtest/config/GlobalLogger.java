package com.horizon.dbtest.config;

import com.horizon.lightkv.LightKV;

public class GlobalLogger implements LightKV.Logger   {
    private static final GlobalLogger INSTANCE = new GlobalLogger();

    private GlobalLogger() {

    }

    public static GlobalLogger getInstance() {
        return INSTANCE;
    }

    @Override
    public void e(String tag, Throwable e) {

    }
}
