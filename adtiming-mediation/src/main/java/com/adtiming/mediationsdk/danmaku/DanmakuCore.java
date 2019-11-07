// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.danmaku;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.device.DeviceUtil;
import com.adtiming.mediationsdk.utils.HandlerUtil;
import com.adtiming.mediationsdk.utils.crash.CrashUtil;

class DanmakuCore {
    private WindowManager mWindowManager = null;
    private WindowManager.LayoutParams mLayoutParams = null;
    private DanmakuTextureView mDanmakuTextureView = null;
    private Context mContext;
    private DanmakuInfo mDanmakuModel;
    private DanmakuCallback mDanmakuCallback;
    private boolean isShow = false;

    private static final class DanmaKuCoreHolder {
        private static final DanmakuCore INSTANCE = new DanmakuCore();
    }

    public static DanmakuCore getSingleton() {
        return DanmaKuCoreHolder.INSTANCE;
    }

    private DanmakuCore() {

    }

    public void show(Activity activity, DanmakuInfo danmakuModel, DanmakuCallback danmakuCallback) {
        if (!DeviceUtil.isActivityAvailable(activity)) {
            return;
        }
        mLayoutParams = initLayoutParams();
        mContext = activity.getApplicationContext();
        mWindowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        mDanmakuModel = danmakuModel;
        mDanmakuCallback = danmakuCallback;
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    mDanmakuTextureView = new DanmakuTextureView(mContext);
                    mDanmakuTextureView.setFitsSystemWindows(true);
                    mDanmakuTextureView.setType(mDanmakuModel.getType());
                    mDanmakuTextureView.setData(mDanmakuModel.getDanmakus());
                    mDanmakuTextureView.setFontColors(mDanmakuModel.getColors());
                    mDanmakuTextureView.setCallback(mDanmakuCallback);
                    mWindowManager.addView(mDanmakuTextureView, mLayoutParams);
                    isShow = true;
                } catch (Exception e) {
                    DeveloperLog.LogD("DanmakuCore", e);
                    CrashUtil.getSingleton().saveException(e);
                }
            }
        });
    }

    public void hide() {
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideDanmaku();
            }
        });
    }

    public void hideDanmaku() {
        try {
            if (mWindowManager != null && mDanmakuTextureView != null) {
                mDanmakuTextureView.setVisibility(View.GONE);
                mWindowManager.removeViewImmediate(mDanmakuTextureView);
                mDanmakuTextureView = null;
                mWindowManager = null;
            }
            isShow = false;
        } catch (Exception e) {
            DeveloperLog.LogD("DanmakuCore", e);
            CrashUtil.getSingleton().saveException(e);
        }
    }

    public boolean isShow() {
        return isShow;
    }

    private WindowManager.LayoutParams initLayoutParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_FULLSCREEN;
        layoutParams.format = PixelFormat.TRANSLUCENT;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        return layoutParams;
    }
}
