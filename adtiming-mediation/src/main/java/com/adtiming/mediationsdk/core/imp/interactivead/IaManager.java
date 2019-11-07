// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.core.imp.interactivead;

import android.app.Activity;
import android.text.TextUtils;

import com.adtiming.mediationsdk.core.Instance;
import com.adtiming.mediationsdk.interactive.InteractiveAdListener;
import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.error.AdTimingError;
import com.adtiming.mediationsdk.utils.interactive.IAUtil;
import com.adtiming.mediationsdk.utils.interactive.IActiveListener;
import com.adtiming.mediationsdk.utils.model.Placement;
import com.adtiming.mediationsdk.utils.model.PlacementInfo;
import com.adtiming.mediationsdk.video.RewardedVideoListener;
import com.adtiming.mediationsdk.core.AbstractAdsManager;
import com.adtiming.mediationsdk.core.AdTimingManager;
import com.adtiming.mediationsdk.test.TestUtil;

public final class IaManager extends AbstractAdsManager implements IaManagerListener, IActiveListener,
        RewardedVideoListener {

    private String mVpId;

    public IaManager() {
        super();
    }

    public void initInteractiveAd() {
        scheduleLoadAdTask();
    }

    public void loadInteractiveAd() {
        loadAdWithAction(AdTimingManager.LOAD_TYPE.MANUAL);
    }

    public void showInteractiveAd(String scene) {
        showAd(scene);
    }

    public boolean isInteractiveAdReady() {
        return isPlacementAvailable();
    }

    public void setInteractiveAdListener(InteractiveAdListener listener) {
        mListenerWrapper.setAdTimingInteractiveAdListener(listener);
    }

    @Override
    protected PlacementInfo getPlacementInfo() {
        return new PlacementInfo(mPlacement.getId()).getPlacementInfo(mPlacement.getT());
    }

    @Override
    protected void initInsAndSendEvent(Instance instance) {
        if (!(instance instanceof IaInstance)) {
            instance.setMediationState(Instance.MEDIATION_STATE.INIT_FAILED);
            onInsInitFailed(instance);
            return;
        }
        IaInstance iaInstance = (IaInstance) instance;
        iaInstance.setIaManagerListener(this);
        iaInstance.initIa(mActivity);
    }

    @Override
    protected boolean isInsAvailable(Instance instance) {
        if (instance instanceof IaInstance) {
            return ((IaInstance) instance).isIaAvailable();
        }
        return false;
    }

    @Override
    protected void insShow(Instance instance) {
        ((IaInstance) instance).showIa(mActivity);
    }

    @Override
    protected void insLoad(Instance instance) {
        ((IaInstance) instance).loadIa(mActivity);
    }

    @Override
    protected void onAvailabilityChanged(boolean available) {
        mListenerWrapper.onInteractiveAdAvailabilityChanged(available);
    }

    @Override
    public void onPause(Activity activity) {
        super.onPause(activity);
    }

    @Override
    public void setCurrentPlacement(Placement placement) {
        super.setCurrentPlacement(placement);
        if (mPlacement != null && mPlacement.getVid() > 0) {
            mVpId = String.valueOf(mPlacement.getVid());
            if (!TextUtils.isEmpty(mVpId)) {
                AdTimingManager.getInstance().setRewardedVideoListener(mVpId, this);
            }
        }
    }

    @Override
    protected void callbackAvailableOnManual() {
        super.callbackAvailableOnManual();
        mListenerWrapper.onInteractiveAdAvailabilityChanged(true);
    }

    @Override
    protected void callbackLoadError(AdTimingError error) {
        boolean hasCache = hasAvailableCache();
        if (shouldNotifyAvailableChanged(hasCache)) {
            mListenerWrapper.onInteractiveAdAvailabilityChanged(hasCache);
        }
        super.callbackLoadError(error);
    }

    @Override
    protected void callbackShowError(AdTimingError error) {
        super.callbackShowError(error);
        mListenerWrapper.onInteractiveAdShowFailed(error);
    }

    @Override
    protected void callbackCappedError(Instance instance) {
        super.callbackCappedError(instance);
        onInteractiveAdCaped((IaInstance) instance);
    }

    @Override
    protected void callbackAdClosed() {
        mListenerWrapper.onInteractiveAdClosed();
    }

    @Override
    public void onInteractiveAdInitSuccess(IaInstance iaInstance) {
        loadInsAndSendEvent(iaInstance);
    }

    @Override
    public void onInteractiveAdInitFailed(String error, IaInstance iaInstance) {
        onInsInitFailed(iaInstance);
    }

    @Override
    public void onInteractiveAdShowFailed(AdTimingError error, IaInstance iaInstance) {
        isInShowingProgress = false;
        mListenerWrapper.onInteractiveAdShowFailed(error);
        TestUtil.getInstance().notifyInsFailed(iaInstance.getPlacementId(), iaInstance);
    }

    @Override
    public void onInteractiveAdOpened(IaInstance iaInstance) {
        IAUtil.getInstance().registerIActiveListener(this);
        onInsOpen(iaInstance);
        mListenerWrapper.onInteractiveAdShowed();
    }

    @Override
    public void onInteractiveAdClosed(IaInstance iaInstance) {
        IAUtil.getInstance().unregisterIActiveListener();
        onInsClose();
    }

    @Override
    public void onInteractiveAdVisible(IaInstance iaInstance) {
    }

    @Override
    public void onInteractiveAdLoadSuccess(IaInstance iaInstance) {
        onInsReady(iaInstance);
    }

    @Override
    public void onInteractiveAdLoadFailed(AdTimingError error, IaInstance iaInstance) {
        DeveloperLog.LogD("IsManager onInteractiveAdLoadFailed : " + iaInstance + " error : " + error);
        onInsLoadFailed(iaInstance);
    }

    @Override
    public void onInteractiveAdCaped(IaInstance iaInstance) {
        iaInstance.setMediationState(Instance.MEDIATION_STATE.CAPPED);
        onInsCapped(iaInstance);
    }

    @Override
    public void onReceivedEvents(String event, IaInstance iaInstance) {
        receivedEvents(event, iaInstance);
    }

    @Override
    public void playVideo() {
        DeveloperLog.LogD("request play video");
        if (!TextUtils.isEmpty(mVpId) && AdTimingManager.getInstance().isRewardedVideoReady(mVpId)) {
            if (mScene != null) {
                AdTimingManager.getInstance().showRewardedVideo(mVpId, mScene.getN());
            } else {
                AdTimingManager.getInstance().showRewardedVideo(mVpId, "");
            }
            IAUtil.getInstance().notifyVideoShow();
        }
    }

    @Override
    public void askVideoReady() {
        boolean result = !TextUtils.isEmpty(mVpId) && AdTimingManager.getInstance().isRewardedVideoReady(mVpId);
        DeveloperLog.LogD("ask video Ready : " + result);
        if (result) {
            IAUtil.getInstance().notifyVideoReady();
        }
    }

    @Override
    public void loadVideo() {
        if (!TextUtils.isEmpty(mVpId)) {
            AdTimingManager.getInstance().loadRewardedVideo(mVpId);
        }
    }

    @Override
    public void onRewardedVideoAvailabilityChanged(boolean available) {
        if (available) {
            IAUtil.getInstance().notifyVideoReady();
        }
    }

    @Override
    public void onRewardedVideoAdShowed() {

    }

    @Override
    public void onRewardedVideoAdShowFailed(AdTimingError error) {

    }

    @Override
    public void onRewardedVideoAdClicked() {

    }

    @Override
    public void onRewardedVideoAdClosed() {
        IAUtil.getInstance().notifyVideoClose();
    }

    @Override
    public void onRewardedVideoAdStarted() {

    }

    @Override
    public void onRewardedVideoAdEnded() {

    }

    @Override
    public void onRewardedVideoAdRewarded() {

    }
}
