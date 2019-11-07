// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt.interstitial;

import android.content.Context;

import com.adtiming.mediationsdk.adt.BaseAd;
import com.adtiming.mediationsdk.adt.BaseAdListener;

public final class InterstitialAd extends BaseAd {

    private InterstitialAdImp mInterAd;

    public InterstitialAd(Context context, String placementId) {
        mInterAd = new InterstitialAdImp(context, placementId);
    }


    @Override
    public void loadAd() {
        mInterAd.load();
    }

    @Override
    public void setAdListener(BaseAdListener listener) {
        mInterAd.setListener(listener);
    }

    @Override
    public boolean isReady() {
        return mInterAd.isReady();
    }

    @Override
    public void show() {
        mInterAd.show();
    }

    @Override
    public void destroy() {
        mInterAd.destroy();
    }
}
