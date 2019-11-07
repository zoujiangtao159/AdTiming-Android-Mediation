// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.banner;

import android.app.Activity;

import com.adtiming.mediationsdk.core.imp.banner.BannerImp;

/**
 * <p>
 * In general you should get instances of {@link BannerAd}
 * <p>
 * When you have a {@link BannerAd} instance and wish to show a view you should:
 * 1. Call {@link #loadAd()} to prepare the ad.
 * 2. When the ad is no longer shown to the user, call {@link #destroy()}. You can later
 * call {@link #loadAd()} again if the ad will be shown.
 *
 * 
 */
public class BannerAd {

    private BannerImp mBanner;

    /**
     * Instantiates the BannerAd
     *
     * @param activity    Must be a non-null, effective Activity.
     * @param placementId Current placement id
     * @param adListener  A lifecycle listener to receive native ad events
     */
    public BannerAd(Activity activity, String placementId, BannerAdListener adListener) {
        this.mBanner = new BannerImp(activity, placementId, adListener);
    }

    public void loadAd() {
        if (mBanner != null) {
            mBanner.loadAd();
        }
    }

    /**
     * Cleans up all {@link BannerAd} state. Call this method when the {@link BannerAd} will never be shown to a
     * user again.
     */
    public void destroy() {
        if (mBanner != null) {
            mBanner.destroy();
        }
    }
}
