// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt.interactive;

import com.adtiming.mediationsdk.adt.BaseAdListener;

public interface InteractiveListener extends BaseAdListener {
    void onInteractiveAdReady(String placementId);

    void onInteractiveAdClose(String placementId);

    void onInteractiveAdShowed(String placementId);

    void onInteractiveAdFailed(String placementId, String error);

    void onInteractiveAdClicked(String placementId);

    void onInteractiveAdEvent(String placementId, String event);

    void onInteractiveAdShowedFailed(String placementId, String error);
}
