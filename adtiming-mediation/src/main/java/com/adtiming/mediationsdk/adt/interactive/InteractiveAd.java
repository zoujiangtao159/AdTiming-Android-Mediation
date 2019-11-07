// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt.interactive;

import android.content.Context;

import com.adtiming.mediationsdk.adt.BaseAd;
import com.adtiming.mediationsdk.adt.BaseAdListener;

public final class InteractiveAd extends BaseAd {

    private InteractiveAdImp mInteractiveAd;

    public InteractiveAd(Context context, String placementId) {
        mInteractiveAd = new InteractiveAdImp(context, placementId);
    }

    @Override
    public void loadAd() {
        mInteractiveAd.load();
    }

    @Override
    public void setAdListener(BaseAdListener listener) {
        mInteractiveAd.setListener(listener);
    }

    @Override
    public boolean isReady() {
        return mInteractiveAd.isReady();
    }

    @Override
    public void show() {
        mInteractiveAd.show();
    }

    @Override
    public void destroy() {
        mInteractiveAd.destroy();
    }
}
