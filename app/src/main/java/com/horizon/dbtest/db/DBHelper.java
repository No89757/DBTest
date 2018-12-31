package com.horizon.dbtest.db;

import android.database.AbstractWindowedCursor;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.horizon.dbtest.util.IOUtil;
import com.horizon.dbtest.util.LogUtil;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DBHelper {
    private static final String TAG = "DbHelper";

    public static void closeCursor(Cursor c) {
        if (c != null && !c.isClosed()) {
            c.close();
        }
    }

    public static boolean isDbOk(SQLiteDatabase db) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("PRAGMA integrity_check", null);
            if (cursor.moveToFirst()) {
                return "ok".equalsIgnoreCase(cursor.getString(0));
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return false;
    }

    private static List<String> getTables(SQLiteDatabase desDb) {
        String sql = "SELECT name FROM sqlite_master " +
                "WHERE type='table' AND name!='android_metadata'";
        Cursor c = desDb.rawQuery(sql, null);
        try {
            List<String> tables = new ArrayList<>(c.getCount());
            while (c.moveToNext()) {
                tables.add(c.getString(0));
            }
            return tables;
        } finally {
            closeCursor(c);
        }
    }

    private static TableData getData(SQLiteDatabase srcDb, String sql) {
        Cursor c = srcDb.rawQuery(sql, null);
        try {
            int rawCount = c.getCount();
            if (rawCount <= 0) {
                return null;
            }
            int columnCount = c.getColumnCount();
            TableData tableData = new TableData();
            tableData.row = rawCount;
            tableData.column = columnCount;
            tableData.data = new Object[rawCount * columnCount];

            int row = 0;
            if (c instanceof AbstractWindowedCursor) {
                final AbstractWindowedCursor windowedCursor = (AbstractWindowedCursor) c;
                while (windowedCursor.moveToNext()) {
                    for (int i = 0; i < columnCount; i++) {
                        int index = row * columnCount + i;
                        if (windowedCursor.isBlob(i)) {
                            tableData.data[index] = windowedCursor.getBlob(i);
                        } else if (windowedCursor.isFloat(i)) {
                            tableData.data[index] = windowedCursor.getDouble(i);
                        } else if (windowedCursor.isLong(i)) {
                            tableData.data[index] = windowedCursor.getLong(i);
                        } else if (windowedCursor.isNull(i)) {
                            tableData.data[index] = null;
                        } else if (windowedCursor.isString(i)) {
                            tableData.data[index] = windowedCursor.getString(i);
                        } else {
                            tableData.data[index] = windowedCursor.getString(i);
                        }
                    }
                    row++;
                }
            } else {
                while (c.moveToNext()) {
                    for (int i = 0; i < columnCount; i++) {
                        int index = row * columnCount + i;
                        tableData.data[index] = c.getString(i);
                    }
                    row++;
                }
            }

            return tableData;
        } finally {
            closeCursor(c);
        }
    }

    private static void insertToDb(SQLiteDatabase desDb,
                                   String sql,
                                   Object[] values,
                                   int rows,
                                   int columns) {
        if (values == null || columns <= 0 || rows <= 0 || values.length < (rows * columns)) {
            return;
        }
        SQLiteStatement statement = desDb.compileStatement(sql);
        try {
            for (int i = 0; i < rows; i++) {
                bindValues(statement, values, i, columns);
                try {
                    statement.executeInsert();
                } catch (SQLiteConstraintException e) {
                    LogUtil.e(TAG, e);
                }
                statement.clearBindings();
            }
        } finally {
            IOUtil.closeQuietly(statement);
        }
    }

    public static void bindValues(SQLiteStatement statement,
                                  Object[] values,
                                  int row,
                                  int columns) {
        for (int j = 0; j < columns; j++) {
            Object value = values[row * columns + j];
            int index = j + 1;
            if (value == null) {
                statement.bindNull(index);
            } else if (value instanceof String) {
                statement.bindString(index, (String) value);
            } else if (value instanceof Number) {
                if (value instanceof Double
                        || value instanceof Float
                        || value instanceof BigDecimal) {
                    statement.bindDouble(index, ((Number) value).doubleValue());
                } else {
                    statement.bindLong(index, ((Number) value).longValue());
                }
            } else if (value instanceof byte[]) {
                statement.bindBlob(index, (byte[]) value);
            } else {
                statement.bindString(index, value.toString());
            }
        }
    }

    private static void copyTable(SQLiteDatabase srcDb, SQLiteDatabase desDb,
                                  String table, StringBuilder builder) {
        TableData tableData = getData(srcDb, "SELECT * FROM " + table);
        if (tableData != null) {
            builder.setLength(0);
            builder.append("INSERT INTO ").append(table).append(" VALUES(");
            for (int i = 0; i < tableData.column; i++) {
                builder.append("?,");
            }
            builder.setCharAt(builder.length() - 1, ')');
            insertToDb(desDb, builder.toString(), tableData.data, tableData.row, tableData.column);
        }
    }

    private static void copyDataToNewDb(SQLiteDatabase srcDb, SQLiteDatabase desDb) {
        srcDb.beginTransaction();
        try {
            List<String> tables = getTables(desDb);
            StringBuilder builder = new StringBuilder(128);
            for (String table : tables) {
                desDb.execSQL("DELETE FROM " + table);
                copyTable(srcDb, desDb, table, builder);
            }
        } finally {
            srcDb.endTransaction();
        }
    }



    public static void fixDbIfNecessary(BaseDBOpenHelper srcDbHelper, BaseDBOpenHelper tempDbHelper) {
        try {
            SQLiteDatabase srcDb = srcDbHelper.getDatabase();
            if (/*isDbOk(srcDb)*/false) {
                srcDb.close();
                return;
            }

            srcDb.disableWriteAheadLogging();

            SQLiteDatabase tempDb = tempDbHelper.getDatabase(false);
            tempDb.beginTransaction();
            try {
                copyDataToNewDb(srcDb, tempDb);
                tempDb.setTransactionSuccessful();
            } finally {
                tempDb.endTransaction();
                tempDb.close();
            }

            srcDb.close();

            File srcDBFile = new File(srcDbHelper.getDatabaseName());
            File tempDBFile = new File(tempDbHelper.getDatabaseName());
            if (srcDBFile.delete() && tempDBFile.renameTo(srcDBFile)) {
                //noinspection ResultOfMethodCallIgnored
                tempDBFile.delete();
            }

            LogUtil.d(TAG, "fix db finish, db path:" + srcDBFile.getPath());
        } catch (Exception e) {
            LogUtil.e(TAG, e);
        }
    }

}
