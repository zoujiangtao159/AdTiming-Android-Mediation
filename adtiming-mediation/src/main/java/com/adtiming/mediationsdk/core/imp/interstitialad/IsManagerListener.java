// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.core.imp.interstitialad;

import com.adtiming.mediationsdk.utils.error.AdTimingError;

public interface IsManagerListener {

    void onInterstitialAdInitSuccess(IsInstance isInstance);

    void onInterstitialAdInitFailed(String error, IsInstance isInstance);

    void onInterstitialAdShowFailed(AdTimingError error, IsInstance isInstance);

    void onInterstitialAdOpened(IsInstance isInstance);

    void onInterstitialAdClick(IsInstance isInstance);

    void onInterstitialAdClosed(IsInstance isInstance);

    void onInterstitialAdVisible(IsInstance isInstance);

    void onInterstitialAdLoadSuccess(IsInstance isInstance);

    void onInterstitialAdLoadFailed(AdTimingError error, IsInstance isInstance);

    void onInterstitialAdCaped(IsInstance isInstance);

    void onReceivedEvents(String event, IsInstance isInstance);
}
