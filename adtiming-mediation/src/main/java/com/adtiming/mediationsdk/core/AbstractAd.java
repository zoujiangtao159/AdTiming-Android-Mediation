// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.core;

import android.app.Activity;
import android.text.TextUtils;

import com.adtiming.mediationsdk.InitCallback;
import com.adtiming.mediationsdk.danmaku.DanmakuLifecycle;
import com.adtiming.mediationsdk.mediation.Callback;
import com.adtiming.mediationsdk.utils.AdRateUtil;
import com.adtiming.mediationsdk.utils.Constants;
import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.HandlerUtil;
import com.adtiming.mediationsdk.utils.IOUtil;
import com.adtiming.mediationsdk.utils.LrReportUtil;
import com.adtiming.mediationsdk.utils.MediationRequest;
import com.adtiming.mediationsdk.utils.cache.DataCache;
import com.adtiming.mediationsdk.utils.crash.CrashUtil;
import com.adtiming.mediationsdk.utils.device.DeviceUtil;
import com.adtiming.mediationsdk.utils.error.AdTimingError;
import com.adtiming.mediationsdk.utils.error.ErrorCode;
import com.adtiming.mediationsdk.utils.model.Config;
import com.adtiming.mediationsdk.utils.model.Placement;
import com.adtiming.mediationsdk.utils.model.PlacementInfo;
import com.adtiming.mediationsdk.utils.request.network.Request;
import com.adtiming.mediationsdk.utils.request.network.Response;
import com.adtiming.mediationsdk.utils.request.network.util.NetworkChecker;
import com.adtiming.mediationsdk.utils.MediationUtil;
import com.adtiming.mediationsdk.test.TestUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;

/**
 * 
 */
public abstract class AbstractAd extends Callback implements Request.OnRequestCallback, InitCallback {

    protected Placement mPlacement;
    protected String mPlacementId;
    protected boolean isDestroyed;
    protected WeakReference<Activity> mActRef;
    protected Instance mCurrentIns;

    Instance[] mTotalIns;
    int mBs;
    boolean isFo;
    int mPt;

    //timestamp of loading
    private long mLoadTs;
    //timestamp of ready/fail callback received
    private long mCallbackTs;

    public abstract boolean isReady();

    protected abstract int getAdType();

    protected abstract PlacementInfo getPlacementInfo();

    protected abstract void dispatchAdRequest();

    protected abstract void onAdErrorCallback(String error);

    protected abstract void onAdReadyCallback();

    protected abstract void onAdClickCallback();

    AbstractAd(Activity activity, String placementId) {
        isDestroyed = false;
        mActRef = new WeakReference<>(activity);
        mPlacementId = placementId;
    }

    public void loadAd() {
        //load returns if in the middle of initialization
        if (InitImp.isInitRunning()) {
            AdTimingManager.getInstance().pendingInit(this);
            return;
        }

        //checks if initialization was successful
        if (!InitImp.isInit()) {
            DeveloperLog.LogD("call sdk init before loading ads");
            reInitSDK();
            return;
        }
        //TODO:modify
        delayLoad(AdTimingManager.LOAD_TYPE.MANUAL);
    }

    public void destroy() {
        mPlacement = null;
        if (mActRef != null) {
            mActRef.clear();
            mActRef = null;
        }
        isDestroyed = true;
    }


    @Override
    public void onSuccess() {
        //initialization successful. starts loading
        delayLoad(AdTimingManager.LOAD_TYPE.INIT);
    }

    @Override
    public void onError(AdTimingError error) {
        callbackAdErrorOnUIThread(error != null ? error.toString() : "");
    }

    @Override
    public void onRequestSuccess(Response response) {
        //parses the response. gets ins array, bs size, pt size，fo switch, campaign info
        try {
            if (response == null || response.code() != 200) {
                DeveloperLog.LogD("Ad", "request cl success, but response is unavailable " + mPlacement);
                callbackAdErrorOnUIThread(ErrorCode.ERROR_NO_FILL);
                return;
            }

            JSONObject clInfo = new JSONObject(response.body().string());
            isFo = clInfo.optInt("fo", 0) == 1;
            mPt = clInfo.optInt("pt", 15);
            mBs = clInfo.optInt("bs");
            if (mBs == 0) {
                DeveloperLog.LogD("Ad", "mBs==0 ,set default value 3 ");
                mBs = 3;
            }

            Instance[] tmp = MediationUtil.getInstances(clInfo, mPlacement, mBs);
            if (tmp == null || tmp.length == 0) {
                DeveloperLog.LogD("Ad", "request cl success, but ins[] is empty" + mPlacement);
                callbackAdErrorOnUIThread(ErrorCode.ERROR_NO_FILL);
            } else {
                mTotalIns = tmp;
                AdManager.getInstance().addInstancesToMap(mPlacementId, Arrays.copyOf(mTotalIns, mTotalIns.length));
                DeveloperLog.LogD("Ad", "TotalIns is : " + Arrays.toString(mTotalIns));
                doLoadOnUIThread();
            }
        } catch (IOException | JSONException e) {
            DeveloperLog.LogD("Ad", "request cl success, but failed when parse response " + mPlacement + ", message:" + e.getMessage());
            CrashUtil.getSingleton().saveException(e);
            callbackAdErrorOnUIThread(ErrorCode.ERROR_NO_FILL);
        } finally {
            IOUtil.closeQuietly(response);
        }
    }

    @Override
    public void onRequestFailed(String error) {//if failed to request cl, calls ad error callback
        DeveloperLog.LogD("Ad", "request cl failed : " + error);
        callbackAdErrorOnUIThread(ErrorCode.ERROR_NO_FILL);
    }


    protected void cleanAfterCloseOrFailed() {
        AdManager.getInstance().removeInstancesFromMap(mPlacementId);
        mTotalIns = null;
        mBs = 0;
        mPt = 0;
        isFo = false;
        if (mCurrentIns != null) {
            mCurrentIns.setObject(null);
            mCurrentIns.setStart(0);
            mCurrentIns = null;
        }
    }

    /**
     * Ad loading error callback
     *
     * @param error error reason
     */
    protected void callbackAdErrorOnUIThread(final String error) {
        if (isDestroyed) {
            return;
        }
        mCallbackTs = System.currentTimeMillis();
        cleanAfterCloseOrFailed();
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onAdErrorCallback(error);
            }
        });
    }

    /**
     * Ad loading success callback
     */
    void callbackAdReadyOnUIThread() {
        if (isDestroyed) {
            return;
        }
        mCallbackTs = System.currentTimeMillis();
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TestUtil.getInstance().notifyInsReady(mPlacementId, mCurrentIns);
                onAdReadyCallback();
            }
        });
    }

    /**
     * Ad click callback
     */
    void callbackAdClickOnUIThread() {
        if (isDestroyed) {
            return;
        }
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onAdClickCallback();
            }
        });
    }

    /**
     * Useless request reporting
     */
    private void aUselessReport() {
        if (mCurrentIns == null) {
            return;
        }
        LrReportUtil.aUselessReport(mPlacementId, mCurrentIns);
    }

    /**
     * All-is-ready reporting
     */
    void aReadyReport() {
        if (isDestroyed) {
            return;
        }
        if (getAdType() == Constants.INTERACTIVE) {
            DeveloperLog.LogD("interactive no need to AReadyReport");
            return;
        }
        LrReportUtil.aReadyReport(mPlacementId, AdTimingManager.LOAD_TYPE.MANUAL);
    }

    /**
     * Invalid request reporting
     */
    private void invalidReport() {
        if (isDestroyed) {
            return;
        }
        LrReportUtil.invalidReport(mPlacementId);
    }

    /**
     *  Instance-level useless request reporting
     */
    protected void iUselessReport(Instance instances) {
        if (isDestroyed) {
            return;
        }
        LrReportUtil.iUselessReport(mPlacementId, instances);
    }

    /**
     * Instance-level load reporting
     */
    protected void iLoadReport(Instance instances) {
        if (isDestroyed) {
            return;
        }
        LrReportUtil.iLoadReport(mPlacementId, AdTimingManager.LOAD_TYPE.MANUAL, instances);
    }

    /**
     * Instance-level ready reporting 
     */
    void iReadyReport(Instance instances) {
        if (isDestroyed) {
            return;
        }
        LrReportUtil.iReadyReport(mPlacementId, AdTimingManager.LOAD_TYPE.MANUAL, instances);
    }

    /**
     * Instance-level impression reporting
     */
    protected void insImpReport(Instance instances) {
        if (isDestroyed) {
            return;
        }
        LrReportUtil.iImpressionReport(mPlacementId, 0, instances);
    }

    /**
     * Instance-level click reporting
     */
    void insClickReport(Instance instances) {
        if (isDestroyed) {
            return;
        }
        LrReportUtil.iClickReport(mPlacementId, 0, instances);
    }

    /**
     * Checks if callback has been trigged
     */
    boolean hasCallbackToUser() {
        return mLoadTs <= mCallbackTs;
    }

    protected Instance getInsByKey(String instanceKey, String instanceId) {
        if (mTotalIns == null || TextUtils.isEmpty(instanceKey) || TextUtils.isEmpty(instanceId)) {
            return null;
        }

        for (Instance ins : mTotalIns) {
            if (ins == null) {
                continue;
            }
            //extra insId check in case adt's id is the same as other ad networks'
            if (TextUtils.equals(instanceKey, ins.getKey()) && TextUtils.equals(instanceId, String.valueOf(ins.getId()))) {
                return ins;
            }
        }
        return null;
    }

    protected boolean checkActRef() {
        if (mActRef == null || mActRef.get() == null || !DeviceUtil.isActivityAvailable(mActRef.get())) {
            Activity activity = DanmakuLifecycle.getInstance().getActivity();
            if (activity == null) {
                return false;
            }
            mActRef = new WeakReference<>(activity);
        }
        return true;
    }

    /**
     * Loads ads on UI thread
     */
    private void doLoadOnUIThread() {
        if (isDestroyed) {
            return;
        }
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resetFields();
                dispatchAdRequest();
            }
        });
    }

    private void delayLoad(AdTimingManager.LOAD_TYPE type) {
        try {
            //returns if load can't start
            if (!checkLoadAvailable()) {
                return;
            }

            //load start timestamp
            mLoadTs = System.currentTimeMillis();
            MediationRequest.cLRequest(getPlacementInfo(), type, this);
        } catch (Exception e) {
            DeveloperLog.LogD("load ad error", e);
            CrashUtil.getSingleton().saveException(e);
        }
    }

    private void reInitSDK() {
        if (!checkActRef()) {
            callbackAdErrorOnUIThread(ErrorCode.ERROR_ACTIVITY);
            return;
        }
        if (DataCache.getInstance().containsKey("AppKey")) {
            String appKey = DataCache.getInstance().get("AppKey", String.class);
            if (TextUtils.isEmpty(appKey)) {
                callbackAdErrorOnUIThread(ErrorCode.ERROR_NOT_INIT);
                return;
            }
            InitImp.init(mActRef.get(), appKey, new InitCallback() {
                @Override
                public void onSuccess() {
                    DeveloperLog.LogD("reInitSDK success do delayLoad");
                    delayLoad(AdTimingManager.LOAD_TYPE.INIT);
                }

                @Override
                public void onError(AdTimingError error) {
                    callbackAdErrorOnUIThread(error != null ? error.toString() : "");
                }
            });
        } else {
            callbackAdErrorOnUIThread(ErrorCode.ERROR_APPKEY);//todo callback message
        }
    }

    /**
     * Various checks before load can start
     */
    private boolean checkLoadAvailable() {
        //empty placementId?
        if (TextUtils.isEmpty(mPlacementId)) {
            DeveloperLog.LogD("placement id is empty");
            callbackAdErrorOnUIThread(ErrorCode.ERROR_PLACEMENT_ID);
            return false;
        }
        //activity effective?
        if (!checkActRef()) {
            DeveloperLog.LogD("load ad but activity is not available");
            callbackAdErrorOnUIThread(ErrorCode.ERROR_ACTIVITY);
            return false;
        }

        //network available?
        if (!NetworkChecker.isAvailable(mActRef.get())) {
            DeveloperLog.LogD("load ad network not available");
            callbackAdErrorOnUIThread(ErrorCode.ERROR_NETWORK_NOT_AVAILABLE);
            return false;
        }

        if (isDestroyed) {
            DeveloperLog.LogD("destroy method has been called , please re-init this ad format before loadAdWithAction");
            callbackAdErrorOnUIThread(ErrorCode.ERROR_LOAD_AD_BUT_DESTROYED);
            return false;
        }
        if (mLoadTs > mCallbackTs) {//loading in progress
            invalidReport();
            return false;
        }

        if (mPlacement == null) {
            Config config = DataCache.getInstance().get("Config", Config.class);
            //placement exists?
            if (config == null || config.getPlacements().isEmpty()) {
                DeveloperLog.LogD("config is empty");
                callbackAdErrorOnUIThread(ErrorCode.ERROR_CONFIG_EMPTY);
                return false;
            }
            mPlacement = config.getPlacements().get(mPlacementId);
            if (mPlacement == null) {
                DeveloperLog.LogD("config does not have placement by : " + mPlacementId);
                callbackAdErrorOnUIThread(ErrorCode.ERROR_PLACEMENT_EMPTY);
                return false;
            }
            if (mPlacement.getT() != getAdType()) {
                DeveloperLog.LogD("the placement type does not match the requested type");
                callbackAdErrorOnUIThread(ErrorCode.ERROR_PLACEMENT_TYPE);
                return false;
            }
        }

        if (AdRateUtil.shouldBlockPlacement(mPlacementId, mPlacement.getFrequencyInterval())) {
            DeveloperLog.LogD("Placement :" + mPlacementId + " is blocked");
            callbackAdErrorOnUIThread(ErrorCode.ERROR_NO_FILL);
            return false;
        }
        return true;
    }

    private void resetFields() {
        if (mCurrentIns != null) {
            mCurrentIns = null;
        }
        isDestroyed = false;
    }
}
