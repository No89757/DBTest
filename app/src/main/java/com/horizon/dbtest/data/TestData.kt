package com.horizon.dbtest.data

import android.os.AsyncTask
import com.horizon.dbtest.DBTestApp
import com.horizon.dbtest.config.GlobalLogger
import com.horizon.lightkv.KVData
import com.horizon.lightkv.LightKV

object TestData : KVData(){
    override val data: LightKV by lazy {
        LightKV.Builder(DBTestApp.getAppContext(), "test_data")
                .logger(GlobalLogger.getInstance())
                .executor(AsyncTask.THREAD_POOL_EXECUTOR)
                .sync()
    }

    var userId by long(1)
    var seq by long(2)
}