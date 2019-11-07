// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.mediation;

import com.adtiming.mediationsdk.nativead.AdInfo;
import com.adtiming.mediationsdk.nativead.NativeAdView;

public abstract class CustomNativeEvent extends CustomAdEvent {
    protected AdInfo mAdInfo = new AdInfo();

    public abstract void registerNativeView(NativeAdView adView);
}
