// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.video;

import com.adtiming.mediationsdk.core.AdTimingManager;
import com.adtiming.mediationsdk.core.BaseAdTimingAds;
import com.adtiming.mediationsdk.utils.Constants;
import com.adtiming.mediationsdk.utils.model.Scene;

/**
 * AdTiming RewardedVideo ads API
 *
 * 
 */
public final class AdTimingRewardedVideo extends BaseAdTimingAds {

    /**
     * Returns default placement availability
     *
     * @return true or false
     */
    public static boolean isReady() {
        return AdTimingManager.getInstance().isRewardedVideoReady("");
    }

    /**
     * Returns specific scene cap status
     *
     * @return true or false
     */
    public static boolean isSceneCapped(String scene) {
        return isSceneCapped(Constants.VIDEO, scene);
    }

    /**
     * Returns specific scene's info
     *
     * @return {@link Scene}
     */
    public static Scene getSceneInfo(String scene) {
        return getSceneInfo(Constants.VIDEO, scene);
    }

    /**
     * Loads ads with default placement
     */
    public static void loadAd() {
        AdTimingManager.getInstance().loadRewardedVideo("");
    }

    /**
     * show ads with default placement and default scene
     */
    public static void showAd() {
        showAd("");
    }

    /**
     * shows ads with default placement and specific scene
     *
     * @param scene optional param ,if null, shows default scene
     */
    public static void showAd(String scene) {
        AdTimingManager.getInstance().showRewardedVideo("", scene);
    }

    /**
     * sets up a custom id to receive rewarded callback through S2S
     *
     * @param scene display scene, can be null
     * @param extId custom id
     */
    public static void setExtId(String scene, String extId) {
        AdTimingManager.getInstance().setRewardedExtId("", scene, extId);
    }

    /**
     * Set the {@link RewardedVideoListener} to default placement that will receive events from the
     * rewarded video system. Set this to null to stop receiving event callbacks.
     */
    public static void setAdListener(RewardedVideoListener listener) {
        AdTimingManager.getInstance().setRewardedVideoListener("", listener);
    }
}
