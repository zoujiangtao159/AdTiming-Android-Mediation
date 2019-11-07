// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt.banner;

import android.content.Context;
import android.widget.RelativeLayout;

public class AdtBannerAd extends RelativeLayout {

    private BannerImp mBannerImp;

    public AdtBannerAd(Context context, String placementId) {
        super(context);
        mBannerImp = new BannerImp(context, placementId, this);
    }

    public void setListener(BannerListener listener) {
        mBannerImp.setListener(listener);
    }

    public void load() {
        mBannerImp.load();
    }

    public void destroy() {
        mBannerImp.destroy();
    }
}
