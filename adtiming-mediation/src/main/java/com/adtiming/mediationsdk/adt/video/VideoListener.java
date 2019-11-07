// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt.video;

import com.adtiming.mediationsdk.adt.BaseAdListener;

public interface VideoListener extends BaseAdListener {
    void onVideoAdReady(String placementId);

    void onVideoAdClose(String placementId, boolean isFullyWatched);

    void onVideoAdShowed(String placementId);

    void onVideoAdRewarded(String placementId);

    void onVideoAdFailed(String placementId, String error);

    void onVideoAdClicked(String placementId);

    void onVideoAdEvent(String placementId, String event);

    void onVideoAdShowFailed(String placementId, String error);
}
