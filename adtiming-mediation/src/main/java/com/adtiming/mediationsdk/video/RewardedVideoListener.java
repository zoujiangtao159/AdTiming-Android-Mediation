// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.video;

import com.adtiming.mediationsdk.utils.error.AdTimingError;

/**
 * Listener for rewarded video events. Implementers of this interface will receive events for rewarded video ads
 *
 * 
 */
public interface RewardedVideoListener {

    /**
     * called when rewardedVideo availability changed
     *
     * @param available if RewardedVideoAd is available
     */
    void onRewardedVideoAvailabilityChanged(boolean available);

    /**
     * called when rewardedVideo shows
     */
    void onRewardedVideoAdShowed();

    /**
     * called when rewardedVideo show failed
     *
     * @param error RewardedVideo show error reason
     */
    void onRewardedVideoAdShowFailed(AdTimingError error);

    /**
     * called when rewardedVideo is clicked
     */
    void onRewardedVideoAdClicked();

    /**
     * called when rewardedVideo is closed
     */
    void onRewardedVideoAdClosed();

    /**
     * called when rewardedVideo starts to play
     */
    void onRewardedVideoAdStarted();

    /**
     * called when rewardedVideo play ends
     */
    void onRewardedVideoAdEnded();

    /**
     * called when rewardedVideo can be rewarded
     */
    void onRewardedVideoAdRewarded();
}
