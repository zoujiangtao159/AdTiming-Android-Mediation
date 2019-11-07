// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.interactive;

public final class IAUtil {
    private IAVideoListener mIAVideoListener;
    private IActiveListener mIActiveListener;

    private static final class IAHolder {
        private static final IAUtil INSTANCE = new IAUtil();
    }

    private IAUtil() {
    }

    public static IAUtil getInstance() {
        return IAHolder.INSTANCE;
    }

    public void registerIAVideoListener(IAVideoListener listener) {
        if (mIAVideoListener != listener) {
            mIAVideoListener = listener;
        }
    }

    public void unregisterIAVideoListener() {
        mIAVideoListener = null;
    }

    public void registerIActiveListener(IActiveListener listener) {
        if (mIActiveListener != listener) {
            mIActiveListener = listener;
        }
    }

    public void unregisterIActiveListener() {
        mIActiveListener = null;
    }

    public void notifyVideoReady() {
        if (mIAVideoListener != null) {
            mIAVideoListener.videoReady();
        }
    }

    public void notifyVideoClose() {
        if (mIAVideoListener != null) {
            mIAVideoListener.videoClose();
        }
    }

    public void notifyVideoShow() {
        if (mIAVideoListener != null) {
            mIAVideoListener.videoShow();
        }
    }

    public void notifyPlayVideo() {
        if (mIActiveListener != null) {
            mIActiveListener.playVideo();
        }
    }

    public void notifyAskVideoReady() {
        if (mIActiveListener != null) {
            mIActiveListener.askVideoReady();
        }
    }

    public void notifyLoadVideo() {
        if (mIActiveListener != null) {
            mIActiveListener.loadVideo();
        }
    }
}
