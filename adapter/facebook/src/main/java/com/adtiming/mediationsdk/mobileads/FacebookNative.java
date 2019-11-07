// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.mobileads;

import android.app.Activity;
import android.view.View;
import android.widget.RelativeLayout;

import com.adtiming.mediationsdk.mediation.CustomNativeEvent;
import com.adtiming.mediationsdk.nativead.AdIconView;
import com.adtiming.mediationsdk.nativead.MediaView;
import com.adtiming.mediationsdk.nativead.NativeAdView;
import com.adtiming.mediationsdk.utils.AdLog;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class FacebookNative extends CustomNativeEvent implements NativeAdListener {
    private AtomicBoolean mDidCallInit = new AtomicBoolean(false);
    private NativeAd nativeAd;
    private AdOptionsView adOptionsView;
    private MediaView mediaView;
    private AdIconView adIconView;

    @Override
    public void registerNativeView(NativeAdView adView) {
        NativeAdLayout fbNativeAdLayout = new NativeAdLayout(adView.getContext());
        List<View> views = new ArrayList<>();
        if (adView.getMediaView() != null) {
            mediaView = adView.getMediaView();
            views.add(mediaView);
        }

        if (adView.getAdIconView() != null) {
            adIconView = adView.getAdIconView();
            views.add(adIconView);
        }

        if (adView.getTitleView() != null) {
            views.add(adView.getTitleView());
        }

        if (adView.getDescView() != null) {
            views.add(adView.getDescView());
        }

        if (adView.getCallToActionView() != null) {
            views.add(adView.getCallToActionView());
        }

        if (adOptionsView == null) {
            adOptionsView = new AdOptionsView(adView.getContext(), nativeAd, fbNativeAdLayout);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            adView.addView(adOptionsView, layoutParams);
        }

        com.facebook.ads.MediaView fb_mediaView = null;
        if (mediaView != null) {
            mediaView.removeAllViews();
            fb_mediaView = new com.facebook.ads.MediaView(adView.getContext());
            mediaView.addView(fb_mediaView);
        }
        com.facebook.ads.MediaView fbAdIconView = null;
        if (adIconView != null) {
            adIconView.removeAllViews();
            fbAdIconView = new com.facebook.ads.MediaView(adView.getContext());
            adIconView.addView(fbAdIconView);
        }
        //pay attention to the order of fb_mediaView and adIconView here
        this.nativeAd.registerViewForInteraction(fbNativeAdLayout, fb_mediaView, fbAdIconView, views);

        if (adOptionsView != null) {
            adOptionsView.bringToFront();
        }
    }

    @Override
    public void loadAd(Activity activity, Map<String, String> config) throws Throwable {
        super.loadAd(activity, config);
        if (!check(activity, config)) {
            return;
        }
        initSdk(activity);
        nativeAd = new NativeAd(activity, mInstancesKey);
        nativeAd.setAdListener(this);
        nativeAd.loadAd();
    }

    @Override
    public int getMediation() {
        return 2;
    }

    @Override
    public void destroy(Activity activity) {
        if (nativeAd != null) {
            nativeAd.destroy();
            nativeAd = null;
        }
        isDestroyed = true;
    }

    @Override
    public void onMediaDownloaded(Ad ad) {

    }

    @Override
    public void onError(Ad ad, AdError adError) {
        if (isDestroyed) {
            return;
        }
        onInsError(adError.getErrorMessage());
        AdLog.getSingleton().LogE("Adt-Facebook: Facebook Native ad load failed " + adError.getErrorMessage());
    }

    @Override
    public void onAdLoaded(Ad ad) {
        if (isDestroyed) {
            return;
        }
        mAdInfo.setDesc(nativeAd.getAdBodyText());
        mAdInfo.setType(2);
        mAdInfo.setCallToActionText(nativeAd.getAdCallToAction());
        mAdInfo.setTitle(nativeAd.getAdHeadline());

        onInsReady(mAdInfo);
        AdLog.getSingleton().LogD("Adt-Facebook", "Facebook Native ad load success ");
    }

    @Override
    public void onAdClicked(Ad ad) {
        if (isDestroyed) {
            return;
        }
        onInsClicked();
    }

    @Override
    public void onLoggingImpression(Ad ad) {

    }

    private void initSdk(Activity activity) {
        if (mDidCallInit.compareAndSet(false, true)) {
            if (AudienceNetworkAds.isInAdsProcess(activity.getApplicationContext())) {
                // According to Xabi from facebook (29/4/19) - the meaning of isInAdsProcess==true is that
                // another process has already initialized Facebook's SDK and in this case there's no need to init it again.
                // Without this check an error will appear in the log.
                return;
            }

            AudienceNetworkAds.buildInitSettings(activity.getApplicationContext())
                    .withInitListener(new AudienceNetworkAds.InitListener() {
                        @Override
                        public void onInitialized(final AudienceNetworkAds.InitResult result) {
                        }
                    }).initialize();
        }
    }
}
