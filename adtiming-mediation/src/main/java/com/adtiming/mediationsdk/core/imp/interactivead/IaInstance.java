// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.core.imp.interactivead;

import android.app.Activity;

import com.adtiming.mediationsdk.core.Instance;
import com.adtiming.mediationsdk.mediation.InteractiveAdCallback;
import com.adtiming.mediationsdk.utils.AdLog;
import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.error.AdTimingError;
import com.adtiming.mediationsdk.utils.error.ErrorCode;
import com.adtiming.mediationsdk.utils.event.EventId;
import com.adtiming.mediationsdk.utils.event.EventUploadManager;
import com.adtiming.mediationsdk.core.runnable.LoadTimeoutRunnable;


/**
 * 
 */
public class IaInstance extends Instance implements InteractiveAdCallback, LoadTimeoutRunnable.OnLoadTimeoutListener {

    private IaManagerListener mListener;

    public IaInstance() {

    }

    void initIa(Activity activity) {
        setMediationState(MEDIATION_STATE.INIT_PENDING);
        if (mAdapter != null) {
            mAdapter.initInteractiveAd(activity, getInitDataMap(), this);
            mInitStart = System.currentTimeMillis();
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_INIT_START, buildReportData());
        }
    }

    void loadIa(Activity activity) {
        setMediationState(MEDIATION_STATE.LOAD_PENDING);
        if (mAdapter != null) {
            DeveloperLog.LogD("load InteractiveAd : " + getMediationId() + " key : " + getKey());
            startInsLoadTimer(this);
            mAdapter.loadInteractiveAd(activity, getKey(), this);
            mLoadStart = System.currentTimeMillis();
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_LOAD, buildReportData());
        }
    }

    void showIa(Activity activity) {
        if (mAdapter != null) {
            mAdapter.showInteractiveAd(activity, getKey(), this);
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_SHOW, buildReportData());
        }
    }

    boolean isIaAvailable() {
        boolean result = mAdapter != null && mAdapter.isInteractiveAdAvailable(getKey())
                && getMediationState() == MEDIATION_STATE.AVAILABLE;
        if (result) {
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_READY_TRUE, buildReportData());
        } else {
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_READY_FALSE, buildReportData());
        }
        return result;
    }

    void setIaManagerListener(IaManagerListener listener) {
        mListener = listener;
    }

    @Override
    public void onInteractiveAdInitSuccess() {
        onInsInitSuccess();
        mListener.onInteractiveAdInitSuccess(this);
    }

    @Override
    public void onInteractiveAdInitFailed(String error) {
        onInsInitFailed(error);
        mListener.onInteractiveAdInitFailed(error, this);
    }

    @Override
    public void onInteractiveAdOpened() {
        onInsOpen();
        mListener.onInteractiveAdOpened(this);
    }

    @Override
    public void onInteractiveAdClosed() {
        onInsClosed();
        mListener.onInteractiveAdClosed(this);
    }

    @Override
    public void onInteractiveAdLoadSuccess() {
        DeveloperLog.LogD("onInteractiveAdLoadSuccess : " + toString());
        onInsLoadSuccess();
        mListener.onInteractiveAdLoadSuccess(this);
    }

    @Override
    public void onInteractiveAdLoadFailed(String error) {
        AdTimingError errorResult = new AdTimingError(ErrorCode.CODE_LOAD_FAILED_IN_ADAPTER
                , ErrorCode.MSG_LOAD_FAILED_IN_ADAPTER
                + ", mediationID:" + getMediationId() + ", error:" + error, -1);
        AdLog.getSingleton().LogE(errorResult.toString() + "onInteractiveAdLoadFailed : " + toString());
        DeveloperLog.LogE(errorResult.toString() + "onInteractiveAdLoadFailed : " + toString() + " error : " + error);
        onInsLoadFailed(errorResult.toString());
        mListener.onInteractiveAdLoadFailed(errorResult, this);
    }


    @Override
    public void onInteractiveAdShowFailed(String error) {
        //todo failed in adapter
        AdTimingError errorResult = new AdTimingError(ErrorCode.CODE_SHOW_FAILED_IN_ADAPTER
                , ErrorCode.MSG_SHOW_FAILED_IN_ADAPTER
                + ", mediationID:" + getMediationId() + ", error:" + error, -1);
        AdLog.getSingleton().LogE(errorResult.toString() + "onInteractiveAdShowFailed : " + toString());
        DeveloperLog.LogE(errorResult.toString() + "onInteractiveAdShowFailed : " + toString() + " error : " + error);
        onInsShowFailed(errorResult.toString());
        mListener.onInteractiveAdShowFailed(errorResult, this);
    }

    @Override
    public void onInteractiveAdVisible() {
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_VISIBLE, buildReportData());
        mListener.onInteractiveAdVisible(this);
    }

    @Override
    public void onReceivedEvents(String event) {
        mListener.onReceivedEvents(event, this);
    }

    @Override
    public void onLoadTimeout() {
        onInteractiveAdLoadFailed(ErrorCode.ERROR_TIMEOUT);
    }
}
