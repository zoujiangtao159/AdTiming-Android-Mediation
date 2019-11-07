// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.mediation;

import android.app.Activity;

import java.util.Map;

public interface BannerAdApi {
    void initBannerAd(Activity activity, Map<String, Object> dataMap, InteractiveAdCallback callback);

    void loadBannerAd(Activity activity, String adUnitId, InteractiveAdCallback callback);

    void destroyBannerAd();
}
