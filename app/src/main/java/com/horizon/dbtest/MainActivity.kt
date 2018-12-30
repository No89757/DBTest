package com.horizon.dbtest

import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.horizon.dbtest.data.TestData
import com.horizon.dbtest.db.DBManager
import com.horizon.dbtest.util.LogUtil

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (TestData.userId == 0L) {
            TestData.userId = 10000L
        }

        TestTask().execute()
    }

    inner class TestTask : AsyncTask<Void, Void, Long>() {
        override fun doInBackground(vararg params: Void?): Long {
            val seq = TestData.seq + 1
            TestData.seq = seq

            val db = DBManager.getInstance().userDB
            db.execSQL("insert into t_category values(?,?)", arrayOf(seq, seq.toString()))

            val c = db.rawQuery("select count(*) from t_category", null);
            try {
                if (c.moveToFirst()) {
                    val recordCount = c.getLong(0);
                    LogUtil.d("MainActivity", "recordCount: " + recordCount)
                    return recordCount;
                }
            } finally {
                c.close();
            }
            return 0L;
        }

        override fun onPostExecute(result: Long) {
            findViewById<TextView>(R.id.test_tv)?.text = result.toString()
        }
    }
}
