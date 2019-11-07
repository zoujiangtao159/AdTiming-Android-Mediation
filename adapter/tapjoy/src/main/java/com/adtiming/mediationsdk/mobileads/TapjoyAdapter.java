// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.mobileads;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.adtiming.mediationsdk.mediation.CustomAdsAdapter;
import com.adtiming.mediationsdk.mediation.InterstitialAdCallback;
import com.adtiming.mediationsdk.mediation.MediationInfo;
import com.adtiming.mediationsdk.mediation.RewardedVideoCallback;
import com.adtiming.mediationsdk.utils.AdLog;
import com.tapjoy.TJActionRequest;
import com.tapjoy.TJConnectListener;
import com.tapjoy.TJError;
import com.tapjoy.TJPlacement;
import com.tapjoy.TJPlacementListener;
import com.tapjoy.TJPlacementVideoListener;
import com.tapjoy.Tapjoy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TapjoyAdapter extends CustomAdsAdapter implements TJPlacementListener, TJPlacementVideoListener {

    private static final String ADT_MEDIATION_NAME = "AdTiming";
    private static final String ADT_MEDIATION_ADAPTER_VERSION = "5.5.7";
    private InitState mInitState = InitState.NOT_INIT;
    private ConcurrentMap<String, TJPlacement> mVideos;
    private ConcurrentMap<TJPlacement, RewardedVideoCallback> mVideoCallbacks;
    private ConcurrentMap<String, TJPlacement> mInterstitialAds;
    private ConcurrentMap<TJPlacement, InterstitialAdCallback> mInterstitialAdCallbacks;
    private Handler mHandler;

    public TapjoyAdapter() {
        mHandler = new Handler(Looper.getMainLooper());
        mVideos = new ConcurrentHashMap<>();
        mVideoCallbacks = new ConcurrentHashMap<>();
        mInterstitialAds = new ConcurrentHashMap<>();
        mInterstitialAdCallbacks = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return Tapjoy.getVersion();
    }

    @Override
    public String getAdapterVersion() {
        return MediationInfo.ADAPTER_10_VERSION;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.PLAT_ID_10;
    }

    private synchronized void initSDK(final Activity activity) {
        mInitState = InitState.INIT_PENDING;
        Tapjoy.limitedConnect(activity.getApplicationContext(), mAppKey, new TJConnectListener() {
            @Override
            public void onConnectSuccess() {
                AdLog.getSingleton().LogD("Adt-Tapjoy", "Tapjoy init success ");
                for (TJPlacement tjPlacement : mVideoCallbacks.keySet()) {
                    callbackOnMainThread(8, tjPlacement, "");
                }
                for (TJPlacement tjPlacement : mInterstitialAdCallbacks.keySet()) {
                    callbackOnMainThread(8, tjPlacement, "");
                }
                mInitState = InitState.INIT_SUCCESS;
            }

            @Override
            public void onConnectFailure() {
                AdLog.getSingleton().LogE("Adt-Tapjoy: Tapjoy init failed ");
                for (TJPlacement tjPlacement : mVideoCallbacks.keySet()) {
                    callbackOnMainThread(9, tjPlacement, "Tapjoy init failed");
                }
                for (TJPlacement tjPlacement : mInterstitialAdCallbacks.keySet()) {
                    callbackOnMainThread(9, tjPlacement, "Tapjoy init failed");
                }
                mInitState = InitState.INIT_FAIL;
            }
        });
    }

    private synchronized TJPlacement requestVideoAd(String placementId) {
        TJPlacement placement = null;
        if (mVideos.containsKey(placementId) && mVideos.get(placementId) != null) {
            placement = mVideos.get(placementId);
        }
        if (placement == null) {
            placement = Tapjoy.getLimitedPlacement(placementId, this);
            placement.setVideoListener(this);
            placement.setMediationName(ADT_MEDIATION_NAME);
            placement.setAdapterVersion(ADT_MEDIATION_ADAPTER_VERSION);
            mVideos.put(placementId, placement);
        }
        return placement;
    }

    private synchronized TJPlacement requestInterstitialAd(String placementId) {
        TJPlacement placement = null;
        if (mInterstitialAds.containsKey(placementId) && mInterstitialAds.get(placementId) != null) {
            placement = mInterstitialAds.get(placementId);
        }
        if (placement == null) {
            placement = Tapjoy.getLimitedPlacement(placementId, this);
            placement.setMediationName(ADT_MEDIATION_NAME);
            placement.setAdapterVersion(ADT_MEDIATION_ADAPTER_VERSION);
            mInterstitialAds.put(placementId, placement);
        }
        return placement;
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap
            , RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            switch (mInitState) {
                case NOT_INIT:
                    Tapjoy.setActivity(activity);
                    initSDK(activity);
                    mVideoCallbacks.put(requestVideoAd((String) dataMap.get("pid")), callback);
                    break;
                case INIT_PENDING:
                    mVideoCallbacks.put(requestVideoAd((String) dataMap.get("pid")), callback);
                    break;
                case INIT_SUCCESS:
                    callback.onRewardedVideoInitSuccess();
                    break;
                case INIT_FAIL:
                    callback.onRewardedVideoInitFailed("Tapjoy init failed");
                    break;
                default:
                    break;
            }
        } else {
            callback.onRewardedVideoInitFailed(error);
        }
    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            if (!Tapjoy.isLimitedConnected()) {
                if (callback != null) {
                    callback.onRewardedVideoLoadFailed("Tapjoy video load failed cause not init");
                }
            } else {
                TJPlacement placement = requestVideoAd(adUnitId);
                if (placement != null) {
                    if (!mVideoCallbacks.containsKey(placement)) {
                        mVideoCallbacks.put(placement, callback);
                    }
                    if (placement.isContentReady()) {
                        if (callback != null) {
                            callback.onRewardedVideoLoadSuccess();
                        }
                    } else {
                        placement.requestContent();
                    }
                }
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(error);
            }
        }
    }

    @Override
    public void showRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        TJPlacement placement = mVideos.get(adUnitId);
        if (placement != null && placement.isContentReady()) {
            if (!mVideoCallbacks.containsKey(placement)) {
                mVideoCallbacks.put(placement, callback);
            }
            placement.showContent();
        } else {
            AdLog.getSingleton().LogE("Adt-Tapjoy: Tapjoy Video ad not ready ");
            callback.onRewardedVideoAdShowFailed("Tapjoy Video ad not ready ");
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        TJPlacement placement = mVideos.get(adUnitId);
        return placement != null && placement.isContentReady();
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            switch (mInitState) {
                case NOT_INIT:
                    Tapjoy.setActivity(activity);
                    initSDK(activity);
                    mInterstitialAdCallbacks.put(requestInterstitialAd((String) dataMap.get("pid")), callback);
                    break;
                case INIT_PENDING:
                    mInterstitialAdCallbacks.put(requestInterstitialAd((String) dataMap.get("pid")), callback);
                    break;
                case INIT_SUCCESS:
                    callback.onInterstitialAdInitSuccess();
                    break;
                case INIT_FAIL:
                    callback.onInterstitialAdInitFailed("Tapjoy init failed");
                    break;
                default:
                    break;
            }
        } else {
            callback.onInterstitialAdInitFailed(error);
        }
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            if (!Tapjoy.isLimitedConnected()) {
                if (callback != null) {
                    callback.onInterstitialAdLoadFailed("Tapjoy interstitial load failed cause not init");
                }
            } else {
                TJPlacement placement = requestInterstitialAd(adUnitId);
                if (placement != null) {
                    if (!mInterstitialAdCallbacks.containsKey(placement)) {
                        mInterstitialAdCallbacks.put(placement, callback);
                    }
                    if (placement.isContentReady()) {
                        if (callback != null) {
                            callback.onInterstitialAdLoadSuccess();
                        }
                    } else {
                        placement.requestContent();
                    }
                }
            }
        } else {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(error);
            }
        }
    }

    @Override
    public void showInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        TJPlacement placement = mInterstitialAds.get(adUnitId);
        if (placement != null && placement.isContentReady()) {
            if (!mInterstitialAdCallbacks.containsKey(placement)) {
                mInterstitialAdCallbacks.put(placement, callback);
            }
            placement.showContent();
        } else {
            AdLog.getSingleton().LogE("Adt-Tapjoy: Tapjoy Interstitial ad not ready ");
            callback.onInterstitialAdShowFailed("Tapjoy Video ad not ready ");
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        TJPlacement placement = mInterstitialAds.get(adUnitId);
        return placement != null && placement.isContentReady();
    }

    @Override
    public void onRequestSuccess(TJPlacement tjPlacement) {
        if (!tjPlacement.isContentAvailable()) {
            //no fill
            callbackOnMainThread(1, tjPlacement, "no fill");
        }
    }

    @Override
    public void onRequestFailure(TJPlacement tjPlacement, TJError tjError) {
        callbackOnMainThread(1, tjPlacement, tjError.message);
    }

    @Override
    public void onContentReady(TJPlacement tjPlacement) {
        callbackOnMainThread(0, tjPlacement, null);
    }

    @Override
    public void onContentShow(TJPlacement tjPlacement) {
        callbackOnMainThread(3, tjPlacement, null);
    }

    @Override
    public void onContentDismiss(TJPlacement tjPlacement) {
        callbackOnMainThread(5, tjPlacement, null);
    }

    @Override
    public void onPurchaseRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String s) {

    }

    @Override
    public void onRewardRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String s, int i) {
        AdLog.getSingleton().LogD("Adt-Tapjoy", "Tapjoy video ad reward request " + s);
    }

    @Override
    public void onClick(TJPlacement tjPlacement) {
        callbackOnMainThread(4, tjPlacement, null);
    }

    @Override
    public void onVideoStart(TJPlacement tjPlacement) {
        callbackOnMainThread(7, tjPlacement, null);
    }

    @Override
    public void onVideoError(TJPlacement tjPlacement, String s) {
        callbackOnMainThread(2, tjPlacement, null);
    }

    @Override
    public void onVideoComplete(TJPlacement tjPlacement) {
        callbackOnMainThread(6, tjPlacement, null);
    }

    private void callbackOnMainThread(final int callbackType, final TJPlacement placement, final String error) {
        final RewardedVideoCallback videoCallback = mVideoCallbacks.get(placement);
        final InterstitialAdCallback isCallback = mInterstitialAdCallbacks.get(placement);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                switch (callbackType) {
                    case 0:
                        AdLog.getSingleton().LogD("Adt-Tapjoy", "Tapjoy ad load success " + placement.getName());
                        if (videoCallback != null) {
                            videoCallback.onRewardedVideoLoadSuccess();
                        }
                        if (isCallback != null) {
                            isCallback.onInterstitialAdLoadSuccess();
                        }
                        break;
                    case 1:
                        AdLog.getSingleton().LogE("Adt-Tapjoy: Tapjoy ad load failed :" + error);
                        if (videoCallback != null) {
                            videoCallback.onRewardedVideoLoadFailed(error);
                        }
                        if (isCallback != null) {
                            isCallback.onInterstitialAdLoadFailed(error);
                        }
                        break;
                    case 2:
                        AdLog.getSingleton().LogE("Adt-Tapjoy: Tapjoy ad show failed :" + error);
                        if (videoCallback != null) {
                            videoCallback.onRewardedVideoAdShowFailed(error);
                        }
                        if (isCallback != null) {
                            isCallback.onInterstitialAdShowFailed(error);
                        }
                        break;
                    case 7:
                        AdLog.getSingleton().LogD("Adt-Tapjoy", "Tapjoy video ad start");
                        if (videoCallback != null) {
                            videoCallback.onRewardedVideoAdStarted();
                        }
                        break;
                    case 3:
                        AdLog.getSingleton().LogD("Adt-Tapjoy", "Tapjoy ad open");
                        if (videoCallback != null) {
                            videoCallback.onRewardedVideoAdVisible();
                            videoCallback.onRewardedVideoAdOpened();
                        }
                        if (isCallback != null) {
                            isCallback.onInterstitialAdVisible();
                            isCallback.onInterstitialAdOpened();
                        }
                        break;
                    case 4:
                        AdLog.getSingleton().LogD("Adt-Tapjoy", "Tapjoy ad click");
                        if (videoCallback != null) {
                            videoCallback.onRewardedVideoAdClicked();
                        }
                        if (isCallback != null) {
                            isCallback.onInterstitialAdClick();
                        }
                        break;
                    case 5:
                        AdLog.getSingleton().LogD("Adt-Tapjoy", "Tapjoy ad close");
                        if (videoCallback != null) {
                            videoCallback.onRewardedVideoAdClosed();
                        }
                        if (isCallback != null) {
                            isCallback.onInterstitialAdClosed();
                        }
                        break;
                    case 6:
                        AdLog.getSingleton().LogD("Adt-Tapjoy", "Tapjoy video ad end");
                        if (videoCallback != null) {
                            videoCallback.onRewardedVideoAdEnded();
                            videoCallback.onRewardedVideoAdRewarded();
                        }
                        break;
                    case 8:
                        if (videoCallback != null) {
                            videoCallback.onRewardedVideoInitSuccess();
                        }

                        if (isCallback != null) {
                            isCallback.onInterstitialAdInitSuccess();
                        }
                        break;
                    case 9:
                        if (videoCallback != null) {
                            videoCallback.onRewardedVideoInitFailed(error);
                        }

                        if (isCallback != null) {
                            isCallback.onInterstitialAdInitFailed(error);
                        }
                        break;
                    default:
                        break;
                }
            }
        };
        mHandler.post(runnable);
    }

    /**
     * Vungle sdk init state
     */
    private enum InitState {
        /**
         * 
         */
        NOT_INIT,
        /**
         * 
         */
        INIT_PENDING,
        /**
         *
         */
        INIT_SUCCESS,
        /**
         * 
         */
        INIT_FAIL
    }
}
