// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.core;

import android.app.Activity;
import android.text.TextUtils;

import com.adtiming.mediationsdk.core.imp.interactivead.IaManager;
import com.adtiming.mediationsdk.core.imp.interstitialad.IsManager;
import com.adtiming.mediationsdk.core.imp.rewardedvideo.RvManager;
import com.adtiming.mediationsdk.interactive.InteractiveAdListener;
import com.adtiming.mediationsdk.interstitial.InterstitialAdListener;
import com.adtiming.mediationsdk.utils.AdLog;
import com.adtiming.mediationsdk.utils.Constants;
import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.PlacementUtils;
import com.adtiming.mediationsdk.utils.cache.DataCache;
import com.adtiming.mediationsdk.utils.error.AdTimingError;
import com.adtiming.mediationsdk.utils.error.ErrorCode;
import com.adtiming.mediationsdk.utils.event.EventId;
import com.adtiming.mediationsdk.utils.event.EventUploadManager;
import com.adtiming.mediationsdk.utils.model.Config;
import com.adtiming.mediationsdk.utils.model.Placement;
import com.adtiming.mediationsdk.video.RewardedVideoListener;
import com.adtiming.mediationsdk.InitCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.adtiming.mediationsdk.AdTimingAds.*;

/**
 *
 */
public final class AdTimingManager implements InitCallback {
    private Map<String, IaManager> mIaManagers;
    private Map<String, IsManager> mIsManagers;
    private Map<String, RvManager> mRvManagers;
    private ConcurrentMap<String, InteractiveAdListener> mIaListeners;
    private ConcurrentMap<String, InterstitialAdListener> mIsListeners;
    private ConcurrentMap<String, RewardedVideoListener> mRvListeners;
    private List<AD_TYPE> mPreloadAdTypes;
    private AtomicBoolean mDidRvInit = new AtomicBoolean(false);
    private AtomicBoolean mDidIsInit = new AtomicBoolean(false);
    private AtomicBoolean mDidIaInit = new AtomicBoolean(false);
    private static ConcurrentLinkedQueue<InitCallback> mInitCallbacks = new ConcurrentLinkedQueue<>();

    private static final class AMHolder {
        private static final AdTimingManager INSTANCE = new AdTimingManager();
    }

    public enum LOAD_TYPE {
        /**
         *
         */
        UNKNOWN(0),
        /**
         * Initialization
         */
        INIT(1),
        /**
         * Scheduled task
         */
        INTERVAL(2),
        /**
         * Ads close
         */
        CLOSE(3),
        /**
         * Manual
         */
        MANUAL(4);

        private int mValue;

        LOAD_TYPE(int value) {
            this.mValue = value;
        }

        public int getValue() {
            return this.mValue;
        }
    }

    private AdTimingManager() {
        mIaManagers = new HashMap<>();
        mIsManagers = new HashMap<>();
        mRvManagers = new HashMap<>();
        mIaListeners = new ConcurrentHashMap<>();
        mIsListeners = new ConcurrentHashMap<>();
        mRvListeners = new ConcurrentHashMap<>();
        mPreloadAdTypes = new ArrayList<>();
    }

    public static AdTimingManager getInstance() {
        return AMHolder.INSTANCE;
    }

    /**
     * The actual init method
     *
     * @param activity required param
     * @param appKey   required param, current app's identifier with AdTiming
     * @param types    Optional param, Ad types for preloading; preloads all if null
     */
    public void init(Activity activity, String appKey, InitCallback callback, AD_TYPE... types) {
        if (InitImp.isInit()) {
            setListeners();
            if (callback != null) {
                callback.onSuccess();
            }
            //checks for preloading and scheduled tasks
            anotherInitCalledAfterInitSuccess(types);
            return;
        } else if (InitImp.isInitRunning()) {
            pendingInit(callback);
        } else {
            pendingInit(callback);
            InitImp.init(activity, appKey, this);
        }

        //adds for use after initialization 
        if (types != null && types.length > 0) {
            mPreloadAdTypes.addAll(Arrays.asList(types));
        }
    }

    void pendingInit(InitCallback callback) {
        if (mInitCallbacks != null && callback != null) {
            mInitCallbacks.add(callback);
        }
    }

    public void onResume(Activity activity) {
        if (!mIaManagers.isEmpty()) {
            Set<Map.Entry<String, IaManager>> iaEntrys = mIaManagers.entrySet();
            for (Map.Entry<String, IaManager> iaManagerEntry : iaEntrys) {
                if (iaManagerEntry != null) {
                    iaManagerEntry.getValue().onResume(activity);
                }
            }
        }
        if (!mIsManagers.isEmpty()) {
            Set<Map.Entry<String, IsManager>> isEntrys = mIsManagers.entrySet();
            for (Map.Entry<String, IsManager> isManagerEntry : isEntrys) {
                if (isManagerEntry != null) {
                    isManagerEntry.getValue().onResume(activity);
                }
            }
        }
        if (!mRvManagers.isEmpty()) {
            Set<Map.Entry<String, RvManager>> rvEntrys = mRvManagers.entrySet();
            for (Map.Entry<String, RvManager> rvManagerEntry : rvEntrys) {
                if (rvManagerEntry != null) {
                    rvManagerEntry.getValue().onResume(activity);
                }
            }
        }
    }

    public void onPause(Activity activity) {
        if (!mIaManagers.isEmpty()) {
            Set<Map.Entry<String, IaManager>> iaEntrys = mIaManagers.entrySet();
            for (Map.Entry<String, IaManager> iaManagerEntry : iaEntrys) {
                if (iaManagerEntry != null) {
                    iaManagerEntry.getValue().onPause(activity);
                }
            }
        }
        if (!mIsManagers.isEmpty()) {
            Set<Map.Entry<String, IsManager>> isEntrys = mIsManagers.entrySet();
            for (Map.Entry<String, IsManager> isManagerEntry : isEntrys) {
                if (isManagerEntry != null) {
                    isManagerEntry.getValue().onPause(activity);
                }
            }
        }
        if (!mRvManagers.isEmpty()) {
            Set<Map.Entry<String, RvManager>> rvEntrys = mRvManagers.entrySet();
            for (Map.Entry<String, RvManager> rvManagerEntry : rvEntrys) {
                if (rvManagerEntry != null) {
                    rvManagerEntry.getValue().onPause(activity);
                }
            }
        }
    }

    public boolean isInitRunning() {
        return InitImp.isInitRunning();
    }

    public boolean isInit() {
        return InitImp.isInit();
    }

    public void setIAP(float iapCount, String currency) {
        IapImp.setIap(iapCount, currency);
    }

    public String getSDKVersion() {
        return Constants.SDK_V;
    }

    /**
     * Only one listener exists in the whole lifecycle
     *
     * @param placementId placementId
     * @param listener    InteractiveAd listener
     */
    public void setInteractiveAdListener(String placementId, InteractiveAdListener listener) {
        if (isInitRunning()) {
            if (mIaListeners == null) {
                mIaListeners = new ConcurrentHashMap<>();
            }
            mIaListeners.put(placementId, listener);
        } else {
            IaManager iaManager = getIaManager(placementId);
            if (iaManager != null) {
                iaManager.setInteractiveAdListener(listener);
            }
        }
    }

    /**
     * Only developers call this method
     */
    public void loadInteractiveAd(String placementId) {
        IaManager iaManager = getIaManager(placementId);
        if (iaManager != null) {
            iaManager.loadInteractiveAd();
        } else {
            if (mIaListeners.containsKey(placementId)) {
                mIaListeners.get(placementId).onInteractiveAdAvailabilityChanged(false);
            } else {
                AdLog.getSingleton().LogE(ErrorCode.MSG_LOAD_SDK_UNINITIALIZED);
            }
        }
        EventUploadManager.getInstance().uploadEvent(EventId.CALLED_LOAD);
    }

    public boolean isInteractiveAdReady(String placementId) {
        IaManager iaManager = getIaManager(placementId);
        if (iaManager != null) {
            boolean result = iaManager.isInteractiveAdReady();
            if (result) {
                EventUploadManager.getInstance().uploadEvent(EventId.CALLED_IS_READY_TRUE);
            } else {
                EventUploadManager.getInstance().uploadEvent(EventId.CALLED_IS_READY_FALSE);
            }
            return result;
        }
        EventUploadManager.getInstance().uploadEvent(EventId.CALLED_IS_READY_FALSE);
        return false;
    }

    /**
     * Shows the given scene's InteractiveAd, shows default if the scene does not exist
     *
     * @param scene       scene name
     * @param placementId placementId
     */
    public void showInteractiveAd(String placementId, String scene) {
        IaManager iaManager = getIaManager(placementId);
        if (iaManager != null) {
            iaManager.showInteractiveAd(scene);
        } else {
            if (mIaListeners.containsKey(placementId)) {
                mIaListeners.get(placementId).onInteractiveAdShowFailed(
                        new AdTimingError(ErrorCode.CODE_SHOW_SDK_UNINITIALIZED,
                                ErrorCode.MSG_SHOW_SDK_UNINITIALIZED, -1));
            } else {
                AdLog.getSingleton().LogE(ErrorCode.MSG_SHOW_SDK_UNINITIALIZED);
            }
        }
        EventUploadManager.getInstance().uploadEvent(EventId.CALLED_SHOW);
    }

    /**
     * only one listener will exist in the whole lifecycle
     *
     * @param listener InterstitialAd listener
     */
    public void setInterstitialAdListener(String placementId, InterstitialAdListener listener) {
        if (isInitRunning()) {
            if (mIsListeners == null) {
                mIsListeners = new ConcurrentHashMap<>();
            }
            mIsListeners.put(placementId, listener);
        } else {
            IsManager isManager = getIsManager(placementId);
            if (isManager != null) {
                isManager.setInterstitialAdListener(listener);
            }
        }
    }

    public void loadInterstitialAd(String placementId) {
        IsManager isManager = getIsManager(placementId);
        if (isManager != null) {
            isManager.loadInterstitialAd();
        } else {
            if (mIsListeners.containsKey(placementId)) {
                mIsListeners.get(placementId).onInterstitialAdAvailabilityChanged(false);
            } else {
                AdLog.getSingleton().LogE(ErrorCode.MSG_LOAD_SDK_UNINITIALIZED);
            }
        }
        EventUploadManager.getInstance().uploadEvent(EventId.CALLED_LOAD);
    }

    public boolean isInterstitialAdReady(String placementId) {
        IsManager isManager = getIsManager(placementId);
        if (isManager != null) {
            boolean result = isManager.isInterstitialAdReady();
            if (result) {
                EventUploadManager.getInstance().uploadEvent(EventId.CALLED_IS_READY_TRUE);
            } else {
                EventUploadManager.getInstance().uploadEvent(EventId.CALLED_IS_READY_FALSE);
            }
            return result;
        }
        EventUploadManager.getInstance().uploadEvent(EventId.CALLED_IS_READY_FALSE);
        return false;
    }

    /**
     * Shows the given scene's InterstitialAd, shows default if the scene does not exist
     *
     * @param scene       scene name
     * @param placementId placementId
     */
    public void showInterstitialAd(String placementId, String scene) {
        IsManager isManager = getIsManager(placementId);
        if (isManager != null) {
            isManager.showInterstitialAd(scene);
        } else {
            if (mIsListeners.containsKey(placementId)) {
                mIsListeners.get(placementId).onInterstitialAdShowFailed(
                        new AdTimingError(ErrorCode.CODE_SHOW_SDK_UNINITIALIZED,
                                ErrorCode.MSG_SHOW_SDK_UNINITIALIZED, -1));
            } else {
                AdLog.getSingleton().LogE(ErrorCode.MSG_SHOW_SDK_UNINITIALIZED);
            }
        }
        EventUploadManager.getInstance().uploadEvent(EventId.CALLED_SHOW);
    }

    /**
     * sets rewarded extId with placementId
     *
     * @param extId extId
     */
    public void setRewardedExtId(String placementId, String scene, String extId) {
        RvManager rvManager = getRvManager(placementId);
        if (rvManager != null) {
            rvManager.setRewardedExtId(scene, extId);
        }
    }

    /**
     * only one listener will exist in the whole lifecycle
     *
     * @param listener RewardedVideo listener
     */
    public void setRewardedVideoListener(String placementId, RewardedVideoListener listener) {
        if (isInitRunning()) {
            if (mRvListeners == null) {
                mRvListeners = new ConcurrentHashMap<>();
            }
            mRvListeners.put(placementId, listener);
        } else {
            RvManager rvManager = getRvManager(placementId);
            if (rvManager != null) {
                rvManager.setRewardedVideoListener(listener);
            }
        }
    }

    /**
     * Only developers call this method
     */
    public void loadRewardedVideo(String placementId) {
        RvManager rvManager = getRvManager(placementId);
        if (rvManager != null) {
            rvManager.loadRewardedVideo();
        } else {
            if (mRvListeners.containsKey(placementId)) {
                mRvListeners.get(placementId).onRewardedVideoAvailabilityChanged(false);
            } else {
                AdLog.getSingleton().LogE(ErrorCode.MSG_LOAD_SDK_UNINITIALIZED);
            }
        }
        EventUploadManager.getInstance().uploadEvent(EventId.CALLED_LOAD);
    }

    public boolean isRewardedVideoReady(String placementId) {
        RvManager rvManager = getRvManager(placementId);
        if (rvManager != null) {
            boolean result = rvManager.isRewardedVideoReady();
            if (result) {
                EventUploadManager.getInstance().uploadEvent(EventId.CALLED_IS_READY_TRUE);
            } else {
                EventUploadManager.getInstance().uploadEvent(EventId.CALLED_IS_READY_FALSE);
            }
            return result;
        }
        EventUploadManager.getInstance().uploadEvent(EventId.CALLED_IS_READY_FALSE);
        return false;
    }

    /**
     * Shows the given scene's RewardedVideo, shows default if the scene does not exist
     *
     * @param scene scene name
     */
    public void showRewardedVideo(String placementId, String scene) {
        RvManager rvManager = getRvManager(placementId);
        if (rvManager != null) {
            rvManager.showRewardedVideo(scene);
        } else {
            if (mRvListeners.containsKey(placementId)) {
                mRvListeners.get(placementId).onRewardedVideoAdShowFailed(
                        new AdTimingError(ErrorCode.CODE_SHOW_SDK_UNINITIALIZED,
                                ErrorCode.MSG_SHOW_SDK_UNINITIALIZED, -1));
            } else {
                AdLog.getSingleton().LogE(ErrorCode.MSG_SHOW_SDK_UNINITIALIZED);
            }
        }
        EventUploadManager.getInstance().uploadEvent(EventId.CALLED_SHOW);
    }

    @Override
    public void onSuccess() {
        initManagerWithDefaultPlacementId();
        setListeners();
        preloadAdWithAdType();
        if (mInitCallbacks != null) {
            for (InitCallback callback : mInitCallbacks) {
                if (callback == null) {
                    continue;
                }
                callback.onSuccess();
            }
            mInitCallbacks.clear();
        }
        startScheduleTaskWithPreloadType();
    }

    private void setListeners() {
        if (mIaListeners != null && !mIaListeners.isEmpty()) {
            Set<Map.Entry<String, InteractiveAdListener>> iaListenerEntrys = mIaListeners.entrySet();
            for (Map.Entry<String, InteractiveAdListener> iaListenerEntry : iaListenerEntrys) {
                if (iaListenerEntry != null) {
                    setInteractiveAdListener(iaListenerEntry.getKey(), iaListenerEntry.getValue());
                }
            }
            mIaListeners.clear();
        }
        if (mIsListeners != null && !mIsListeners.isEmpty()) {
            Set<Map.Entry<String, InterstitialAdListener>> isListenerEntrys = mIsListeners.entrySet();
            for (Map.Entry<String, InterstitialAdListener> isListenerEntry : isListenerEntrys) {
                if (isListenerEntry != null) {
                    setInterstitialAdListener(isListenerEntry.getKey(), isListenerEntry.getValue());
                }
            }
            mIsListeners.clear();
        }
        if (mRvListeners != null && !mRvListeners.isEmpty()) {
            Set<Map.Entry<String, RewardedVideoListener>> rvListenerEntrys = mRvListeners.entrySet();
            for (Map.Entry<String, RewardedVideoListener> rvListenerEntry : rvListenerEntrys) {
                if (rvListenerEntry != null) {
                    setRewardedVideoListener(rvListenerEntry.getKey(), rvListenerEntry.getValue());
                }
            }
            mRvListeners.clear();
        }
    }

    @Override
    public void onError(AdTimingError result) {
        clearCacheListeners();
        if (mInitCallbacks != null) {
            for (InitCallback callback : mInitCallbacks) {
                if (callback == null) {
                    AdLog.getSingleton().LogD(ErrorCode.ERROR_INIT_FAILED + " " + result);
                    continue;
                }
                callback.onError(result);
            }
            mInitCallbacks.clear();
        }
    }

    private void clearCacheListeners() {
        if (mRvListeners != null) {
            mRvListeners.clear();
        }
        if (mIaListeners != null) {
            mIaListeners.clear();
        }
        if (mIsListeners != null) {
            mIsListeners.clear();
        }
    }

    private IaManager getIaManager(String placementId) {
        if (!isInit()) {
            return null;
        }
        if (TextUtils.isEmpty(placementId)) {
            Placement placement = getPlacement("", Constants.INTERACTIVE);
            if (placement == null) {
                return null;
            }
            placementId = placement.getId();
        }
        if (!mIaManagers.containsKey(placementId)) {
            IaManager iaManager = new IaManager();
            iaManager.setCurrentPlacement(getPlacement(placementId, Constants.INTERACTIVE));
            mIaManagers.put(placementId, iaManager);
            return iaManager;
        }
        return mIaManagers.get(placementId);
    }

    private IsManager getIsManager(String placementId) {
        if (!isInit()) {
            return null;
        }
        if (TextUtils.isEmpty(placementId)) {
            Placement placement = getPlacement("", Constants.INTERSTITIAL);
            if (placement == null) {
                return null;
            }
            placementId = placement.getId();
        }
        if (!mIsManagers.containsKey(placementId)) {
            IsManager isManager = new IsManager();
            isManager.setCurrentPlacement(getPlacement(placementId, Constants.INTERSTITIAL));
            mIsManagers.put(placementId, isManager);
            return isManager;
        }
        return mIsManagers.get(placementId);
    }

    private RvManager getRvManager(String placementId) {
        if (!isInit()) {
            return null;
        }
        if (TextUtils.isEmpty(placementId)) {
            Placement placement = getPlacement("", Constants.VIDEO);
            if (placement == null) {
                return null;
            }
            placementId = placement.getId();
        }
        if (!mRvManagers.containsKey(placementId)) {
            RvManager rvManager = new RvManager();
            rvManager.setCurrentPlacement(getPlacement(placementId, Constants.VIDEO));
            mRvManagers.put(placementId, rvManager);
            return rvManager;
        }
        return mRvManagers.get(placementId);
    }

    private void initManagerWithDefaultPlacementId() {
        Config config = DataCache.getInstance().get("Config", Config.class);
        if (config == null) {
            return;
        }

        Map<String, Placement> placementMap = config.getPlacements();
        if (placementMap == null || placementMap.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Placement> placementEntry : placementMap.entrySet()) {
            if (placementEntry == null) {
                continue;
            }
            Placement placement = placementEntry.getValue();
            if (placement != null) {
                int adType = placement.getT();
                String placementId = placement.getId();
                switch (adType) {
                    case Constants.INTERACTIVE:
                        if (!mIaManagers.containsKey(placementId)) {
                            IaManager iaManager = new IaManager();
                            iaManager.setCurrentPlacement(placement);
                            mIaManagers.put(placementId, iaManager);
                        }
                        break;
                    case Constants.INTERSTITIAL:
                        if (!mIsManagers.containsKey(placementId)) {
                            IsManager isManager = new IsManager();
                            isManager.setCurrentPlacement(placement);
                            mIsManagers.put(placementId, isManager);
                        }
                        break;
                    case Constants.VIDEO:
                        if (!mRvManagers.containsKey(placementId)) {
                            RvManager rvManager = new RvManager();
                            rvManager.setCurrentPlacement(placement);
                            mRvManagers.put(placementId, rvManager);
                        }
                        break;
                    default:
                        break;

                }
            }
        }
    }

    /**
     * Called after init done, with different AD_TYPEs
     *
     * @param adTypes AD_TYPEs to be preloaded
     */
    private void anotherInitCalledAfterInitSuccess(AD_TYPE... adTypes) {
        DeveloperLog.LogD("anotherInitCalledAfterInitSuccess");
        if (adTypes == null) {
            return;
        }
        Config config = DataCache.getInstance().get("Config", Config.class);
        if (config == null) {
            DeveloperLog.LogD("anotherInitCalledAfterInitSuccess failed cause config empty");
            return;
        }
        Map<String, Placement> placementMap = config.getPlacements();
        if (placementMap == null || placementMap.isEmpty()) {
            DeveloperLog.LogD("anotherInitCalledAfterInitSuccess failed cause placementMap empty");
            return;
        }
        for (AD_TYPE adType : adTypes) {
            if (adType == AD_TYPE.REWARDED_VIDEO) {
                if (mDidRvInit.get()) {
                    return;
                }
                preloadRV(placementMap.entrySet());
                startScheduleRv();
            } else if (adType == AD_TYPE.INTERSTITIAL) {
                if (mDidIsInit.get()) {
                    return;
                }
                preloadIS(placementMap.entrySet());
                startScheduleIs();
            } else if (adType == AD_TYPE.INTERACTIVE) {
                if (mDidIaInit.get()) {
                    return;
                }
                preloadIA(placementMap.entrySet());
                startScheduleIa();
            }
        }
    }

    /**
     * Gets the 1st Placement for the asType if PlacementId is empty
     *
     * @param placementId
     */
    private Placement getPlacement(String placementId, int adType) {
        if (TextUtils.isEmpty(placementId)) {
            return PlacementUtils.getPlacement(adType);
        }
        return PlacementUtils.getPlacement(placementId);
    }

    private void preloadAdWithAdType() {
        DeveloperLog.LogD("preloadAdWithAdType");
        Config config = DataCache.getInstance().get("Config", Config.class);
        if (config == null) {
            DeveloperLog.LogD("preloadAdWithAdType failed cause config empty");
            return;
        }
        Map<String, Placement> placementMap = config.getPlacements();
        if (placementMap == null || placementMap.isEmpty()) {
            DeveloperLog.LogD("preloadAdWithAdType failed cause placementMap empty");
            return;
        }
        Set<Map.Entry<String, Placement>> placements = placementMap.entrySet();
        if (mPreloadAdTypes.isEmpty()) {
            DeveloperLog.LogD("preload all ad");
            preloadIA(placements);
            preloadIS(placements);
            preloadRV(placements);
        } else {
            for (AD_TYPE adType : mPreloadAdTypes) {
                if (adType == AD_TYPE.INTERACTIVE) {
                    preloadIA(placements);
                } else if (adType == AD_TYPE.INTERSTITIAL) {
                    preloadIS(placements);
                } else if (adType == AD_TYPE.REWARDED_VIDEO) {
                    preloadRV(placements);
                }
            }
        }
    }

    private void preloadIA(Set<Map.Entry<String, Placement>> placements) {
        mDidIaInit.set(true);
        for (Map.Entry<String, Placement> placementEntry : placements) {
            Placement placement = placementEntry.getValue();
            if (placement != null) {
                int type = placement.getT();
                if (type == Constants.INTERACTIVE) {
                    IaManager iaManager = getIaManager(placement.getId());
                    if (iaManager != null) {
                        DeveloperLog.LogD("preloadIA for placementId : " + placement.getId());
                        iaManager.loadAdWithAction(LOAD_TYPE.INIT);
                    }
                }
            }
        }
    }

    private void preloadIS(Set<Map.Entry<String, Placement>> placements) {
        mDidIsInit.set(true);
        for (Map.Entry<String, Placement> placementEntry : placements) {
            Placement placement = placementEntry.getValue();
            if (placement != null) {
                int type = placement.getT();
                if (type == Constants.INTERSTITIAL) {
                    IsManager isManager = getIsManager(placement.getId());
                    if (isManager != null) {
                        DeveloperLog.LogD("preloadIS for placementId : " + placement.getId());
                        isManager.loadAdWithAction(LOAD_TYPE.INIT);
                    }
                }
            }
        }
    }

    private void preloadRV(Set<Map.Entry<String, Placement>> placements) {
        DeveloperLog.LogD("preloadRV");
        mDidRvInit.set(true);
        for (Map.Entry<String, Placement> placementEntry : placements) {
            Placement placement = placementEntry.getValue();
            if (placement != null) {
                int type = placement.getT();
                if (type == Constants.VIDEO) {
                    RvManager rvManager = getRvManager(placement.getId());
                    if (rvManager != null) {
                        DeveloperLog.LogD("preloadRV for placementId : " + placement.getId());
                        rvManager.loadAdWithAction(LOAD_TYPE.INIT);
                    }
                }
            }
        }
    }

    private void startScheduleTaskWithPreloadType() {
        if (mPreloadAdTypes.isEmpty()) {
            startScheduleRv();
            startScheduleIs();
            startScheduleIa();
        } else {
            for (AD_TYPE adType : mPreloadAdTypes) {
                if (adType == AD_TYPE.REWARDED_VIDEO) {
                    startScheduleRv();
                } else if (adType == AD_TYPE.INTERSTITIAL) {
                    startScheduleIs();
                } else if (adType == AD_TYPE.INTERACTIVE) {
                    startScheduleIa();
                }
            }
        }
    }

    private void startScheduleRv() {
        if (!mRvManagers.isEmpty()) {
            Set<Map.Entry<String, RvManager>> rvEntrys = mRvManagers.entrySet();
            for (Map.Entry<String, RvManager> rvManagerEntry : rvEntrys) {
                if (rvManagerEntry != null) {
                    DeveloperLog.LogD("startScheduleRv for placementId : " + rvManagerEntry.getKey());
                    rvManagerEntry.getValue().initRewardedVideo();
                }
            }
        }
    }

    private void startScheduleIs() {
        if (!mIsManagers.isEmpty()) {
            Set<Map.Entry<String, IsManager>> isEntrys = mIsManagers.entrySet();
            for (Map.Entry<String, IsManager> isManagerEntry : isEntrys) {
                if (isManagerEntry != null) {
                    DeveloperLog.LogD("startScheduleIs for placementId : " + isManagerEntry.getKey());
                    isManagerEntry.getValue().initInterstitialAd();
                }
            }
        }
    }

    private void startScheduleIa() {
        if (!mIaManagers.isEmpty()) {
            Set<Map.Entry<String, IaManager>> iaEntrys = mIaManagers.entrySet();
            for (Map.Entry<String, IaManager> iaManagerEntry : iaEntrys) {
                if (iaManagerEntry != null) {
                    DeveloperLog.LogD("startScheduleIa for placementId : " + iaManagerEntry.getKey());
                    iaManagerEntry.getValue().initInteractiveAd();
                }
            }
        }
    }

    /**
     * For testing purpose: gets ads stock by placementid
     *
     * @param placementId placementId
     */
    public String getTotalInsById(String placementId, int adType) {
        if (adType == Constants.VIDEO) {
            RvManager rvManager = getRvManager(placementId);
            if (rvManager != null) {
                return rvManager.getTotalIns();
            }
        }
        if (adType == Constants.INTERSTITIAL) {
            IsManager isManager = getIsManager(placementId);
            if (isManager != null) {
                return isManager.getTotalIns();
            }
        }
        if (adType == Constants.INTERACTIVE) {
            IaManager iaManager = getIaManager(placementId);
            if (iaManager != null) {
                return iaManager.getTotalIns();
            }
        }


        return "stock is empty";
    }

    /**
     * For testing purpose: resets ads stock by placementid
     *
     * @param placementId placementId
     */
    public void resetStock(String placementId, int adType) {
        if (adType == Constants.VIDEO) {
            RvManager rvManager = getRvManager(placementId);
            if (rvManager != null) {
                rvManager.resetStock();
            }
        }
        if (adType == Constants.INTERSTITIAL) {
            IsManager isManager = getIsManager(placementId);
            if (isManager != null) {
                isManager.resetStock();
            }
        }
        if (adType == Constants.INTERACTIVE) {
            IaManager iaManager = getIaManager(placementId);
            if (iaManager != null) {
                iaManager.resetStock();
            }
        }
    }
}
