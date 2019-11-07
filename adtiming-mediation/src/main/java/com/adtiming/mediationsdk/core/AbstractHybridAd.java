// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.core;

import android.app.Activity;
import android.os.Looper;

import com.adtiming.mediationsdk.mediation.CallbackManager;
import com.adtiming.mediationsdk.utils.AdRateUtil;
import com.adtiming.mediationsdk.utils.Constants;
import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.HandlerUtil;
import com.adtiming.mediationsdk.utils.crash.CrashUtil;
import com.adtiming.mediationsdk.utils.error.ErrorCode;
import com.adtiming.mediationsdk.utils.event.EventId;
import com.adtiming.mediationsdk.utils.event.EventUploadManager;
import com.adtiming.mediationsdk.test.TestUtil;

/**
 * 
 */
public abstract class AbstractHybridAd extends AbstractAd {
    protected Instance mShowedIns;
    private volatile int mCanCallbackIndex;//index of current callback
    private HandlerUtil.HandlerHolder mHandler;

    protected abstract void loadInsOnUIThread(Instance instances) throws Throwable;

    protected abstract boolean isInsReady(Instance instances);

    protected void destroyAdEvent(Instance instances) {
    }

    public AbstractHybridAd(Activity activity, String placementId) {
        super(activity, placementId);
        mHandler = new HandlerUtil.HandlerHolder(null, Looper.getMainLooper());
        CallbackManager.getInstance().addCallback(placementId, this);
    }

    @Override
    protected void dispatchAdRequest() {
        mCanCallbackIndex = 0;
        //starts loading the 1st
        startNextInstance(0);
    }

    @Override
    public void destroy() {
        CallbackManager.getInstance().removeCallback(mPlacementId);
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_DESTROY);
        super.destroy();
    }

    /**
     * 1、calls load method at the given instanceIndex
     * 2、checks totalInstances to see if any instance is available for starting
     */
    private void startNextInstance(int instanceIndex) {
        if (mTotalIns == null) {
            return;
        }

        mCanCallbackIndex = instanceIndex;

        boolean result = checkReadyInstance(true);
        if (result) {
            DeveloperLog.LogD("Ad is prepared for : " + mPlacementId + " callbackIndex is : " + mCanCallbackIndex);
            return;
        }

        try {
            if (mBs <= 0 || mTotalIns.length <= instanceIndex) {
                callbackAdErrorOnUIThread(ErrorCode.ERROR_NO_FILL);
                return;
            }

            //# of instances to load
            int pendingLoadInstanceCount = mBs;
            while (!hasCallbackToUser() && mTotalIns.length > instanceIndex && pendingLoadInstanceCount > 0) {
                final Instance i = mTotalIns[instanceIndex];
                instanceIndex++;
                pendingLoadInstanceCount--;

                if (i == null) {
                    continue;
                }

                testNotifyInsLoad(i);

                //blocked?
                if (AdRateUtil.shouldBlockInstance(mPlacementId + i.getKey(), i)) {
                    onInsError(i, ErrorCode.ERROR_NO_FILL);
                    continue;
                }

                try {
                    loadInsOnUIThread(i);
                } catch (Throwable e) {
                    onInsError(i, e.getMessage());
                    DeveloperLog.LogD("load ins : " + i.toString() + " error ", e);
                    CrashUtil.getSingleton().saveException(e);
                }
            }
            //no need to time out if a callback was given
            if (!hasCallbackToUser()) {
                //times out with the index of currently loaded instance
                startTimeout(instanceIndex);
            }
        } catch (Exception e) {
            DeveloperLog.LogD("startNextInstance error", e);
        }
    }

    protected synchronized void onInsReady(boolean doInsReadyReport, final Instance instances, Object o) {
        if (doInsReadyReport) {
            DeveloperLog.LogD("do ins ready report");
            iReadyReport(instances);
        } else {
            DeveloperLog.LogD("do ins useless report");
        }
        //saves every instance's ready data, mainly for Banner and Native
        instances.setObject(o);
        TestUtil.getInstance().notifyInsReadyInLoading(mPlacementId, instances);
        if (isFo || instances.getIndex() <= mCanCallbackIndex) {
            //gives ready callback without waiting for priority checking 
            placementReadyCallback(instances);
        } else {
            checkReadyInstance(false);
        }
    }

    @Override
    protected synchronized void onInsReady(String instanceKey, String instanceId, Object o) {
        super.onInsReady(instanceKey, instanceId, o);
        Instance instances = getInsByKey(instanceKey, instanceId);
        if (instances == null) {
            return;
        }
        onInsReady(true, instances, o);
    }

    @Override
    protected synchronized void onInsError(String instanceKey, String instanceId, String error) {
        super.onInsError(instanceKey, instanceId, error);
        Instance instances = getInsByKey(instanceKey, instanceId);
        if (instances == null) {
            EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_LOAD_ERROR);
            return;
        }
        onInsError(instances, error);
    }

    protected synchronized void onInsError(Instance instances, String error) {
        //handles load error only
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_LOAD_ERROR);
        if (mShowedIns != null) {//showing in progress
            return;
        }
        if (instances == null) {
            return;
        }
        testNotifyInsFailed(instances);
        if (getAdType() == Constants.BANNER) {
            //MopubBanner registered a receiver, we need to take care of it
            destroyAdEvent(instances);
        }
        DeveloperLog.LogD("load ins : " + instances.toString() + " error : " + error);

        int len = mTotalIns.length;
        //groupIndex of current failed instance
        int groupIndex = instances.getGrpIndex();
        //allInstanceGroup failed?
        boolean allInstanceGroupIsNull = true;
        //allInstanceGroup member failed?
        boolean allInstanceIsNullAtGroupIndex = true;
        //traverses to set all failed members to null
        for (int a = 0; a < len; a++) {
            Instance i = mTotalIns[a];
            if (i == instances) {
                mTotalIns[a] = null;
            }

            if (mTotalIns[a] != null) {
                allInstanceGroupIsNull = false;
                if (i.getGrpIndex() != groupIndex) {
                    continue;
                }
                allInstanceIsNullAtGroupIndex = false;
            }
        }

        //gives no_fill  call back if allInstanceGroupIsNull
        if (allInstanceGroupIsNull && !hasCallbackToUser()) {
            callbackAdErrorOnUIThread(ErrorCode.ERROR_NO_FILL);
            cancelTimeout();
            return;
        }

        //
        if (allInstanceIsNullAtGroupIndex) {//only this group
            cancelTimeout();
            //loads the next group
            startNextInstance((groupIndex + 1) * mBs); 
            return;
        }

        if (instances.isFirst()) {
            //e.g. assuming 0, 1, 2 in the group, bs is 3; when 0 fails, index moves forward by 2: 0 + 3 - 1 = 2
            DeveloperLog.LogD("first instance failed, add callbackIndex : " + instances.toString() + " error : " + error);
            mCanCallbackIndex = instances.getIndex() + mBs - 1;
            checkReadyInstance(false);
        }
    }

    @Override
    protected void onInstanceClick(String instanceKey, String instanceId) {
        DeveloperLog.LogD("onInstanceClick : " + instanceKey);
        EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_CLICKED);
        Instance instances = getInsByKey(instanceKey, instanceId);
        if (instances == null) {
            return;
        }
        insClickReport(instances);
        callbackAdClickOnUIThread();
    }

    /**
     * Releases all bu the ready instances' AdEvents. Currently for banner only
     */
    protected void releaseAdEvent() {
        if (mTotalIns == null) {
            return;
        }

        for (Instance in : mTotalIns) {
            if (in == null) {
                continue;
            }
            if (in == mCurrentIns) {
                continue;
            }
            destroyAdEvent(in);
        }
    }

    private synchronized void placementReadyCallback(Instance ins) {
        if (mTotalIns == null) {
            return;
        }
        if (mCurrentIns == null) {
            mCurrentIns = ins;
            aReadyReport();
            callbackAdReadyOnUIThread();
            cancelTimeout();
        } else if (mCurrentIns.getIndex() > ins.getIndex()) {
            //Native and Banner doesn't update instance after receiving callback
            if (getAdType() == Constants.NATIVE || getAdType() == Constants.BANNER) {
                return;
            }
            mCurrentIns = ins;
        }
    }

    /**
     * Checks for ready instances between 0 and indexMap. Checks when
     * 1. a new loading sequence starts
     * 2. when the 1st instance in the group failed
     * 
     */
    private synchronized boolean checkReadyInstance(boolean uselessReport) {
        try {
            if (mTotalIns == null) {
                return false;
            }
            //traverses to get smaller index ready instances
            for (Instance i : mTotalIns) {
                if (i == null) {
                    continue;
                }
                DeveloperLog.LogD("Ad", "checkReadyInstance index : " + i.getIndex() + " callbackIndex : " + mCanCallbackIndex);
                //ignores trailing members
                if (i.getIndex() > mCanCallbackIndex) {
                    break;
                }
                if (isInsReady(i)) {
                    if (uselessReport) {
                        iUselessReport(i);
                    }
                    placementReadyCallback(i);
                    return true;
                }
            }
        } catch (Throwable e) {
            DeveloperLog.LogD("checkReadyInstancesOnUiThread error : " + e.getMessage());
            CrashUtil.getSingleton().saveException(e);
        }
        return false;
    }

    private void startTimeout(int insIndex) {
        TimeoutRunnable timeout = new TimeoutRunnable(insIndex);
        mHandler.postDelayed(timeout, mPt * 1000L);
    }

    private void cancelTimeout() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    private synchronized void testNotifyInsLoad(Instance i) {
        TestUtil.getInstance().notifyInsLoad(mPlacementId, i);
    }


    private synchronized void testNotifyInsFailed(Instance i) {
        TestUtil.getInstance().notifyInsFailed(mPlacementId, i);
    }

    private class TimeoutRunnable implements Runnable {

        private int insIndex;

        TimeoutRunnable(int insIndex) {
            this.insIndex = insIndex;
        }

        @Override
        public void run() {
            DeveloperLog.LogD("timeout startNextInstance : " + insIndex);
            startNextInstance(insIndex);
        }
    }
}
