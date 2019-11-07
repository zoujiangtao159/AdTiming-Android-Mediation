// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.mediation;

import android.app.Activity;
import android.os.Build;
import android.text.TextUtils;

import java.util.Map;

/**
 * CustomAdsAdapter is a base class for custom events that support RewardedVideo,Interstitial. By
 * implementing subclasses of CustomAdsAdapter, you can enable the AdTiming SDK to natively
 * support a wider variety of third-party ad networks, or execute any of your application code on
 * demand.
 * <p>
 * At runtime, the AdTiming SDK will find and instantiate a CustomAdsAdapter subclass as needed
 * and invoke its methods.
 */
public abstract class CustomAdsAdapter implements RewardedVideoApi, InterstitialAdApi, InteractiveAdApi
        , BannerAdApi {

    protected String mAppKey;

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap
            , RewardedVideoCallback callback) {
        initData(activity, dataMap);
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        initData(activity, dataMap);
    }

    @Override
    public void initInteractiveAd(Activity activity, Map<String, Object> dataMap, InteractiveAdCallback callback) {
        initData(activity, dataMap);
    }

    @Override
    public void initBannerAd(Activity activity, Map<String, Object> dataMap, InteractiveAdCallback callback) {
        initData(activity, dataMap);
    }

    public void onResume(Activity activity) {

    }

    public void onPause(Activity activity) {

    }

    /**
     * Gets current third-part ad network's version
     *
     * @return third-part ad network's version
     */
    public abstract String getMediationVersion();

    /**
     * Gets current adapter's version
     *
     * @return adapter's version
     */
    public abstract String getAdapterVersion();

    /**
     * Get current third-part ad network's id with AdTiming
     *
     * @return third-part ad network's id with AdTiming
     */
    public abstract int getAdNetworkId();


    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {

    }

    @Override
    public void showRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {

    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        return false;
    }


    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {

    }

    @Override
    public void showInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {

    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        return false;
    }

    @Override
    public void loadInteractiveAd(Activity activity, String adUnitId, InteractiveAdCallback callback) {

    }

    @Override
    public void showInteractiveAd(Activity activity, String adUnitId, InteractiveAdCallback callback) {

    }

    @Override
    public boolean isInteractiveAdAvailable(String adUnitId) {
        return false;
    }

    @Override
    public void loadBannerAd(Activity activity, String adUnitId, InteractiveAdCallback callback) {

    }

    @Override
    public void destroyBannerAd() {

    }

    protected String check(Activity activity, String adUnitId) {
        if (activity == null) {
            return "activity is null";
        }
        if (isDestroyed(activity)) {
            return "activity is destroied";
        }
        if (mAppKey == null) {
            return "app key is null";
        }
        if (TextUtils.isEmpty(adUnitId)) {
            return "instanceKey is null";
        }
        return "";
    }

    protected String check(Activity activity) {
        if (activity == null) {
            return "activity is null";
        }
        if (isDestroyed(activity)) {
            return "activity is destroied";
        }
        if (mAppKey == null) {
            return "app key is null";
        }
        return "";
    }

    private void initData(Activity activity, Map<String, Object> dataMap) {
        if (!TextUtils.isEmpty(mAppKey)) {
            return;
        }
        if (activity == null) {
            throw new IllegalArgumentException("Activity Destroy");
        }
        if (!dataMap.containsKey("AppKey") || TextUtils.isEmpty((CharSequence) dataMap.get("AppKey"))) {
            throw new IllegalArgumentException("AppKey is empty");
        }
        mAppKey = (String) dataMap.get("AppKey");
    }

    /**
     * Checks if an Activity is available
     *
     * @param activity the given activity
     * @return activity availability
     */
    private boolean isDestroyed(Activity activity) {
        boolean flage = false;
        if (Build.VERSION.SDK_INT >= 17) {
            if (activity == null || activity.isDestroyed()) {
                flage = true;
            }
        } else {
            if (activity == null || activity.isFinishing()) {
                flage = true;
            }
        }
        return flage;
    }

}
