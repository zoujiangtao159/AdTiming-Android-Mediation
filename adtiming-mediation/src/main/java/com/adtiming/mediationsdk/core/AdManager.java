// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.core;

import com.adtiming.mediationsdk.mediation.CustomAdEvent;
import com.adtiming.mediationsdk.mediation.CustomEventFactory;
import com.adtiming.mediationsdk.utils.Constants;
import com.adtiming.mediationsdk.utils.DeveloperLog;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to manage banner&native ads's relationShip between instance and adapter
 *
 * 
 */
public class AdManager {
    private Map<Instance, CustomAdEvent> mAdEvents;//adEvents making requests
    private Map<String, Instance[]> mInstancesMap; //server returned priority group

    private static final class AdManagerHolder {
        private static final AdManager INSTANCE = new AdManager();
    }

    public static AdManager getInstance() {
        return AdManagerHolder.INSTANCE;
    }

    protected AdManager() {
        mAdEvents = new HashMap<>();
        mInstancesMap = new HashMap<>();
    }

    private void addAdEvent(Instance instances, CustomAdEvent adEvent) {
        if (instances == null || adEvent == null) {
            return;
        }
        mAdEvents.put(instances, adEvent);
    }


    public CustomAdEvent getInsAdEvent(int adType, Instance instances) {
        try {
            if (instances == null) {
                return null;
            }
            CustomAdEvent adEvent = mAdEvents.get(instances);
            if (adEvent == null) {
                DeveloperLog.LogD("get Ins Event by create new : " + instances.toString());
                switch (adType) {
                    case Constants.BANNER:
                        adEvent = CustomEventFactory.createBanner(instances.getPath());
                        break;
                    case Constants.NATIVE:
                        adEvent = CustomEventFactory.createNative(instances.getPath());
                        break;
                    default:
                        break;
                }

                addAdEvent(instances, adEvent);
            } else {
                DeveloperLog.LogD("get Ins Event from map: " + instances.toString());
            }
            return adEvent;
        } catch (Exception e) {
            DeveloperLog.LogD("AdManager", e);
        }
        return null;
    }

    public void removeInsAdEvent(Instance instances) {
        if (mAdEvents.isEmpty()) {
            return;
        }
        mAdEvents.remove(instances);
    }


    Instance[] getInstancesById(String placementId) {
        return mInstancesMap.get(placementId);
    }

    void addInstancesToMap(String placementId, Instance[] instances) {
        if (instances == null) {
            return;
        }
        mInstancesMap.put(placementId, instances);
    }

    void removeInstancesFromMap(String placementId) {
        mInstancesMap.remove(placementId);
    }
}
