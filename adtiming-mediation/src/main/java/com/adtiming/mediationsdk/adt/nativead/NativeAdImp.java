// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt.nativead;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.adtiming.mediationsdk.utils.Constants;
import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.IOUtil;
import com.adtiming.mediationsdk.utils.ImageUtils;
import com.adtiming.mediationsdk.utils.cache.Cache;
import com.adtiming.mediationsdk.utils.crash.CrashUtil;
import com.adtiming.mediationsdk.utils.error.ErrorCode;
import com.adtiming.mediationsdk.utils.request.network.AdRequest;
import com.adtiming.mediationsdk.utils.request.network.Response;
import com.adtiming.mediationsdk.adt.BaseAdImp;
import com.adtiming.mediationsdk.adt.BaseAdListener;
import com.adtiming.mediationsdk.adt.bean.AdMark;
import com.adtiming.mediationsdk.adt.report.AdReport;
import com.adtiming.mediationsdk.adt.utils.PUtils;
import com.adtiming.mediationsdk.adt.view.AdMarketView;
import com.adtiming.mediationsdk.adt.bean.AdBean;

final class NativeAdImp extends BaseAdImp implements View.OnClickListener, View.OnAttachStateChangeListener {

    private NativeListener mListener;
    private AdBean mAdBean;
    private boolean isImpReported;
    private Ad mAd;

    NativeAdImp(Context context, String placementId) {
        super(context, placementId, Constants.NATIVE);
    }

    @Override
    protected void setListener(BaseAdListener adListener) {
        super.setListener(adListener);
        mListener = (NativeListener) adListener;
    }

    void registerActionView(View view) {
        view.setOnClickListener(this);
        view.addOnAttachStateChangeListener(this);
    }

    /**
     * draws logo based on adMark
     */
    void setUpLogo(ViewGroup parent) {
        final AdMark adMark = mAdBean.getAdMark();
        if (adMark != null) {
            if (!TextUtils.isEmpty(adMark.getLogo())) {
                if (Cache.existCache(mContext, adMark.getLogo())) {
                    drawLogo(parent, ImageUtils.getBitmap(Cache.getCacheFile(mContext, adMark.getLogo(),
                            null)), adMark.getLink());
                } else {
                    Response response = AdRequest.get().url(adMark.getLogo()).connectTimeout(3000)
                            .readTimeout(6000).syncRequest();
                    if (response == null || response.code() != 200) {
                        drawLogo(parent, null, adMark.getLink());
                    } else {
                        try {
                            drawLogo(parent, ImageUtils.getBitmap(response.body().stream()), adMark.getLink());
                        } catch (Exception e) {
                            drawLogo(parent, null, adMark.getLink());
                        }
                    }
                }
            } else {
                drawLogo(parent, null, adMark.getLink());
            }
        }
    }

    private void drawLogo(ViewGroup parent, Bitmap bitmap, String link) {
        AdMarketView mobyAdMarketView = new AdMarketView(mContext, bitmap, link);
        parent.addView(mobyAdMarketView);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mobyAdMarketView.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
        params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        mobyAdMarketView.bringToFront();
    }

    @Override
    protected void destroy() {
        super.destroy();
        mListener = null;
        mAd = null;
        mAdBean = null;
    }

    @Override
    protected void callbackReady() {
        super.callbackReady();
        if (mListener != null && mAd != null) {
            mListener.onNativeAdReady(mPlacementId, mAd);
        }
    }

    @Override
    protected void callbackError(String error) {
        super.callbackError(error);
        if (mListener != null) {
            mListener.onNativeAdFailed(mPlacementId, error);
        }
    }

    @Override
    protected void callbackClick() {
        super.callbackClick();
        if (mListener != null) {
            mListener.onNativeAdClicked(mPlacementId);
        }
    }

    @Override
    protected void callbackShowFailed(String error) {
        super.callbackShowFailed(error);
        if (mListener != null) {
            mListener.onNativeAdShowFailed(mPlacementId, error);
        }
    }

    @Override
    public void onLoadAdSuccess(AdBean adBean) {
        super.onLoadAdSuccess(adBean);
        try {
            mAdBean = adBean;
            if (mListener != null) {
                Bitmap content = ImageUtils.getBitmap(IOUtil.getFileInputStream(Cache.getCacheFile(mContext,
                        adBean.getMainimgUrl(), null)));
                if (content == null) {
                    callbackAdErrorOnUIThread(ErrorCode.ERROR_AD_RESOURCE_EMPTY);
                    return;
                }
                Bitmap icon = ImageUtils.getBitmap(Cache.getCacheFile(mContext, adBean.getIconUrl(), null));

                Ad.Builder builder = new Ad.Builder();
                builder.title(adBean.getTitle()).
                        description(adBean.getDescription())
                        .cta("install").
                        content(content)
                        .icon(icon);

                mAd = builder.build();
                callbackAdReadyOnUIThread();
            }
        } catch (Exception e) {
            callbackAdErrorOnUIThread(e.getMessage());
            CrashUtil.getSingleton().saveException(e);
            DeveloperLog.LogD("Adt-Native", e);
        }
    }

    @Override
    public void onClick(View v) {
        if (mAdBean == null) {
            return;
        }
        AdReport.CLKReport(mContext, mPlacementId, mAdBean);
        PUtils.doClick(mContext, mPlacementId, mAdBean);
        callbackAdClickOnUIThread();
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        try {
            if (isImpReported || mAdBean == null) {
                return;

            }
            mAdManager.onAdShowed();
            AdReport.impReport(mContext, mPlacementId, mAdBean, false);
            isImpReported = true;
        } catch (Exception e) {
            DeveloperLog.LogE("adt-native onViewAttachedToWindow ", e);
            CrashUtil.getSingleton().saveException(e);
            callbackAdShowFailedOnUIThread(e.getLocalizedMessage());
        }
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        isImpReported = false;
        v.removeOnAttachStateChangeListener(this);
    }
}
