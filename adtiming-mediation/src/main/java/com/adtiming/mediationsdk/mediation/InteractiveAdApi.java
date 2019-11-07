// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.mediation;

import android.app.Activity;

import java.util.Map;

public interface InteractiveAdApi {

    void initInteractiveAd(Activity activity, Map<String, Object> dataMap, InteractiveAdCallback callback);

    void loadInteractiveAd(Activity activity, String adUnitId, InteractiveAdCallback callback);

    void showInteractiveAd(Activity activity, String adUnitId, InteractiveAdCallback callback);

    boolean isInteractiveAdAvailable(String adUnitId);
}
