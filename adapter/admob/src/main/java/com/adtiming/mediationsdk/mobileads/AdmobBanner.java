// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.mobileads;

import android.app.Activity;

import com.adtiming.mediationsdk.mediation.CustomBannerEvent;
import com.adtiming.mediationsdk.utils.AdLog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.Map;

public class AdmobBanner extends CustomBannerEvent {

    private AdView mBannerView;

    @Override
    public void loadAd(Activity activity, Map<String, String> config) throws Throwable {
        super.loadAd(activity, config);
        if (!check(activity, config)) {
            return;
        }
        if (mBannerView != null) {
            mBannerView.loadAd(new AdRequest.Builder().build());
            return;
        }
        mBannerView = new AdView(activity.getApplicationContext());
        mBannerView.setAdUnitId(mInstancesKey);
        mBannerView.setAdSize(new AdSize(320, 50));
        mBannerView.setAdListener(new AdListener() {

            @Override
            public void onAdLoaded() {
                if (!isDestroyed) {
                    onInsReady(mBannerView);
                }
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                if (!isDestroyed) {
                    onInsClicked();
                }
            }

            @Override
            public void onAdFailedToLoad(int i) {
                AdLog.getSingleton().LogE("Adt-AdMob: AdMob banner failed to load, error code is : " + i);
                if (!isDestroyed) {
                    onInsError("Admob onAdFailedToLoad:" + i);
                }
            }
        });
        mBannerView.loadAd(new AdRequest.Builder().build());
    }

    @Override
    public int getMediation() {
        return 1;
    }

    @Override
    public void destroy(Activity activity) {
        if (mBannerView != null) {
            mBannerView.destroy();
            mBannerView = null;
        }
        isDestroyed = true;
    }
}
