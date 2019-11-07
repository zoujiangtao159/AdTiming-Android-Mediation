// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt.banner;

import android.view.View;

import com.adtiming.mediationsdk.adt.BaseAdListener;

public interface BannerListener extends BaseAdListener {
    void onBannerAdReady(String placementId, View view);

    void onBannerAdFailed(String placementId, String error);

    void onBannerAdClicked(String placementId);

    void onBannerAdShowFailed(String placementId, String error);
}
