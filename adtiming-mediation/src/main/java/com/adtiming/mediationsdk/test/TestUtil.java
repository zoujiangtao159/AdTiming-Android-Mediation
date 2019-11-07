// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.test;

import com.adtiming.mediationsdk.core.Instance;
import com.adtiming.mediationsdk.utils.HandlerUtil;
import com.adtiming.mediationsdk.utils.cache.DataCache;
import com.adtiming.mediationsdk.utils.model.BaseInstance;
import com.adtiming.mediationsdk.utils.model.Config;
import com.adtiming.mediationsdk.utils.model.Placement;
import com.adtiming.mediationsdk.utils.MediationUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestUtil {

    private Map<String, TObserverListener> mObservers;

    private static final class THolder {
        private static final TestUtil INSTANCE = new TestUtil();
    }

    private TestUtil() {
        mObservers = new HashMap<>();
    }

    public static TestUtil getInstance() {
        return THolder.INSTANCE;
    }


    public void registerObserver(String placementId, TObserverListener observerListener) {
        mObservers.put(placementId, observerListener);
    }

    public void unRegisterObserver(String placementId) {
        mObservers.remove(placementId);
    }

    public void notifyInsLoad(String placementId, Instance instances) {
        if (mObservers != null && mObservers.containsKey(placementId) && mObservers.get(placementId) != null) {
            mObservers.get(placementId).updateLoadInstance(instances);
        }
    }

    public void notifyInsReady(String placementId, Instance instances) {
        if (mObservers != null && mObservers.containsKey(placementId)
                && mObservers.get(placementId) != null) {
            mObservers.get(placementId).updateReadyInstance(instances);
        }
    }

    public synchronized void notifyInsReadyInLoading(String placementId, Instance instances) {
        if (mObservers != null && mObservers.containsKey(placementId)
                && mObservers.get(placementId) != null) {
            mObservers.get(placementId).updateReadyInstanceInLoading(instances);
        }
    }

    public void notifyInsFailed(final String placementId, final Instance instances) {
        if (mObservers != null && mObservers.containsKey(placementId)
                && mObservers.get(placementId) != null) {
            HandlerUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mObservers.get(placementId).updateFailedInstance(instances);
                }
            });
        }
    }

    public void notifyLoadFailed(final Placement placement, final String errorMessage) {
        if (placement == null) {
            return;
        }
        final String placementId = placement.getId();
        if (mObservers != null && mObservers.containsKey(placementId)
                && mObservers.get(placementId) != null) {
            HandlerUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mObservers.get(placementId).updateLoadFailed(placementId, errorMessage);
                }
            });
        }
    }

    public void notifyShowFailed(final String placementId, final String errorMessage) {
        if (mObservers != null && mObservers.containsKey(placementId)
                && mObservers.get(placementId) != null) {
            HandlerUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mObservers.get(placementId).updateShowFailed(placementId, errorMessage);
                }
            });
        }
    }

    public void notifyInsShow(final String placementId, final Instance instances) {
        if (mObservers != null && mObservers.containsKey(placementId)
                && mObservers.get(placementId) != null) {
            HandlerUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mObservers.get(placementId).updateShowInstance(instances);
                }
            });
        }
    }

    public void notifyInsConfig(String placementId, BaseInstance instance, int loadLimit, int currentAvailableAdsCount, int loadCount) {
        if (mObservers != null && mObservers.containsKey(placementId)
                && mObservers.get(placementId) != null) {
            mObservers.get(placementId).updateInsConfig(instance, loadLimit, currentAvailableAdsCount, loadCount);
        }
    }

    public void notifyPlacementConfig(Placement placement) {
        if (mObservers != null && placement != null && mObservers.containsKey(placement.getId())
                && mObservers.get(placement.getId()) != null) {
            mObservers.get(placement.getId()).updatePlacementConfig(placement);
        }
    }

    public void notifyClKs(String placementId, List<String> clks) {
        if (mObservers != null && mObservers.containsKey(placementId)
                && mObservers.get(placementId) != null) {
            mObservers.get(placementId).updateCLks(clks);
        }
    }

    public static Collection<Placement> getPlacements() {
        Config config = DataCache.getInstance().get("Config", Config.class);
        if (config == null) {
            return null;
        }
        return config.getPlacements().values();
    }

    public static void setTestInstance(String placementId, BaseInstance[] testInstances) {
        MediationUtil.setTestInstance(placementId, testInstances);
    }

    public static void cleanTestInstance() {
        MediationUtil.cleanTestInstance();
    }

}
