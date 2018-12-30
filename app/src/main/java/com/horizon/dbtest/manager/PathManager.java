package com.horizon.dbtest.manager;

import android.text.TextUtils;

import com.horizon.dbtest.DBTestApp;

import java.io.File;

public class PathManager {
    private static String sFilesDir = "";

    public static String getFilesDir() {
        if(TextUtils.isEmpty(sFilesDir)){
            File file = DBTestApp.getAppContext().getFilesDir();
            if (file != null) {
                sFilesDir = file.getAbsolutePath();
            }
            if(TextUtils.isEmpty(sFilesDir)){
                sFilesDir =  "/data/data/" + DBTestApp.getAppContext().getPackageName() + "/files";
            }
        }
        return sFilesDir;
    }

    public static String getUserPath(String user) {
        String root = getFilesDir() + "/userdata";
        if (TextUtils.isEmpty(user)) {
            return root;
        } else {
            return root + "/" + user;
        }
    }
}
