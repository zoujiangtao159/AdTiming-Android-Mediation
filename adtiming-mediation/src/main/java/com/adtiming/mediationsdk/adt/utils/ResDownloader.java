// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt.utils;

import android.text.TextUtils;

import com.adtiming.mediationsdk.utils.AdtUtil;
import com.adtiming.mediationsdk.utils.Constants;
import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.IOUtil;
import com.adtiming.mediationsdk.utils.cache.Cache;
import com.adtiming.mediationsdk.utils.request.HeaderUtils;
import com.adtiming.mediationsdk.utils.request.network.AdRequest;
import com.adtiming.mediationsdk.utils.request.network.Response;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Set;

class ResDownloader {

    static boolean downloadFile(final Set<URI> urls) throws Exception {
        int totalSize = urls.size();
        int failSize = 0;
        for (URI url : urls) {
            if (!Cache.existCache(AdtUtil.getApplication(), url.toURL().toString())) {
                File file = downloadFile(url.toURL().toString());
                if (file == null) {
                    failSize++;
                }
            }
        }
        return failSize <= totalSize / 2;
    }

    static File downloadFile(String url) throws Exception {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        Response response = null;
        try {
            response = AdRequest.get().url(url).connectTimeout(30 * 1000).readTimeout(10 * 60 * 1000)
                    .headers(HeaderUtils.getCacheHeaders(AdtUtil.getApplication(), url)).syncRequest();

            if (response == null) {
                return null;
            }
            int code = response.code();
            if (code == HttpURLConnection.HTTP_OK) {
                boolean success = Cache.saveFile(AdtUtil.getApplication(), url, response);
                if (success) {
                    return Cache.getCacheFile(AdtUtil.getApplication(), url, null);
                } else {
                    deleteFileWhenError(url);
                    return null;
                }
            } else if (code == HttpURLConnection.HTTP_NOT_MODIFIED) {//文件内容无需更新，但是更新一下header
                if (Cache.existCache(AdtUtil.getApplication(), url)) {
                    Cache.saveHeaderFields(AdtUtil.getApplication(), url, response);
                    return Cache.getCacheFile(AdtUtil.getApplication(), url, null);
                } else {
                    deleteFileWhenError(url);
                    return null;
                }
            } else if (code == 301 || code == 302 || code == 303 || code == 307) {
                Cache.saveHeaderFields(AdtUtil.getApplication(), url, response);
                String redirectUrl = response.headers().getLocation();
                URL u = new URL(new URL(url), redirectUrl);
                DeveloperLog.LogD("ResDownLoader", "redirect url is : " + u.toString());
                return downloadFile(u.toString());
            } else {
                deleteFileWhenError(url);
                return null;
            }
        } finally {
            DeveloperLog.LogD("ResDownLoader", "url is : " + url + " finally close response");
            IOUtil.closeQuietly(response);
        }
    }

    private static void deleteFileWhenError(String url) {
        File content = Cache.getCacheFile(AdtUtil.getApplication(), url, null);
        if (content != null && content.exists()) {
            DeveloperLog.LogD("ResDownLoader", "delete content file when error : " + content.delete());
        }
        File header = Cache.getCacheFile(AdtUtil.getApplication(), url, Constants.FILE_HEADER_SUFFIX);
        if (header != null && header.exists()) {
            DeveloperLog.LogD("ResDownLoader", "delete header file when error : " + header.delete());
        }
    }
}
