// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.mobileads;

import android.app.Activity;

import com.adtiming.mediationsdk.mediation.CustomBannerEvent;
import com.adtiming.mediationsdk.utils.AdLog;
import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubView;

import java.util.Map;

public class MopubBanner extends CustomBannerEvent implements MoPubView.BannerAdListener {

    private MoPubView adView;

    @Override
    public void loadAd(final Activity activity, Map<String, String> config) throws Throwable {
        super.loadAd(activity, config);
        if (!check(activity, config)) {
            return;
        }
        if (!MoPub.isSdkInitialized()) {
            SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(mInstancesKey).build();
            MoPub.initializeSdk(activity, sdkConfiguration, new SdkInitializationListener() {
                @Override
                public void onInitializationFinished() {
                    loadBannerAd(activity);
                }
            });
        } else {
            loadBannerAd(activity);
        }
    }

    @Override
    public int getMediation() {
        return 8;
    }

    @Override
    public void destroy(Activity activity) {
        if (adView != null) {
            adView.destroy();
            adView = null;
        }
        isDestroyed = true;
    }

    @Override
    public void onBannerLoaded(MoPubView banner) {
        if (isDestroyed) {
            return;
        }
        onInsReady(banner);
        AdLog.getSingleton().LogD("Adt-Mopub", "Mopub Banner ad load success ");
    }

    @Override
    public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
        if (isDestroyed) {
            return;
        }
        onInsError("load mopub banner error: " + errorCode.toString());
        AdLog.getSingleton().LogE("Adt-Mopub: Mopub Banner ad load failed " + errorCode.name());
    }

    @Override
    public void onBannerClicked(MoPubView banner) {
        if (isDestroyed) {
            return;
        }
        onInsClicked();
    }

    @Override
    public void onBannerExpanded(MoPubView banner) {

    }

    @Override
    public void onBannerCollapsed(MoPubView banner) {

    }

    private void loadBannerAd(Activity activity) {
        if (adView == null) {
            adView = new MoPubView(activity);
            adView.setAdUnitId(mInstancesKey);
            adView.setBannerAdListener(this);
        }

        adView.loadAd();
    }
}
