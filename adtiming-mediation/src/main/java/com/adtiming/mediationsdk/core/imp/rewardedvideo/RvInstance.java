// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.core.imp.rewardedvideo;

import android.app.Activity;

import com.adtiming.mediationsdk.core.Instance;
import com.adtiming.mediationsdk.mediation.RewardedVideoCallback;
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
public class RvInstance extends Instance implements RewardedVideoCallback, LoadTimeoutRunnable.OnLoadTimeoutListener {

    private RvManagerListener mListener;

    public RvInstance() {

    }

    void initRv(Activity activity) {
        setMediationState(MEDIATION_STATE.INIT_PENDING);
        if (mAdapter != null) {
            mAdapter.initRewardedVideo(activity, getInitDataMap(), this);
            mInitStart = System.currentTimeMillis();
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_INIT_START, buildReportData());
        }
    }

    void loadRv(Activity activity) {
        setMediationState(MEDIATION_STATE.LOAD_PENDING);
        if (mAdapter != null) {
            DeveloperLog.LogD("load RewardedVideo : " + getMediationId() + " key : " + getKey());
            startInsLoadTimer(this);
            mAdapter.loadRewardedVideo(activity, getKey(), this);
            mLoadStart = System.currentTimeMillis();
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_LOAD, buildReportData());
        }
    }

    void showRv(Activity activity) {
        if (mAdapter != null) {
            mAdapter.showRewardedVideo(activity, getKey(), this);
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_SHOW, buildReportData());
        }
    }

    boolean isRvAvailable() {
        boolean result = mAdapter != null && mAdapter.isRewardedVideoAvailable(getKey())
                && getMediationState() == MEDIATION_STATE.AVAILABLE;
        if (result) {
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_READY_TRUE, buildReportData());
        } else {
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_READY_FALSE, buildReportData());
        }
        return result;
    }

    void setRvManagerListener(RvManagerListener listener) {
        mListener = listener;
    }

    @Override
    public void onRewardedVideoInitSuccess() {
        onInsInitSuccess();
        mListener.onRewardedVideoInitSuccess(this);
    }

    @Override
    public void onRewardedVideoInitFailed(String error) {
        onInsInitFailed(error);
        mListener.onRewardedVideoInitFailed(error, this);
    }

    @Override
    public void onRewardedVideoAdOpened() {
        onInsOpen();
        mListener.onRewardedVideoAdOpened(this);
    }

    @Override
    public void onRewardedVideoAdClosed() {
        onInsClosed();
        mListener.onRewardedVideoAdClosed(this);
    }

    @Override
    public void onRewardedVideoLoadSuccess() {
        DeveloperLog.LogD("RvInstance onRewardedVideoLoadSuccess : " + toString());
        onInsLoadSuccess();
        mListener.onRewardedVideoLoadSuccess(this);
    }

    @Override
    public void onRewardedVideoLoadFailed(String error) {
        AdTimingError errorResult = new AdTimingError(ErrorCode.CODE_LOAD_FAILED_IN_ADAPTER
                , ErrorCode.MSG_LOAD_FAILED_IN_ADAPTER
                + ", mediationID:" + getMediationId() + ", error:" + error, -1);
        AdLog.getSingleton().LogE(errorResult.toString() + ", onRewardedVideoLoadFailed :" + toString());
        DeveloperLog.LogD("RvInstance onRewardedVideoLoadFailed : " + toString() + " error : " + errorResult);
        onInsLoadFailed(errorResult.toString());
        mListener.onRewardedVideoLoadFailed(error, this);
    }

    @Override
    public void onRewardedVideoAdStarted() {
        onInsStarted();
        mListener.onRewardedVideoAdStarted(this);
    }

    @Override
    public void onRewardedVideoAdEnded() {
        mListener.onRewardedVideoAdEnded(this);
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_VIDEO_COMPLETED, buildReportData());
    }

    @Override
    public void onRewardedVideoAdRewarded() {
        mListener.onRewardedVideoAdRewarded(this);
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_VIDEO_REWARDED, buildReportData());
    }

    @Override
    public void onRewardedVideoAdShowFailed(String error) {
        AdTimingError errorResult = new AdTimingError(ErrorCode.CODE_SHOW_FAILED_IN_ADAPTER
                , ErrorCode.MSG_SHOW_FAILED_IN_ADAPTER
                + ", mediationId:" + getMediationId() + ", error:" + error, -1);
        AdLog.getSingleton().LogE(errorResult.toString() + ", onRewardedVideoAdShowFailed: " + toString());
        DeveloperLog.LogE(errorResult.toString() + ", onRewardedVideoAdShowFailed: " + toString());
        onInsShowFailed(errorResult.toString());
        mListener.onRewardedVideoAdShowFailed(errorResult, this);
    }

    @Override
    public void onRewardedVideoAdClicked() {
        mListener.onRewardedVideoAdClicked(this);
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_VIDEO_CLICK, buildReportData());
    }

    @Override
    public void onRewardedVideoAdVisible() {
        mListener.onRewardedVideoAdVisible(this);
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_VISIBLE, buildReportData());
    }

    @Override
    public void onReceivedEvents(String event) {
        mListener.onReceivedEvents(event, this);
    }

    @Override
    public void onLoadTimeout() {
        DeveloperLog.LogD("rvInstance onLoadTimeout : " + toString());
        onRewardedVideoLoadFailed(ErrorCode.ERROR_TIMEOUT);
    }
}
