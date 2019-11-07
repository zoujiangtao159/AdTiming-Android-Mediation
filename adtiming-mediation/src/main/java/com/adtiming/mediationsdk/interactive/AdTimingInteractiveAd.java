// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.interactive;

import com.adtiming.mediationsdk.core.AdTimingManager;
import com.adtiming.mediationsdk.core.BaseAdTimingAds;
import com.adtiming.mediationsdk.utils.Constants;
import com.adtiming.mediationsdk.utils.model.Scene;

/**
 * AdTiming InteractiveAd API
 *
 * 
 */
public class AdTimingInteractiveAd extends BaseAdTimingAds {

    /**
     * Returns default placement's availability
     *
     * @return true or false
     */
    public static boolean isReady() {
        return AdTimingManager.getInstance().isInteractiveAdReady("");
    }

    /**
     * Returns specific scene's cap status
     *
     * @return true or false
     */
    public static boolean isSceneCapped(String scene) {
        return isSceneCapped(Constants.INTERACTIVE, scene);
    }

    /**
     * Returns specific scene's info
     *
     * @return {@link Scene}
     */
    public static Scene getSceneInfo(String scene) {
        return getSceneInfo(Constants.INTERACTIVE, scene);
    }

    /**
     * Loads ad for default Placement
     */
    public static void loadAd() {
        AdTimingManager.getInstance().loadInteractiveAd("");
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
     * @param scene optional param ,if null, shows default scene
     */
    public static void showAd(String scene) {
        AdTimingManager.getInstance().showInteractiveAd("", scene);
    }

    /**
     * Set the {@link InteractiveAdListener} to default placement that will receive events from the
     * rewarded video system. Set this to null to stop receiving event callbacks.
     */
    public static void setAdListener(InteractiveAdListener listener) {
        AdTimingManager.getInstance().setInteractiveAdListener("", listener);
    }
}
