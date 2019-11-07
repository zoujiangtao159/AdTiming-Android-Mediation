// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk;

import android.app.Activity;

import com.adtiming.mediationsdk.core.AdTimingManager;

/**
 * 
 */
public abstract class AdTimingAds {

    /**
     * AdTiming SDK init method
     *
     * @param activity required param
     * @param appKey   required param: current app's identifier on AdTiming
     * @param types    optional param: ad types to be preloaded; null means preload all
     */
    public static void init(Activity activity, String appKey, InitCallback callback, AD_TYPE... types) {
        AdTimingManager.getInstance().init(activity, appKey, callback, types);
    }

    /**
     * Sets an activity that is resumed
     *
     * @param activity current resume activity
     */
    public static void onResume(Activity activity) {
        AdTimingManager.getInstance().onResume(activity);
    }

    /**
     * Sets an activity that is on pause
     *
     * @param activity currently paused activity
     */
    public static void onPause(Activity activity) {
        AdTimingManager.getInstance().onPause(activity);
    }

    /**
     * Returns AdTiming SDK init status
     *
     * @return true: init OK; or false: init wrong
     */
    public static boolean isInit() {
        return AdTimingManager.getInstance().isInit();
    }

    /**
     * Sets In-App-Purchase
     *
     * @param iapCount the IAP count
     * @param currency the IAP currency unit
     */
    public static void setIAP(float iapCount, String currency) {
        AdTimingManager.getInstance().setIAP(iapCount, currency);
    }

    /**
     * Returns the SDk version
     */
    public static String getSDKVersion() {
        return AdTimingManager.getInstance().getSDKVersion();
    }

    /**
     * AdTiming supported preloadable Ad types
     */
    public static enum AD_TYPE {
        /*Ad type Rewarded_Video*/
        REWARDED_VIDEO("rewardedVideo"),
        /*Ad type Interstitial*/
        INTERSTITIAL("interstitial"),
        /*Ad type Interactive*/
        INTERACTIVE("interactive");

        private String mValue;

        private AD_TYPE(String value) {
            this.mValue = value;
        }

        @Override
        public String toString() {
            return this.mValue;
        }

    }
}
