// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt.interstitial;

import android.content.Context;

import com.adtiming.mediationsdk.utils.Constants;
import com.adtiming.mediationsdk.adt.AdManager;
import com.adtiming.mediationsdk.adt.BaseAdImp;
import com.adtiming.mediationsdk.adt.BaseAdListener;
import com.adtiming.mediationsdk.adt.ListenerMap;
import com.adtiming.mediationsdk.adt.bean.AdBean;

final class InterstitialAdImp extends BaseAdImp implements AdManager.OnLoadAdCallback {

    private InterstitialListener mListener;

    InterstitialAdImp(Context context, String placementId) {
        super(context, placementId, Constants.INTERSTITIAL);
    }

    @Override
    protected void setListener(BaseAdListener adListener) {
        super.setListener(adListener);
        mListener = (InterstitialListener) adListener;
        ListenerMap.addListenerToMap(mPlacementId, adListener);
    }

    @Override
    protected void show() {
        super.show();
        mAdManager.show(mContext, InterstitialActivity.class, mPlacementId);
    }

    @Override
    protected void destroy() {
        super.destroy();
        ListenerMap.removeListenerFromMap(mPlacementId);
        mListener = null;
    }

    @Override
    public void onLoadAdSuccess(AdBean adBean) {
        super.onLoadAdSuccess(adBean);
        callbackAdReadyOnUIThread();
    }

    @Override
    protected void callbackReady() {
        super.callbackReady();
        if (mListener != null) {
            mListener.onInterstitialAdReady(mPlacementId);
        }
    }

    @Override
    protected void callbackError(String error) {
        super.callbackError(error);
        if (mListener != null) {
            mListener.onInterstitialAdFailed(mPlacementId, error);
        }
    }

    @Override
    protected void callbackClick() {
        super.callbackClick();
        if (mListener != null) {
            mListener.onInterstitialAdClicked(mPlacementId);
        }
    }

    @Override
    protected void callbackShowFailed(String error) {
        super.callbackShowFailed(error);
        if (mListener != null) {
            mListener.onInterstitialAdShowFailed(mPlacementId, error);
        }
    }
}
