// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.mobileads;

import android.app.Activity;
import android.view.View;

import com.adtiming.mediationsdk.mediation.CustomBannerEvent;
import com.adtiming.mediationsdk.adt.banner.AdtBannerAd;
import com.adtiming.mediationsdk.adt.banner.BannerListener;

import java.util.Map;

public class AdtimingBanner extends CustomBannerEvent implements BannerListener {

    private AdtBannerAd mBannerAd;

    @Override
    public void loadAd(Activity activity, Map<String, String> config) throws Throwable {
        super.loadAd(activity, config);
        if (!check(activity, config)) {
            return;
        }
        if (mBannerAd != null) {
            mBannerAd.load();
            return;
        }

        mBannerAd = new AdtBannerAd(activity, mInstancesKey);
        mBannerAd.setListener(this);
        mBannerAd.load();
    }

    @Override
    public int getMediation() {
        return 0;
    }

    @Override
    public void destroy(Activity activity) {
        if (mBannerAd != null) {
            mBannerAd.destroy();
            mBannerAd = null;
        }
        isDestroyed = true;
    }

    @Override
    public void onBannerAdReady(String placementId,View view) {
        if (!isDestroyed) {
            onInsReady(view);
        }
    }

    @Override
    public void onBannerAdFailed(String placementId,String error) {
        if (!isDestroyed) {
            onInsError(error);
        }
    }

    @Override
    public void onBannerAdClicked(String placementId) {
        onInsClicked();
    }

    @Override
    public void onBannerAdShowFailed(String placementId, String error) {

    }
}
