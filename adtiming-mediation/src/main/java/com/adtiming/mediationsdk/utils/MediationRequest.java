// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils;

import android.text.TextUtils;

import com.adtiming.mediationsdk.utils.cache.DataCache;
import com.adtiming.mediationsdk.utils.model.Config;
import com.adtiming.mediationsdk.utils.model.Placement;
import com.adtiming.mediationsdk.utils.model.PlacementInfo;
import com.adtiming.mediationsdk.utils.request.HeaderUtils;
import com.adtiming.mediationsdk.utils.request.RequestBuilder;
import com.adtiming.mediationsdk.utils.request.network.AdRequest;
import com.adtiming.mediationsdk.utils.request.network.ByteRequestBody;
import com.adtiming.mediationsdk.utils.request.network.Headers;
import com.adtiming.mediationsdk.utils.request.network.Request;
import com.adtiming.mediationsdk.core.AdTimingManager;
import com.adtiming.mediationsdk.utils.crash.CrashUtil;

import static com.adtiming.mediationsdk.utils.request.RequestBuilder.buildConfigRequestBody;

/**
 *
 */
public class MediationRequest {

    private static final String HEADER_NAME_BUILD = "build";
    private static final String HEADER_VALUE_BUILD = "1";

    private MediationRequest() {
    }

    /**
     * @param placementId   placementId
     * @param mediationId   mediationId
     * @param adPackageName adPackageName
     */
    public static void httpDanmaku(String placementId, int mediationId,
                                   String adPackageName, Request.OnRequestCallback requestCallback) {
        try {
            if (requestCallback == null) {
                return;

            }
            String danmakuUrl = RequestBuilder.buildDanmakuUrl(getHost(), placementId,
                    String.valueOf(mediationId), adPackageName);

            if (TextUtils.isEmpty(danmakuUrl)) {
                requestCallback.onRequestFailed("empty Url");
                return;
            }

            Headers headers = HeaderUtils.getBaseHeaders();
            //
            AdRequest.post()
                    .url(danmakuUrl)
                    .headers(headers)
                    .body(new ByteRequestBody(RequestBuilder.buildRequestBody(RequestBuilder.REQUEST_TYPE.REQUEST_TYPE_DANMAKU)))
                    .connectTimeout(30000)
                    .readTimeout(60000)
                    .instanceFollowRedirects(true)
                    .callback(requestCallback)
                    .performRequest(AdtUtil.getApplication());
        } catch (Exception e) {
            DeveloperLog.LogE("httpDanmaku error ", e);
            CrashUtil.getSingleton().saveException(e);
        }
    }

    /**
     * AllLoad, MediationLoad reporting interface
     * AllLoad: called by developer, as well as ads Ready
     * MediationLoad: called by everytime an ad network is mediated: load/Ready
     * E.g., developer calls VideoLoad once, then ALoad & AReady will be reported once,
     * plus multiple times (corresponding to the number of Mediated adnetworks) of MLoad & MReady
     * ADT is not reported for now
     *
     * @param placementId placementID
     * @param mediationId mediationId
     * @param mType       reporting type
     */
    static void httpLr(String placementId, int sceneId, AdTimingManager.LOAD_TYPE loadType,
                       int instanceId, int mediationId, String mType) {
        try {
            String lrUrl = RequestBuilder.buildLRUrl(getHost(), mType);

            if (TextUtils.isEmpty(lrUrl)) {
                return;
            }

            Headers headers = HeaderUtils.getBaseHeaders();
            headers.add(HEADER_NAME_BUILD, HEADER_VALUE_BUILD);
            AdRequest.post()
                    .url(lrUrl)
                    .headers(headers)
                    .body(new ByteRequestBody(RequestBuilder.buildRequestBody(RequestBuilder.REQUEST_TYPE.REQUEST_TYPE_LR,
                            placementId, String.valueOf(sceneId),
                            String.valueOf(loadType.getValue()),
                            String.valueOf(mediationId),
                            String.valueOf(instanceId))))
                    .connectTimeout(30000)
                    .readTimeout(60000)
                    .instanceFollowRedirects(true)
                    .performRequest(AdtUtil.getApplication());
        } catch (Exception e) {
            DeveloperLog.LogE("httpLr error ", e);
            CrashUtil.getSingleton().saveException(e);
        }
    }

    /**
     * reporting given extId; called upon mediation Video ads Finish
     */
    public static void httpVpc(String placementId, String extId) {
        try {
            String vpcUrl = RequestBuilder.buildVPCUrl(getHost());

            if (TextUtils.isEmpty(vpcUrl)) {
                return;
            }

            Headers headers = HeaderUtils.getBaseHeaders();
            headers.add(HEADER_NAME_BUILD, HEADER_VALUE_BUILD);
            AdRequest.post()
                    .url(vpcUrl)
                    .headers(headers)
                    .body(new ByteRequestBody(RequestBuilder.buildRequestBody(RequestBuilder.REQUEST_TYPE.REQUEST_TYPE_VPC,
                            placementId, extId)))
                    .connectTimeout(30000)
                    .readTimeout(60000)
                    .instanceFollowRedirects(true)
                    .performRequest(AdtUtil.getApplication());
        } catch (Exception e) {
            DeveloperLog.LogE("httpVpc error ", e);
            CrashUtil.getSingleton().saveException(e);
        }
    }

    /**
     * Gets config data through server Init API
     */
    public static void httpInit(String appKey, Request.OnRequestCallback requestCallback) throws Exception {
        if (requestCallback == null) {
            return;
        }
        //
        String configUrl = RequestBuilder.buildConfigUrl(appKey);
        if (TextUtils.isEmpty(configUrl)) {
            requestCallback.onRequestFailed("empty Url");
            return;
        }
        Headers headers = HeaderUtils.getBaseHeaders();
        headers.add(HEADER_NAME_BUILD, HEADER_VALUE_BUILD);

        AdRequest.post()
                .url(configUrl)
                .headers(headers)
                .body(new ByteRequestBody(buildConfigRequestBody(AdapterUtil.getAdns())))
                .connectTimeout(30000)
                .readTimeout(60000)
                .instanceFollowRedirects(true)
                .callback(requestCallback)
                .performRequest(AdtUtil.getApplication());
    }

    public static void cLRequest(PlacementInfo info, AdTimingManager.LOAD_TYPE type, Request.OnRequestCallback callback) throws Exception {
        String url = RequestBuilder.buildCLUrl(getHost());
        if (TextUtils.isEmpty(url)) {
            callback.onRequestFailed("empty Url");
            return;
        }
        Config config = DataCache.getInstance().get("Config", Config.class);
        Placement placement = config.getPlacements().get(info.getId());
        String ba = "";
        boolean na = false;
        if (placement != null) {
            int maxImpr = placement.getMi();
            if (maxImpr != 0) {
                ba = PlacementUtils.getBaByPid(placement, maxImpr);
            }

            na = AdRateUtil.shouldBlockAdtInstance(placement.getInsMap());
        }

        byte[] bytes = RequestBuilder.buildCLRequestBody(info.getId(),
                String.valueOf(info.getWidth()),
                String.valueOf(info.getHeight()),
                String.valueOf(type.getValue()),
                String.valueOf(ba),
                String.valueOf(PlacementUtils.getPlacementImprCount(info.getId())),
                String.valueOf(na));

        if (bytes == null) {
            callback.onRequestFailed("build request data error");
            return;
        }

        ByteRequestBody requestBody = new ByteRequestBody(bytes);
        Headers headers = HeaderUtils.getBaseHeaders();
        headers.add(HEADER_NAME_BUILD, HEADER_VALUE_BUILD);
        AdRequest.post().url(url).body(requestBody).headers(headers).connectTimeout(30000).readTimeout(60000)
                .callback(callback).performRequest(AdtUtil.getApplication());
    }

    public static void httpIap(String iapNumber, String currency, String iapt, Request.OnRequestCallback callback) {
        try {

            String url = RequestBuilder.buildIAPUrl(getHost());

            DeveloperLog.LogD(String.format("iap url : %s", url));

            Headers headers = HeaderUtils.getBaseHeaders();
            headers.add(HEADER_NAME_BUILD, HEADER_VALUE_BUILD);

            byte[] bytes = RequestBuilder.buildRequestBody(RequestBuilder.REQUEST_TYPE.REQUEST_TYPE_IAP,
                    currency,
                    iapNumber,
                    iapt);

            if (bytes == null) {
                if (callback != null) {
                    callback.onRequestFailed("Iap param is null");
                }
                return;
            }

            ByteRequestBody requestBody = new ByteRequestBody(bytes);
            AdRequest.post()
                    .url(url)
                    .body(requestBody)
                    .headers(headers)
                    .connectTimeout(30000)
                    .readTimeout(60000)
                    .callback(callback)
                    .performRequest(AdtUtil.getApplication());

        } catch (Exception e) {
            DeveloperLog.LogE("HttpIAP error ", e);
            CrashUtil.getSingleton().saveException(e);

            if (callback != null) {
                callback.onRequestFailed("httpIAP error");
            }
        }
    }

    private static String getHost() {
        Config config = DataCache.getInstance().get("Config", Config.class);
        if (config == null) {
            return "";
        }
        return config.getSdkHost();
    }
}
