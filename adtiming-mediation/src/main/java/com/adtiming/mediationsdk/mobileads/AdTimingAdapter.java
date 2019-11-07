// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.mobileads;

import android.app.Activity;

import com.adtiming.mediationsdk.adt.AdtSDK;
import com.adtiming.mediationsdk.adt.interactive.InteractiveAd;
import com.adtiming.mediationsdk.adt.interactive.InteractiveListener;
import com.adtiming.mediationsdk.adt.interstitial.InterstitialAd;
import com.adtiming.mediationsdk.adt.interstitial.InterstitialListener;
import com.adtiming.mediationsdk.adt.video.VideoAd;
import com.adtiming.mediationsdk.adt.video.VideoListener;
import com.adtiming.mediationsdk.mediation.CustomAdsAdapter;
import com.adtiming.mediationsdk.mediation.InteractiveAdCallback;
import com.adtiming.mediationsdk.mediation.InterstitialAdCallback;
import com.adtiming.mediationsdk.mediation.MediationInfo;
import com.adtiming.mediationsdk.mediation.RewardedVideoCallback;
import com.adtiming.mediationsdk.utils.AdLog;
import com.adtiming.mediationsdk.utils.Constants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AdTimingAdapter extends CustomAdsAdapter implements VideoListener, InteractiveListener, InterstitialListener {

    private boolean isDidInit;
    private ConcurrentMap<String, VideoAd> mVideos;
    private ConcurrentMap<String, RewardedVideoCallback> mVideoListeners;
    private ConcurrentMap<String, InteractiveAd> mInteractiveAds;
    private ConcurrentMap<String, InteractiveAdCallback> mInteractiveListeners;
    private ConcurrentMap<String, InterstitialAd> mInterstitialAds;
    private ConcurrentMap<String, InterstitialAdCallback> mInterstitialListeners;

    public AdTimingAdapter() {
        isDidInit = false;
        mVideos = new ConcurrentHashMap<>();
        mVideoListeners = new ConcurrentHashMap<>();
        mInteractiveAds = new ConcurrentHashMap<>();
        mInteractiveListeners = new ConcurrentHashMap<>();
        mInterstitialAds = new ConcurrentHashMap<>();
        mInterstitialListeners = new ConcurrentHashMap<>();
    }

    private synchronized boolean initSDK(Activity activity) {
        if (!isDidInit) {
            if (activity == null) {
                return false;
            }
            AdtSDK.initializeSdk(activity);
            isDidInit = true;
            return true;
        }
        return true;
    }

    @Override
    public String getMediationVersion() {
        return Constants.SDK_V;
    }

    @Override
    public String getAdapterVersion() {
        return Constants.SDK_V;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.PLAT_ID_0;
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap, RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        boolean init = initSDK(activity);
        if (init) {
            callback.onRewardedVideoInitSuccess();
        } else {
            callback.onRewardedVideoInitFailed("");
        }
    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        mVideoListeners.put(adUnitId, callback);
        VideoAd videoAd = getVideoAd(activity, adUnitId);
        videoAd.loadAd();
    }

    @Override
    public void showRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        VideoAd videoAd = mVideos.get(adUnitId);
        if (videoAd != null && videoAd.isReady()) {
            videoAd.show();
        } else {
            callback.onRewardedVideoAdShowFailed("no reward ad or not ready");
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        VideoAd videoAd = mVideos.get(adUnitId);
        return videoAd != null && videoAd.isReady();
    }

    private VideoAd getVideoAd(Activity activity, String adUnitId) {
        VideoAd videoAd = mVideos.get(adUnitId);
        if (videoAd == null) {
            videoAd = new VideoAd(activity, adUnitId);
            videoAd.setAdListener(this);
            mVideos.put(adUnitId, videoAd);
        }
        return videoAd;
    }

    @Override
    public void onVideoAdReady(String placementId) {
        AdLog.getSingleton().LogD("onVideoAdReady : " + placementId);
        RewardedVideoCallback callback = mVideoListeners.get(placementId);
        if (callback != null) {
            callback.onRewardedVideoLoadSuccess();
        }
    }

    @Override
    public void onVideoAdClose(String placementId, boolean isFullyWatched) {
        AdLog.getSingleton().LogD("onVideoAdClose : " + placementId);
        RewardedVideoCallback callback = mVideoListeners.get(placementId);
        if (callback != null) {
            callback.onRewardedVideoAdClosed();
        }
    }

    @Override
    public void onVideoAdShowed(String placementId) {
        AdLog.getSingleton().LogD("onVideoAdShowed : " + placementId);
        RewardedVideoCallback callback = mVideoListeners.get(placementId);
        if (callback != null) {
            callback.onRewardedVideoAdOpened();
        }
    }

    @Override
    public void onVideoAdRewarded(String placementId) {
        AdLog.getSingleton().LogD("onVideoAdRewarded : " + placementId);
        RewardedVideoCallback callback = mVideoListeners.get(placementId);
        if (callback != null) {
            callback.onRewardedVideoAdRewarded();
        }
    }

    @Override
    public void onVideoAdFailed(String placementId, String error) {
        AdLog.getSingleton().LogE("onVideoAdFailed : " + placementId + " cause :" + error);
        RewardedVideoCallback callback = mVideoListeners.get(placementId);
        if (callback != null) {
            callback.onRewardedVideoLoadFailed(error);
        }
    }

    @Override
    public void onVideoAdClicked(String placementId) {
        AdLog.getSingleton().LogD("onVideoAdClicked : " + placementId);
        RewardedVideoCallback callback = mVideoListeners.get(placementId);
        if (callback != null) {
            callback.onRewardedVideoAdClicked();
        }
    }

    @Override
    public void onVideoAdEvent(String placementId, String event) {
        AdLog.getSingleton().LogD("onVideoAdEvent : " + placementId + " event : " + event);
        RewardedVideoCallback callback = mVideoListeners.get(placementId);
        if (callback != null) {
            callback.onReceivedEvents(event);
        }
    }

    @Override
    public void onVideoAdShowFailed(String placementId, String error) {
        AdLog.getSingleton().LogE("onVideoAdShowFailed : " + placementId + " cause :" + error);
        RewardedVideoCallback callback = mVideoListeners.get(placementId);
        if (callback != null) {
            callback.onRewardedVideoAdShowFailed(error);
        }
    }

    @Override
    public void initInteractiveAd(Activity activity, Map<String, Object> dataMap, InteractiveAdCallback callback) {
        super.initInteractiveAd(activity, dataMap, callback);
        boolean init = initSDK(activity);
        if (init) {
            callback.onInteractiveAdInitSuccess();
        } else {
            callback.onInteractiveAdInitFailed("");
        }
    }

    @Override
    public void loadInteractiveAd(Activity activity, String adUnitId, InteractiveAdCallback callback) {
        super.loadInteractiveAd(activity, adUnitId, callback);
        mInteractiveListeners.put(adUnitId, callback);
        InteractiveAd interactiveAd = getInteractiveAd(activity, adUnitId);
        interactiveAd.loadAd();
    }

    @Override
    public void showInteractiveAd(Activity activity, String adUnitId, InteractiveAdCallback callback) {
        super.showInteractiveAd(activity, adUnitId, callback);
        InteractiveAd interactiveAd = mInteractiveAds.get(adUnitId);
        if (interactiveAd != null && interactiveAd.isReady()) {
            interactiveAd.show();
        } else {
            callback.onInteractiveAdShowFailed("no interactive ad or not ready");
        }
    }

    @Override
    public boolean isInteractiveAdAvailable(String adUnitId) {
        InteractiveAd interactiveAd = mInteractiveAds.get(adUnitId);
        return interactiveAd != null && interactiveAd.isReady();
    }

    private InteractiveAd getInteractiveAd(Activity activity, String adUnitId) {
        InteractiveAd interactiveAd = mInteractiveAds.get(adUnitId);
        if (interactiveAd == null) {
            interactiveAd = new InteractiveAd(activity, adUnitId);
            interactiveAd.setAdListener(this);
            mInteractiveAds.put(adUnitId, interactiveAd);
        }
        return interactiveAd;
    }

    @Override
    public void onInteractiveAdReady(String placementId) {
        AdLog.getSingleton().LogD("onInteractiveAdReady : " + placementId);
        InteractiveAdCallback callback = mInteractiveListeners.get(placementId);
        if (callback != null) {
            callback.onInteractiveAdLoadSuccess();
        }
    }

    @Override
    public void onInteractiveAdClose(String placementId) {
        AdLog.getSingleton().LogD("onInteractiveAdClose : " + placementId);
        InteractiveAdCallback callback = mInteractiveListeners.get(placementId);
        if (callback != null) {
            callback.onInteractiveAdClosed();
        }
    }

    @Override
    public void onInteractiveAdShowed(String placementId) {
        AdLog.getSingleton().LogD("onInteractiveAdShowed : " + placementId);
        InteractiveAdCallback callback = mInteractiveListeners.get(placementId);
        if (callback != null) {
            callback.onInteractiveAdOpened();
        }
    }

    @Override
    public void onInteractiveAdFailed(String placementId, String error) {
        AdLog.getSingleton().LogE("onInteractiveAdFailed : " + placementId);
        InteractiveAdCallback callback = mInteractiveListeners.get(placementId);
        if (callback != null) {
            callback.onInteractiveAdLoadFailed(error);
        }
    }

    @Override
    public void onInteractiveAdClicked(String placementId) {
        AdLog.getSingleton().LogD("onInteractiveAdClicked : " + placementId);
    }

    @Override
    public void onInteractiveAdEvent(String placementId, String event) {
        AdLog.getSingleton().LogD("onInteractiveAdEvent : " + placementId + " event : " + event);
        InteractiveAdCallback callback = mInteractiveListeners.get(placementId);
        if (callback != null) {
            callback.onReceivedEvents(event);
        }
    }

    @Override
    public void onInteractiveAdShowedFailed(String placementId, String error) {
        AdLog.getSingleton().LogE("onInteractiveAdShowedFailed : " + placementId);
        InteractiveAdCallback callback = mInteractiveListeners.get(placementId);
        if (callback != null) {
            callback.onInteractiveAdShowFailed(error);
        }
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        boolean init = initSDK(activity);
        if (init) {
            callback.onInterstitialAdInitSuccess();
        } else {
            callback.onInterstitialAdInitFailed("");
        }
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, callback);
        mInterstitialListeners.put(adUnitId, callback);
        InterstitialAd interstitialAd = getInterstitialAd(activity, adUnitId);
        interstitialAd.loadAd();
    }

    @Override
    public void showInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        InterstitialAd interstitialAd = mInterstitialAds.get(adUnitId);
        if (interstitialAd != null && interstitialAd.isReady()) {
            interstitialAd.show();
        } else {
            callback.onInterstitialAdShowFailed("no interstitial ad or not ready");
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        InterstitialAd interstitialAd = mInterstitialAds.get(adUnitId);
        return interstitialAd != null && interstitialAd.isReady();
    }

    private InterstitialAd getInterstitialAd(Activity activity, String adUnitId) {
        InterstitialAd interstitialAd = mInterstitialAds.get(adUnitId);
        if (interstitialAd == null) {
            interstitialAd = new InterstitialAd(activity, adUnitId);
            interstitialAd.setAdListener(this);
            mInterstitialAds.put(adUnitId, interstitialAd);
        }
        return interstitialAd;
    }

    @Override
    public void onInterstitialAdReady(String placementId) {
        AdLog.getSingleton().LogD("onInterstitialAdReady : " + placementId);
        InterstitialAdCallback callback = mInterstitialListeners.get(placementId);
        if (callback != null) {
            callback.onInterstitialAdLoadSuccess();
        }
    }

    @Override
    public void onInterstitialAdClose(String placementId) {
        AdLog.getSingleton().LogD("onInterstitialAdClose : " + placementId);
        InterstitialAdCallback callback = mInterstitialListeners.get(placementId);
        if (callback != null) {
            callback.onInterstitialAdClosed();
        }
    }

    @Override
    public void onInterstitialAdShowed(String placementId) {
        AdLog.getSingleton().LogD("onInterstitialAdShowed : " + placementId);
        InterstitialAdCallback callback = mInterstitialListeners.get(placementId);
        if (callback != null) {
            callback.onInterstitialAdOpened();
        }
    }

    @Override
    public void onInterstitialAdFailed(String placementId, String error) {
        AdLog.getSingleton().LogE("onInterstitialAdFailed : " + placementId);
        InterstitialAdCallback callback = mInterstitialListeners.get(placementId);
        if (callback != null) {
            callback.onInterstitialAdLoadFailed(error);
        }
    }

    @Override
    public void onInterstitialAdClicked(String placementId) {
        AdLog.getSingleton().LogD("onInterstitialAdClicked : " + placementId);
        InterstitialAdCallback callback = mInterstitialListeners.get(placementId);
        if (callback != null) {
            callback.onInterstitialAdClick();
        }
    }

    @Override
    public void onInterstitialAdEvent(String placementId, String event) {
        AdLog.getSingleton().LogD("onInterstitialAdEvent : " + placementId + " event : " + event);
        InterstitialAdCallback callback = mInterstitialListeners.get(placementId);
        if (callback != null) {
            callback.onReceivedEvents(event);
        }
    }

    @Override
    public void onInterstitialAdShowFailed(String placementId, String error) {
        AdLog.getSingleton().LogE("onInterstitialAdShowFailed : " + placementId);
        InterstitialAdCallback callback = mInterstitialListeners.get(placementId);
        if (callback != null) {
            callback.onInterstitialAdShowFailed(error);
        }
    }
}
