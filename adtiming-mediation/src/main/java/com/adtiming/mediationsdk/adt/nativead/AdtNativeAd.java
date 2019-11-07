// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt.nativead;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.adtiming.mediationsdk.adt.BaseAd;
import com.adtiming.mediationsdk.adt.BaseAdListener;

public final class AdtNativeAd extends BaseAd {

    private NativeAdImp mNativeAd;

    public AdtNativeAd(Context context, String placementId) {
        mNativeAd = new NativeAdImp(context, placementId);
    }

    @Override
    public void loadAd() {
        mNativeAd.load();
    }

    @Override
    public void setAdListener(BaseAdListener listener) {
        mNativeAd.setListener(listener);
    }

    public void registerActionView(View view) {
        mNativeAd.registerActionView(view);
    }

    public void setUpAdLogo(ViewGroup parent) {
        mNativeAd.setUpLogo(parent);
    }

    @Override
    public void destroy() {
        mNativeAd.destroy();
    }
}
