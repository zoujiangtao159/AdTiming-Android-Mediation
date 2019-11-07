// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.mobileads;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.adtiming.mediationsdk.mediation.CustomAdsAdapter;
import com.adtiming.mediationsdk.mediation.InterstitialAdCallback;
import com.adtiming.mediationsdk.mediation.MediationInfo;
import com.adtiming.mediationsdk.mediation.RewardedVideoCallback;
import com.adtiming.mediationsdk.utils.AdLog;
import com.mopub.common.MoPub;
import com.mopub.common.MoPubReward;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubRewardedVideoListener;
import com.mopub.mobileads.MoPubRewardedVideoManager;
import com.mopub.mobileads.MoPubRewardedVideos;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MoPubAdapter extends CustomAdsAdapter implements MoPubRewardedVideoListener
        , MoPubInterstitial.InterstitialAdListener {
    private static final String TP_PARAM = "imext";

    private volatile InitState mInitState = InitState.NOT_INIT;
    private ConcurrentMap<String, RewardedVideoCallback> mRvCallback;
    private ConcurrentMap<String, MoPubInterstitial> mInterstitialAds;
    private ConcurrentMap<MoPubInterstitial, InterstitialAdCallback> mIsCallback;


    private MoPubRewardedVideoManager.RequestParameters mRequestParameters;
    private String mShowingId;
    private Activity mActivity;

    public MoPubAdapter() {
        mRvCallback = new ConcurrentHashMap<>();
        mInterstitialAds = new ConcurrentHashMap<>();
        mIsCallback = new ConcurrentHashMap<>();
        // adjustment requested by MoPub to be able to report on this incremental supply
        mRequestParameters = new MoPubRewardedVideoManager.RequestParameters(TP_PARAM);
    }

    @Override
    public String getMediationVersion() {
        return MoPub.SDK_VERSION;
    }

    @Override
    public String getAdapterVersion() {
        return MediationInfo.ADAPTER_8_VERSION;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.PLAT_ID_8;
    }

    @Override
    public void onResume(Activity activity) {
        super.onResume(activity);
        mActivity = activity;
        MoPub.onCreate(activity);
        MoPub.onStart(activity);
        MoPub.onResume(activity);
    }

    @Override
    public void onPause(Activity activity) {
        super.onPause(activity);
        MoPub.onPause(activity);
        MoPub.onStop(activity);
    }

    private void initSDK(Activity activity, String pid) {
        mInitState = InitState.INIT_PENDING;
        SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(pid).build();
        MoPub.initializeSdk(activity, sdkConfiguration, new SdkInitializationListener() {
            @Override
            public void onInitializationFinished() {
                MoPubRewardedVideos.setRewardedVideoListener(MoPubAdapter.this);
                for (Map.Entry<String, RewardedVideoCallback> videoCallbackEntry : mRvCallback.entrySet()) {
                    if (videoCallbackEntry != null) {
                        videoCallbackEntry.getValue().onRewardedVideoInitSuccess();
                    }
                }

                for (Map.Entry<MoPubInterstitial, InterstitialAdCallback> interstitialAdCallbackEntry : mIsCallback.entrySet()) {
                    if (interstitialAdCallbackEntry != null) {
                        interstitialAdCallbackEntry.getValue().onInterstitialAdInitSuccess();
                    }
                }
                mInitState = InitState.INIT_SUCCESS;
            }
        });
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap
            , final RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String pid = (String) dataMap.get("pid");
        switch (mInitState) {
            case NOT_INIT:
                mRvCallback.put(pid, callback);
                initSDK(activity, pid);
                break;
            case INIT_PENDING:
                mRvCallback.put(pid, callback);
                break;
            case INIT_SUCCESS:
                callback.onRewardedVideoInitSuccess();
                break;
            case INIT_FAIL:
                callback.onRewardedVideoInitFailed("MoPub initRewardedVideo failed");
                break;
            default:
                break;
        }
    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            mActivity = activity;
            if (!mRvCallback.containsKey(adUnitId)) {
                mRvCallback.put(adUnitId, callback);
            }
            if (MoPubRewardedVideos.hasRewardedVideo(adUnitId)) {
                if (callback != null) {
                    callback.onRewardedVideoLoadSuccess();
                }
            } else {
                MoPubRewardedVideos.loadRewardedVideo(adUnitId, mRequestParameters);
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(error);
            }
        }
    }

    @Override
    public void showRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        mActivity = activity;
        if (isRewardedVideoAvailable(adUnitId)) {
            if (!mRvCallback.containsKey(adUnitId)) {
                mRvCallback.put(adUnitId, callback);
            }
            MoPubRewardedVideos.showRewardedVideo(adUnitId);
        } else {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed("MoPub ad not ready to show");
            }
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        return MoPubRewardedVideos.hasRewardedVideo(adUnitId);
    }

    @Override
    public void onRewardedVideoLoadSuccess(@NonNull String adUnitId) {
        AdLog.getSingleton().LogD("onRewardedVideoLoadSuccess : " + adUnitId);
        RewardedVideoCallback callback = mRvCallback.get(adUnitId);
        if (callback != null) {
            callback.onRewardedVideoLoadSuccess();
        }
    }

    @Override
    public void onRewardedVideoLoadFailure(@NonNull String adUnitId, @NonNull MoPubErrorCode errorCode) {
        AdLog.getSingleton().LogE("onRewardedVideoLoadFailure : " + adUnitId + ", errorCode:" + errorCode);
        RewardedVideoCallback callback = mRvCallback.get(adUnitId);
        if (callback != null) {
            callback.onRewardedVideoLoadFailed("MoPub ad load failed : " + errorCode.name());
        }
    }

    @Override
    public void onRewardedVideoStarted(@NonNull String adUnitId) {
        AdLog.getSingleton().LogD("onRewardedVideoStarted : " + adUnitId);
        mShowingId = adUnitId;
        RewardedVideoCallback callback = mRvCallback.get(adUnitId);
        if (callback != null) {
            callback.onRewardedVideoAdStarted();
        }
    }

    @Override
    public void onRewardedVideoPlaybackError(@NonNull String adUnitId, @NonNull MoPubErrorCode errorCode) {
        AdLog.getSingleton().LogE("onRewardedVideoPlaybackError : " + adUnitId);
        RewardedVideoCallback callback = mRvCallback.get(adUnitId);
        if (callback != null) {
            callback.onRewardedVideoAdShowFailed("MoPub ad PlaybackError : " + errorCode.name());
        }
    }

    @Override
    public void onRewardedVideoClicked(@NonNull String adUnitId) {
        AdLog.getSingleton().LogD("onRewardedVideoClicked : " + adUnitId);
        RewardedVideoCallback callback = mRvCallback.get(adUnitId);
        if (callback != null) {
            callback.onRewardedVideoAdClicked();
        }
    }

    @Override
    public void onRewardedVideoClosed(@NonNull String adUnitId) {
        AdLog.getSingleton().LogD("onRewardedVideoClosed : " + adUnitId);
        RewardedVideoCallback callback = mRvCallback.get(adUnitId);
        if (callback != null) {
            callback.onRewardedVideoAdClosed();
        }
    }

    @Override
    public void onRewardedVideoCompleted(@NonNull Set<String> adUnitIds, @NonNull MoPubReward reward) {
        AdLog.getSingleton().LogD("onRewardedVideoCompleted : " + adUnitIds + ", reward:" + reward);
        if (TextUtils.isEmpty(mShowingId)) {
            RewardedVideoCallback callback = mRvCallback.get(mShowingId);
            if (callback != null) {
                callback.onRewardedVideoAdEnded();
                callback.onRewardedVideoAdRewarded();
            }
        }
    }

    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String pid = (String) dataMap.get("pid");
        switch (mInitState) {
            case NOT_INIT:
                mIsCallback.put(getInterstitialAd(activity, pid), callback);
                initSDK(activity, pid);
                break;
            case INIT_PENDING:
                mIsCallback.put(getInterstitialAd(activity, pid), callback);
                break;
            case INIT_SUCCESS:
                callback.onInterstitialAdInitSuccess();
                break;
            case INIT_FAIL:
                callback.onInterstitialAdInitFailed("MoPub initInterstitialAd failed");
                break;
            default:
                break;
        }
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            mActivity = activity;
            MoPubInterstitial interstitial = getInterstitialAd(activity, adUnitId);
            if (!mIsCallback.containsKey(interstitial)) {
                mIsCallback.put(interstitial, callback);
            }
            if (interstitial.isReady()) {
                if (callback != null) {
                    callback.onInterstitialAdLoadSuccess();
                }
            } else {
                interstitial.load();
            }
        } else {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(error);
            }
        }
    }

    @Override
    public void showInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        mActivity = activity;
        if (isInterstitialAdAvailable(adUnitId)) {
            MoPubInterstitial interstitial = getInterstitialAd(activity, adUnitId);
            if (!mIsCallback.containsKey(interstitial)) {
                mIsCallback.put(interstitial, callback);
            }
            interstitial.show();
        } else {
            if (callback != null) {
                callback.onInterstitialAdShowFailed("MoPub interstitial is not ready");
            }
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        MoPubInterstitial interstitial = getInterstitialAd(mActivity, adUnitId);
        return interstitial != null && interstitial.isReady();
    }

    private MoPubInterstitial getInterstitialAd(Activity activity, String adUnitId) {
        MoPubInterstitial interstitialAd = mInterstitialAds.get(adUnitId);
        if (interstitialAd == null) {
            interstitialAd = new MoPubInterstitial(activity, adUnitId);
            interstitialAd.setInterstitialAdListener(this);
            mInterstitialAds.put(adUnitId, interstitialAd);
        }
        return interstitialAd;
    }

    @Override
    public void onInterstitialLoaded(MoPubInterstitial interstitial) {
        AdLog.getSingleton().LogD("onInterstitialLoaded : "
                + (interstitial != null ? interstitial.getLocation() : null));
        InterstitialAdCallback callback = mIsCallback.get(interstitial);
        if (callback != null) {
            callback.onInterstitialAdInitSuccess();
        }
    }

    @Override
    public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
        AdLog.getSingleton().LogE("onInterstitialFailed : "
                + (interstitial != null ? interstitial.getLocation() : null)
                + ", errorCode:" + errorCode);
        InterstitialAdCallback callback = mIsCallback.get(interstitial);
        if (callback != null) {
            callback.onInterstitialAdLoadFailed("MoPub interstitial load failed : " + errorCode.name());
        }
    }

    @Override
    public void onInterstitialShown(MoPubInterstitial interstitial) {
        AdLog.getSingleton().LogD("onInterstitialShown : "
                + (interstitial != null ? interstitial.getLocation() : null));
        InterstitialAdCallback callback = mIsCallback.get(interstitial);
        if (callback != null) {
            callback.onInterstitialAdOpened();
            callback.onInterstitialAdVisible();
        }
    }

    @Override
    public void onInterstitialClicked(MoPubInterstitial interstitial) {
        AdLog.getSingleton().LogD("onInterstitialClicked : "
                + (interstitial != null ? interstitial.getLocation() : null));
        InterstitialAdCallback callback = mIsCallback.get(interstitial);
        if (callback != null) {
            callback.onInterstitialAdClick();
        }
    }

    @Override
    public void onInterstitialDismissed(MoPubInterstitial interstitial) {
        AdLog.getSingleton().LogD("onInterstitialDismissed : "
                + (interstitial != null ? interstitial.getLocation() : null));
        InterstitialAdCallback callback = mIsCallback.get(interstitial);
        if (callback != null) {
            callback.onInterstitialAdClosed();
        }
    }

    /**
     * MoPub sdk init state
     */
    private enum InitState {
        /**
         * 
         */
        NOT_INIT,
        /**
         * 
         */
        INIT_PENDING,
        /**
         * 
         */
        INIT_SUCCESS,
        /**
         * 
         */
        INIT_FAIL
    }
}
