// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.mediation;

import android.app.Activity;

import java.util.Map;

/**
 * This interface is the base API for interstitialAds
 *
 * 
 */
public interface InterstitialAdApi {

    /**
     * Third-party ad networks can be inited in this method
     *
     * @param activity must be an activity
     * @param dataMap  some configs for third-party ad networks on AdTiming dashboard
     * @param callback {@link InterstitialAdCallback}
     */
    void initInterstitialAd(Activity activity, Map<String, Object> dataMap, InterstitialAdCallback callback);

    /**
     * Calls third-party ad networks to get ads
     *
     * @param activity must be an activity
     * @param adUnitId ad unit id on third-party ad networks
     * @param callback {@link InterstitialAdCallback}
     */
    void loadInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback);

    /**
     * Calls third-party ad networks to show ads
     *
     * @param activity must be an activity
     * @param adUnitId ad unit id on third-party ad networks
     * @param callback {@link InterstitialAdCallback}
     */
    void showInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback);

    /**
     * Checks if third-party ad networks have available ads
     *
     * @param adUnitId ad unit id on third-party ad networks
     * @return third-party ad networks have available ads or not
     */
    boolean isInterstitialAdAvailable(String adUnitId);
}
