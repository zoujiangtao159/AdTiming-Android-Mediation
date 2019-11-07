// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.cache;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.crash.CrashUtil;

import java.util.ArrayList;

/**
 * Operations on DB
 * 
 */
public class DataBaseHelper extends SQLiteOpenHelper {

    protected SQLiteDatabase mSQLiteDatabase;
    private static DataBaseHelper instance;

    public static synchronized DataBaseHelper getSingleton(Context context, String name, int version) {
        if (instance == null) {
            instance = new DataBaseHelper(context, name, version);
        }
        return instance;
    }

    public DataBaseHelper(Context context, String name, int version) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        this.mSQLiteDatabase = sqLiteDatabase;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }

    /**
     * 
     *
     * @param sql
     * @return
     */
    public boolean execSQL(String sql) {
        boolean flage = false;
        getWritableDatabase();
        if (!isRead()) {
            return flage;
        }
        try {
            mSQLiteDatabase.execSQL(sql);
            flage = true;
        } catch (Exception e) {
            DeveloperLog.LogD("DataBaseHelper", e);
            CrashUtil.getSingleton().saveException(e);
        } finally {
            close();
        }
        return flage;
    }

    /**
     * 
     *
     * @param sql
     * @return
     */
    ArrayList<String[]> rawQuery(String sql) {
        Cursor cursor = null;
        ArrayList<String[]> dataList = new ArrayList<>();
        getWritableDatabase();
        if (!isRead()) {
            return dataList;
        }
        try {
            cursor = mSQLiteDatabase.rawQuery(sql, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int index = cursor.getColumnCount();
                    if (dataList.size() == 0) {
                        String[] values = new String[index];
                        for (int i = 0; i < index; i++) {
                            values[i] = cursor.getColumnName(i);
                        }
                        dataList.add(values);
                    }
                    String[] values = new String[index];
                    for (int i = 0; i < index; i++) {
                        values[i] = cursor.getString(i);
                    }
                    dataList.add(values);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            DeveloperLog.LogD("DataBaseHelper", e);
            CrashUtil.getSingleton().saveException(e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            close();
        }
        return dataList;
    }

    /**
     * 
     */
    @Override
    public synchronized void close() {
        if (isRead()) {
            mSQLiteDatabase.close();
        }
    }

    /**
     * 
     *
     * @return
     */
    public boolean isRead() {
        boolean flage = false;
        if (mSQLiteDatabase != null && mSQLiteDatabase.isOpen()) {
            flage = true;
        }
        return flage;
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        mSQLiteDatabase = super.getReadableDatabase();
        return mSQLiteDatabase;
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        mSQLiteDatabase = super.getWritableDatabase();
        return mSQLiteDatabase;
    }
}
