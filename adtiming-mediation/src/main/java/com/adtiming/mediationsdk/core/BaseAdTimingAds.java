// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.core;

import com.adtiming.mediationsdk.utils.AdRateUtil;
import com.adtiming.mediationsdk.utils.PlacementUtils;
import com.adtiming.mediationsdk.utils.SceneUtil;
import com.adtiming.mediationsdk.utils.event.EventId;
import com.adtiming.mediationsdk.utils.event.EventUploadManager;
import com.adtiming.mediationsdk.utils.model.Placement;
import com.adtiming.mediationsdk.utils.model.Scene;

/**
 * 
 */
public abstract class BaseAdTimingAds {
    /**
     * Returns whether this scene is capped
     *
     * @param scene scene name
     * @return true or false
     */
    public static boolean isSceneCapped(String scene) {
        return false;
    }

    protected static boolean isSceneCapped(int adType, String sceneName) {
        Placement placement = PlacementUtils.getPlacement(adType);
        Scene scene = SceneUtil.getScene(placement, sceneName);
        boolean isCapped = AdRateUtil.shouldBlockScene(placement != null ? placement.getId() : "", scene);
        if (isCapped) {
            EventUploadManager.getInstance().uploadEvent(EventId.CALLED_IS_CAPPED_TRUE);
        } else {
            EventUploadManager.getInstance().uploadEvent(EventId.CALLED_IS_CAPPED_FALSE);
        }
        return isCapped;
    }

    /**
     * Returns SceneInfo with given scene
     *
     * @param scene scene name
     */
    public static Scene getSceneInfo(String scene) {
        return null;
    }

    protected static Scene getSceneInfo(int adType, String scene) {
        Placement placement = PlacementUtils.getPlacement(adType);
        return SceneUtil.getScene(placement, scene);
    }
}
