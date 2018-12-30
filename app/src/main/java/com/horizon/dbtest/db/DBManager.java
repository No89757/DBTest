package com.horizon.dbtest.db;

import android.database.sqlite.SQLiteDatabase;

import com.horizon.dbtest.data.TestData;
import com.horizon.dbtest.manager.PathManager;


public class DBManager  {
    private static final String USER_DB_NAME = "userdata.sqlite";
    private SQLiteDatabase userDb;
    private long userId;

    private static final DBManager INSTANCE = new DBManager();

    private DBManager() {
    }

    public static DBManager getInstance() {
        return INSTANCE;
    }

    public synchronized SQLiteDatabase getUserDB() {
        if (userDb == null) {
            openUserBD();
        } else {
            long currentUserId = TestData.INSTANCE.getUserId();
            if(currentUserId  != userId){
                closeUserDB();
                openUserBD();
            }
        }
        return userDb;
    }

    private void openUserBD(){
        userId = TestData.INSTANCE.getUserId();
        String user = Long.toString(userId);
        UserDBOpenHelper openHelper = new UserDBOpenHelper(PathManager.getUserPath(user) + "/"+USER_DB_NAME);
        UserDBOpenHelper tempOpenHelper =  new UserDBOpenHelper(PathManager.getUserPath(user) + "/temp_"+USER_DB_NAME);
        DBHelper.fixDbIfNecessary(openHelper, tempOpenHelper);
        userDb = openHelper.getDatabase();
    }

    private void closeUserDB() {
        if (userDb != null) {
            userDb.close();
            userDb = null;
        }
    }

}
