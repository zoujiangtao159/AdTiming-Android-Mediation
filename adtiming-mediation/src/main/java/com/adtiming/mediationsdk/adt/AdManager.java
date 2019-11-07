// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.adtiming.mediationsdk.adt.bean.AdBean;
import com.adtiming.mediationsdk.adt.utils.ResUtil;
import com.adtiming.mediationsdk.adt.utils.ResponseUtil;
import com.adtiming.mediationsdk.utils.Constants;
import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.WorkExecutor;
import com.adtiming.mediationsdk.utils.cache.DataCache;
import com.adtiming.mediationsdk.utils.crash.CrashUtil;
import com.adtiming.mediationsdk.utils.error.ErrorCode;
import com.adtiming.mediationsdk.utils.model.AdtConfig;
import com.adtiming.mediationsdk.utils.request.network.util.NetworkChecker;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class AdManager {

    public interface OnLoadAdCallback {
        void onLoadAdSuccess(AdBean adBean);

        void onLoadAdFailed(String error);
    }

    private AdBean mAdBean;
    private Context mContext;
    private String mPlacementId;
    private int mAdType;
    private OnLoadAdCallback mCallback;
    private boolean isShowed;

    AdManager(String placementId, int adType, OnLoadAdCallback callback) {
        mPlacementId = placementId;
        mAdType = adType;
        mCallback = callback;
    }

    public void loadAd(Context context) {
        if (context == null) {
            callbackError(ErrorCode.ERROR_CONTEXT);
            return;
        }

        if (TextUtils.isEmpty(mPlacementId)) {
            callbackError(ErrorCode.ERROR_PLACEMENT_ID);
            return;
        }

        if (!NetworkChecker.isAvailable(context)) {
            callbackError(ErrorCode.ERROR_NETWORK_NOT_AVAILABLE);
            return;
        }

        mContext = context.getApplicationContext();
        deleteAdPkg();

        isShowed = false;
        JSONArray ad = DataCache.getInstance().get(mPlacementId + "-campaigns", JSONArray.class);
        if (ad == null || ad.length() == 0) {
            callbackError(ErrorCode.ERROR_NO_FILL);
            return;
        }
        String tkHost = DataCache.getInstance().get("AdtConfig", AdtConfig.class).getTkHost();
        mAdBean = ResponseUtil.transformResponse(ad, tkHost);
        if (mAdBean == null) {
            callbackError(ErrorCode.ERROR_NO_FILL);
        } else {
            DeveloperLog.LogD("adt campaignId: " + mAdBean.getCampaignId() + " creativeId : " + mAdBean.getCid());
            preLoadRes();
        }
    }

    public boolean isReady() {
        return !isShowed && mAdBean != null;
    }

    /**
     * @param clazz Interactive Interstitial Video
     */
    public void show(Context context, Class clazz, String placementId) {
        if (!isReady()) {
            return;
        }

        Intent intent = new Intent(context, clazz);
        Bundle bundle = new Bundle();
        bundle.putParcelable("ad", mAdBean);
        intent.putExtra("bundle", bundle);
        intent.putExtra("placementId", placementId);
        if (mAdType == Constants.VIDEO) {
            AdtConfig config = DataCache.getInstance().get("AdtConfig", AdtConfig.class);
            AdtConfig.PlacementConfig placementConfig = config.getPlacementConfigs().get(placementId);
            if (placementConfig != null) {
                intent.putExtra("vd", placementConfig.getVideoDuration());
                intent.putExtra("vskp", placementConfig.getVideoSkip());
            }
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        onAdShowed();
    }

    public AdBean getAd() {
        return mAdBean;
    }

    /**
     * Native & Banner 
     */
    public void onAdShowed() {
        DataCache.getInstance().delete(mPlacementId + "-campaigns");
        isShowed = true;
    }

    public void destroy() {
        isShowed = false;
        mAdBean = null;
        deleteAdPkg();
    }

    private void preLoadRes() {

        final List<String> res = new ArrayList<>();
        final int adType = mAdType;
        switch (adType) {
            case Constants.BANNER:
            case Constants.NATIVE:
                res.add(mAdBean.getIconUrl());
                res.add(mAdBean.getMainimgUrl());
                break;
            case Constants.INTERACTIVE:
                if (mAdBean.getResources() != null) {
                    res.addAll(mAdBean.getResources());
                }
                break;
            case Constants.INTERSTITIAL:
            case Constants.VIDEO:
                if (mAdBean.getResources() != null) {
                    res.addAll(mAdBean.getResources());
                    res.add(mAdBean.getIconUrl());
                    res.add(mAdBean.getVideoUrl());
                    res.add(mAdBean.getMainimgUrl());
                } else {
                    callbackError(ErrorCode.ERROR_AD_RESOURCE_EMPTY);
                    return;
                }
                break;
            default:
                break;
        }

        if (mAdType == Constants.INTERACTIVE) {//Interactive gives a ready callback
            mCallback.onLoadAdSuccess(mAdBean);
            saveAdPkg();
        }
        WorkExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean ready = ResUtil.loadRes(mContext, res);
                    if (ready) {
                        if (adType == Constants.INTERACTIVE) {
                            DeveloperLog.LogD("Adt Interactive has given a ready callback");
                            return;
                        }
                        mCallback.onLoadAdSuccess(mAdBean);
                        saveAdPkg();
                    } else {
                        if (adType == Constants.INTERACTIVE) {
                            DeveloperLog.LogD("Adt Interactive has given a ready callback, no need error callback");
                            return;
                        }
                        callbackError("response is not available");
                    }
                } catch (Exception e) {
                    DeveloperLog.LogD("AdManager load res exception : ", e);
                    CrashUtil.getSingleton().saveException(e);

                    if (adType == Constants.INTERACTIVE) {
                        DeveloperLog.LogD("Adt Interactive has given a ready callback, no need error callback");
                        return;
                    }
                    callbackError("response is not available");
                }
            }
        });
    }

    private void callbackError(String error) {
        mAdBean = null;
        if (mCallback != null) {
            mCallback.onLoadAdFailed(error);
        } else {
            throw new IllegalArgumentException(error);
        }
    }

    private void saveAdPkg() {
        if (TextUtils.isEmpty(mPlacementId) || mAdBean == null) {
            return;
        }
        DataCache.getInstance().setMEM(mPlacementId, mAdBean.getPkgName());
    }

    private void deleteAdPkg() {
        if (TextUtils.isEmpty(mPlacementId)) {
            return;
        }
        DataCache.getInstance().delete(mPlacementId);
    }
}
