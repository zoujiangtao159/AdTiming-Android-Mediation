// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.core.imp.interactivead;

import com.adtiming.mediationsdk.utils.error.AdTimingError;

public interface IaManagerListener {

    void onInteractiveAdInitSuccess(IaInstance iaInstance);

    void onInteractiveAdInitFailed(String error, IaInstance iaInstance);

    void onInteractiveAdShowFailed(AdTimingError error, IaInstance iaInstance);

    void onInteractiveAdOpened(IaInstance iaInstance);

    void onInteractiveAdClosed(IaInstance iaInstance);

    void onInteractiveAdVisible(IaInstance iaInstance);

    void onInteractiveAdLoadSuccess(IaInstance iaInstance);

    void onInteractiveAdLoadFailed(AdTimingError error, IaInstance iaInstance);

    void onInteractiveAdCaped(IaInstance iaInstance);

    void onReceivedEvents(String event, IaInstance iaInstance);
}
