// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.danmaku;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.adtiming.mediationsdk.utils.AdtUtil;
import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.crash.CrashUtil;

import java.util.concurrent.atomic.AtomicReference;

public class DanmakuLifecycle implements Application.ActivityLifecycleCallbacks {

    private AtomicReference<Activity> mThisActivity = new AtomicReference<>(null);

    private static final class DKLifecycleHolder {
        private static final DanmakuLifecycle INSTANCE = new DanmakuLifecycle();
    }

    private DanmakuLifecycle() {
        try {
            AdtUtil.getApplication().registerActivityLifecycleCallbacks(this);
        } catch (Exception e) {
            CrashUtil.getSingleton().saveException(e);
        }
    }

    public static DanmakuLifecycle getInstance() {
        return DKLifecycleHolder.INSTANCE;
    }

    public void init(Activity activity) {
        mThisActivity.set(activity);
    }

    public Activity getActivity() {
        return mThisActivity.get();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        mThisActivity.set(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        DeveloperLog.LogD("onActivityStarted: " + activity.toString());
        Activity old = mThisActivity.get();
        if (old == null || old != activity) {
            mThisActivity.set(activity);
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        DeveloperLog.LogD("onActivityDestroyed: " + activity.toString());
        Danmaku.getSingleton().hide();
        Activity current = mThisActivity.get();
        if (current == activity) {
            mThisActivity.set(null);
        }
    }
}
