// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.core.imp.nativead;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;

import com.adtiming.mediationsdk.core.AbstractHybridAd;
import com.adtiming.mediationsdk.core.Instance;
import com.adtiming.mediationsdk.mediation.CustomNativeEvent;
import com.adtiming.mediationsdk.nativead.AdInfo;
import com.adtiming.mediationsdk.nativead.NativeAdListener;
import com.adtiming.mediationsdk.nativead.NativeAdView;
import com.adtiming.mediationsdk.utils.AdRateUtil;
import com.adtiming.mediationsdk.utils.Constants;
import com.adtiming.mediationsdk.utils.PlacementUtils;
import com.adtiming.mediationsdk.utils.error.ErrorCode;
import com.adtiming.mediationsdk.utils.event.EventId;
import com.adtiming.mediationsdk.utils.event.EventUploadManager;
import com.adtiming.mediationsdk.utils.model.PlacementInfo;
import com.adtiming.mediationsdk.core.AdManager;

import java.util.Map;

/**
 * 
 */
public final class NativeImp extends AbstractHybridAd implements View.OnAttachStateChangeListener {
    private NativeAdListener mNativeListener;
    private NativeAdView mNativeAdView;
    private boolean isImpressed;

    public NativeImp(Activity activity, String placementId, NativeAdListener listener) {
        super(activity, placementId);
        mNativeListener = listener;
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
        CustomNativeEvent nativeEvent = getAdEvent(instances);
        if (nativeEvent == null) {
            onInsError(instances, ErrorCode.ERROR_CREATE_MEDATION_ADAPTER);
            return;
        }
        Map<String, String> config = PlacementUtils.getPlacementInfo(mPlacementId, instances);
        instances.setStart(System.currentTimeMillis());
        nativeEvent.loadAd(mActRef.get(), config);
        iLoadReport(instances);
    }

    @Override
    public void destroy() {
        if (mNativeAdView != null) {
            mNativeAdView.removeAllViews();
            mNativeAdView = null;
        }
        if (mCurrentIns != null) {
            CustomNativeEvent nativeEvent = getAdEvent(mCurrentIns);
            if (nativeEvent != null) {
                nativeEvent.destroy(mActRef.get());
            }
            AdManager.getInstance().removeInsAdEvent(mCurrentIns);
        }
        cleanAfterCloseOrFailed();
        super.destroy();
    }

    @Override
    protected boolean isInsReady(Instance instances) {
        boolean ready = instances != null && instances.getObject() != null && instances.getObject() instanceof AdInfo;
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
        return Constants.NATIVE;
    }

    @Override
    protected PlacementInfo getPlacementInfo() {
        return new PlacementInfo(mPlacementId).getPlacementInfo(getAdType());
    }

    @Override
    protected void onAdErrorCallback(String error) {
        if (mNativeListener != null) {
            mNativeListener.onAdFailed(error);
        }
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_SHOW_FAILED);
    }

    @Override
    protected void onAdReadyCallback() {
        if (mNativeListener == null) {
            return;
        }
        if (mCurrentIns != null) {
            Object o = mCurrentIns.getObject();
            if (o instanceof AdInfo) {
                AdInfo adInfo = (AdInfo) o;
                mNativeListener.onAdReady(adInfo);
                EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_LOAD_SUCCESS);
            } else {
                mNativeListener.onAdFailed(ErrorCode.ERROR_NO_FILL);
                EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_LOAD_NO_FILL);
            }
        } else {
            mNativeListener.onAdFailed(ErrorCode.ERROR_NO_FILL);
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_LOAD_NO_FILL);
        }
    }

    @Override
    protected void onAdClickCallback() {
        if (mNativeListener != null) {
            mNativeListener.onAdClicked();
        }
    }

    public void registerView(NativeAdView adView) {
        if (isDestroyed) {
            return;
        }
        mNativeAdView = adView;
        if (mCurrentIns != null) {
            CustomNativeEvent nativeEvent = getAdEvent(mCurrentIns);
            if (nativeEvent != null) {
                mNativeAdView.addOnAttachStateChangeListener(this);
                nativeEvent.registerNativeView(adView);
            }
        }
    }

    private CustomNativeEvent getAdEvent(Instance instances) {
        return (CustomNativeEvent) AdManager.getInstance().getInsAdEvent(Constants.NATIVE, instances);
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        if (isImpressed || mCurrentIns == null) {
            return;
        }

        isImpressed = true;
        insImpReport(mCurrentIns);
        AdRateUtil.onInstancesShowed(mPlacementId, mCurrentIns.getKey());
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_PRESENT_SCREEN);
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_SHOW);
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        isImpressed = false;
        v.removeOnAttachStateChangeListener(this);
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_DISMISS_SCREEN);
    }
}
