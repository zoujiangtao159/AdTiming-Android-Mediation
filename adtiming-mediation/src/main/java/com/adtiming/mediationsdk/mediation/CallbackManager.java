// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.mediation;

import java.util.HashMap;
import java.util.Map;

public final class CallbackManager {
    private Map<String, Callback> mCallbacks = new HashMap<>();

    private static final class CallbackManagerHolder {
        private static final CallbackManager INSTANCE = new CallbackManager();
    }

    private CallbackManager() {
    }

    public static CallbackManager getInstance() {
        return CallbackManagerHolder.INSTANCE;
    }

    public void addCallback(String placementId, Callback callback) {
        if (callback == null) {
            return;
        }
        mCallbacks.put(placementId, callback);
    }

    private Callback getCallback(String placementId) {
        return mCallbacks.get(placementId);
    }

    public void removeCallback(String placementId) {
        mCallbacks.remove(placementId);
    }

    void onInsClick(String placementId, String instanceKey, String instanceId) {
        Callback callback = getCallback(placementId);
        if (callback != null) {
            callback.onInstanceClick(instanceKey, instanceId);
        }
    }

    public synchronized void onInsReady(String placementId, String instanceKey, String instanceId, Object o) {
        Callback callback = getCallback(placementId);
        if (callback != null) {
            callback.onInsReady(instanceKey, instanceId, o);
        }
    }

    public synchronized void onInsError(String placementId, String instanceKey, String instanceId, String error) {
        Callback callback = getCallback(placementId);
        if (callback != null) {
            callback.onInsError(instanceKey, instanceId, error);
        }
    }
}
