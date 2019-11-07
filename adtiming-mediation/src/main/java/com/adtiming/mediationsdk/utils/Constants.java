// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils;

public interface Constants {
    String CONTENT_TYPE_STREAM = "application/octet-stream";

    int API_VERSION_9 = 9;
    String DEVICE_PLATFORM = "1";//android
    String SDK_V = "6.0.0.beta1";
    int VERSION = 300;

    String FILE_HEADER_SUFFIX = "-header";
    String KEY_REQUEST_TIME = "request_time";
    String KEY_CONTENT_TYPE = "Content-Type";
    String KEY_CACHE_CONTROL = "Cache-Control";
    String KEY_ETAG = "ETag";
    String KEY_LAST_MODIFIED = "Last-Modified";
    String KEY_MAX_AGE = "max-age";
    String KEY_IF_NONE_MATCH = "If-None-Match";
    String KEY_IF_MODIFIED_SINCE = "If-Modified-Since";
    String KEY_LOCATION = "Location";

    //AdType
    int BANNER = 0;
    int NATIVE = 1;
    int VIDEO = 2;
    int INTERACTIVE = 3;
    int INTERSTITIAL = 4;

    String ADTYPE_BANNER = "Banner";
    String ADTYPE_NATIVE = "Native";
    String ADTYPE_INIT = "Init";

    String PKG_FB = "com.facebook.katana";
    String PKG_GP = "com.android.vending";

    String DB_NAME = "adtimingDB.db";
    int DB_VERSION = 2;
}
