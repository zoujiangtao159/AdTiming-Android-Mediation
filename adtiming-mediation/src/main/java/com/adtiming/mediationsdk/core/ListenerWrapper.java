// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.core;

import com.adtiming.mediationsdk.interactive.InteractiveAdListener;
import com.adtiming.mediationsdk.interstitial.InterstitialAdListener;
import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.HandlerUtil;
import com.adtiming.mediationsdk.utils.error.AdTimingError;
import com.adtiming.mediationsdk.utils.event.EventId;
import com.adtiming.mediationsdk.utils.event.EventUploadManager;
import com.adtiming.mediationsdk.video.RewardedVideoListener;

/**
 * 
 */
public class ListenerWrapper implements RewardedVideoListener, InterstitialAdListener, InteractiveAdListener {
    private RewardedVideoListener mRvListener;
    private InterstitialAdListener mIsListener;
    private InteractiveAdListener mIaListener;


    private boolean canSendCallback(Object listener) {
        return listener != null;
    }

    private void sendCallback(Runnable callbackRunnable) {
        if (callbackRunnable != null) {
            HandlerUtil.runOnUiThread(callbackRunnable);
        }
    }

    public void setRewardedVideoListener(RewardedVideoListener listener) {
        mRvListener = listener;
    }

    public void setAdTimingInterstitialAdListener(InterstitialAdListener listener) {
        mIsListener = listener;
    }

    public void setAdTimingInteractiveAdListener(InteractiveAdListener listener) {
        mIaListener = listener;
    }

    @Override
    public void onRewardedVideoAvailabilityChanged(final boolean available) {
        DeveloperLog.LogD("onRewardedVideoAvailabilityChanged : " + available);
        if (canSendCallback(mRvListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mRvListener.onRewardedVideoAvailabilityChanged(available);
                    if (available) {
                        EventUploadManager.getInstance().uploadEvent(EventId.CALLBACK_LOAD_SUCCESS);
                    } else {
                        EventUploadManager.getInstance().uploadEvent(EventId.CALLBACK_LOAD_ERROR);
                    }
                }
            });
        }
    }

    @Override
    public void onRewardedVideoAdShowed() {
        DeveloperLog.LogD("onRewardedVideoAdShowed");
        if (canSendCallback(mRvListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mRvListener.onRewardedVideoAdShowed();
                    EventUploadManager.getInstance().uploadEvent(EventId.CALLBACK_PRESENT_SCREEN);
                }
            });
        }
    }

    @Override
    public void onRewardedVideoAdShowFailed(final AdTimingError error) {
        DeveloperLog.LogD("onRewardedVideoAdShowFailed : " + error);
        if (canSendCallback(mRvListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mRvListener.onRewardedVideoAdShowFailed(error);
                    EventUploadManager.getInstance().uploadEvent(EventId.CALLBACK_SHOW_FAILED);
                }
            });
        }
    }

    @Override
    public void onRewardedVideoAdClicked() {
        DeveloperLog.LogD("onRewardedVideoAdClicked");
        if (canSendCallback(mRvListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mRvListener.onRewardedVideoAdClicked();
                    EventUploadManager.getInstance().uploadEvent(EventId.CALLBACK_CLICK);
                }
            });
        }
    }

    @Override
    public void onRewardedVideoAdClosed() {
        DeveloperLog.LogD("onRewardedVideoAdClosed");
        if (canSendCallback(mRvListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mRvListener.onRewardedVideoAdClosed();
                    EventUploadManager.getInstance().uploadEvent(EventId.CALLBACK_DISMISS_SCREEN);
                }
            });
        }
    }

    @Override
    public void onRewardedVideoAdStarted() {
        DeveloperLog.LogD("onRewardedVideoAdStarted");
        if (canSendCallback(mRvListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mRvListener.onRewardedVideoAdStarted();
                }
            });
        }
    }

    @Override
    public void onRewardedVideoAdEnded() {
        DeveloperLog.LogD("onRewardedVideoAdEnded : ");
        if (canSendCallback(mRvListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mRvListener.onRewardedVideoAdEnded();
                    EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_END_CARD_DISPLAYED);
                }
            });
        }
    }

    @Override
    public void onRewardedVideoAdRewarded() {
        DeveloperLog.LogD("onRewardedVideoAdRewarded");
        if (canSendCallback(mRvListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mRvListener.onRewardedVideoAdRewarded();
                }
            });
        }
    }

    @Override
    public void onInterstitialAdAvailabilityChanged(final boolean available) {
        DeveloperLog.LogD("onInterstitialAdAvailabilityChanged : " + available);
        if (canSendCallback(mIsListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mIsListener.onInterstitialAdAvailabilityChanged(available);
                    if (available) {
                        EventUploadManager.getInstance().uploadEvent(EventId.CALLBACK_LOAD_SUCCESS);
                    } else {
                        EventUploadManager.getInstance().uploadEvent(EventId.CALLBACK_LOAD_ERROR);
                    }
                }
            });
        }
    }

    @Override
    public void onInterstitialAdShowed() {
        DeveloperLog.LogD("onInterstitialAdShowed");
        if (canSendCallback(mIsListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mIsListener.onInterstitialAdShowed();
                    EventUploadManager.getInstance().uploadEvent(EventId.CALLBACK_PRESENT_SCREEN);
                }
            });
        }
    }

    @Override
    public void onInterstitialAdShowFailed(final AdTimingError error) {
        DeveloperLog.LogD("onInterstitialAdShowFailed");
        if (canSendCallback(mIsListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mIsListener.onInterstitialAdShowFailed(error);
                    EventUploadManager.getInstance().uploadEvent(EventId.CALLBACK_SHOW_FAILED);
                }
            });
        }
    }

    @Override
    public void onInterstitialAdClosed() {
        DeveloperLog.LogD("onInterstitialAdClosed");
        if (canSendCallback(mIsListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mIsListener.onInterstitialAdClosed();
                    EventUploadManager.getInstance().uploadEvent(EventId.CALLBACK_DISMISS_SCREEN);
                }
            });
        }
    }

    @Override
    public void onInterstitialAdClicked() {
        DeveloperLog.LogD("onInterstitialAdClicked");
        if (canSendCallback(mIsListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mIsListener.onInterstitialAdClicked();
                    EventUploadManager.getInstance().uploadEvent(EventId.CALLBACK_CLICK);
                }
            });
        }
    }

    @Override
    public void onInteractiveAdAvailabilityChanged(final boolean available) {
        DeveloperLog.LogD("onInteractiveAdAvailabilityChanged : " + available);
        if (canSendCallback(mIaListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mIaListener.onInteractiveAdAvailabilityChanged(available);
                    if (available) {
                        EventUploadManager.getInstance().uploadEvent(EventId.CALLBACK_LOAD_SUCCESS);
                    } else {
                        EventUploadManager.getInstance().uploadEvent(EventId.CALLBACK_LOAD_ERROR);
                    }
                }
            });
        }
    }

    @Override
    public void onInteractiveAdShowed() {
        DeveloperLog.LogD("onInteractiveAdShowed");
        if (canSendCallback(mIaListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mIaListener.onInteractiveAdShowed();
                    EventUploadManager.getInstance().uploadEvent(EventId.CALLBACK_PRESENT_SCREEN);
                }
            });
        }
    }

    @Override
    public void onInteractiveAdShowFailed(final AdTimingError error) {
        DeveloperLog.LogD("onInteractiveAdShowFailed");
        if (canSendCallback(mIaListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mIaListener.onInteractiveAdShowFailed(error);
                    EventUploadManager.getInstance().uploadEvent(EventId.CALLBACK_SHOW_FAILED);
                }
            });
        }
    }

    @Override
    public void onInteractiveAdClosed() {
        DeveloperLog.LogD("onInteractiveAdClosed");
        if (canSendCallback(mIaListener)) {
            sendCallback(new Runnable() {
                @Override
                public void run() {
                    mIaListener.onInteractiveAdClosed();
                    EventUploadManager.getInstance().uploadEvent(EventId.CALLBACK_DISMISS_SCREEN);
                }
            });
        }
    }
}
