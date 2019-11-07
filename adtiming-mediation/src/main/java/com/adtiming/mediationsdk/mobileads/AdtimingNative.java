// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.mobileads;

import android.app.Activity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.adtiming.mediationsdk.adt.nativead.NativeListener;
import com.adtiming.mediationsdk.mediation.CustomNativeEvent;
import com.adtiming.mediationsdk.nativead.AdIconView;
import com.adtiming.mediationsdk.nativead.AdInfo;
import com.adtiming.mediationsdk.nativead.MediaView;
import com.adtiming.mediationsdk.nativead.NativeAdView;
import com.adtiming.mediationsdk.adt.nativead.Ad;
import com.adtiming.mediationsdk.adt.nativead.AdtNativeAd;

import java.util.Map;

public class AdtimingNative extends CustomNativeEvent implements NativeListener {
    private AdtNativeAd mNativeAd;
    private Ad mAd;

    @Override
    public void loadAd(Activity activity, Map<String, String> config) throws Throwable {
        super.loadAd(activity, config);

        if (!check(activity, config)) {
            return;
        }

        if (mNativeAd != null) {
            mNativeAd.loadAd();
            return;
        }
        mNativeAd = new AdtNativeAd(activity, mInstancesKey);
        mNativeAd.setAdListener(this);
        mNativeAd.loadAd();
    }

    @Override
    public void registerNativeView(NativeAdView adView) {
        try {
            if (isDestroyed || mAd == null) {
                return;
            }
            if (adView.getMediaView() != null) {
                MediaView mediaView = adView.getMediaView();

                if (mAd.getContent() != null) {
                    mediaView.removeAllViews();
                    ImageView imageView = new ImageView(adView.getContext());
                    mediaView.addView(imageView);
                    imageView.setImageBitmap(mAd.getContent());
                    imageView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
                    imageView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
                }
                mNativeAd.registerActionView(mediaView);
            }

            if (adView.getAdIconView() != null) {
                AdIconView adIconView = adView.getAdIconView();

                if (mAd.getIcon() != null) {
                    adIconView.removeAllViews();
                    ImageView iconImageView = new ImageView(adView.getContext());
                    adIconView.addView(iconImageView);
                    iconImageView.setImageBitmap(mAd.getIcon());
                    iconImageView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
                    iconImageView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
                }
                mNativeAd.registerActionView(adIconView);
            }

            if (adView.getTitleView() != null) {
                mNativeAd.registerActionView(adView.getTitleView());
            }

            if (adView.getDescView() != null) {
                mNativeAd.registerActionView(adView.getDescView());
            }

            if (adView.getCallToActionView() != null) {
                mNativeAd.registerActionView(adView.getCallToActionView());
            }

            addAndShowAdLogo(adView);
        } catch (Throwable e) {

        }
    }

    @Override
    public void onNativeAdReady(String placementId, Ad ad) {
        if (!isDestroyed) {
            mAd = ad;
            AdInfo adInfo = new AdInfo();
            adInfo.setDesc(ad.getDescription());
            adInfo.setType(0);
            adInfo.setTitle(ad.getTitle());
            adInfo.setCallToActionText(ad.getCTA());
            onInsReady(adInfo);
        }
    }

    @Override
    public void onNativeAdFailed(String placementId, String error) {
        if (!isDestroyed) {
            onInsError(error);
        }
    }

    @Override
    public void onNativeAdClicked(String placementId) {
        if (!isDestroyed) {
            onInsClicked();
        }
    }

    @Override
    public void onNativeAdShowFailed(String placementId, String error) {

    }

    @Override
    public void destroy(Activity activity) {
        if (mNativeAd != null) {
            mNativeAd.destroy();
            mNativeAd = null;
        }
        isDestroyed = true;
    }

    @Override
    public int getMediation() {
        return 0;
    }

    private void addAndShowAdLogo(ViewGroup parent) {
        if (mNativeAd != null) {
            mNativeAd.setUpAdLogo(parent);
        }
    }
}
