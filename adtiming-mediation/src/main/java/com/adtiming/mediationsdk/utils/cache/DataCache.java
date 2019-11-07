// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.cache;

import android.content.Context;

import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.device.DeviceUtil;
import com.adtiming.mediationsdk.utils.Constants;
import com.adtiming.mediationsdk.utils.crash.CrashUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Operations on Data Cache
 * 
 */
public class DataCache {
    private static String TABLE_NAME = "table_core";
    private static String TABLE_CREATE_COLUMN = "KEY VARCHAR(30),VALUE VARCHAR";
    private static String TABLE_COLUMN = "KEY,VALUE";

    private Map<String, Object> mHashMap = new HashMap<>();
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private DataBaseUtil mDataBaseUtil;

    private DataCache() {
    }

    private static final class DataCacheHolder {
        private static DataCache INSTANCE = new DataCache();
    }

    public static DataCache getInstance() {
        return DataCacheHolder.INSTANCE;
    }

    /**
     * Creates db table; queries data into memory; inits memory data
     *
     * @param context
     */
    public void init(Context context) throws Exception {
        this.mDataBaseUtil = DataBaseUtil.getSingleton();
        this.mDataBaseUtil.init(context, Constants.DB_NAME, Constants.DB_VERSION);
        this.mDataBaseUtil.createTable(TABLE_NAME, TABLE_CREATE_COLUMN);

        hashSet();
        dataCreate(context);
    }

    /**
     * inserts/updates
     *
     * @param key
     * @param value
     */
    public void set(String key, Object value) {
        if (mDataBaseUtil == null) {
            return;
        }
        lock.writeLock().lock();
        try {
            if (!mHashMap.containsKey(key)) {
                if (mDataBaseUtil.insert(TABLE_NAME, TABLE_COLUMN, String.format("\"%s\",\"%s\"", key, value))) {
                    mHashMap.put(key, value);
                }
            } else {
                Object oldValue = mHashMap.get(key);
                if (oldValue != null && !oldValue.equals(value)) {
                    if (mDataBaseUtil.update(TABLE_NAME, new String[]{String.format("%s=\"%s\"", "VALUE", value)}, String.format("%s=\"%s\"", "KEY", key))) {
                        mHashMap.put(key, value);
                    }
                }
            }
        } catch (Exception e) {
            DeveloperLog.LogD("AdtAds init", e);
            CrashUtil.getSingleton().saveException(e);
        }
        lock.writeLock().unlock();
    }

    /**
     * saves to memory only
     *
     * @param key
     * @param value
     */
    public void setMEM(String key, Object value) {
        try {
            lock.writeLock().lock();
            if (!mHashMap.containsKey(key)) {
                mHashMap.put(key, value);
            } else {
                Object oldValue = mHashMap.get(key);
                if (oldValue != null && !oldValue.equals(value)) {
                    mHashMap.put(key, value);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * inserts/updates in batch
     *
     * @param maps
     */
    public void set(Map<String, Object> maps) {
        lock.writeLock().lock();
        ArrayList<String> deleteData = new ArrayList<>();
        //finds data that needs to be updated; adds unchanged for deletion
        Iterator<Map.Entry<String, Object>> it = maps.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            String key = entry.getKey();
            Object value = entry.getValue();
            if (mHashMap.containsKey(key)) {
                Object oldValue = mHashMap.get(key);
                if (oldValue != null && !oldValue.equals(value)) {
                    deleteData.add(key);
                } else {
                    it.remove();
                }
            }
        }
        //actual deletion
        if (deleteData.size() != 0) {
            delete(deleteData.toArray(new String[deleteData.size()]));
        }
        //inserts
        if (maps.size() != 0) {
            String[] inserts = new String[maps.size()];
            int i = 0;
            for (Map.Entry<String, Object> entry : maps.entrySet()) {
                inserts[i] = String.format("\"%s\",\"%s\"", entry.getKey(), entry.getValue());
                i++;
            }
            if (mDataBaseUtil.insert(TABLE_NAME, TABLE_COLUMN, inserts)) {
                mHashMap.putAll(maps);
            }
        }
        lock.writeLock().unlock();
    }

    /**
     * deltes one or more
     *
     * @param keys
     */
    public void delete(String... keys) {
        if (mDataBaseUtil == null) {
            return;
        }
        lock.writeLock().lock();
        try {
            ArrayList<String> deleteKey = new ArrayList<>();
            for (String key : keys) {
                if (mHashMap.containsKey(key)) {
                    deleteKey.add(key);
                }
            }
            if (deleteKey.size() != 0) {
                String[] deleteWhere = deleteKey.toArray(new String[deleteKey.size()]);
                if (mDataBaseUtil.delete(TABLE_NAME, "KEY", deleteWhere)) {
                    for (int i = 0; i < deleteKey.size(); i++) {
                        mHashMap.remove(deleteKey.get(i));
                    }
                }
            }
        } catch (Exception e) {
            DeveloperLog.LogD("AdtAds init", e);
            CrashUtil.getSingleton().saveException(e);
        }
        lock.writeLock().unlock();
    }

    public boolean containsKey(String key) {
        return mHashMap.containsKey(key);
    }

    /**
     * saves db data in memory; no more db queries until app restarts
     */
    private void hashSet() {
        lock.readLock().lock();
        ArrayList<String[]> dataArray = null;
        try {
            dataArray = mDataBaseUtil.query(TABLE_NAME, TABLE_COLUMN);
            if (dataArray != null) {
                int keyIndex = 0;
                int valueIndex = 0;
                for (int i = 0; i < dataArray.size(); i++) {
                    String[] values = dataArray.get(i);
                    if (i == 0) {
                        for (int j = 0; j < values.length; j++) {
                            String keyValue = values[j];
                            if (keyValue.equals("KEY")) {
                                keyIndex = j;
                            }
                            if (keyValue.equals("VALUE")) {
                                valueIndex = j;
                            }
                        }
                    } else {
                        mHashMap.put(values[keyIndex], values[valueIndex]);
                    }
                }
            }
        } catch (Exception e) {
            DeveloperLog.LogD("DataCache", e);
            CrashUtil.getSingleton().saveException(e);
        }
        lock.readLock().unlock();
    }

    /**
     * 
     *
     * @param key  
     * @param type 
     * @param <T>  your desired data type
     * @return Attention: could be null. Check before using
     */
    public <T> T get(String key, Class<T> type) {
        lock.readLock().lock();
        T value = null;
        if (mHashMap.containsKey(key)) {
            value = typeSet(type, mHashMap.get(key));
        }
        lock.readLock().unlock();
        return value;
    }

    /**
     * gets all keys
     *
     * @return
     */
    public List<String> getKeys() {
        try {
            lock.readLock().lock();
            return new ArrayList<>(mHashMap.keySet());
        } finally {
            lock.readLock().unlock();
        }
    }

    private <T> T typeSet(Class<T> virtualType, Object realValue) {
        T value = null;
        String toValue = null;
        try {
            toValue = String.valueOf(realValue);
            if (virtualType == String.class) {
                value = (T) String.valueOf(toValue);
            } else if (virtualType == int.class) {
                value = (T) Integer.valueOf(toValue);
            } else if (virtualType == long.class) {
                value = (T) Long.valueOf(toValue);
            } else if (virtualType == float.class) {
                value = (T) Float.valueOf(toValue);
            } else if (virtualType == boolean.class) {
                value = (T) Boolean.valueOf(toValue);
            } else if (virtualType == double.class) {
                value = (T) Double.valueOf(toValue);
            } else {
                value = (T) realValue;
            }
        } catch (Exception e) {
            value = null;
            toValue = null;
            DeveloperLog.LogD("DataCache", e);
            CrashUtil.getSingleton().saveException(e);
        }
        return value;
    }

    private void dataCreate(Context context) throws Exception {
        set(DeviceUtil.getBuildInfo());
        set(DeviceUtil.getDeviceInfo(context));
        set(DeviceUtil.getLocaleInfo());
        set(DeviceUtil.getResourcesInfo(context));
        set(DeviceUtil.getSystemProperty(context));
        set(DeviceUtil.getTelephonyManagerInfo(context));
//        mHashMap.putAll(DeviceUtil.getBuildInfo());
//        mHashMap.putAll(DeviceUtil.getDeviceInfo(context));
//        mHashMap.putAll(DeviceUtil.getLocaleInfo(context));
//        mHashMap.putAll(DeviceUtil.getResourcesInfo(context));
//        mHashMap.putAll(DeviceUtil.getSystemProperty(context));
//        mHashMap.putAll(DeviceUtil.getTelephonyManagerInfo(context));
    }
}
