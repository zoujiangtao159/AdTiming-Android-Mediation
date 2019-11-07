// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.core.imp.interstitialad;

import android.app.Activity;

import com.adtiming.mediationsdk.core.Instance;
import com.adtiming.mediationsdk.core.runnable.LoadTimeoutRunnable;
import com.adtiming.mediationsdk.mediation.InterstitialAdCallback;
import com.adtiming.mediationsdk.utils.AdLog;
import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.error.AdTimingError;
import com.adtiming.mediationsdk.utils.error.ErrorCode;
import com.adtiming.mediationsdk.utils.event.EventId;
import com.adtiming.mediationsdk.utils.event.EventUploadManager;


public class IsInstance extends Instance implements InterstitialAdCallback, LoadTimeoutRunnable.OnLoadTimeoutListener {

    private IsManagerListener mListener;

    public IsInstance() {
    }

    void initIs(Activity activity) {
        setMediationState(MEDIATION_STATE.INIT_PENDING);
        if (mAdapter != null) {
            mAdapter.initInterstitialAd(activity, getInitDataMap(), this);
            mInitStart = System.currentTimeMillis();
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_INIT_START, buildReportData());
        }
    }

    void loadIs(Activity activity) {
        setMediationState(MEDIATION_STATE.LOAD_PENDING);
        if (mAdapter != null) {
            DeveloperLog.LogD("load InterstitialAd : " + getMediationId() + " key : " + getKey());
            startInsLoadTimer(this);
            mAdapter.loadInterstitialAd(activity, getKey(), this);
            mLoadStart = System.currentTimeMillis();
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_LOAD, buildReportData());
        }
    }

    void showIs(Activity activity) {
        if (mAdapter != null) {
            mAdapter.showInterstitialAd(activity, getKey(), this);
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_SHOW, buildReportData());
        }
    }

    boolean isIsAvailable() {
        boolean result = mAdapter != null && mAdapter.isInterstitialAdAvailable(getKey())
                && getMediationState() == MEDIATION_STATE.AVAILABLE;
        if (result) {
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_READY_TRUE, buildReportData());
        } else {
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_READY_FALSE, buildReportData());
        }
        return result;
    }

    void setIsManagerListener(IsManagerListener listener) {
        mListener = listener;
    }

    @Override
    public void onInterstitialAdInitSuccess() {
        onInsInitSuccess();
        mListener.onInterstitialAdInitSuccess(this);
    }

    @Override
    public void onInterstitialAdInitFailed(String error) {
        onInsLoadFailed(error);
        mListener.onInterstitialAdInitFailed(error, this);
    }

    @Override
    public void onInterstitialAdOpened() {
        onInsOpen();
        mListener.onInterstitialAdOpened(this);
    }

    @Override
    public void onInterstitialAdClosed() {
        onInsClosed();
        mListener.onInterstitialAdClosed(this);
    }

    @Override
    public void onInterstitialAdLoadSuccess() {
        DeveloperLog.LogD("onInterstitialAdLoadSuccess : " + toString());
        onInsLoadSuccess();
        mListener.onInterstitialAdLoadSuccess(this);
    }

    @Override
    public void onInterstitialAdLoadFailed(String error) {
        AdTimingError errorResult = new AdTimingError(ErrorCode.CODE_LOAD_FAILED_IN_ADAPTER
                , ErrorCode.MSG_LOAD_FAILED_IN_ADAPTER
                + ", mediationID:" + getMediationId() + ", error:" + error, -1);
        AdLog.getSingleton().LogE(errorResult.toString() + "onInterstitialAdLoadFailed : " + toString());
        DeveloperLog.LogE(errorResult.toString() + "onInterstitialAdLoadFailed : " + toString() + " error : " + error);
        onInsLoadFailed(errorResult.toString());
        mListener.onInterstitialAdLoadFailed(errorResult, this);
    }

    @Override
    public void onInterstitialAdShowFailed(String error) {
        AdTimingError errorResult = new AdTimingError(ErrorCode.CODE_SHOW_FAILED_IN_ADAPTER
                , ErrorCode.MSG_SHOW_FAILED_IN_ADAPTER
                + ", mediationID:" + getMediationId() + ", error:" + error, -1);
        AdLog.getSingleton().LogE(errorResult.toString() + "onInterstitialAdShowFailed : " + toString());
        DeveloperLog.LogE(errorResult.toString() + "onInterstitialAdShowFailed : " + toString() + " error : " + error);
        onInsShowFailed(errorResult.toString());
        mListener.onInterstitialAdShowFailed(errorResult, this);
    }

    @Override
    public void onInterstitialAdVisible() {
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_VISIBLE, buildReportData());
        mListener.onInterstitialAdVisible(this);
    }

    @Override
    public void onInterstitialAdClick() {
        mListener.onInterstitialAdClick(this);
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_CLICKED, buildReportData());
    }

    @Override
    public void onReceivedEvents(String event) {
        mListener.onReceivedEvents(event, this);
    }

    @Override
    public void onLoadTimeout() {
        onInterstitialAdLoadFailed(ErrorCode.ERROR_TIMEOUT);
    }
}
