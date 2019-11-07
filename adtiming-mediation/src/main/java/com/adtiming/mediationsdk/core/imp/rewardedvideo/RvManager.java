// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.core.imp.rewardedvideo;

import com.adtiming.mediationsdk.core.Instance;
import com.adtiming.mediationsdk.test.TestUtil;
import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.MediationRequest;
import com.adtiming.mediationsdk.utils.error.AdTimingError;
import com.adtiming.mediationsdk.utils.model.PlacementInfo;
import com.adtiming.mediationsdk.video.RewardedVideoListener;
import com.adtiming.mediationsdk.core.AbstractAdsManager;
import com.adtiming.mediationsdk.core.AdTimingManager;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public final class RvManager extends AbstractAdsManager implements RvManagerListener {
    private Map<String, String> mExtIds = new HashMap<>();


    public RvManager() {
        super();
    }

    public void initRewardedVideo() {
        scheduleLoadAdTask();
    }

    public void loadRewardedVideo() {
        loadAdWithAction(AdTimingManager.LOAD_TYPE.MANUAL);
    }

    public void showRewardedVideo(String scene) {
        showAd(scene);
    }

    public boolean isRewardedVideoReady() {
        return isPlacementAvailable();
    }

    public void setRewardedExtId(String scene, String extId) {
        mExtIds.put(scene, extId);
    }

    public void setRewardedVideoListener(RewardedVideoListener listener) {
        mListenerWrapper.setRewardedVideoListener(listener);
    }

    @Override
    protected PlacementInfo getPlacementInfo() {
        return new PlacementInfo(mPlacement.getId()).getPlacementInfo(mPlacement.getT());
    }

    @Override
    protected void initInsAndSendEvent(Instance instance) {
        if (!(instance instanceof RvInstance)) {
            instance.setMediationState(Instance.MEDIATION_STATE.INIT_FAILED);
            onInsInitFailed(instance);
            return;
        }
        RvInstance rvInstance = (RvInstance) instance;
        rvInstance.setRvManagerListener(this);
        rvInstance.initRv(mActivity);
    }

    @Override
    protected boolean isInsAvailable(Instance instance) {
        if (instance instanceof RvInstance) {
            return ((RvInstance) instance).isRvAvailable();
        }
        return false;
    }

    @Override
    protected void insShow(Instance instance) {
        ((RvInstance) instance).showRv(mActivity);
    }

    @Override
    protected void insLoad(Instance instance) {
        RvInstance rvInstance = (RvInstance) instance;
        rvInstance.loadRv(mActivity);
    }

    @Override
    protected void onAvailabilityChanged(boolean available) {
        mListenerWrapper.onRewardedVideoAvailabilityChanged(available);
    }

    @Override
    protected void callbackAvailableOnManual() {
        super.callbackAvailableOnManual();
        mListenerWrapper.onRewardedVideoAvailabilityChanged(true);
    }

    @Override
    protected void callbackLoadError(AdTimingError error) {
        boolean hasCache = hasAvailableCache();
        if (shouldNotifyAvailableChanged(hasCache)) {
            mListenerWrapper.onRewardedVideoAvailabilityChanged(hasCache);
        }
        super.callbackLoadError(error);
    }

    @Override
    protected void callbackShowError(AdTimingError error) {
        super.callbackShowError(error);
        mListenerWrapper.onRewardedVideoAdShowFailed(error);
    }

    @Override
    protected void callbackCappedError(Instance instance) {
        super.callbackCappedError(instance);
        onRewardedVideoAdCaped((RvInstance) instance);
    }

    @Override
    protected void callbackAdClosed() {
        mListenerWrapper.onRewardedVideoAdClosed();
    }

    @Override
    public void onRewardedVideoInitSuccess(RvInstance rvInstance) {
        loadInsAndSendEvent(rvInstance);
    }

    @Override
    public void onRewardedVideoInitFailed(String error, RvInstance rvInstance) {
        onInsInitFailed(rvInstance);
    }

    @Override
    public void onRewardedVideoAdShowFailed(AdTimingError error, RvInstance rvInstance) {
        isInShowingProgress = false;
        mListenerWrapper.onRewardedVideoAdShowFailed(error);
        //FixMe
        TestUtil.getInstance().notifyInsFailed(rvInstance.getPlacementId(), rvInstance);
    }

    @Override
    public void onRewardedVideoAdOpened(RvInstance rvInstance) {
        onInsOpen(rvInstance);
        mListenerWrapper.onRewardedVideoAdShowed();
    }

    @Override
    public void onRewardedVideoAdClosed(RvInstance rvInstance) {
        onInsClose();
    }

    @Override
    public void onRewardedVideoLoadSuccess(RvInstance rvInstance) {
        onInsReady(rvInstance);
    }

    @Override
    public void onRewardedVideoLoadFailed(String error, RvInstance rvInstance) {
        DeveloperLog.LogD("RvManager onRewardedVideoLoadFailed : " + rvInstance + " error : " + error);
        onInsLoadFailed(rvInstance);
    }

    @Override
    public void onRewardedVideoAdStarted(RvInstance rvInstance) {
        mListenerWrapper.onRewardedVideoAdStarted();
    }

    @Override
    public void onRewardedVideoAdEnded(RvInstance rvInstance) {
        mListenerWrapper.onRewardedVideoAdEnded();
    }

    @Override
    public void onRewardedVideoAdRewarded(RvInstance rvInstance) {
        if (!mExtIds.isEmpty() && mScene != null && mExtIds.containsKey(mScene.getN())) {
            MediationRequest.httpVpc(mPlacement.getId(), mExtIds.get(mScene.getN()));
        } else {
            mListenerWrapper.onRewardedVideoAdRewarded();
        }
    }

    @Override
    public void onRewardedVideoAdClicked(RvInstance rvInstance) {
        mListenerWrapper.onRewardedVideoAdClicked();
        onInsClick(rvInstance);
    }

    @Override
    public void onRewardedVideoAdVisible(RvInstance rvInstance) {
    }

    @Override
    public void onRewardedVideoAdCaped(RvInstance rvInstance) {
        rvInstance.setMediationState(Instance.MEDIATION_STATE.CAPPED);
        onInsCapped(rvInstance);
    }

    @Override
    public void onReceivedEvents(String event, RvInstance baseInstance) {
        receivedEvents(event, baseInstance);
    }
}
