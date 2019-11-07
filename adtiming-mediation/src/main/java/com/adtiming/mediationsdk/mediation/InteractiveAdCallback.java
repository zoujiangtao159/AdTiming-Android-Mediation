// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.mediation;

public interface InteractiveAdCallback {
    void onInteractiveAdInitSuccess();

    void onInteractiveAdInitFailed(String error);

    void onInteractiveAdOpened();

    void onInteractiveAdClosed();

    void onInteractiveAdLoadSuccess();

    void onInteractiveAdLoadFailed(String error);

    void onInteractiveAdShowFailed(String error);

    void onInteractiveAdVisible();

    void onReceivedEvents(String event);

}
