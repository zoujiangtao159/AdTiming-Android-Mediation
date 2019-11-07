// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.nativead;

import android.app.Activity;

import com.adtiming.mediationsdk.core.imp.nativead.NativeImp;

/**
 * <p>
 * In general you should get instances of {@link NativeAd}
 * <p>
 * When you have a {@link NativeAd} instance and wish to show a view you should:
 * 1. Call {@link #loadAd()} to prepare ad.
 * 2. Call {@link #registerNativeAdView(NativeAdView)} with a compatible {@link NativeAdView} to render the ad data into the view.
 * 3. When the ad view is no longer shown to the user, call {@link #destroy()}. You can later
 * call {@link #loadAd()} again if the ad will be shown to users.
 *
 * 
 */
public class NativeAd {

    private NativeImp mNative;

    /**
     * Instantiates NativeAd
     *
     * @param activity    
     * @param placementId 
     * @param adListener  
     */
    public NativeAd(Activity activity, String placementId, NativeAdListener adListener) {
        mNative = new NativeImp(activity, placementId, adListener);
    }

    public void loadAd() {
        mNative.loadAd();
    }

    /**
     * Registers a {@link NativeAdView} by filling it with ad data.
     *
     * @param adView The ad {@link NativeAdView}
     */
    public void registerNativeAdView(NativeAdView adView) {
        mNative.registerView(adView);
    }

    /**
     * Cleans up all {@link NativeAd} state. Call this method when the {@link NativeAd} will never be shown to a
     * user again.
     */
    public void destroy() {
        mNative.destroy();
    }
}
