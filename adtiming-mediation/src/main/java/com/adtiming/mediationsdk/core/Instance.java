// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.core;

import android.app.Activity;
import android.text.TextUtils;
import android.util.SparseArray;

import com.adtiming.mediationsdk.mediation.CustomAdsAdapter;
import com.adtiming.mediationsdk.mediation.MediationInfo;
import com.adtiming.mediationsdk.utils.AdRateUtil;
import com.adtiming.mediationsdk.utils.AdtUtil;
import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.InsExecutor;
import com.adtiming.mediationsdk.utils.JsonUtil;
import com.adtiming.mediationsdk.utils.PlacementUtils;
import com.adtiming.mediationsdk.utils.cache.DataCache;
import com.adtiming.mediationsdk.utils.error.ErrorCode;
import com.adtiming.mediationsdk.utils.event.EventId;
import com.adtiming.mediationsdk.utils.event.EventUploadManager;
import com.adtiming.mediationsdk.utils.model.BaseInstance;
import com.adtiming.mediationsdk.utils.model.Config;
import com.adtiming.mediationsdk.utils.model.Mediation;
import com.adtiming.mediationsdk.utils.model.Placement;
import com.adtiming.mediationsdk.core.runnable.LoadTimeoutRunnable;
import com.adtiming.mediationsdk.utils.DensityUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * One Instance for one Adapter
 *
 * 
 */
public class Instance extends BaseInstance {

    protected CustomAdsAdapter mAdapter;

    private Instance.MEDIATION_STATE mMediationState;
    private LoadTimeoutRunnable mTimeoutRunnable;
    private ScheduledFuture mScheduledFuture;


    void onResume(Activity activity) {
        if (mAdapter != null) {
            mAdapter.onResume(activity);
        }
    }

    void onPause(Activity activity) {
        if (mAdapter != null) {
            mAdapter.onPause(activity);
        }
    }

    public void setAdapter(CustomAdsAdapter adapter) {
        mAdapter = adapter;
    }

    public CustomAdsAdapter getAdapter() {
        return mAdapter;
    }


    public void setMediationState(MEDIATION_STATE state) {
        mMediationState = state;
    }

    public MEDIATION_STATE getMediationState() {
        return mMediationState;
    }

    public boolean isCaped() {
        return getMediationState() == MEDIATION_STATE.CAPPED;
    }

    protected JSONObject buildReportData() {
        try {
            JSONObject jsonObject = new JSONObject();
            JsonUtil.put(jsonObject, "pid", mPlacementId);
            JsonUtil.put(jsonObject, "iid", id);
            JsonUtil.put(jsonObject, "mid", mediationId);
            if (mAdapter != null) {
                JsonUtil.put(jsonObject, "adapterv", mAdapter.getAdapterVersion());
                JsonUtil.put(jsonObject, "msdkv", mAdapter.getMediationVersion());
            }
            JsonUtil.put(jsonObject, "priority", index);
            Placement placement = PlacementUtils.getPlacement(mPlacementId);
            if (placement != null) {
                JsonUtil.put(jsonObject, "cs", placement.getCs());
            }
            return jsonObject;
        } catch (Exception e) {
            DeveloperLog.LogD("buildReportData exception : ", e);
        }
        return null;
    }

    /**
     * takes care of instance load timeout
     */
    protected void startInsLoadTimer(LoadTimeoutRunnable.OnLoadTimeoutListener listener) {
        if (mTimeoutRunnable == null) {
            mTimeoutRunnable = new LoadTimeoutRunnable();
            mTimeoutRunnable.setTimeoutListener(listener);
        }
        Placement placement = PlacementUtils.getPlacement(mPlacementId);
        int timeout = placement != null ? placement.getPt() : 30;
        mScheduledFuture = InsExecutor.execute(mTimeoutRunnable, timeout, TimeUnit.SECONDS);
    }

    protected Map<String, Object> getInitDataMap() {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("AppKey", getAppKey());
        dataMap.put("pid", key);
        if (getMediationId() == MediationInfo.PLAT_ID_6) {
            Config config = DataCache.getInstance().get("Config", Config.class);
            if (config != null) {
                Map<String, Placement> placements = config.getPlacements();
                if (placements != null && !placements.isEmpty()) {
                    Set<String> keys = placements.keySet();
                    if (!keys.isEmpty()) {
                        SparseArray<Mediation> mediations = config.getMediations();
                        Mediation mediation = mediations.get(getMediationId());
                        if (mediation != null) {
                            dataMap.put("zoneIds", buildZoneIds(keys, placements, mediation));
                        }
                    }
                }
            }
        }
        return dataMap;
    }

    protected void onInsInitSuccess() {
        setMediationState(MEDIATION_STATE.INITIATED);
        JSONObject data = buildReportData();
        if (mInitStart > 0) {
            int dur = (int) (System.currentTimeMillis() - mInitStart);
            JsonUtil.put(data, "duration", dur);
            mInitStart = 0;
        }
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_INIT_SUCCESS, data);
    }

    protected void onInsInitFailed(String error) {
        setMediationState(MEDIATION_STATE.INIT_FAILED);
        JSONObject data = buildReportData();
        JsonUtil.put(data, "msg", error);
        if (mInitStart > 0) {
            int dur = (int) (System.currentTimeMillis() - mInitStart);
            JsonUtil.put(data, "duration", dur);
            mInitStart = 0;
        }
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_INIT_FAILED, data);
    }

    protected void onInsOpen() {
        AdRateUtil.onInstancesShowed(mPlacementId, key);
        JSONObject data = buildReportData();
        JsonUtil.put(data, "ot", DensityUtil.getDirection(AdtUtil.getApplication()));
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_OPENED, data);
    }

    protected void onInsClosed() {
        setMediationState(MEDIATION_STATE.NOT_AVAILABLE);
        JSONObject data = buildReportData();
        if (mShowStart > 0) {
            int dur = (int) (System.currentTimeMillis() - mShowStart);
            JsonUtil.put(data, "duration", dur);
            mShowStart = 0;
        }
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_CLOSED, data);
    }

    protected void onInsLoadSuccess() {
        cancelInsLoadTimer();
        setMediationState(MEDIATION_STATE.AVAILABLE);
        JSONObject data = buildReportData();
        if (mLoadStart > 0) {
            int dur = (int) (System.currentTimeMillis() - mLoadStart);
            JsonUtil.put(data, "duration", dur);
        }
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_LOAD_SUCCESS, data);
    }

    protected void onInsLoadFailed(String error) {
        setMediationState(MEDIATION_STATE.LOAD_FAILED);
        cancelInsLoadTimer();
        JSONObject data = buildReportData();
        JsonUtil.put(data, "msg", error);
        if (mLoadStart > 0) {
            int dur = (int) (System.currentTimeMillis() - mLoadStart);
            JsonUtil.put(data, "duration", dur);
        }
        if (!TextUtils.isEmpty(error) && error.contains(ErrorCode.ERROR_TIMEOUT)) {
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_LOAD_TIMEOUT, data);
        } else {
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_LOAD_ERROR, data);
        }
    }

    protected void onInsStarted() {
        mShowStart = System.currentTimeMillis();
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_VIDEO_START, buildReportData());
    }

    protected void onInsShowFailed(String error) {
        setMediationState(MEDIATION_STATE.NOT_AVAILABLE);
        JSONObject data = buildReportData();
        JsonUtil.put(data, "msg", error);
        if (mShowStart > 0) {
            int dur = (int) (System.currentTimeMillis() - mShowStart);
            JsonUtil.put(data, "duration", dur);
            mShowStart = 0;
        }
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_SHOW_FAILED, data);
    }

    /**
     * Cancels instance load timeout
     */
    private void cancelInsLoadTimer() {
        if (mScheduledFuture != null) {
            if (!mScheduledFuture.isCancelled()) {
                mScheduledFuture.cancel(true);
            }
            mScheduledFuture = null;
        }

        if (mTimeoutRunnable != null) {
            InsExecutor.remove(mTimeoutRunnable);
            mTimeoutRunnable = null;
        }
    }

    private static List<String> buildZoneIds(Set<String> keys
            , Map<String, Placement> placements, Mediation mediation) {
        List<String> instanceKeys = new ArrayList<>();
        for (String key : keys) {
            Placement p = placements.get(key);
            if (p == null) {
                continue;
            }
            SparseArray<BaseInstance> instances = p.getInsMap();
            if (instances != null && instances.size() > 0) {
                int size = instances.size();
                for (int i = 0; i < size; i++) {
                    BaseInstance mp = instances.valueAt(i);
                    if (mp == null) {
                        continue;
                    }
                    //mp doesn't belong to the AdNetwork
                    if (mp.getMediationId() != mediation.getId()) {
                        continue;
                    }
                    instanceKeys.add(mp.getKey());
                }
            }
        }
        return instanceKeys;
    }

    public enum MEDIATION_STATE {
        /**
         * mediation not yet initialized; sets instance's state to after SDK init is done
         */
        NOT_INITIATED(0),
        /**
         * set after initialization failure
         */
        INIT_FAILED(1),
        /**
         * set after initialization success
         */
        INITIATED(2),
        /**
         * set after load success
         */
        AVAILABLE(3),
        /**
         * set after load failure
         */
        NOT_AVAILABLE(4),

        /**
         *
         */
        CAPPED_PER_SESSION(5),
        /**
         * set after initialization starts
         */
        INIT_PENDING(6),
        /**
         * set after load starts
         */
        LOAD_PENDING(7),

        /**
         * set after load fails
         */
        LOAD_FAILED(8),
        CAPPED_PER_DAY(9),

        /**
         * set in the case of frequency control
         */
        CAPPED(10);

        private int mValue;

        private MEDIATION_STATE(int value) {
            this.mValue = value;
        }

        public int getValue() {
            return this.mValue;
        }
    }
}
