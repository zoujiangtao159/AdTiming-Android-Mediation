// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.interstitial;

import com.adtiming.mediationsdk.utils.error.AdTimingError;

/**
 * Listener for interstitial events. Implementers of this interface will receive events for interstitial ads
 *
 * 
 */
public interface InterstitialAdListener {

    /**
     * called when interstitialAd availability changed
     *
     * @param available represent interstitialAd available status
     */
    void onInterstitialAdAvailabilityChanged(boolean available);

    /**
     * called when interstitialAd is shown
     */
    void onInterstitialAdShowed();

    /**
     * called when interstitialAd show failed
     *
     * @param error Interstitial ads show failed reason
     */
    void onInterstitialAdShowFailed(AdTimingError error);

    /**
     * called when interstitialAd closes
     */
    void onInterstitialAdClosed();

    /**
     * called when interstitialAd is clicked
     */
    void onInterstitialAdClicked();

}
