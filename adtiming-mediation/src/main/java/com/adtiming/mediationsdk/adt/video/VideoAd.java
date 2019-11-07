// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt.video;

import android.content.Context;

import com.adtiming.mediationsdk.adt.BaseAd;
import com.adtiming.mediationsdk.adt.BaseAdListener;

public final class VideoAd extends BaseAd {

    private VideoAdImp mVideoAd;

    public VideoAd(Context context, String placementId) {
        mVideoAd = new VideoAdImp(context, placementId);
    }

    @Override
    public void loadAd() {
        mVideoAd.load();
    }

    @Override
    public void setAdListener(BaseAdListener listener) {
        mVideoAd.setListener(listener);
    }

    @Override
    public boolean isReady() {
        return mVideoAd.isReady();
    }

    @Override
    public void show() {
        mVideoAd.show();
    }

    @Override
    public void destroy() {
        mVideoAd.destroy();
    }
}
