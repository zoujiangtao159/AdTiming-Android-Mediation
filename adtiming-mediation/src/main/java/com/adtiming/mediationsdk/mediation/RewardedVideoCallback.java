// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.mediation;

/**
 * This interface is used to notify adapter's lifecycle to AdTiming's sdk
 *
 * 
 */
public interface RewardedVideoCallback {
    /**
     * called when third-party ad network ads init success
     */
    void onRewardedVideoInitSuccess();

    /**
     * called when third-party ad network ads init failed
     *
     * @param error init failed reason
     */
    void onRewardedVideoInitFailed(String error);

    /**
     * called when third-party ad network shows an ads
     */
    void onRewardedVideoAdOpened();

    /**
     * called when third-party ad networkads  closed
     */
    void onRewardedVideoAdClosed();

    /**
     * called when third-party ad network ads load success
     */
    void onRewardedVideoLoadSuccess();

    /**
     * called when third-party ad network load failed
     *
     * @param error load failed reason
     */
    void onRewardedVideoLoadFailed(String error);

    /**
     * called when third-party ad network ads start to play video
     */
    void onRewardedVideoAdStarted();

    /**
     * called when third-party ad network ads play completed
     */
    void onRewardedVideoAdEnded();

    /**
     * called when third-party ad network ads rewarded
     */
    void onRewardedVideoAdRewarded();

    /**
     * called when third-party ad network show failed
     *
     * @param error show failed reason
     */
    void onRewardedVideoAdShowFailed(String error);

    /**
     * called when third-party ad network ads are clicked
     */
    void onRewardedVideoAdClicked();

    /**
     * called when third-party ad network ads become visible to user
     */
    void onRewardedVideoAdVisible();

    /**
     * called when third-party ad network has extra events
     * Optional. can be empty implementation
     *
     * @param event extra events
     */
    void onReceivedEvents(String event);
}
