// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt.video;

import android.content.Context;

import com.adtiming.mediationsdk.adt.BaseAdImp;
import com.adtiming.mediationsdk.adt.BaseAdListener;
import com.adtiming.mediationsdk.adt.bean.AdBean;
import com.adtiming.mediationsdk.utils.Constants;
import com.adtiming.mediationsdk.utils.cache.Cache;
import com.adtiming.mediationsdk.utils.error.ErrorCode;
import com.adtiming.mediationsdk.adt.ListenerMap;

import java.io.File;

final class VideoAdImp extends BaseAdImp {
    private VideoListener mListener;

    VideoAdImp(Context context, String placementId) {
        super(context, placementId, Constants.VIDEO);
    }

    @Override
    protected void setListener(BaseAdListener adListener) {
        super.setListener(adListener);
        mListener = (VideoListener) adListener;
        ListenerMap.addListenerToMap(mPlacementId, adListener);
    }

    @Override
    public boolean isReady() {
        try {
            boolean result = super.isReady();
            File video = Cache.getCacheFile(mContext, mAdManager.getAd().getVideoUrl(), null);
            return result && video != null && video.exists() && video.length() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void show() {
        super.show();
        mAdManager.show(mContext, VideoActivity.class, mPlacementId);
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
        if (mContext == null) {
            callbackAdErrorOnUIThread(ErrorCode.ERROR_CONTEXT);
            return;
        }
        if (adBean == null) {
            callbackAdErrorOnUIThread(ErrorCode.ERROR_NO_FILL);
            return;
        }

        File video = Cache.getCacheFile(mContext, adBean.getVideoUrl(), null);
        if (video == null || video.length() == 0) {
            callbackAdErrorOnUIThread(ErrorCode.ERROR_NO_FILL);
        } else {
            callbackAdReadyOnUIThread();
        }
    }

    @Override
    protected void callbackReady() {
        super.callbackReady();
        if (mListener != null) {
            mListener.onVideoAdReady(mPlacementId);
        }
    }

    @Override
    protected void callbackError(String error) {
        super.callbackError(error);
        if (mListener != null) {
            mListener.onVideoAdFailed(mPlacementId, error);
        }
    }

    @Override
    protected void callbackClick() {
        super.callbackClick();
        if (mListener != null) {
            mListener.onVideoAdClicked(mPlacementId);
        }
    }

    @Override
    protected void callbackAdShowFailedOnUIThread(String error) {
        super.callbackAdShowFailedOnUIThread(error);
        if (mListener != null) {
            mListener.onVideoAdShowFailed(mPlacementId, error);
        }
    }
}
