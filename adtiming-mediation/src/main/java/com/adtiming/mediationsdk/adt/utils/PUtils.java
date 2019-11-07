// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebViewClient;

import com.adtiming.mediationsdk.adt.AdtActivity;
import com.adtiming.mediationsdk.adt.bean.AdBean;
import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.HandlerUtil;
import com.adtiming.mediationsdk.utils.PlacementUtils;
import com.adtiming.mediationsdk.utils.cache.DataCache;
import com.adtiming.mediationsdk.utils.crash.CrashUtil;
import com.adtiming.mediationsdk.utils.model.ImpRecord;
import com.adtiming.mediationsdk.utils.webview.AdWebView;
import com.adtiming.mediationsdk.adt.utils.webview.ActWebView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 
 */
public final class PUtils extends PlacementUtils {

    /**
     * Ads click
     */
    public static void doClick(final Context context, final String placementId, final AdBean adBean) {
        try {
            saveClickPackage(adBean.getPkgName());
            if (!adBean.isWebview() && adBean.getSc() == 1) {
                String landingUrl = "market://details?id=" + adBean.getPkgName();
                GpUtil.goGp(context, landingUrl);
                HandlerUtil.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            AdWebView adView = ActWebView.getInstance().getActView();
                            if (adView == null) {
                                adView = new AdWebView(context);
                            }
                            final Map<String, String> additionalHttpHeaders = new HashMap<>();
                            additionalHttpHeaders.put("Cache-Control", "no-cache");
                            adView.setWebViewClient(new WebViewClient() {
                                @Override
                                public boolean shouldOverrideUrlLoading(android.webkit.WebView view, String url) {
                                    Uri dst = Uri.parse(url);
                                    String scheme = dst.getScheme();
                                    if ("market".equals(scheme) || "intent".equals(scheme)) {
                                        String query = dst.getEncodedQuery();
                                        view.loadUrl("https://play.google.com/store/apps/details?" + query);
                                    } else {
                                        view.loadUrl(url, additionalHttpHeaders);
                                    }
                                    return true;
                                }
                            });
                            int sceneId = 0;
                            if (DataCache.getInstance().containsKey(placementId + "_sceneId")) {
                                sceneId = DataCache.getInstance().get(placementId + "_sceneId", Integer.class);
                            }
                            String adUrl = adBean.getAdUrl();
                            if (adUrl.contains("{scene}")) {
                                adUrl = adUrl.replace("{scene}", sceneId + "");
                            }
                            adView.loadUrl(adUrl, additionalHttpHeaders);
                        } catch (Throwable e) {
                            DeveloperLog.LogD("AdReport", e);
                            CrashUtil.getSingleton().saveException(e);
                        }
                    }
                });
            } else if (adBean.isWebview() || adBean.getSc() != 1) {
                Intent intent = new Intent(context, AdtActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("ad", adBean);
                intent.putExtra("placementId", placementId);
                context.startActivity(intent);
            }
        } catch (Exception e) {
            DeveloperLog.LogD("AdReport", e);
            CrashUtil.getSingleton().saveException(e);
        }
    }

    public static void saveClInfo(AdBean adBean, String placementId) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);
            Date date = new Date();

            String today = dateFormat.format(date);
            DeveloperLog.LogD("saveClInfo : " + placementId);
            ImpRecord impRecord = parseFromJson(DataCache.getInstance().get("ImpRecord", String.class));
            if (impRecord == null) {
                impRecord = new ImpRecord();
            }

            Map<String, List<ImpRecord.Imp>> imps = impRecord.getImpMap();
            if (imps == null) {
                imps = new HashMap<>();
            }
            String tpmKey = placementId.trim().concat("_imp");
            List<ImpRecord.Imp> imprs = imps.get(tpmKey);
            if (imprs == null || imprs.isEmpty()) {
                imprs = new ArrayList<>();
                ImpRecord.Imp imp = new ImpRecord.Imp();
                imp.setPlacmentId(placementId);
                imp.setCampaignId(adBean.getCampaignId());
                imp.setTime(today);
                imp.setImpCount(imp.getImpCount() + 1);
                imp.setPkgName(adBean.getPkgName());
                imp.setLashImpTime(System.currentTimeMillis());
                imprs.add(imp);
            } else {
                for (ImpRecord.Imp impr : imprs) {
                    if (impr.getCampaignId().equals(adBean.getCampaignId())) {
                        impr.setPlacmentId(placementId);
                        impr.setCampaignId(adBean.getCampaignId());
                        impr.setTime(today);
                        impr.setImpCount(impr.getImpCount() + 1);
                        impr.setPkgName(adBean.getPkgName());
                        impr.setLashImpTime(System.currentTimeMillis());
                        imprs.add(impr);
                        break;
                    }
                }
            }

            imps.put(tpmKey, imprs);
            impRecord.setImpMap(imps);

            DataCache.getInstance().set("ImpRecord", Uri.encode(transformToString(impRecord)));
        } catch (Throwable e) {
            DeveloperLog.LogD("PlacementUtils", e);
            CrashUtil.getSingleton().saveException(e);
        }
    }

    private static void saveClickPackage(String pkg) {
        try {
            DataCache.getInstance().set(pkg, String.valueOf(System.currentTimeMillis()));
        } catch (Throwable e) {
            DeveloperLog.LogD("PlacementUtils", e);
            CrashUtil.getSingleton().saveException(e);
        }
    }

}
