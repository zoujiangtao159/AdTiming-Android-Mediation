// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt.nativead;

import com.adtiming.mediationsdk.adt.BaseAdListener;

public interface NativeListener extends BaseAdListener {

    void onNativeAdReady(String placementId, Ad ad);

    void onNativeAdFailed(String placementId, String error);

    void onNativeAdClicked(String placementId);

    void onNativeAdShowFailed(String placementId, String error);
}
