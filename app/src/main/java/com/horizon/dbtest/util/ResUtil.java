package com.horizon.dbtest.util;

import com.horizon.dbtest.DBTestApp;

public class ResUtil {
    private static final String TAG = "ResUtil";

    public static String getAssetsString(String name) {
        try {
            return IOUtil.streamToString(DBTestApp.getAppContext().getAssets().open(name));
        } catch (Exception e) {
            LogUtil.e(TAG, e);
        }
        return "";
    }
}
