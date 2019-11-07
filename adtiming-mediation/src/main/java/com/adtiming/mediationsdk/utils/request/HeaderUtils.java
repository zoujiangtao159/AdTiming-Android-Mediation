// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.request;

import android.content.Context;
import android.text.TextUtils;

import com.adtiming.mediationsdk.utils.request.network.Headers;
import com.adtiming.mediationsdk.utils.Constants;
import com.adtiming.mediationsdk.utils.cache.Cache;
import com.adtiming.mediationsdk.utils.cache.DataCache;

import java.io.File;

public final class HeaderUtils {

    public static Headers getCacheHeaders(Context context, String url) throws Exception {
        Headers headers = getBaseHeaders();
        File header = Cache.getCacheFile(context, url, Constants.FILE_HEADER_SUFFIX);
        if (header.exists()) {
            String eTag = Cache.getValueFromFile(header, Constants.KEY_ETAG);
            if (!TextUtils.isEmpty(eTag)) {
                headers.set(Constants.KEY_IF_NONE_MATCH, eTag);
            } else {
                String lastModified = Cache.getValueFromFile(header, Constants.KEY_LAST_MODIFIED);
                if (!TextUtils.isEmpty(lastModified)) {
                    headers.set(Constants.KEY_IF_MODIFIED_SINCE, lastModified);
                }
            }
        }
        return headers;
    }

    public static Headers getBaseHeaders() {
        Headers headers = new Headers();
        headers.set("User-Agent", DataCache.getInstance().get("UserAgent", String.class));
        headers.set("Content-Type", Constants.CONTENT_TYPE_STREAM);
//        headers.set("Connection", "close");
        return headers;
    }
}
