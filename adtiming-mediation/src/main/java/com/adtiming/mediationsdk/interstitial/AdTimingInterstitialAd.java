// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.interstitial;

import com.adtiming.mediationsdk.core.AdTimingManager;
import com.adtiming.mediationsdk.core.BaseAdTimingAds;
import com.adtiming.mediationsdk.utils.Constants;
import com.adtiming.mediationsdk.utils.model.Scene;

/**
 * AdTiming InterstitialAd API
 *
 * 
 */
public class AdTimingInterstitialAd extends BaseAdTimingAds {

    /**
     * Returns default placement availability
     *
     * @return true or false
     */
    public static boolean isReady() {
        return AdTimingManager.getInstance().isInterstitialAdReady("");
    }

    /**
     * Returns specific scene's cap status
     *
     * @return true or false
     */
    public static boolean isSceneCapped(String scene) {
        return isSceneCapped(Constants.INTERSTITIAL, scene);
    }

    /**
     * Returns specific scene's info
     *
     * @return {@link Scene}
     */
    public static Scene getSceneInfo(String scene) {
        return getSceneInfo(Constants.INTERSTITIAL, scene);
    }

    /**
     * 
     */
    public static void loadAd() {
        AdTimingManager.getInstance().loadInterstitialAd("");
    }

    /**
     * shows ad with default placement and default scene
     */
    public static void showAd() {
        showAd("");
    }

    /**
     * shows ad with default placement and specific scene
     *
     * @param scene optional param ,if null, show default scene
     */
    public static void showAd(String scene) {
        AdTimingManager.getInstance().showInterstitialAd("", scene);
    }

    /**
     * Set the {@link InterstitialAdListener} to default placement that will receive events from the
     * rewarded video system. Set this to null to stop receiving event callbacks.
     */
    public static void setAdListener(InterstitialAdListener listener) {
        AdTimingManager.getInstance().setInterstitialAdListener("", listener);
    }
}
