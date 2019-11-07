// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.core.imp.rewardedvideo;

import com.adtiming.mediationsdk.utils.error.AdTimingError;

public interface RvManagerListener {
    void onRewardedVideoInitSuccess(RvInstance rvInstance);

    void onRewardedVideoInitFailed(String error, RvInstance rvInstance);

    void onRewardedVideoAdShowFailed(AdTimingError error, RvInstance rvInstance);

    void onRewardedVideoAdOpened(RvInstance rvInstance);

    void onRewardedVideoAdClosed(RvInstance rvInstance);

    void onRewardedVideoLoadSuccess(RvInstance rvInstance);

    void onRewardedVideoLoadFailed(String error, RvInstance rvInstance);

    void onRewardedVideoAdStarted(RvInstance rvInstance);

    void onRewardedVideoAdEnded(RvInstance rvInstance);

    void onRewardedVideoAdRewarded(RvInstance rvInstance);

    void onRewardedVideoAdClicked(RvInstance rvInstance);

    void onRewardedVideoAdVisible(RvInstance rvInstance);

    void onRewardedVideoAdCaped(RvInstance rvInstance);

    void onReceivedEvents(String event, RvInstance rvInstance);
}
