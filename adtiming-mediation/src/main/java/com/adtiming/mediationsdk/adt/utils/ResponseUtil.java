// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt.utils;

import android.text.TextUtils;

import com.adtiming.mediationsdk.adt.bean.AdBean;
import com.adtiming.mediationsdk.adt.bean.AdMark;
import com.adtiming.mediationsdk.utils.constant.KeyConstants;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 *
 */
public final class ResponseUtil {

    public static AdBean transformResponse(JSONArray array, String tkHost) {
        if (array == null || array.length() == 0) {
            return null;
        }
        LinkedList<AdBean> adBeans = new LinkedList<>();

        int len = array.length();
        for (int i = 0; i < len; i++) {
            JSONObject adbean = array.optJSONObject(i);
            adBeans.add(jsonToAd(adbean, tkHost));
        }
        return adBeans.pop();
    }

    private static AdBean jsonToAd(JSONObject adBean, String tkHost) {
        AdBean.AdBuilder builder = new AdBean.AdBuilder();
        builder.oriData(adBean.toString())
                .campaignId(adBean.optString("campaign_id"))
                .adId(adBean.optString("ad_id"))
                .adType(adBean.optString("ad_type"))
                .apkUrl(adBean.optString("apk_url"))
                .title(adBean.optString("title"))
                .description(adBean.optString("description"))
                .adUrl(adBean.optString("ad_url"))
                .cacheVideo(adBean.optBoolean("cache_video", false))
                .cid(adBean.optString("cid"))
                .iconUrl(adBean.optString("icon_url"))
                .mainImgUrl(adBean.optString("mainimg_url"))
                .videoUrl(adBean.optString("video_url"))
                .isWebview(adBean.optBoolean("is_webview", false))
                .gpId(adBean.optString("google_store_id"))
                .pkgName(adBean.optString("app_id"))
                .playUrl(adBean.optString("play_url"))
                .rating(adBean.optDouble("rating"))
                .sc(adBean.optInt("sc"))
                .action(adBean.optInt("action"))
                .vq(adBean.optString(KeyConstants.Response.KEY_VQ))
                .expire(adBean.optInt(KeyConstants.Response.KEY_EXPIRE)) 
                .vpc(adBean.optInt("vpc"))
                .setAppName(adBean.optString(KeyConstants.Response.KEY_APP_NAME))
                .setAppSize(adBean.optInt(KeyConstants.Response.KEY_APP_SIZE))
                .setRatingCount(adBean.optInt(KeyConstants.Response.KEY_RATING_COUNT))
                .resourceMd5(adBean.optString("resource_md5"));

        JSONArray imptrackerArray = adBean.optJSONArray("imptrackers");
        if (imptrackerArray != null && imptrackerArray.length() > 0) {
            List<String> imptrackers = new ArrayList<>();
            int impLen = imptrackerArray.length();
            for (int b = 0; b < impLen; b++) {
                String imptracker = imptrackerArray.optString(b);
                if (!TextUtils.isEmpty(imptracker)) {
                    imptrackers.add(imptracker);
                }
            }
            builder.impTrackers(imptrackers);
        }

        JSONArray clktrackerArray = adBean.optJSONArray("clks");
        if (clktrackerArray != null && clktrackerArray.length() > 0) {
            List<String> clktrackers = new ArrayList<>();
            int clkLen = clktrackerArray.length();
            for (int b = 0; b < clkLen; b++) {
                String clktracker = clktrackerArray.optString(b);
                if (!TextUtils.isEmpty(clktracker)) {
                    clktrackers.add(clktracker);
                }
            }
            builder.clkTrackers(clktrackers);
        }
        JSONArray resArray = adBean.optJSONArray("resources");
        if (resArray != null && resArray.length() > 0) {
            List<String> resources = new ArrayList<>();
            int resLen = resArray.length();
            for (int b = 0; b < resLen; b++) {
                String res = resArray.optString(b);
                if (!TextUtils.isEmpty(res)) {
                    resources.add(res);
                }
            }
            builder.resources(resources);
        }
        JSONArray imgArray = adBean.optJSONArray(KeyConstants.Response.KEY_IMGS);
        if (imgArray != null && imgArray.length() > 0) {
            List<String> imgs = new ArrayList<>();
            int impLen = imgArray.length();
            for (int b = 0; b < impLen; b++) {
                String img = imgArray.optString(b);
                if (!TextUtils.isEmpty(img)) {
                    imgs.add(img);
                }
            }
            builder.setImgs(imgs);
        }

        JSONObject ves = adBean.optJSONObject(KeyConstants.Response.KEY_VES);
        if (ves != null) {
            Iterator<String> keys = ves.keys();
            if (keys != null) {
                HashMap<String, String[]> vesMap = new HashMap<>();
                while (keys.hasNext()) {
                    String key = keys.next();
                    JSONArray array = ves.optJSONArray(key);
                    int len = array.length();
                    String[] vesUrls = new String[len + 1];
                    if (len != 0) {
                        for (int i = 0; i < len; i++) {
                            vesUrls[i] = array.optString(i);
                        }
                    }
                    //Adds adt reporting url when an event exists
                    vesUrls[len] = buildAdtEventUrl(tkHost, adBean.optString(KeyConstants.Response.KEY_VQ), key);
                    vesMap.put(key, vesUrls);
                }
                builder.ves(vesMap);
            }
        }

        JSONObject piObject = adBean.optJSONObject(KeyConstants.Response.KEY_PI);
        if (piObject != null) {
            builder.piid(piObject.optString("id"));
            builder.pihs(piObject.optString("hs"));
            builder.piot(piObject.optString("ot"));
            builder.picp(piObject.optInt("cp"));
        }
        JSONObject mkObject = adBean.optJSONObject(KeyConstants.Response.KEY_MK);
        if (mkObject != null) {
            AdMark mk = new AdMark(mkObject);
            builder.setMk(mk);
        }
        return builder.build();
    }

    private static String buildAdtEventUrl(String tkHost, String vq, String eventName) {
        if (TextUtils.isEmpty(tkHost)) {
            return null;
        }

        return tkHost + "/videotr?" + (TextUtils.isEmpty(vq) ? "+ \"event=\"" : vq + "&event=") + eventName;
    }
}
