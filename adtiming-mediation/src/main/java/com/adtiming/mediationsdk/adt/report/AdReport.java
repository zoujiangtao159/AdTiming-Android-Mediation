// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt.report;

import android.content.Context;
import android.text.TextUtils;

import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.WorkExecutor;
import com.adtiming.mediationsdk.utils.cache.DataCache;
import com.adtiming.mediationsdk.utils.crash.CrashUtil;
import com.adtiming.mediationsdk.utils.model.AdtConfig;
import com.adtiming.mediationsdk.utils.request.network.AdRequest;
import com.adtiming.mediationsdk.adt.utils.PUtils;
import com.adtiming.mediationsdk.adt.bean.AdBean;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 
 */
public final class AdReport {

    private  static ConcurrentLinkedQueue<AdBean> sImpReport = new ConcurrentLinkedQueue<>();
    private  static ConcurrentLinkedQueue<AdBean> sClickReport = new ConcurrentLinkedQueue<>();

    /**
     * Click reporting
     */
    public static void CLKReport(Context context, String placementId, AdBean adBean) {
        if (adBean == null) {
            return;
        }

        if (sClickReport.contains(adBean)) {
            return;
        }

        List<String> clks = adBean.getClktrackers();
        if (clks == null || clks.isEmpty()) {
            return;
        }

        for (String tracker : clks) {
            AdRequest.get().url(tracker).readTimeout(60000).connectTimeout(30000)
                    .instanceFollowRedirects(true).performRequest(context);
        }

        sClickReport.add(adBean);
    }

    /**
     * Impression reporting
     */
    public static void impReport(Context context, String placementId, AdBean adBean, boolean reportExtra) {

        if (adBean == null) {
            return;
        }

        if (sImpReport.contains(adBean)) {
            return;
        }
        saveCLImprInfo(placementId, adBean);

        List<String> impTrackers = adBean.getImptrackers();
        if (impTrackers == null || impTrackers.isEmpty()) {
            return;
        }

        if (reportExtra) {
            String url = adBean.getAdUrl();
            if (!TextUtils.isEmpty(url)) {
                impTrackers.add(url);
            }
        }
        int sceneId = 0;
        if (DataCache.getInstance().containsKey(placementId + "_sceneId")) {
            sceneId = DataCache.getInstance().get(placementId + "_sceneId", Integer.class);
        }

        for (String tracker : impTrackers) {
            if (tracker.contains("{scene}")) {
                tracker = tracker.replace("{scene}", sceneId + "");
            }
            DeveloperLog.LogD("adt report Url : " + tracker);
            AdRequest.get().url(tracker).readTimeout(60000).connectTimeout(30000)
                    .instanceFollowRedirects(true).performRequest(context);
        }

        sImpReport.add(adBean);
    }

    /**
     * VideoEvent reporting
     */
    public static void reportVideoEvent(Context context, String placementId, String[] events) {
        try {
            String tkHost = DataCache.getInstance().get("AdtConfig", AdtConfig.class).getTkHost();
            if (TextUtils.isEmpty(tkHost)) {
                return;
            }
            int sceneId = 0;
            if (DataCache.getInstance().containsKey(placementId + "_sceneId")) {
                sceneId = DataCache.getInstance().get(placementId + "_sceneId", Integer.class);
            }
            for (String event : events) {
                if (TextUtils.isEmpty(event)) {
                    continue;
                }
                if (event.contains("{scene}")) {
                    event = event.replace("{scene}", sceneId + "");
                }
                AdRequest.get().url(event).connectTimeout(3000).readTimeout(6000).performRequest(context);
            }
        } catch (Exception e) {
            DeveloperLog.LogD("AdReport", e);
            CrashUtil.getSingleton().saveException(e);
        }
    }

    private static void saveCLImprInfo(final String placementId, final AdBean adBean) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (adBean == null) {
                    return;
                }
                PUtils.saveClInfo(adBean, placementId);
            }
        };

        WorkExecutor.execute(runnable);
    }
}
