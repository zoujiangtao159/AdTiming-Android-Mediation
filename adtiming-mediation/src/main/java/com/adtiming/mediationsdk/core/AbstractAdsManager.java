// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.core;

import android.app.Activity;
import android.text.TextUtils;

import com.adtiming.mediationsdk.danmaku.Danmaku;
import com.adtiming.mediationsdk.danmaku.DanmakuLifecycle;
import com.adtiming.mediationsdk.mediation.CustomAdsAdapter;
import com.adtiming.mediationsdk.test.TestUtil;
import com.adtiming.mediationsdk.utils.AdLog;
import com.adtiming.mediationsdk.utils.AdRateUtil;
import com.adtiming.mediationsdk.utils.AdapterUtil;
import com.adtiming.mediationsdk.utils.Constants;
import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.HandlerUtil;
import com.adtiming.mediationsdk.utils.IOUtil;
import com.adtiming.mediationsdk.utils.JsonUtil;
import com.adtiming.mediationsdk.utils.LrReportUtil;
import com.adtiming.mediationsdk.utils.MediationRequest;
import com.adtiming.mediationsdk.utils.MediationUtil;
import com.adtiming.mediationsdk.utils.PlacementUtils;
import com.adtiming.mediationsdk.utils.SceneUtil;
import com.adtiming.mediationsdk.utils.WorkExecutor;
import com.adtiming.mediationsdk.utils.cache.DataCache;
import com.adtiming.mediationsdk.utils.constant.KeyConstants;
import com.adtiming.mediationsdk.utils.crash.CrashUtil;
import com.adtiming.mediationsdk.utils.device.DeviceUtil;
import com.adtiming.mediationsdk.utils.error.AdTimingError;
import com.adtiming.mediationsdk.utils.error.ErrorCode;
import com.adtiming.mediationsdk.utils.event.EventId;
import com.adtiming.mediationsdk.utils.event.EventUploadManager;
import com.adtiming.mediationsdk.utils.model.Placement;
import com.adtiming.mediationsdk.utils.model.PlacementInfo;
import com.adtiming.mediationsdk.utils.model.Scene;
import com.adtiming.mediationsdk.utils.request.network.Request;
import com.adtiming.mediationsdk.utils.request.network.Response;
import com.adtiming.mediationsdk.utils.request.network.util.NetworkChecker;
import com.adtiming.mediationsdk.InitCallback;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 
 */
public abstract class AbstractAdsManager implements InitCallback, Request.OnRequestCallback {
    protected Activity mActivity;
    protected Placement mPlacement;
    protected ListenerWrapper mListenerWrapper;
    protected boolean isInShowingProgress;
    protected Scene mScene;

    private AdTimingManager.LOAD_TYPE mLoadType;
    //Adapters to be loaded
    private CopyOnWriteArrayList<Instance> mTotalIns;
    //
    private int mCacheSize;
    //
    private boolean isInLoadingProgress;
    //
    private boolean isManualTriggered;

    //last callback's status
    private AtomicBoolean mLastAvailability = new AtomicBoolean(false);
    private AtomicBoolean mDidScheduleTaskStarted = new AtomicBoolean(false);

    public AbstractAdsManager() {
        mTotalIns = new CopyOnWriteArrayList<>();
        mListenerWrapper = new ListenerWrapper();
    }

    /**
     * Returns placement info for the ad type
     *
     * @return placement info
     */
    protected abstract PlacementInfo getPlacementInfo();

    /**
     * Instance initialization method
     */
    protected abstract void initInsAndSendEvent(Instance instance);

    /**
     * Checks if an instance is available
     *
     * @param instance the instance to be checked
     * @return whether the instance's adapter is available
     */
    protected abstract boolean isInsAvailable(Instance instance);

    /**
     * Instance shows ads
     *
     * @param instance the instance to show ads
     */
    protected abstract void insShow(Instance instance);

    /**
     * Instance loads ads
     *
     * @param instance the instance to load ads
     */
    protected abstract void insLoad(Instance instance);

    /**
     * Called when availability changes
     *
     * @param available availability
     */
    protected abstract void onAvailabilityChanged(boolean available);

    /**
     * Ends this loading
     */
    protected void callbackLoadError(AdTimingError error) {
        isInLoadingProgress = false;
        isManualTriggered = false;
        TestUtil.getInstance().notifyLoadFailed(mPlacement,
                "load error: " + error);
    }

    protected void callbackAvailableOnManual() {
    }

    protected void callbackShowError(AdTimingError error) {
        isInShowingProgress = false;
    }

    protected void callbackCappedError(Instance instance) {
    }

    protected void callbackAdClosed() {
    }

    /**
     * For an instance to load ads
     */
    protected void loadInsAndSendEvent(Instance instance) {
        TestUtil.getInstance().notifyInsLoad(mPlacement != null ? mPlacement.getId() : "", instance);
        if (AdRateUtil.shouldBlockInstance(mPlacement != null ? mPlacement.getId() : "" + instance.getKey(), instance)) {
            DeveloperLog.LogD("instance :" + instance.getKey() + " is blocked");
            callbackCappedError(instance);
            return;
        }
        LrReportUtil.iLoadReport(instance.getPlacementId(), mLoadType, instance);
        insLoad(instance);
        TestUtil.getInstance().notifyInsReadyInLoading(instance.getPlacementId(), instance);
    }

    public void onResume(Activity activity) {
        if (activity != null) {
            mActivity = activity;
            if (mTotalIns != null && !mTotalIns.isEmpty()) {
                for (Instance in : mTotalIns) {
                    in.onResume(activity);
                }
            }
        }
    }

    public void onPause(Activity activity) {
        if (activity != null) {
            mActivity = activity;
            if (mTotalIns != null && !mTotalIns.isEmpty()) {
                for (Instance in : mTotalIns) {
                    in.onPause(activity);
                }
            }
        }
    }

    protected void setCurrentPlacement(Placement placement) {
        if (placement != null) {
            mPlacement = placement;
            mCacheSize = placement.getCs();
            TestUtil.getInstance().notifyPlacementConfig(placement);
        }
    }

    /**
     * before load starts, checks: init--->frequency control--->show in progress--->trigger type
     * When manuallly trigged, first checks available ads, and replenishs if necessary before checking if loading is in progress
     * Tiggers other than Manual are automatically called by the SDK,
     *
     * @param type load trigged by: Manual,Init,AdClose,Interval
     */
    protected void loadAdWithAction(AdTimingManager.LOAD_TYPE type) {
        DeveloperLog.LogD("loadAdWithAction : " + mPlacement + " action: " + type.toString());

        //if load is manually triggered
        if (type == AdTimingManager.LOAD_TYPE.MANUAL) {
            isManualTriggered = true;
            EventUploadManager.getInstance().uploadEvent(EventId.LOAD);
            //only checks ScheduleTask when manuallly trigged
            checkScheduleTaskStarted();
        } else {
            String pid = mPlacement != null ? mPlacement.getId() : "";
            EventUploadManager.getInstance().uploadEvent(EventId.ATTEMPT_TO_BRING_NEW_FEED,
                    PlacementUtils.placementEventParams(pid));
        }

        //returns if load can't start
        if (!checkLoadAvailable()) {
            return;
        }

        //does nothing if Showing in Progress
        if (isInShowingProgress) {
            DeveloperLog.LogD("loadAdWithAction: " + mPlacement + ", type:"
                    + type.toString() + " stopped," +
                    " cause current is in showing progress");
            return;
        }

        int availableCount = getCurrentAvailableAdsCount();

        //When manuallly trigged, first checks available ads in cache
        if (isManualTriggered) {
            if (hasAvailableCache() && shouldNotifyAvailableChanged(true)) {
                callbackAvailableOnManual();
                EventUploadManager.getInstance().uploadEvent(EventId.AVAILABLE_FROM_CACHE);
            }
        }

        //to replenish?
        if (availableCount < mCacheSize) {
            if (isInLoadingProgress) {
                DeveloperLog.LogD("loadAdWithAction: " + mPlacement + ", type:"
                        + type.toString() + " stopped," +
                        " because current is in loading progress");
                return;
            }
            delayLoad(type);
        } else {
            DeveloperLog.LogD("cache is full, cancel this request");
        }
    }

    protected void showAd(String scene) {
        AdTimingError error = checkShowAvailable();
        if (error != null) {
            callbackShowError(error);
            TestUtil.getInstance().notifyShowFailed(mPlacement != null ? mPlacement.getId() : ""
                    , "error before show " + mPlacement + ", error:" + error);
            return;
        }
        mScene = SceneUtil.getScene(mPlacement, scene);
        if (AdRateUtil.shouldBlockScene(mPlacement.getId(), mScene)) {
            EventUploadManager.getInstance().uploadEvent(EventId.SCENE_CAPPED,
                    SceneUtil.sceneCappedReport(mPlacement.getId(), scene));
            error = new AdTimingError(ErrorCode.CODE_SHOW_SCENE_CAPPED
                    , ErrorCode.MSG_SHOW_SCENE_CAPPED, -1);
            AdLog.getSingleton().LogE(error.toString());
            DeveloperLog.LogE(error.toString());
            callbackShowError(error);
            EventUploadManager.getInstance().uploadEvent(EventId.CALLBACK_SCENE_CAPPED,
                    SceneUtil.sceneCappedReport(mPlacement.getId(), scene));
            TestUtil.getInstance().notifyShowFailed(mPlacement.getId(), "block scene when show " + mPlacement);
            return;
        }
        for (Instance in : mTotalIns) {
            if (!isInsAvailable(in)) {
                continue;
            }
            //reserved for adt
            if (in.getMediationId() == 0 && mScene != null) {
                DataCache.getInstance().setMEM(mPlacement.getId() + "_sceneId", mScene.getId());
            }
            insShow(in);
            return;
        }
        error = new AdTimingError(ErrorCode.CODE_SHOW_NO_AD_READY
                , ErrorCode.MSG_SHOW_NO_AD_READY, -1);
        AdLog.getSingleton().LogE(error.toString());
        DeveloperLog.LogE(error.toString());
        callbackShowError(error);
        TestUtil.getInstance().notifyShowFailed(mPlacement.getId(), "no ad ready when show " + mPlacement);
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_LOAD_NO_FILL);
    }

    protected boolean isPlacementAvailable() {
        if (!checkReady()) {
            return false;
        }
        if (mTotalIns.isEmpty()) {
            return false;
        }
        for (Instance in : mTotalIns) {
            if (!isInsAvailable(in)) {
                continue;
            }
            return true;
        }
        return false;
    }

    protected boolean hasAvailableCache() {
        return getCurrentAvailableAdsCount() > 0;
    }

    /**
     * schedules Load Ad Task
     */
    protected void scheduleLoadAdTask() {
        EventUploadManager.getInstance().uploadEvent(EventId.REFRESH_INTERVAL);
        if (mPlacement == null || mPlacement.getRf() <= 0) {
            return;
        }
        WorkExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                mDidScheduleTaskStarted.set(true);
                loadAdWithAction(AdTimingManager.LOAD_TYPE.INTERVAL);
            }
        }, mPlacement.getRf(), mPlacement.getRf(), TimeUnit.SECONDS);
    }

    /**
     * Notifies availability change when manually trigged, or cache availability changed
     * 
     */
    protected boolean shouldNotifyAvailableChanged(boolean available) {
        if (isInShowingProgress) {
            DeveloperLog.LogD("shouldNotifyAvailableChanged : " + false + " because current is in showing");
            return false;
        }
        if ((isManualTriggered || mLastAvailability.get() != available)) {
            DeveloperLog.LogD("shouldNotifyAvailableChanged for placement: " + mPlacement + " " + true);
            isManualTriggered = false;
            mLastAvailability.set(available);
            return true;
        }
        DeveloperLog.LogD("shouldNotifyAvailableChanged for placement : " + mPlacement + " " + false);
        return false;
    }

    protected void receivedEvents(String event, Instance baseInstance) {
        try {
            JSONObject jsonObject = new JSONObject(event);
            JsonUtil.put(jsonObject, "pid", baseInstance.getPlacementId());
            JsonUtil.put(jsonObject, "iid", baseInstance.getId());
            JsonUtil.put(jsonObject, "mid", baseInstance.getMediationId());
            if (baseInstance.getAdapter() != null) {
                JsonUtil.put(jsonObject, "adapterv", baseInstance.getAdapter().getAdapterVersion());
                JsonUtil.put(jsonObject, "msdkv", baseInstance.getAdapter().getMediationVersion());
            }
            JsonUtil.put(jsonObject, "priority", baseInstance.getIndex());
            if (mPlacement != null) {
                JsonUtil.put(jsonObject, "cs", mPlacement.getCs());
            }
            EventUploadManager.getInstance().uploadEvent(jsonObject);
        } catch (Exception e) {
            CrashUtil.getSingleton().saveException(e);
            DeveloperLog.LogD("onReceivedEvents : ", e);
        }
    }

    protected void onInsInitFailed(Instance instance) {
        if (shouldFinishLoad()) {
            boolean hasCache = hasAvailableCache();
            if (shouldNotifyAvailableChanged(hasCache)) {
                onAvailabilityChanged(hasCache);
            }
        } else {
            initOrFetchNextAdapter();
        }
        //FixMe
        TestUtil.getInstance().notifyInsFailed(instance.getPlacementId(), instance);
    }

    protected synchronized void onInsReady(final Instance instance) {
        //FixMe
        TestUtil.getInstance().notifyInsReady(instance.getPlacementId(), instance);
        LrReportUtil.iReadyReport(instance.getPlacementId(), mLoadType, instance);
        if (!shouldFinishLoad()) {
            initOrFetchNextAdapter();
        }
        if (shouldNotifyAvailableChanged(true)) {
            if (mPlacement.getT() != Constants.INTERACTIVE) {
                LrReportUtil.aReadyReport(instance.getPlacementId(), mLoadType);
            }
            onAvailabilityChanged(true);
        }
    }

    protected synchronized void onInsLoadFailed(Instance instance) {
        if (shouldFinishLoad()) {
            DeveloperLog.LogD("onInsLoadFailed shouldFinishLoad ");
            boolean hasCache = hasAvailableCache();
            if (shouldNotifyAvailableChanged(hasCache)) {
                DeveloperLog.LogD("onInsLoadFailed shouldFinishLoad shouldNotifyAvailableChanged " + hasCache);
                onAvailabilityChanged(hasCache);
                if (!hasCache) {
                    EventUploadManager.getInstance().uploadEvent(EventId.NO_MORE_OFFERS);
                }
            }
        } else {
            initOrFetchNextAdapter();
        }
        //FixMe
        TestUtil.getInstance().notifyInsFailed(instance.getPlacementId(), instance);
    }

    protected void onInsOpen(final Instance instance) {
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_SHOW_SUCCESS);
        if (mScene != null) {
            AdRateUtil.onSceneShowed(mPlacement.getId(), mScene);
            LrReportUtil.iImpressionReport(instance.getPlacementId(), mScene.getId(), instance);
        }
        //if availability changed from false to true
        if (shouldNotifyAvailableChanged(false)) {
            onAvailabilityChanged(false);
        }
        isInShowingProgress = true;

        if (mPlacement != null && (mPlacement.getT() == Constants.VIDEO || mPlacement.getT() == Constants.INTERSTITIAL)) {
            final int mediationId = instance.getMediationId();
            if (mediationId == 0) {
                String packageName = DataCache.getInstance().get(instance.getPlacementId(), String.class);
                DeveloperLog.LogD("PackageName:" + instance.getKey() + ":" + packageName);
                if (mPlacement.getDm() == 1) {
                    Danmaku.getSingleton().show(instance.getPlacementId(), mediationId, packageName);
                }
            }
        }
        //FixMe
        TestUtil.getInstance().notifyInsShow(instance.getPlacementId(), instance);
    }

    protected void onInsClick(Instance instance) {
        if (mScene != null) {
            LrReportUtil.iClickReport(instance.getPlacementId(), mScene.getId(), instance);
        }
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_CLICK_DOWNLOAD_NOW);
    }

    protected void onInsClose() {
        isInShowingProgress = false;
        if (mPlacement != null && (mPlacement.getT() == Constants.VIDEO || mPlacement.getT() == Constants.INTERSTITIAL)) {
            Danmaku.getSingleton().hide();
        }
        callbackAdClosed();
        boolean hasCache = hasAvailableCache();
        if (shouldNotifyAvailableChanged(hasCache)) {
            onAvailabilityChanged(hasCache);
        }
        checkShouldLoadsWhenClose();
    }

    protected void onInsCapped(Instance instance) {
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_CAPPED, instance.buildReportData());
        if (shouldFinishLoad()) {
            boolean hasCache = hasAvailableCache();
            if (shouldNotifyAvailableChanged(hasCache)) {
                onAvailabilityChanged(hasCache);
            }
        } else {
            initOrFetchNextAdapter();
        }
    }

    @Override
    public void onSuccess() {
        //only trigged by manual 
        delayLoad(AdTimingManager.LOAD_TYPE.MANUAL);
    }

    @Override
    public void onError(AdTimingError result) {
        //
        callbackLoadError(result);
    }

    @Override
    public void onRequestSuccess(Response response) {
        try {
            if (response == null || response.code() != HttpURLConnection.HTTP_OK) {
                AdTimingError error = new AdTimingError(ErrorCode.CODE_LOAD_SERVER_ERROR
                        , ErrorCode.MSG_LOAD_SERVER_ERROR, ErrorCode.CODE_INTERNAL_SERVER_ERROR);
                AdLog.getSingleton().LogE(error.toString() + ", request cl http code:"
                        + (response != null ? response.code() : "null") + ", placement:" + mPlacement);
                DeveloperLog.LogE(error.toString() + ", request cl http code:"
                        + (response != null ? response.code() : "null") + ", placement:" + mPlacement);
                callbackLoadError(error);
                return;
            }

            //when not trigged by init, checks cache before aReady reporting
            if (mLoadType != AdTimingManager.LOAD_TYPE.INIT) {
                if (getCurrentAvailableAdsCount() > 0 && mPlacement.getT() != Constants.INTERACTIVE) {
                    LrReportUtil.aReadyReport(mPlacement.getId(), mLoadType);
                }
            }

            JSONObject clInfo = new JSONObject(response.body().string());
            List<Instance> tmp = MediationUtil.getAbsIns(clInfo, mPlacement);
            if (tmp == null || tmp.isEmpty()) {
                List<Instance> lastAvailableIns = getInsWithStatus(Instance.MEDIATION_STATE.AVAILABLE);
                if (lastAvailableIns == null || lastAvailableIns.isEmpty()) {
                    AdTimingError error = new AdTimingError(ErrorCode.CODE_LOAD_SERVER_ERROR
                            , ErrorCode.MSG_LOAD_SERVER_ERROR, ErrorCode.CODE_INTERNAL_SERVER_ERROR);
                    AdLog.getSingleton().LogE(error.toString() + ", tmp:" + tmp + ", last:" + lastAvailableIns);
                    DeveloperLog.LogE(error.toString() + ", tmp:" + tmp + ", last:" + lastAvailableIns);
                    callbackLoadError(error);
                } else {
                    DeveloperLog.LogD("request cl success, but ins[] is empty, but has history");
                    isInLoadingProgress = false;
                }
            } else {
                List<Instance> lastAvailableIns = getInsWithStatus(Instance.MEDIATION_STATE.AVAILABLE);
                if (!lastAvailableIns.isEmpty()) {
                    reOrderIns(lastAvailableIns, tmp);
                }
                mTotalIns.clear();
                mTotalIns.addAll(tmp);
                resetInsStateOnClResponse();
                DeveloperLog.LogD("TotalIns is : " + mTotalIns.toString());
                int availableCount = instanceCount(Instance.MEDIATION_STATE.AVAILABLE);
                reSizeCacheSize();
                DeveloperLog.LogD("after cl, cache size is : " + mCacheSize);
                //if availableCount == mCacheSize, do not load any new instance
                if (availableCount == mCacheSize) {
                    DeveloperLog.LogD("no new ins should be loaded, current load progress finishes");
                    isInLoadingProgress = false;
                } else {
                    doLoadOnUiThread();
                }
            }
        } catch (Exception e) {
            AdTimingError error = new AdTimingError(ErrorCode.CODE_LOAD_SERVER_ERROR
                    , ErrorCode.MSG_LOAD_SERVER_ERROR, ErrorCode.CODE_INTERNAL_UNKNOWN_OTHER);
            AdLog.getSingleton().LogE(error.toString() + ", failed when parse");
            DeveloperLog.LogE(error.toString() + ", request cl success, but failed when parse response" +
                    ", Placement:" + mPlacement, e);
            CrashUtil.getSingleton().saveException(e);
            callbackLoadError(error);
        } finally {
            IOUtil.closeQuietly(response);
        }
    }

    @Override
    public void onRequestFailed(String error) {
        AdTimingError errorResult = new AdTimingError(ErrorCode.CODE_LOAD_SERVER_ERROR
                , ErrorCode.MSG_LOAD_SERVER_ERROR, ErrorCode.CODE_INTERNAL_SERVER_FAILED);
        AdLog.getSingleton().LogE(errorResult.toString() + ", " + error);
        DeveloperLog.LogD(errorResult.toString() + ", request cl failed : " + errorResult + ", error" + error);
        callbackLoadError(errorResult);
    }

    private boolean isInitRunningOrNotInited() {
        //chechs if init in progress
        if (AdTimingManager.getInstance().isInitRunning()) {
            AdTimingManager.getInstance().pendingInit(this);
            return true;
        }

        //init OK?
        if (!AdTimingManager.getInstance().isInit()) {
            DeveloperLog.LogD("Sdk hasn't been inited or init failed");
            reInitSDK();
            return true;
        }
        return false;
    }

    private void reInitSDK() {
        AdTimingError error;
        if (!checkActRef()) {
            error = new AdTimingError(ErrorCode.CODE_LOAD_SDK_UNINITIALIZED
                    , ErrorCode.MSG_LOAD_SDK_UNINITIALIZED, ErrorCode.CODE_INTERNAL_UNKNOWN_ACTIVITY);
            AdLog.getSingleton().LogE(error.toString());
            DeveloperLog.LogE(error.toString());
            callbackLoadError(error);
            return;
        }
        if (DataCache.getInstance().containsKey(KeyConstants.Storage.KEY_APP_KEY)) {
            String appKey = DataCache.getInstance().get(KeyConstants.Storage.KEY_APP_KEY, String.class);
            if (TextUtils.isEmpty(appKey)) {
                error = new AdTimingError(ErrorCode.CODE_LOAD_SDK_UNINITIALIZED
                        , ErrorCode.MSG_LOAD_SDK_UNINITIALIZED, ErrorCode.CODE_INTERNAL_REQUEST_APPKEY);
                AdLog.getSingleton().LogE(error.toString());
                DeveloperLog.LogE(error.toString());
                callbackLoadError(error);
                return;
            }
            AdTimingManager.getInstance().init(mActivity, appKey, this);
        } else {
            error = new AdTimingError(ErrorCode.CODE_LOAD_SDK_UNINITIALIZED
                    , ErrorCode.MSG_LOAD_SDK_UNINITIALIZED, ErrorCode.CODE_INTERNAL_REQUEST_APPKEY);
            AdLog.getSingleton().LogE(error.toString());
            DeveloperLog.LogE(error.toString());
            callbackLoadError(error);
        }
    }

    private void delayLoad(AdTimingManager.LOAD_TYPE type) {
        try {
            isInLoadingProgress = true;
            mLoadType = type;
            MediationRequest.cLRequest(getPlacementInfo(), type, this);
        } catch (Exception e) {
            DeveloperLog.LogD("load ad error", e);
            CrashUtil.getSingleton().saveException(e);
        }
    }

    /**
     * load can start if
     * 1.Activity available
     * 2.network available
     */
    private boolean checkLoadAvailable() {
        //activity available?
        if (!checkActRef()) {
            AdTimingError error = new AdTimingError(ErrorCode.CODE_LOAD_INVALID_REQUEST
                    , ErrorCode.MSG_LOAD_INVALID_REQUEST, ErrorCode.CODE_INTERNAL_UNKNOWN_ACTIVITY);
            AdLog.getSingleton().LogE(error.toString());
            DeveloperLog.LogE(error.toString() + "load ad but activity is not available");
            callbackLoadError(error);
            EventUploadManager.getInstance().uploadEvent(EventId.LOAD_BLOCKED);
            return false;
        }

        //network available?
        if (!NetworkChecker.isAvailable(mActivity)) {
            AdTimingError error = new AdTimingError(ErrorCode.CODE_LOAD_NETWORK_ERROR
                    , ErrorCode.MSG_LOAD_NETWORK_ERROR, -1);
            AdLog.getSingleton().LogE(error.toString());
            DeveloperLog.LogE("load ad network not available");
            callbackLoadError(error);
            EventUploadManager.getInstance().uploadEvent(EventId.LOAD_BLOCKED);
            return false;
        }

        if (isInitRunningOrNotInited()) {
            AdTimingError error = new AdTimingError(ErrorCode.CODE_LOAD_SDK_UNINITIALIZED
                    , ErrorCode.MSG_LOAD_SDK_UNINITIALIZED, -1);
            AdLog.getSingleton().LogE(error.toString());
            DeveloperLog.LogE(error.toString() + ", load ad pause, cause SDK hasn't been init or init is running, reInit sdk for result");
            callbackLoadError(error);
            return false;
        }

        if (mPlacement == null) {
            AdTimingError error = new AdTimingError(ErrorCode.CODE_LOAD_INVALID_REQUEST
                    , ErrorCode.MSG_LOAD_INVALID_REQUEST, ErrorCode.CODE_INTERNAL_REQUEST_PLACEMENTID);
            AdLog.getSingleton().LogE(error.toString());
            DeveloperLog.LogE(error.toString() + ", placement is null");
            callbackLoadError(error);
            EventUploadManager.getInstance().uploadEvent(EventId.LOAD_BLOCKED);
            return false;
        }

        if (AdRateUtil.shouldBlockPlacement(mPlacement.getId(), mPlacement.getFrequencyInterval())) {
            AdTimingError error = new AdTimingError(ErrorCode.CODE_LOAD_CAPPED
                    , ErrorCode.MSG_LOAD_CAPPED, -1);
            AdLog.getSingleton().LogE(error.toString() + ", placement:" + mPlacement);
            DeveloperLog.LogD(error.toString() + ", Placement :" + mPlacement.getId() + " is blocked");
            callbackLoadError(error);
            EventUploadManager.getInstance().uploadEvent(EventId.PLACEMENT_CAPPED, placementCappedReport());
            return false;
        }
        return true;
    }

    /**
     * 
     */
    private void checkScheduleTaskStarted() {
        if (!mDidScheduleTaskStarted.get()) {
            scheduleLoadAdTask();
        }
    }

    /**
     * 
     */
    private void doLoadOnUiThread() {
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                resetFields();
                initOrFetchNextAdapter();
            }
        });
    }

    /**
     * 
     *
     * @return limit of loadable instances
     */
    private int getLoadLimit() {
        //compares with server issued max concurrent number
        return Math.min(mPlacement.getMpc(), mCacheSize - getCurrentAvailableAdsCount());
//        return 2;
    }

    private synchronized List<Instance> getInsWithStatus(Instance.MEDIATION_STATE... states) {
        if (mTotalIns == null) {
            return Collections.emptyList();
        }

        List<Instance> instanceList = new ArrayList<>();
        for (Instance in : mTotalIns) {
            for (Instance.MEDIATION_STATE state : states) {
                if (in.getMediationState() == state) {
                    instanceList.add(in);
                }
            }
        }
        return instanceList;
    }

    private synchronized CustomAdsAdapter startAdapter(Instance instance) {

        try {
            CustomAdsAdapter customAdsAdapter = AdapterUtil.getCustomAdsAdapter(instance.getMediationId());
            if (customAdsAdapter == null) {
                return null;
            } else {
                instance.setAdapter(customAdsAdapter);
                return customAdsAdapter;
            }
        } catch (Throwable var5) {
            return null;
        }
    }

    /**
     * Adds last instances not in the new instances to the end of the new
     *
     * @param lastIns last available instances
     * @param newIns  new instances
     */
    private void reOrderIns(List<Instance> lastIns, List<Instance> newIns) {
        for (Instance ins : lastIns) {
            if (newIns.contains(ins)) {
                continue;
            }
            ins.setIndex(newIns.size() - 1);
            newIns.add(ins);
        }
    }

    /**
     * 
     *
     * @return Current Available Ads Count
     */
    private int getCurrentAvailableAdsCount() {
        if (mTotalIns != null && !mTotalIns.isEmpty()) {
            return instanceCount(Instance.MEDIATION_STATE.AVAILABLE);
        }
        return 0;
    }

    /**
     * Resets instance's state when: init failed, load failed, Capped
     */
    private void resetInsStateOnClResponse() {
        if (mTotalIns.isEmpty()) {
            return;
        }

        for (Instance in : mTotalIns) {
            Instance.MEDIATION_STATE state = in.getMediationState();
            if (state == Instance.MEDIATION_STATE.INIT_FAILED) {
                in.setMediationState(Instance.MEDIATION_STATE.NOT_INITIATED);
            } else if (state == Instance.MEDIATION_STATE.LOAD_FAILED ||
                    state == Instance.MEDIATION_STATE.CAPPED) {
                in.setMediationState(Instance.MEDIATION_STATE.NOT_AVAILABLE);
            } else if (state == Instance.MEDIATION_STATE.NOT_AVAILABLE) {
                in.setObject(null);
                in.setStart(0);
            }
        }
    }

    /**
     * Inits an adapter and loads. Skips if already in progress
     */
    private synchronized void initOrFetchNextAdapter() {
        int canLoadCount = 0;
        for (Instance instance : mTotalIns) {
            Instance.MEDIATION_STATE state = instance.getMediationState();
            if (state == Instance.MEDIATION_STATE.INIT_PENDING ||
                    state == Instance.MEDIATION_STATE.LOAD_PENDING) {
                ++canLoadCount;
            } else if (state == Instance.MEDIATION_STATE.NOT_INITIATED) {
                //inits first if not
                CustomAdsAdapter adsAdapter = startAdapter(instance);
                if (adsAdapter == null) {
                    instance.setMediationState(Instance.MEDIATION_STATE.INIT_FAILED);
                } else {
                    ++canLoadCount;
                    initInsAndSendEvent(instance);
                    TestUtil.getInstance().notifyInsConfig(mPlacement != null ? mPlacement.getId() : ""
                            , instance, getLoadLimit(), getCurrentAvailableAdsCount(), getLoadCount());
                }
            } else if (state == Instance.MEDIATION_STATE.INITIATED
                    || state == Instance.MEDIATION_STATE.NOT_AVAILABLE) {
                ++canLoadCount;
                this.loadInsAndSendEvent(instance);
            }

            if (canLoadCount >= getLoadLimit()) {
                return;
            }
        }
        //
        if (canLoadCount == 0) {
            AdTimingError error = new AdTimingError(ErrorCode.CODE_LOAD_NO_AVAILABLE_AD
                    , ErrorCode.MSG_LOAD_NO_AVAILABLE_AD, -1);
            AdLog.getSingleton().LogE(error.toString());
            DeveloperLog.LogE(error.toString());
            callbackLoadError(error);
        }
    }

    /**
     * Finishes load when ads count suffices or all instances have been loaded: sum of ready, initFailed, loadFailed, Capped
     *
     * @return should finish load or not
     */
    private boolean shouldFinishLoad() {
        int readyCount = getCurrentAvailableAdsCount();
        int allLoadedCount = instanceCount(Instance.MEDIATION_STATE.AVAILABLE,
                Instance.MEDIATION_STATE.INIT_FAILED, Instance.MEDIATION_STATE.LOAD_FAILED,
                Instance.MEDIATION_STATE.CAPPED);
        if (readyCount >= mCacheSize || allLoadedCount == mTotalIns.size()) {
            DeveloperLog.LogD("full of cache or loaded all ins, current load is finished : " + getCurrentAvailableAdsCount());
            isInLoadingProgress = false;
            return true;
        }
        return false;
    }

    /**
     * Called at ads close
     */
    private void checkShouldLoadsWhenClose() {
        loadAdWithAction(AdTimingManager.LOAD_TYPE.CLOSE);
    }

    private int instanceCount(Instance.MEDIATION_STATE... states) {
        return getInsWithStatus(states).size();
    }

    private JSONObject placementCappedReport() {
        JSONObject jsonObject = new JSONObject();
        JsonUtil.put(jsonObject, "pid", mPlacement.getId());
        return jsonObject;
    }

    /**
     * 
     *
     * @return activity's availability
     */
    private boolean checkActRef() {
        if (!DeviceUtil.isActivityAvailable(mActivity)) {
            Activity activity = DanmakuLifecycle.getInstance().getActivity();
            if (activity == null) {
                return false;
            }
            mActivity = activity;
        }
        return true;
    }

    /**
     * showing is available if
     * 1.Activity is available
     * 2.init finished
     * 3.placement isn't null
     */
    private AdTimingError checkShowAvailable() {
        AdTimingError error = null;
        if (isInShowingProgress) {
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_SHOW_FAILED);
            error = new AdTimingError(-1, null, -1);
            DeveloperLog.LogE("show ad failed,current is showing");
        }
        //Activity is available?
        if (!checkActRef()) {
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_SHOW_FAILED);
            error = new AdTimingError(ErrorCode.CODE_SHOW_UNKNOWN_INTERNAL_ERROR
                    , ErrorCode.MSG_SHOW_UNKNOWN_INTERNAL_ERROR, ErrorCode.CODE_INTERNAL_UNKNOWN_ACTIVITY);
            AdLog.getSingleton().LogE(error.toString());
            DeveloperLog.LogE(error.toString() + "show ad but activity is not available");
        }

        if (isInitRunningOrNotInited()) {
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_SHOW_FAILED);
            error = new AdTimingError(ErrorCode.CODE_SHOW_SDK_UNINITIALIZED
                    , ErrorCode.MSG_SHOW_SDK_UNINITIALIZED, -1);
            AdLog.getSingleton().LogE(error.toString());
            DeveloperLog.LogE(error.toString() + ",  show ad pause, cause SDK hasn't been init or init is running, reInit sdk for result");
        }

        if (mPlacement == null) {
            DeveloperLog.LogD("placement is null");
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_SHOW_FAILED);
            error = new AdTimingError(ErrorCode.CODE_SHOW_INVALID_ARGUMENT
                    , ErrorCode.MSG_SHOW_INVALID_ARGUMENT, ErrorCode.CODE_INTERNAL_REQUEST_PLACEMENTID);
            AdLog.getSingleton().LogE(error.toString());
            DeveloperLog.LogE(error.toString() + ", placement is null");
        }
        return error;
    }

    /**
     * 1st-step check for isReady:
     * 1.init finished?
     * 2.placement == null?
     */
    private boolean checkReady() {
        if (isInShowingProgress) {
            DeveloperLog.LogD("checkReady isInShowingProgress");
            return false;
        }
        if (isInitRunningOrNotInited()) {
            DeveloperLog.LogD("checkReady pause, cause SDK hasn't been init or init is running, reInit sdk for result");
            return false;
        }

        if (mPlacement == null) {
            DeveloperLog.LogD("checkReady placement is null");
            return false;
        }
        return true;
    }

    /**
     * re-calculates cached ads count
     */
    private void reSizeCacheSize() {
        mCacheSize = Math.min(mCacheSize, mTotalIns.size());
    }

    /*
     * For testing purpose: getting all ads
     * */
    public String getTotalIns() {
        StringBuilder builder = new StringBuilder();
        if (mTotalIns != null && !mTotalIns.isEmpty()) {
            for (Instance instance : mTotalIns) {
                if (instance != null) {
                    builder.append(instance.toString());
                    builder.append(instance.getMediationState());
                    builder.append("\n");
                }
            }
        }
        return builder.toString();
    }

    /*
     * For testing purpose: resetting stocked ads' states to NOT_AVAILABLE
     * */
    public void resetStock() {
        if (mTotalIns != null && !mTotalIns.isEmpty()) {
            for (Instance instance : mTotalIns) {
                if (instance != null && instance.getMediationState() == Instance.MEDIATION_STATE.AVAILABLE) {
                    instance.setMediationState(Instance.MEDIATION_STATE.NOT_AVAILABLE);
                }
            }
        }
    }

    /*
     * For testing purpose: getting concurrency limit, i.e. number of INIT/LOAD_PENDING state
     * */
    private int getLoadCount() {
        int initCount = 0;
        if (mTotalIns != null && !mTotalIns.isEmpty()) {
            for (Instance instance : mTotalIns) {
                if (instance.getMediationState() == Instance.MEDIATION_STATE.INIT_PENDING
                        || instance.getMediationState() == Instance.MEDIATION_STATE.LOAD_PENDING) {
                    initCount++;
                }
            }
        }
        DeveloperLog.LogE("concurrency limit：loadCount:" + initCount
                + ", getLoadLimit:" + getLoadLimit()
                + ", placement:" + mPlacement + ", return");
        return initCount;
    }
}
