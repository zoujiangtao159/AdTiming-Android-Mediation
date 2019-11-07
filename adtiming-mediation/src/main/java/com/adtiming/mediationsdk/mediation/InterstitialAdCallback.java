// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.mediation;

/**
 * This interface is used to notify adapter's lifecycle to AdTiming's SDK
 *
 * 
 */
public interface InterstitialAdCallback {
    /**
     * called t third-party ad network ads init success
     */
    void onInterstitialAdInitSuccess();

    /**
     * called when third-party ad networks ads init failed
     *
     * @param error init failed reason
     */
    void onInterstitialAdInitFailed(String error);

    /**
     * called when third-party ad networks show ads
     */
    void onInterstitialAdOpened();

    /**
     * called when third-party ad networks ads closed
     */
    void onInterstitialAdClosed();

    /**
     * called at third-party ad networks ads load success
     */
    void onInterstitialAdLoadSuccess();

    /**
     * called when third-party ad networks ads load failed
     *
     * @param error load failure reason
     */
    void onInterstitialAdLoadFailed(String error);

    /**
     * called when third-party ad networks ads show failed
     *
     * @param error show failure reason
     */
    void onInterstitialAdShowFailed(String error);

    /**
     * called when third-party ad networks ads become visible to user
     */
    void onInterstitialAdVisible();

    /**
     * called when third-party ad networks ads are clicked
     */
    void onInterstitialAdClick();

    /**
     * called when third-party ad networks have extra events
     * Optional. can be empty implementation
     *
     * @param event extra events
     */
    void onReceivedEvents(String event);
}
