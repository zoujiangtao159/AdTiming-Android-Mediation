// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.mobileads;

import android.app.Activity;
import android.text.TextUtils;

import com.adtiming.mediationsdk.mediation.CustomAdsAdapter;
import com.adtiming.mediationsdk.mediation.MediationInfo;
import com.adtiming.mediationsdk.mediation.InterstitialAdCallback;
import com.adtiming.mediationsdk.mediation.RewardedVideoCallback;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.BuildConfig;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.ads.RewardedVideoAd;
import com.facebook.ads.RewardedVideoAdListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class FacebookAdapter extends CustomAdsAdapter {
    private Boolean mDidInitSuccess = null;
    private AtomicBoolean mDidCallInit;

    private ConcurrentMap<String, RewardedVideoAd> mFbRvAds;
    private ConcurrentMap<String, InterstitialAd> mFbIsAds;
    private ConcurrentMap<String, RewardedVideoCallback> mRvCallbacks;
    private ConcurrentMap<String, InterstitialAdCallback> mIsCallbacks;

    public FacebookAdapter() {
        mFbRvAds = new ConcurrentHashMap<>();
        mFbIsAds = new ConcurrentHashMap<>();
        mRvCallbacks = new ConcurrentHashMap<>();
        mIsCallbacks = new ConcurrentHashMap<>();
        mDidCallInit = new AtomicBoolean(false);
    }

    @Override
    public String getMediationVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public String getAdapterVersion() {
        return MediationInfo.ADAPTER_2_VERSION;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.PLAT_ID_2;
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap, RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            mRvCallbacks.put((String) dataMap.get("pid"), callback);
            initSdk(activity);
            if (mDidInitSuccess != null) {
                if (mDidInitSuccess) {
                    callback.onRewardedVideoInitSuccess();
                } else {
                    callback.onRewardedVideoInitFailed("Init facebook sdk failed");
                }
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoInitFailed(error);
            }
        }
    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            RewardedVideoAd rewardedVideoAd = getRv(activity, adUnitId);
            if (rewardedVideoAd.isAdLoaded()) {
                if (callback != null) {
                    callback.onRewardedVideoLoadSuccess();
                }
            } else {
                rewardedVideoAd.loadAd();
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(error);
            }
        }
    }

    @Override
    public void showRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        RewardedVideoAd rewardedVideoAd = mFbRvAds.get(adUnitId);
        if (rewardedVideoAd != null && rewardedVideoAd.isAdLoaded()) {
            rewardedVideoAd.show();
        } else {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed("Facebook rewardedVideo is not ready");
            }
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        RewardedVideoAd rewardedVideoAd = mFbRvAds.get(adUnitId);
        return rewardedVideoAd != null && rewardedVideoAd.isAdLoaded();
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            mIsCallbacks.put((String) dataMap.get("pid"), callback);
            initSdk(activity);
            if (mDidInitSuccess != null) {
                if (mDidInitSuccess) {
                    callback.onInterstitialAdInitSuccess();
                } else {
                    callback.onInterstitialAdInitFailed("Init facebook sdk failed");
                }
            }
        } else {
            if (callback != null) {
                callback.onInterstitialAdInitFailed(error);
            }
        }

    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            InterstitialAd interstitialAd = getIs(activity, adUnitId);
            if (interstitialAd.isAdLoaded()) {
                if (callback != null) {
                    callback.onInterstitialAdLoadSuccess();
                }
            } else {
                interstitialAd.loadAd();
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
        InterstitialAd interstitialAd = mFbIsAds.get(adUnitId);
        if (interstitialAd != null && interstitialAd.isAdLoaded()) {
            interstitialAd.show();
        } else {
            if (callback != null) {
                callback.onInterstitialAdShowFailed("Facebook interstitial is not ready");
            }
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        InterstitialAd interstitialAd = mFbIsAds.get(adUnitId);
        return interstitialAd != null && interstitialAd.isAdLoaded();
    }

    private RewardedVideoAd getRv(Activity activity, String adUnitId) {
        RewardedVideoAd rewardedVideoAd = mFbRvAds.get(adUnitId);
        if (rewardedVideoAd == null) {
            rewardedVideoAd = new RewardedVideoAd(activity, adUnitId);
            rewardedVideoAd.setAdListener(new FbRvListener(mRvCallbacks.get(adUnitId)));
            mFbRvAds.put(adUnitId, rewardedVideoAd);
        }
        return rewardedVideoAd;
    }

    private InterstitialAd getIs(Activity activity, String adUnitId) {
        InterstitialAd interstitialAd = mFbIsAds.get(adUnitId);
        if (interstitialAd == null) {
            interstitialAd = new InterstitialAd(activity, adUnitId);
            interstitialAd.setAdListener(new FbIsAdListener(mIsCallbacks.get(adUnitId)));
            mFbIsAds.put(adUnitId, interstitialAd);
        }
        return interstitialAd;
    }

    private void initSdk(final Activity activity) {
        if (mDidCallInit.compareAndSet(false, true)) {
            if (AudienceNetworkAds.isInAdsProcess(activity.getApplicationContext())) {
                // According to Xabi from facebook (29/4/19) - the meaning of isInAdsProcess==true is that
                // another process has already initialized Facebook's SDK and in this case there's no need to init it again.
                // Without this check an error will appear in the log.
                mDidInitSuccess = true;
                return;
            }

            AudienceNetworkAds.buildInitSettings(activity.getApplicationContext())
                    .withInitListener(new AudienceNetworkAds.InitListener() {
                        @Override
                        public void onInitialized(final AudienceNetworkAds.InitResult result) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (result.isSuccess()) {
                                        mDidInitSuccess = true;
                                        for (InterstitialAdCallback callback : mIsCallbacks.values()) {
                                            callback.onInterstitialAdInitSuccess();
                                        }
                                        for (RewardedVideoCallback callback : mRvCallbacks.values()) {
                                            callback.onRewardedVideoInitSuccess();
                                        }
                                    } else {
                                        mDidInitSuccess = false;
                                        String message = "Facebook init failed:" + result.getMessage();

                                        for (InterstitialAdCallback callback : mIsCallbacks.values()) {
                                            callback.onInterstitialAdInitFailed(message);
                                        }
                                        for (RewardedVideoCallback callback : mRvCallbacks.values()) {
                                            callback.onRewardedVideoInitFailed(message);
                                        }
                                    }
                                }
                            });
                        }
                    }).initialize();
        }
    }

    private class FbRvListener implements RewardedVideoAdListener {

        private RewardedVideoCallback rvCallback;

        FbRvListener(RewardedVideoCallback callback) {
            rvCallback = callback;
        }

        @Override
        public void onRewardedVideoCompleted() {
            if (rvCallback != null) {
                rvCallback.onRewardedVideoAdRewarded();
                rvCallback.onRewardedVideoAdEnded();
            }
        }

        @Override
        public void onError(Ad ad, AdError adError) {
            if (rvCallback != null) {
                rvCallback.onRewardedVideoLoadFailed("Facebook rewardedVideo load failed : " + adError.getErrorMessage());
            }
        }

        @Override
        public void onAdLoaded(Ad ad) {
            if (rvCallback != null) {
                rvCallback.onRewardedVideoLoadSuccess();
            }
        }

        @Override
        public void onAdClicked(Ad ad) {
            if (rvCallback != null) {
                rvCallback.onRewardedVideoAdClicked();
            }
        }

        @Override
        public void onLoggingImpression(Ad ad) {
            if (rvCallback != null) {
                rvCallback.onRewardedVideoAdOpened();
                rvCallback.onRewardedVideoAdVisible();
                rvCallback.onRewardedVideoAdStarted();
            }
        }

        @Override
        public void onRewardedVideoClosed() {
            if (rvCallback != null) {
                rvCallback.onRewardedVideoAdClosed();
            }
        }
    }

    private class FbIsAdListener implements InterstitialAdListener {

        private InterstitialAdCallback isCallback;

        FbIsAdListener(InterstitialAdCallback callback) {
            isCallback = callback;
        }

        @Override
        public void onInterstitialDisplayed(Ad ad) {

        }

        @Override
        public void onInterstitialDismissed(Ad ad) {
            if (isCallback != null) {
                isCallback.onInterstitialAdClosed();
            }
        }

        @Override
        public void onError(Ad ad, AdError adError) {
            if (isCallback != null) {
                isCallback.onInterstitialAdLoadFailed("Facebook interstitial ad load failed : " + adError.getErrorMessage());
            }
        }

        @Override
        public void onAdLoaded(Ad ad) {
            if (isCallback != null) {
                isCallback.onInterstitialAdLoadSuccess();
            }
        }

        @Override
        public void onAdClicked(Ad ad) {
            if (isCallback != null) {
                isCallback.onInterstitialAdClick();
            }
        }

        @Override
        public void onLoggingImpression(Ad ad) {
            if (isCallback != null) {
                isCallback.onInterstitialAdOpened();
                isCallback.onInterstitialAdVisible();
            }
        }
    }
}
