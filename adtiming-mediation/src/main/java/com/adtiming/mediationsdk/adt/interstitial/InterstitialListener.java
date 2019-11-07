// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt.interstitial;

import com.adtiming.mediationsdk.adt.BaseAdListener;

public interface InterstitialListener extends BaseAdListener {
    void onInterstitialAdReady(String placementId);

    void onInterstitialAdClose(String placementId);

    void onInterstitialAdShowed(String placementId);

    void onInterstitialAdFailed(String placementId, String error);

    void onInterstitialAdClicked(String placementId);

    void onInterstitialAdEvent(String placementId, String event);

    void onInterstitialAdShowFailed(String placementId, String error);
}
