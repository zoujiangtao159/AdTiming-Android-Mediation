// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils;

import android.text.TextUtils;
import android.util.SparseArray;

import com.adtiming.mediationsdk.utils.model.BaseInstance;
import com.adtiming.mediationsdk.utils.model.Scene;
import com.adtiming.mediationsdk.utils.cache.DataCache;

/**
 * Ads impression rate control
 *
 * 
 */
public class AdRateUtil {
    private static String RATE = "Rate";
    private static String CAP = "CAP";
    private static String CAP_TIME = "CAPTime";

    /**
     * saves placement's and instance's impression time and count
     */
    public static void onInstancesShowed(String placementId, String instancesKey) {
        AdRateUtil.saveShowTime(placementId + instancesKey);
        AdRateUtil.addCAP(placementId + instancesKey);
        //
        PlacementUtils.savePlacementImprCount(placementId);
    }

    public static void onSceneShowed(String placementId, Scene scene) {
        if (scene != null) {
            AdRateUtil.saveShowTime(placementId + scene.getN());
            AdRateUtil.addCAP(placementId + scene.getN());
        }
    }

    /**
     * 
     *
     * @param key 
     */
    private static void saveShowTime(String key) {
        DataCache.getInstance().set(RATE + key, System.currentTimeMillis());
    }

    public static boolean shouldBlockPlacement(String key, int interval) {
        if (TextUtils.isEmpty(key) || interval == 0) {
            return false;
        }
        return AdRateUtil.isInterval(key, interval);
    }

    public static boolean shouldBlockInstance(String key, BaseInstance instance) {
        if (TextUtils.isEmpty(key)) {
            return false;
        }
        return AdRateUtil.isInterval(key, instance.getFrequencyInterval())
                || AdRateUtil.isCAP(key, instance.getFrequencyUnit(), instance.getFrequencyCap());
    }

    public static boolean shouldBlockScene(String placementId, Scene scene) {
        return scene != null && AdRateUtil.isCAP(placementId + scene.getN(), scene.getFrequencyUnit(),
                scene.getFrequencyCap());
    }

    static boolean shouldBlockAdtInstance(SparseArray<BaseInstance> insMap) {
        try {
            if (insMap == null || insMap.size() <= 0) {
                return false;
            }
            //
            BaseInstance adtIns = null;
            int size = insMap.size();
            for (int i = 0; i < size; i++) {
                BaseInstance instance = insMap.valueAt(i);
                //filters adt Instances
                if (instance != null && instance.getMediationId() == 0) {
                    adtIns = instance;
                    break;
                }
            }

            //no adt instance for the placement
            if (adtIns == null) {
                return true;
            }

            return AdRateUtil.shouldBlockInstance(adtIns.getPlacementId() + adtIns.getKey(), adtIns);

        } catch (Throwable e) {
            DeveloperLog.LogD("PlacementUtils-checkAdtInstanceCap", e);
            return false;
        }
    }

    /**
     * is Interval long enough
     *
     * @param key
     * @param rate
     * @return
     */
    private static boolean isInterval(String key, long rate) {
        Long beforeTime = DataCache.getInstance().get(RATE + key, long.class);
        if (beforeTime == null) {
            return false;
        }
        DeveloperLog.LogD("Interval:" + key + ":" + (System.currentTimeMillis() - beforeTime) + ":" + rate);
        return System.currentTimeMillis() - beforeTime < rate;
    }

    /**
     * saves current Placement/Mediation impression count and 1st impression time
     *
     * @param key
     */
    private static void addCAP(String key) {
        Integer cap = DataCache.getInstance().get(CAP + key, int.class);
        if (cap == null) {
            cap = 0;
        }
        DeveloperLog.LogD("AddCAP:" + key + ":" + cap + 1);
        DataCache.getInstance().set(CAP + key, cap + 1);
        Long capTime = DataCache.getInstance().get(CAP_TIME + key, long.class);
        if (capTime == null) {
            DataCache.getInstance().set(CAP_TIME + key, System.currentTimeMillis());
        }
    }

    /**
     * checks how many impressions exist between current Placement's 1st impression time and now
     *
     * @param key
     * @param time  hours
     * @param count
     * @return
     */
    private static boolean isCAP(String key, int time, int count) {
        if (count <= 0) {
            return false;
        }
        Long capTime = DataCache.getInstance().get(CAP_TIME + key, long.class);
        if (capTime == null) {
            return false;
        }
        Integer cap = DataCache.getInstance().get(CAP + key, int.class);
        DeveloperLog.LogD("CapTime:" + key + ":" + (System.currentTimeMillis() - capTime) +
                ":" + time + ":Cap:" + cap + ":" + count);
        if (System.currentTimeMillis() - capTime < time) {
            return cap >= count;
        } else {
            DataCache.getInstance().delete(CAP_TIME + key);
            DataCache.getInstance().delete(CAP + key);
            return false;
        }
    }
}
