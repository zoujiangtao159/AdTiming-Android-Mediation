// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt.video;

import android.content.Context;
import android.text.TextUtils;

import com.adtiming.mediationsdk.adt.bean.AdBean;
import com.adtiming.mediationsdk.adt.report.AdReport;
import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.request.network.Request;

public class VideoUtil {
    public static void checkPlayINAvailable(String host, Request.OnRequestCallback callback) {
        if (callback == null) {
            return;
        }
        //builds request parameter
//        String configUrl = RequestBuilder.buildRequestUrl(REQUEST_TYPE.REQUEST_TYPE_CONFIG, appKey);
//        if (TextUtils.isEmpty(configUrl)) {
//            callback.onRequestFailed("empty Url");
//            return;
//        }
//        //posts a request
//        AdRequest.post()
//                .url(configUrl)
//                .headers(new Headers())
//                .body(new ByteRequestBody())
//                .connectTimeout(30000)
//                .readTimeout(60000)
//                .instanceFollowRedirects(true)
//                .callback(callback)
//                .performRequest(AdtUtil.getApplication());
    }

    public static void reportVideoEvents(Context context, String placementId, AdBean adBean, String event) {
        DeveloperLog.LogD("reportVideoEvents : " + event);
        if (adBean.getVes() == null || !adBean.getVes().containsKey(event) || TextUtils.isEmpty(placementId)) {
            return;
        }
        String[] events = adBean.getVes().get(event);
        //reports events
        AdReport.reportVideoEvent(context, placementId, events);
    }
}
