// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.mobileads;

import android.app.Activity;
import android.text.TextUtils;

import com.adtiming.mediationsdk.mediation.CustomAdsAdapter;
import com.adtiming.mediationsdk.mediation.InterstitialAdCallback;
import com.adtiming.mediationsdk.mediation.MediationInfo;
import com.adtiming.mediationsdk.mediation.RewardedVideoCallback;
import com.adtiming.mediationsdk.utils.AdLog;
import com.chartboost.sdk.Chartboost;
import com.chartboost.sdk.ChartboostDelegate;
import com.chartboost.sdk.Model.CBError;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChartboostAdapter extends CustomAdsAdapter {

    private AtomicBoolean hasInit = new AtomicBoolean(false);

    private ConcurrentLinkedQueue<String> mRvLoadTriggerIds;
    private ConcurrentLinkedQueue<String> mIsLoadTriggerIds;
    private ConcurrentMap<String, RewardedVideoCallback> mRvCallbacks;
    private ConcurrentMap<String, InterstitialAdCallback> mIsCallbacks;

    public ChartboostAdapter() {
        mIsLoadTriggerIds = new ConcurrentLinkedQueue<>();
        mRvLoadTriggerIds = new ConcurrentLinkedQueue<>();
        mRvCallbacks = new ConcurrentHashMap<>();
        mIsCallbacks = new ConcurrentHashMap<>();
    }

    private void initSDK(final Activity activity) {
        AdLog.getSingleton().LogD("init chartboost sdk");
        if (!hasInit.get()) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        hasInit.set(true);
                        String[] tmp = mAppKey.split("#");
                        String appId = tmp[0];
                        String signature = tmp[1];
                        Chartboost.setActivityCallbacks(false);
                        Chartboost.setPIDataUseConsent(activity, Chartboost.CBPIDataUseConsent.YES_BEHAVIORAL);
                        Chartboost.startWithAppId(activity, appId, signature);
                        Chartboost.setDelegate(new CbCallback());
                        Chartboost.setMediation(Chartboost.CBMediation.CBMediationOther, getAdapterVersion());
                        Chartboost.setShouldRequestInterstitialsInFirstSession(false);
                        Chartboost.setShouldPrefetchVideoContent(false);
                        Chartboost.setAutoCacheAds(true);

                        Chartboost.onCreate(activity);
                        Chartboost.onStart(activity);
                        Chartboost.onResume(activity);
                    } catch (Exception e) {
                        AdLog.getSingleton().LogE("Adt-Chartboost", e);
                    }
                }
            });
        }
    }

    private void onInitCallback() {
        for (Map.Entry<String, RewardedVideoCallback> videoCallbackEntry : mRvCallbacks.entrySet()) {
            videoCallbackEntry.getValue().onRewardedVideoInitSuccess();
        }
        for (Map.Entry<String, InterstitialAdCallback> interstitialAdCallbackEntry : mIsCallbacks.entrySet()) {
            interstitialAdCallbackEntry.getValue().onInterstitialAdInitSuccess();
        }
    }

    @Override
    public String getMediationVersion() {
        if (hasInit.get()) {
            return Chartboost.getSDKVersion();
        } else {
            return "";
        }
    }

    @Override
    public String getAdapterVersion() {
        return MediationInfo.ADAPTER_11_VERSION;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.PLAT_ID_11;
    }

    @Override
    public void onResume(Activity activity) {
        if (activity != null) {
            Chartboost.onStart(activity);
            Chartboost.onResume(activity);
        }
    }

    @Override
    public void onPause(Activity activity) {
        if (activity != null) {
            Chartboost.onPause(activity);
            Chartboost.onStop(activity);
        }
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap
            , RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String checkError = check(activity);
        if (TextUtils.isEmpty(checkError)) {
            mRvCallbacks.put((String) dataMap.get("pid"), callback);
            if (!hasInit.get()) {
                initSDK(activity);
            } else {
                callback.onRewardedVideoInitSuccess();
            }
        } else {
            callback.onRewardedVideoLoadFailed(checkError);
        }
    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, callback);
        String checkError = check(activity, adUnitId);
        if (TextUtils.isEmpty(checkError)) {
            mRvLoadTriggerIds.add(adUnitId);
            if (Chartboost.hasRewardedVideo(adUnitId)) {
                if (callback != null) {
                    callback.onRewardedVideoLoadSuccess();
                }
            } else {
                Chartboost.cacheRewardedVideo(adUnitId);
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(checkError);
            }
        }
    }

    @Override
    public void showRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        String checkError = check(activity, adUnitId);
        if (TextUtils.isEmpty(checkError)) {
            if (Chartboost.hasRewardedVideo(adUnitId)) {
                Chartboost.showRewardedVideo(adUnitId);
            } else {
                AdLog.getSingleton().LogE("chartboost ad not ready");
                callback.onRewardedVideoAdShowFailed("chartboost ad not ready");
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(checkError);
            }
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        return Chartboost.hasRewardedVideo(adUnitId);
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String checkError = check(activity);
        if (TextUtils.isEmpty(checkError)) {
            mIsCallbacks.put((String) dataMap.get("pid"), callback);
            if (!hasInit.get()) {
                initSDK(activity);
            } else {
                callback.onInterstitialAdInitSuccess();
            }
        } else {
            callback.onInterstitialAdInitFailed(checkError);
        }
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, callback);
        String checkError = check(activity, adUnitId);
        if (TextUtils.isEmpty(checkError)) {
            mIsLoadTriggerIds.add(adUnitId);
            if (Chartboost.hasInterstitial(adUnitId)) {
                if (callback != null) {
                    callback.onInterstitialAdLoadSuccess();
                }
            } else {
                Chartboost.cacheInterstitial(adUnitId);
            }
        } else {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(checkError);
            }
        }
    }

    @Override
    public void showInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        String checkError = check(activity, adUnitId);
        if (TextUtils.isEmpty(checkError)) {
            if (Chartboost.hasInterstitial(adUnitId)) {
                Chartboost.showInterstitial(adUnitId);
            } else {
                AdLog.getSingleton().LogE("chartboost ad not ready");
                callback.onInterstitialAdShowFailed("chartboost ad not ready");
            }
        } else {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(checkError);
            }
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        return Chartboost.hasInterstitial(adUnitId);
    }

    class CbCallback extends ChartboostDelegate {

        @Override
        public void didCacheInterstitial(String location) {
            InterstitialAdCallback listener = mIsCallbacks.get(location);
            AdLog.getSingleton().LogD("Adt-Chartboost Chartboost Interstitial ad load success");
            if (listener != null && mIsLoadTriggerIds.contains(location)) {
                listener.onInterstitialAdLoadSuccess();
                mIsLoadTriggerIds.remove(location);
            }
        }

        @Override
        public void didFailToLoadInterstitial(String location, CBError.CBImpressionError error) {
            InterstitialAdCallback listener = mIsCallbacks.get(location);
            String errorString = error != null ? error.name() : " error message ";
            AdLog.getSingleton().LogE("Adt-Chartboost Chartboost Interstitial ad load failed : " + errorString);
            if (listener != null && mIsLoadTriggerIds.contains(location)) {
                listener.onInterstitialAdLoadFailed(errorString);
                mIsLoadTriggerIds.remove(location);
            }
        }

        @Override
        public void didClickInterstitial(String location) {
            AdLog.getSingleton().LogD("Adt-Chartboost Chartboost Interstitial ad click");
            InterstitialAdCallback listener = mIsCallbacks.get(location);
            if (listener != null) {
                listener.onInterstitialAdClick();
            }
        }

        @Override
        public void didDisplayInterstitial(String location) {
            AdLog.getSingleton().LogD("Adt-Chartboost Chartboost Interstitial ad display");
            InterstitialAdCallback listener = mIsCallbacks.get(location);
            if (listener != null) {
                listener.onInterstitialAdVisible();
                listener.onInterstitialAdOpened();
            }
        }

        @Override
        public void didDismissInterstitial(String location) {
            AdLog.getSingleton().LogD("Adt-Chartboost Chartboost Interstitial ad close");
            InterstitialAdCallback listener = mIsCallbacks.get(location);
            if (listener != null) {
                listener.onInterstitialAdClosed();
            }
        }

        @Override
        public void didCacheRewardedVideo(String location) {
            RewardedVideoCallback listener = mRvCallbacks.get(location);
            AdLog.getSingleton().LogD("Adt-Chartboost Chartboost RewardVideo ad load success");
            if (listener != null && mRvLoadTriggerIds.contains(location)) {
                listener.onRewardedVideoLoadSuccess();
                mRvLoadTriggerIds.remove(location);
            }
        }

        @Override
        public void didFailToLoadRewardedVideo(String location, CBError.CBImpressionError error) {
            RewardedVideoCallback listener = mRvCallbacks.get(location);
            String errorString = error != null ? error.name() : " error message ";
            AdLog.getSingleton().LogE("Adt-Chartboost Chartboost RewardVideo ad load failed:" + errorString);
            if (listener != null && mRvLoadTriggerIds.contains(location)) {
                listener.onRewardedVideoLoadFailed(errorString);
                mRvLoadTriggerIds.remove(location);
            }
        }

        @Override
        public void didClickRewardedVideo(String location) {
            RewardedVideoCallback listener = mRvCallbacks.get(location);
            AdLog.getSingleton().LogD("Adt-Chartboost Chartboost RewardVideo ad click");
            if (listener != null) {
                listener.onRewardedVideoAdClicked();
            }
        }

        @Override
        public void didCompleteRewardedVideo(String location, int reward) {
            RewardedVideoCallback listener = mRvCallbacks.get(location);
            AdLog.getSingleton().LogD("Adt-Chartboost Chartboost RewardVideo ad complete");
            if (listener != null) {
                listener.onRewardedVideoAdEnded();
                listener.onRewardedVideoAdRewarded();
            }
        }

        @Override
        public void didDismissRewardedVideo(String location) {
            AdLog.getSingleton().LogD("Adt-Chartboost Chartboost RewardVideo ad close");
            RewardedVideoCallback listener = mRvCallbacks.get(location);
            if (listener != null) {
                listener.onRewardedVideoAdClosed();
            }
        }

        @Override
        public void didDisplayRewardedVideo(String location) {
            AdLog.getSingleton().LogD("Adt-Chartboost Chartboost RewardVideo ad display");
            RewardedVideoCallback listener = mRvCallbacks.get(location);
            if (listener != null) {
                listener.onRewardedVideoAdVisible();
                listener.onRewardedVideoAdOpened();
                listener.onRewardedVideoAdStarted();
            }
        }

        @Override
        public void didInitialize() {
            super.didInitialize();
            AdLog.getSingleton().LogD("Adt-Chartboost Chartboost init success");
            onInitCallback();
        }
    }
}
