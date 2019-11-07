// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.adtiming.mediationsdk.mediation.CustomAdsAdapter;
import com.adtiming.mediationsdk.mediation.InterstitialAdCallback;
import com.adtiming.mediationsdk.mediation.MediationInfo;
import com.adtiming.mediationsdk.mediation.RewardedVideoCallback;
import com.adtiming.mediationsdk.utils.AdLog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AdMobAdapter extends CustomAdsAdapter {

    private ConcurrentMap<String, RewardedAd> mRewardedAds;
    private ConcurrentMap<String, InterstitialAd> mInterstitialAds;
    private boolean mDidInitSdk;

    public AdMobAdapter() {
        mRewardedAds = new ConcurrentHashMap<>();
        mInterstitialAds = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        if (mDidInitSdk) {
            return MobileAds.getVersionString();
        }
        return "";
    }

    @Override
    public String getAdapterVersion() {
        return MediationInfo.ADAPTER_1_VERSION;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.PLAT_ID_1;
    }

    // All calls to MobileAds must be on the main thread --> run all calls to initSDK in a thread.
    private synchronized void initSDK(Context context) {
        if (!mDidInitSdk) {
            mDidInitSdk = true;

            String adMobAppKey = null;
            try {
                ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                        PackageManager.GET_META_DATA);
                Bundle bundle = appInfo.metaData;
                adMobAppKey = bundle.getString("com.google.android.gms.ads.APPLICATION_ID");
            } catch (Exception e) {
                AdLog.getSingleton().LogE("AdMob can't find APPLICATION_ID in manifest.xml ");
            }

            if (TextUtils.isEmpty(adMobAppKey)) {
                adMobAppKey = mAppKey;
            }

            if (TextUtils.isEmpty(adMobAppKey)) {
                MobileAds.initialize(context);
            } else {
                MobileAds.initialize(context, adMobAppKey);
            }
        }
    }

    /*********************************RewardedVideo***********************************/
    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap, RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        if (Looper.myLooper() != Looper.getMainLooper()) {
            callback.onRewardedVideoInitFailed("Must be called on the main UI thread. ");
            return;
        }
        initSDK(activity);
        if (mDidInitSdk) {
            callback.onRewardedVideoInitSuccess();
        }
    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            callback.onRewardedVideoLoadFailed("Must be called on the main UI thread. ");
            return;
        }

        if (TextUtils.isEmpty(adUnitId)) {
            callback.onRewardedVideoLoadFailed("AdMob load RewardedVideo error cause adUnitId is null or empty");
            return;
        }
        RewardedAd rewardedAd = getRewardedAd(activity, adUnitId);
        AdLog.getSingleton().LogD("load RewardedVideo : " + getAdNetworkId() + " key : " + adUnitId);
        if (rewardedAd != null) {
            if (!rewardedAd.isLoaded()) {
                rewardedAd.loadAd(new AdRequest.Builder().build(), createRvLoadListener(callback));
            } else {
                callback.onRewardedVideoLoadSuccess();
            }
        }
    }

    @Override
    public void showRewardedVideo(final Activity activity, final String adUnitId
            , final RewardedVideoCallback callback) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                RewardedAd rewardedAd = mRewardedAds.get(adUnitId);
                if (rewardedAd != null && rewardedAd.isLoaded()) {
                    rewardedAd.show(activity, createRvCallback(adUnitId, callback));
                } else {
                    callback.onRewardedVideoAdShowFailed("");
                }
            }
        });
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        RewardedAd rewardedAd = mRewardedAds.get(adUnitId);
        return rewardedAd != null && rewardedAd.isLoaded();
    }

    /**
     * Creates a new one everytime for it can't be re-used.
     */
    private RewardedAd getRewardedAd(Activity activity, String adUnitId) {
        RewardedAd rewardedAd = new RewardedAd(activity, adUnitId);
        mRewardedAds.put(adUnitId, rewardedAd);
        return rewardedAd;
    }

    private RewardedAdLoadCallback createRvLoadListener(final RewardedVideoCallback callback) {
        return new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                super.onRewardedAdLoaded();
                if (callback != null) {
                    callback.onRewardedVideoLoadSuccess();
                }
            }

            @Override
            public void onRewardedAdFailedToLoad(int i) {
                super.onRewardedAdFailedToLoad(i);
                if (callback != null) {
                    callback.onRewardedVideoLoadFailed("onRewardedAdFailedToLoad" + i);
                }
            }
        };
    }

    private RewardedAdCallback createRvCallback(final String adUnitId, final RewardedVideoCallback callback) {
        return new RewardedAdCallback() {
            @Override
            public void onRewardedAdOpened() {
                super.onRewardedAdOpened();
                if (callback != null) {
                    callback.onRewardedVideoAdOpened();
                }
            }

            @Override
            public void onRewardedAdFailedToShow(int i) {
                super.onRewardedAdFailedToShow(i);
                if (callback != null) {
                    callback.onRewardedVideoAdShowFailed("onRewardedAdFailedToShow" + i);
                }
            }

            @Override
            public void onUserEarnedReward(com.google.android.gms.ads.rewarded.RewardItem rewardItem) {
                super.onUserEarnedReward(rewardItem);
                if (callback != null) {
                    callback.onRewardedVideoAdRewarded();
                }
            }

            @Override
            public void onRewardedAdClosed() {
                super.onRewardedAdClosed();
                mRewardedAds.remove(adUnitId);
                if (callback != null) {
                    callback.onRewardedVideoAdClosed();
                }
            }
        };

    }

    /*********************************Interstitial***********************************/
    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        if (Looper.myLooper() != Looper.getMainLooper()) {
            if (callback != null) {
                callback.onInterstitialAdInitFailed("Must be called on the main UI thread. ");
            }
            return;
        }
        initSDK(activity);
        if (mDidInitSdk) {
            if (callback != null) {
                callback.onInterstitialAdInitSuccess();
            }
        }
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, callback);
        if (Looper.myLooper() != Looper.getMainLooper()) {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed("Must be called on the main UI thread. ");
            }
            return;
        }

        if (TextUtils.isEmpty(adUnitId)) {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed("AdMob load RewardedVideo error cause adUnitId is null or empty");
            }
            return;
        }

        InterstitialAd interstitialAd = getInterstitialAd(activity, adUnitId);
        if (interstitialAd.isLoaded()) {
            callback.onInterstitialAdLoadSuccess();
        } else {
            interstitialAd.setAdListener(createInterstitialListener(adUnitId, callback));
            interstitialAd.loadAd(new AdRequest.Builder().build());
        }
    }

    @Override
    public void showInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        if (!isInterstitialAdAvailable(adUnitId)) {
            callback.onInterstitialAdShowFailed("ad not ready");
            return;
        }
        mInterstitialAds.get(adUnitId).show();
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        InterstitialAd interstitialAd = mInterstitialAds.get(adUnitId);
        return interstitialAd != null && interstitialAd.isLoaded();
    }

    private InterstitialAd getInterstitialAd(Activity activity, String adUnitId) {
        InterstitialAd interstitialAd = new InterstitialAd(activity);
        interstitialAd.setAdUnitId(adUnitId);
        mInterstitialAds.put(adUnitId, interstitialAd);
        return interstitialAd;
    }

    private AdListener createInterstitialListener(final String adUnitId, final InterstitialAdCallback callback) {
        return new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                if (callback != null) {
                    callback.onInterstitialAdLoadSuccess();
                }
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                if (callback != null) {
                    callback.onInterstitialAdLoadFailed("AdMob load failed " + i);
                }
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                if (callback != null) {
                    callback.onInterstitialAdClick();
                }
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                if (callback != null) {
                    callback.onInterstitialAdOpened();
                    callback.onInterstitialAdVisible();
                }
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                mInterstitialAds.remove(adUnitId);
                if (callback != null) {
                    callback.onInterstitialAdClosed();
                }
            }
        };
    }
}
