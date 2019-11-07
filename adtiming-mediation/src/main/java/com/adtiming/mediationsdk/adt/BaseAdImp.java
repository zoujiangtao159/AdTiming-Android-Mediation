// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt;

import android.content.Context;
import android.text.TextUtils;

import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.HandlerUtil;
import com.adtiming.mediationsdk.utils.crash.CrashUtil;
import com.adtiming.mediationsdk.utils.error.ErrorCode;
import com.adtiming.mediationsdk.adt.bean.AdBean;

public class BaseAdImp implements AdManager.OnLoadAdCallback {
    protected Context mContext;
    protected AdManager mAdManager;
    protected String mPlacementId;
    private BaseAdListener mAdListener;
    private long mLoadTs;//Timestamp to begin loading
    private long mCallbackTs;//Timestamp to receive ready/fail callback 

    public BaseAdImp(Context context, String placementId, int adType) {
        if (context == null) {
            callbackAdErrorOnUIThread(ErrorCode.ERROR_CONTEXT);
            return;
        }

        if (TextUtils.isEmpty(placementId)) {
            DeveloperLog.LogD("BaseAdImp", "placementId is null");
            callbackAdErrorOnUIThread(ErrorCode.ERROR_PLACEMENT_ID);
            return;
        }
        mContext = context.getApplicationContext();
        mAdManager = new AdManager(placementId, adType, this);
        mPlacementId = placementId;
    }

    public boolean isReady() {
        return mAdManager != null && mAdManager.isReady();
    }

    protected void setListener(BaseAdListener adListener) {
        if (adListener == null) {
            throw new IllegalArgumentException("Please setUp a adListener instance, current is null");
        }
        this.mAdListener = adListener;
    }

    public void load() {
        try {
            if (mLoadTs > mCallbackTs) {//Loading continues
                return;
            }

            mLoadTs = System.currentTimeMillis();
            if (mAdManager == null) {
                if (TextUtils.isEmpty(mPlacementId)) {
                    DeveloperLog.LogD("BaseAdImp", "create adManager null, cause placementId is null");
                    callbackAdErrorOnUIThread(ErrorCode.ERROR_PLACEMENT_ID);
                    return;
                }
                DeveloperLog.LogD("BaseAdImp", "create adManager null, can't load ad");
                callbackAdErrorOnUIThread(ErrorCode.ERROR_UNSPECIFIED);
                return;
            }

            if (isReady()) {
                callbackAdReadyOnUIThread();
                return;
            }
            mAdManager.loadAd(mContext);
        } catch (Exception e) {
            callbackAdErrorOnUIThread(ErrorCode.ERROR_UNSPECIFIED);
            DeveloperLog.LogD("BaseAdImp", e);
            CrashUtil.getSingleton().saveException(e);
        }
    }

    protected void show() {

    }

    protected void destroy() {
        if (mAdManager != null) {
            mAdManager.destroy();
        }
        mAdListener = null;
        mContext = null;
    }


    /**
     * Ads loading error callback
     *
     * @param error Error info
     */
    protected void callbackAdErrorOnUIThread(final String error) {
        mCallbackTs = System.currentTimeMillis();
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mAdListener == null) {
                    return;
                }
                callbackError(error);
            }
        });
    }

    /**
     * Ads ready callback
     */
    protected void callbackAdReadyOnUIThread() {
        mCallbackTs = System.currentTimeMillis();
        if (mAdListener == null) {
            return;
        }
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callbackReady();
            }
        });
    }

    /**
     * Ads click callback
     */
    protected void callbackAdClickOnUIThread() {
        if (mAdListener == null) {
            return;
        }
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callbackClick();
            }
        });
    }

    protected void callbackAdShowFailedOnUIThread(String error) {
        if (mAdListener == null) {
            return;
        }
    }

    protected void callbackReady() {
    }

    protected void callbackError(String error) {
    }

    protected void callbackClick() {
    }

    protected void callbackShowFailed(final String error) {
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callbackShowFailed(error);
            }
        });
    }

    @Override
    public void onLoadAdSuccess(AdBean adBean) {

    }

    @Override
    public void onLoadAdFailed(String error) {
        callbackAdErrorOnUIThread("request ad failed : " + error);
    }
}
