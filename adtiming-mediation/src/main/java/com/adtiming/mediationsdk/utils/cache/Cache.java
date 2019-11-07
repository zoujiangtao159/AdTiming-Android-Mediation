// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.cache;

import android.content.Context;
import android.text.TextUtils;

import com.adtiming.mediationsdk.utils.AdtUtil;
import com.adtiming.mediationsdk.utils.Constants;
import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.Encrypter;
import com.adtiming.mediationsdk.utils.IOUtil;
import com.adtiming.mediationsdk.utils.request.network.Headers;
import com.adtiming.mediationsdk.utils.request.network.Response;
import com.adtiming.mediationsdk.utils.crash.CrashUtil;

import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Comparator;

public final class Cache {

    private static final String FILE_DIR_NAME = "adtiming";//cache dir name
    private static final long FREE_SD_SPACE_NEEDED_TO_CACHE = 100 * 1024 * 1024L;//Cache dir max size
    private static final long MIN_CACHE_INTERVAL = 60 * 60 * 1000L;//min cache time
    private static final long MAX_CACHE_INTERVAL = 24 * MIN_CACHE_INTERVAL;//max cache time

    private static String[] HEADERS = new String[]{
            Constants.KEY_CACHE_CONTROL,
            Constants.KEY_CONTENT_TYPE,
            Constants.KEY_ETAG,
            Constants.KEY_LAST_MODIFIED,
            Constants.KEY_LOCATION
    };

    public static void init() {
        if (AdtUtil.getApplication() == null) {
            return;
        }
        createCacheRootDir(AdtUtil.getApplication());
        freeSpaceIfNeeded(AdtUtil.getApplication());
    }

    /**
     * Gets default cache root dir
     */
    private static File getRootDir(Context context) {
        File root = context.getCacheDir();
        String path = root.getAbsolutePath() + File.separator + FILE_DIR_NAME;

        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    /**
     * checks if url is in cache
     */
    public static boolean existCache(Context context, String url) {
        try {
            if (context == null) {
                if (AdtUtil.getApplication() == null) {
                    return false;
                } else {
                    context = AdtUtil.getApplication();
                }
            }
            File rootDir = getRootDir(context);
            String md5 = Encrypter.md5(url);
            File content = new File(rootDir, md5);
            File header = new File(rootDir, md5.concat(Constants.FILE_HEADER_SUFFIX));
            if (!header.exists() || !content.exists()) {
                return false;
            }

            updateFileTime(header);
            updateFileTime(content);

            long interval = getRequestInterval(header);
            long maxAge = getMaxAge(header);

            return interval < maxAge;
        } catch (Exception e) {
            DeveloperLog.LogD("Cache", e);
            CrashUtil.getSingleton().saveException(e);
            return false;
        }
    }

    /**
     * caches file
     */
    public static boolean saveFile(Context context, String url, Response response) {
        if (context == null) {
            return false;
        }
        boolean success;
        try {
            success = saveContent(context, url, response);
            if (success) {
                saveHeaderFields(context, url, response);
            }
        } catch (Exception e) {
            success = false;
            DeveloperLog.LogD("Cache", e);
            CrashUtil.getSingleton().saveException(e);
        }
        return success;
    }

    /**
     * caches header fields
     */
    public static void saveHeaderFields(Context context, String url, Response response) throws Exception {
        Headers headers = response.headers();
        if (headers == null || headers.isEmpty()) {
            return;
        }
        File rootDir = getRootDir(context);
        String path = Encrypter.md5(url).concat(Constants.FILE_HEADER_SUFFIX);
        File header = new File(rootDir, path);
        if (header.length() > 0) {
            header.delete();
            header = new File(rootDir, path);
        }

        JSONObject object = new JSONObject();
        for (String s : HEADERS) {
            if (headers.containsKey(s)) {
                String value = headers.get(s).get(0);
                String tmp = value.split(";")[0];
                object.put(s, tmp.trim());
            }
        }

        object.put(Constants.KEY_REQUEST_TIME, String.valueOf(System.currentTimeMillis()));
        IOUtil.writeToFile(object.toString().getBytes(Charset.forName("utf-8")), header);
    }

    /**
     * caches file content
     */
    private static boolean saveContent(Context context, String url, Response response) throws Exception {
        boolean success;
        File rootDir = getRootDir(context);
        String fileName = Encrypter.md5(url);
        File result = new File(rootDir, fileName);
        if (result.exists()) {//if exists, delete it
            result.delete();
            result = new File(rootDir, fileName);
        }

        File cache = new File(rootDir, String.format("%scache", fileName));

        int contentLength = (int) response.headers().getContentLength();//only image and video res have value ,otherwise value = -1
        if (contentLength <= 0) {//neither image nor video
            if (cache.exists()) {
                cache.delete();
            }
            cache = new File(rootDir, String.format("%scache", fileName));
            success = convertToResult(cache, result, response, contentLength);
        } else {
            if (cache.length() == contentLength) {//cache file exists, can be reused
                cache.renameTo(result);
                success = result.length() == contentLength;
            } else {
                if (cache.exists()) {
                    cache.delete();
                }
                cache = new File(rootDir, String.format("%scache", fileName));
                success = convertToResult(cache, result, response, contentLength);
            }
        }
        return success;
    }

    /**
     * gets cache file by url and suffix
     */
    public static File getCacheFile(Context context, String url, String suffix) {
        File dir = getRootDir(context);
        String fileName = Encrypter.md5(url);
        if (!TextUtils.isEmpty(suffix)) {
            fileName = fileName.concat(Constants.FILE_HEADER_SUFFIX);
        }

        File result = new File(dir, fileName);
        DeveloperLog.LogD("result:" + result.toString());
        updateFileTime(result);
        return result;
    }

    /**
     * Returns value from file for key
     */
    public static String getValueFromFile(File file, String key) throws Exception {
        updateFileTime(file);
        InputStream inputStream = IOUtil.getFileInputStream(file);
        if (inputStream == null) {
            return "";
        }
        String values = IOUtil.toString(inputStream);
        if (TextUtils.isEmpty(values)) {
            return "";
        }
        String tmp = values.substring(values.indexOf("{"), values.lastIndexOf("}") + 1);
        JSONObject object = new JSONObject(tmp);
        return object.optString(key);
    }

    /**
     * updates file's last modify time
     */
    private static void updateFileTime(File file) {
        if (file != null && file.exists()) {
            long newModifiedTime = System.currentTimeMillis();
            file.setLastModified(newModifiedTime);
        }
    }

    /**
     * Stores received data to file
     */
    private static boolean convertToResult(File cache, File result, Response response, int length) throws Exception {
        boolean success;
        InputStream in = response.body().stream();
        IOUtil.writeToFile(in, cache);
        IOUtil.closeQuietly(in);
        cache.renameTo(result);
        if (length <= 0) {
            success = result.length() > 0;
        } else {
            success = result.length() == length;
        }
        return success;
    }

    /**
     * creates cache root dir
     */
    private static void createCacheRootDir(Context context) {
        File root = context.getCacheDir();

        String path = root.getAbsolutePath() + File.separator + FILE_DIR_NAME;

        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * checks free space, if not enough in cache left, deletes files by LRU
     */
    private static void freeSpaceIfNeeded(Context context) {
        File dir = getRootDir(context);
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        int dirSize = 0;
        for (File file : files) {
            dirSize += file.length();
        }
        // if the dir size is larger than 100MB
        // frees 40% space 
        if (dirSize > FREE_SD_SPACE_NEEDED_TO_CACHE) {
            // delete 40% files by LRU
            int removeFactor = (int) ((0.4 * files.length) + 1);
            // sort the files by modify time
            Arrays.sort(files, new FileLastModifySort());
            // delete files
            for (int i = 0; i < removeFactor; i++) {
                files[i].delete();
            }
        }
    }

    /**
     * gets request interval
     */
    private static long getRequestInterval(File header) throws Exception {
        String requestTime = getValueFromFile(header, Constants.KEY_REQUEST_TIME);
        if (TextUtils.isEmpty(requestTime)) {
            return 0;
        }
        return System.currentTimeMillis() - Long.parseLong(requestTime);
    }

    /**
     * gets cache max-age
     */
    private static long getMaxAge(File header) throws Exception {
        long maxAge = 0;
        String cacheControl = getValueFromFile(header, Constants.KEY_CACHE_CONTROL);
        if (!TextUtils.isEmpty(cacheControl)) {
            if (cacheControl.contains(Constants.KEY_MAX_AGE)) {
                String[] tmp = cacheControl.split(",");
                for (String s : tmp) {
                    if (s.contains(Constants.KEY_MAX_AGE)) {
                        maxAge = Long.parseLong(s.split("=")[1]) * 1000;
                    }
                }
            }
        }

        if (maxAge > MAX_CACHE_INTERVAL) {
            return MAX_CACHE_INTERVAL;
        } else if (maxAge < MIN_CACHE_INTERVAL) {
            return MIN_CACHE_INTERVAL;
        } else {
            return maxAge;
        }
    }

    /**
     * Compares with lastModified
     */
    private static class FileLastModifySort implements Comparator<File> {
        public int compare(File arg0, File arg1) {
            if (arg0.lastModified() > arg1.lastModified()) {
                return 1;
            } else if (arg0.lastModified() == arg1.lastModified()) {
                return 0;
            } else {
                return -1;
            }
        }
    }
}
