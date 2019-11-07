// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.danmaku;

import android.app.Activity;

import com.adtiming.mediationsdk.utils.AdtUtil;
import com.adtiming.mediationsdk.utils.device.DeviceUtil;

import java.util.concurrent.atomic.AtomicBoolean;

public class Danmaku {
    private AtomicBoolean isInit = new AtomicBoolean(false);

    private static final class DanmakuHolder {
        private static Danmaku instance = new Danmaku();
    }

    public static Danmaku getSingleton() {
        return DanmakuHolder.instance;
    }

    public void init(Activity activity) {
        if (AdtUtil.getApplication() == null) {
            return;
        }
        if (isInit.get()) {
            return;
        }

        DanmakuLifecycle.getInstance().init(activity);
        isInit.set(true);
    }

    public void show(String placementId, int mediationId, String adPackageName) {
        if (!isInit.get()) {
            return;
        }
        Activity activity = DanmakuLifecycle.getInstance().getActivity();
        if (!DeviceUtil.isActivityAvailable(activity)) {
            return;
        }
        if (!activity.getComponentName().getClassName().contains("com.adtiming")) {
            return;
        }
        DanmakuManager.getSingleton().show(activity, placementId, mediationId, adPackageName);
    }

    public void hide() {
        if (!isInit.get()) {
            return;
        }
        DanmakuManager.getSingleton().hide();
    }
}
