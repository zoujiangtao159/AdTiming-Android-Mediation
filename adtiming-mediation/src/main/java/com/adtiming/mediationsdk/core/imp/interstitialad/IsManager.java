// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.core.imp.interstitialad;

import com.adtiming.mediationsdk.core.Instance;
import com.adtiming.mediationsdk.interstitial.InterstitialAdListener;
import com.adtiming.mediationsdk.test.TestUtil;
import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.error.AdTimingError;
import com.adtiming.mediationsdk.utils.model.PlacementInfo;
import com.adtiming.mediationsdk.core.AbstractAdsManager;
import com.adtiming.mediationsdk.core.AdTimingManager;

public final class IsManager extends AbstractAdsManager implements IsManagerListener {

    public IsManager() {
        super();
    }

    public void initInterstitialAd() {
        scheduleLoadAdTask();
    }

    public void loadInterstitialAd() {
        loadAdWithAction(AdTimingManager.LOAD_TYPE.MANUAL);
    }

    public void showInterstitialAd(String scene) {
        showAd(scene);
    }

    public boolean isInterstitialAdReady() {
        return isPlacementAvailable();
    }

    public void setInterstitialAdListener(InterstitialAdListener listener) {
        mListenerWrapper.setAdTimingInterstitialAdListener(listener);
    }

    @Override
    protected PlacementInfo getPlacementInfo() {
        return new PlacementInfo(mPlacement.getId()).getPlacementInfo(mPlacement.getT());
    }

    @Override
    protected void initInsAndSendEvent(Instance instance) {
        if (!(instance instanceof IsInstance)) {
            instance.setMediationState(Instance.MEDIATION_STATE.INIT_FAILED);
            onInsInitFailed(instance);
            return;
        }
        DeveloperLog.LogE("is ins cast : " + instance.toString());
        IsInstance isInstance = (IsInstance) instance;
        isInstance.setIsManagerListener(this);
        isInstance.initIs(mActivity);
    }

    @Override
    protected boolean isInsAvailable(Instance instance) {
        if (instance instanceof IsInstance) {
            return ((IsInstance) instance).isIsAvailable();
        }
        return false;
    }

    @Override
    protected void insShow(Instance instance) {
        ((IsInstance) instance).showIs(mActivity);
    }

    @Override
    protected void insLoad(Instance instance) {
        IsInstance isInstance = (IsInstance) instance;
        isInstance.loadIs(mActivity);
    }

    @Override
    protected void onAvailabilityChanged(boolean available) {
        mListenerWrapper.onInterstitialAdAvailabilityChanged(available);
    }

    @Override
    protected void callbackAvailableOnManual() {
        super.callbackAvailableOnManual();
        mListenerWrapper.onInterstitialAdAvailabilityChanged(true);
    }

    @Override
    protected void callbackLoadError(AdTimingError error) {
        boolean hasCache = hasAvailableCache();
        if (shouldNotifyAvailableChanged(hasCache)) {
            mListenerWrapper.onInterstitialAdAvailabilityChanged(hasCache);
        }
        super.callbackLoadError(error);
    }

    @Override
    protected void callbackShowError(AdTimingError error) {
        super.callbackShowError(error);
        mListenerWrapper.onInterstitialAdShowFailed(error);
    }

    @Override
    protected void callbackAdClosed() {
        mListenerWrapper.onInterstitialAdClosed();
    }

    @Override
    protected void callbackCappedError(Instance instance) {
        super.callbackCappedError(instance);
        onInterstitialAdCaped((IsInstance) instance);
    }

    @Override
    public void onInterstitialAdInitSuccess(IsInstance isInstance) {
        loadInsAndSendEvent(isInstance);
    }

    @Override
    public void onInterstitialAdInitFailed(String error, IsInstance isInstance) {
        onInsInitFailed(isInstance);
    }

    @Override
    public void onInterstitialAdShowFailed(AdTimingError error, IsInstance isInstance) {
        isInShowingProgress = false;
        mListenerWrapper.onInterstitialAdShowFailed(error);
        TestUtil.getInstance().notifyInsFailed(isInstance.getPlacementId(), isInstance);
    }

    @Override
    public void onInterstitialAdOpened(IsInstance isInstance) {
        onInsOpen(isInstance);
        mListenerWrapper.onInterstitialAdShowed();
    }

    @Override
    public void onInterstitialAdClick(IsInstance isInstance) {
        mListenerWrapper.onInterstitialAdClicked();
        onInsClick(isInstance);
    }

    @Override
    public void onInterstitialAdClosed(IsInstance isInstance) {
        onInsClose();
    }

    @Override
    public void onInterstitialAdVisible(IsInstance isInstance) {
    }

    @Override
    public void onInterstitialAdLoadSuccess(IsInstance isInstance) {
        onInsReady(isInstance);
    }

    @Override
    public void onInterstitialAdLoadFailed(AdTimingError error, IsInstance isInstance) {
        DeveloperLog.LogD("IsManager onInterstitialAdLoadFailed : " + isInstance + " error : " + error);
        onInsLoadFailed(isInstance);
    }

    @Override
    public void onInterstitialAdCaped(IsInstance isInstance) {
        onInsCapped(isInstance);
    }

    @Override
    public void onReceivedEvents(String event, IsInstance isInstance) {
        receivedEvents(event, isInstance);
    }
}
