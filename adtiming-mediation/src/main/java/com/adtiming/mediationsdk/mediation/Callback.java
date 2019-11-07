// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.mediation;

public abstract class Callback {

    protected abstract void onInstanceClick(String instanceKey, String instanceId);

    protected void onInsReady(String instanceKey, String instanceId, Object o) {
    }

    protected void onInsError(String instanceKey, String instanceId, String error) {

    }
}
