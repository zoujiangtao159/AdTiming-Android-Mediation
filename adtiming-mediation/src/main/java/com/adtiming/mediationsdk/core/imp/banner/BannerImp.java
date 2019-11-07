// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.core.imp.banner;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;

import com.adtiming.mediationsdk.banner.BannerAdListener;
import com.adtiming.mediationsdk.core.AbstractHybridAd;
import com.adtiming.mediationsdk.core.Instance;
import com.adtiming.mediationsdk.mediation.CustomBannerEvent;
import com.adtiming.mediationsdk.utils.AdRateUtil;
import com.adtiming.mediationsdk.utils.Constants;
import com.adtiming.mediationsdk.utils.PlacementUtils;
import com.adtiming.mediationsdk.utils.error.ErrorCode;
import com.adtiming.mediationsdk.utils.event.EventId;
import com.adtiming.mediationsdk.utils.event.EventUploadManager;
import com.adtiming.mediationsdk.utils.model.PlacementInfo;
import com.adtiming.mediationsdk.core.AdManager;


/**
 * Actual banner ad imp
 *
 * 
 */
public final class BannerImp extends AbstractHybridAd implements View.OnAttachStateChangeListener {
    private BannerAdListener mBannerListener;

    public BannerImp(Activity activity, String placementId, BannerAdListener listener) {
        super(activity, placementId);
        mBannerListener = listener;
    }

    @Override
    protected void loadInsOnUIThread(Instance instances) throws Throwable {
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_LOAD);
        if (!checkActRef()) {
            onInsError(instances, ErrorCode.ERROR_ACTIVITY);
            return;
        }
        if (TextUtils.isEmpty(instances.getKey())) {
            onInsError(instances, ErrorCode.ERROR_EMPTY_INSTANCE_KEY);
            return;
        }
        CustomBannerEvent bannerEvent = getAdEvent(instances);
        if (bannerEvent == null) {
            onInsError(instances, ErrorCode.ERROR_CREATE_MEDATION_ADAPTER);
            return;
        }

        instances.setStart(System.currentTimeMillis());
        bannerEvent.loadAd(mActRef.get(), PlacementUtils.getPlacementInfo(mPlacementId, instances));
        iLoadReport(instances);
    }

    @Override
    public void destroy() {
        if (mCurrentIns != null && checkActRef()) {
            CustomBannerEvent bannerEvent = getAdEvent(mCurrentIns);
            if (bannerEvent != null) {
                bannerEvent.destroy(mActRef.get());
            }
            AdManager.getInstance().removeInsAdEvent(mCurrentIns);
        }
        cleanAfterCloseOrFailed();
        super.destroy();
    }

    @Override
    protected boolean isInsReady(Instance instances) {
        boolean ready = instances != null && instances.getObject() != null && instances.getObject() instanceof View;
        if (ready) {
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_READY_TRUE);
        } else {
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_READY_FALSE);
        }
        return ready;
    }

    @Override
    public boolean isReady() {
        return isInsReady(mCurrentIns);
    }

    @Override
    protected int getAdType() {
        return Constants.BANNER;
    }

    @Override
    protected PlacementInfo getPlacementInfo() {
        return new PlacementInfo(mPlacementId).getPlacementInfo(getAdType());
    }

    @Override
    protected void onAdErrorCallback(String error) {
        if (mBannerListener != null) {
            mBannerListener.onAdFailed(error);
        }
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_SHOW_FAILED);
    }

    @Override
    protected void onAdReadyCallback() {
        if (mBannerListener == null) {
            return;
        }
        if (mCurrentIns != null) {
            if (mCurrentIns.getObject() instanceof View) {
                View banner = (View) mCurrentIns.getObject();
                banner.addOnAttachStateChangeListener(this);
                releaseAdEvent();
                mBannerListener.onAdReady(banner);
                EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_LOAD_SUCCESS);
            } else {
                mBannerListener.onAdFailed(ErrorCode.ERROR_NO_FILL);
                EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_LOAD_NO_FILL);
            }
        } else {
            mBannerListener.onAdFailed(ErrorCode.ERROR_NO_FILL);
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_LOAD_NO_FILL);
        }
    }

    @Override
    protected void onAdClickCallback() {
        if (mBannerListener != null) {
            mBannerListener.onAdClicked();
        }
    }

    @Override
    protected void destroyAdEvent(Instance instances) {
        super.destroyAdEvent(instances);
        CustomBannerEvent bannerEvent = getAdEvent(instances);
        if (bannerEvent != null && checkActRef()) {
            bannerEvent.destroy(mActRef.get());
        }
        instances.setObject(null);
    }

    private CustomBannerEvent getAdEvent(Instance instances) {
        return (CustomBannerEvent) AdManager.getInstance().getInsAdEvent(Constants.BANNER, instances);
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        if (mCurrentIns == null) {
            return;
        }
        insImpReport(mCurrentIns);
        AdRateUtil.onInstancesShowed(mPlacementId, mCurrentIns.getKey());
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_PRESENT_SCREEN);
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_SHOW);
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        v.removeOnAttachStateChangeListener(this);
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_DISMISS_SCREEN);
    }
}
