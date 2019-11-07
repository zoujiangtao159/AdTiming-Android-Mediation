// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt.banner;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.adtiming.mediationsdk.utils.Constants;
import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.HandlerUtil;
import com.adtiming.mediationsdk.utils.IOUtil;
import com.adtiming.mediationsdk.utils.ImageUtils;
import com.adtiming.mediationsdk.utils.cache.Cache;
import com.adtiming.mediationsdk.utils.crash.CrashUtil;
import com.adtiming.mediationsdk.utils.error.ErrorCode;
import com.adtiming.mediationsdk.utils.request.network.AdRequest;
import com.adtiming.mediationsdk.utils.request.network.Request;
import com.adtiming.mediationsdk.utils.request.network.Response;
import com.adtiming.mediationsdk.adt.BaseAdImp;
import com.adtiming.mediationsdk.adt.BaseAdListener;
import com.adtiming.mediationsdk.adt.bean.AdMark;
import com.adtiming.mediationsdk.adt.report.AdReport;
import com.adtiming.mediationsdk.adt.utils.PUtils;
import com.adtiming.mediationsdk.adt.bean.AdBean;
import com.adtiming.mediationsdk.adt.view.AdMarketView;

final class BannerImp extends BaseAdImp implements View.OnAttachStateChangeListener,
        View.OnClickListener, Request.OnRequestCallback {

    private static final int DEFAULT_WIDTH = 640;
    private static final int DEFAULT_HEIGHT = 100;

    private AdBean mAdBean;
    private BannerListener mListener;
    private RelativeLayout mLytBanner;

    BannerImp(Context context, String placementId, RelativeLayout lytBanner) {
        super(context, placementId, Constants.BANNER);

        mLytBanner = lytBanner;
        mLytBanner.addOnAttachStateChangeListener(this);
    }

    @Override
    protected void setListener(BaseAdListener adListener) {
        super.setListener(adListener);
        mListener = (BannerListener) adListener;
    }

    @Override
    protected void destroy() {
        super.destroy();
        mAdBean = null;
        mLytBanner.removeAllViews();
    }

    @Override
    protected void callbackReady() {
        super.callbackReady();
        if (mListener != null) {
            mListener.onBannerAdReady(mPlacementId, mLytBanner);
        }
    }

    @Override
    protected void callbackError(String error) {
        super.callbackError(error);
        if (mListener != null) {
            mListener.onBannerAdFailed(mPlacementId, error);
        }
    }

    @Override
    protected void callbackClick() {
        super.callbackClick();
        if (mListener != null) {
            mListener.onBannerAdClicked(mPlacementId);
        }
    }

    @Override
    protected void callbackShowFailed(String error) {
        super.callbackShowFailed(error);
        if (mListener != null) {
            mListener.onBannerAdShowFailed(mPlacementId, error);
        }
    }

    private void drawBanner(AdBean adBean) {
        try {
            String url = adBean.getMainimgUrl();
            if (TextUtils.isEmpty(url)) {
                callbackAdErrorOnUIThread(ErrorCode.ERROR_AD_RESOURCE_EMPTY);
                return;
            }
            if (Cache.existCache(mContext, url)) {
                setUpBanner(ImageUtils.getBitmap(Cache.getCacheFile(mContext, url, null)));
            } else {
                AdRequest.get().url(url).connectTimeout(3000).readTimeout(6000).callback(this).performRequest(mContext);
            }
        } catch (Exception e) {
            callbackAdShowFailedOnUIThread(e.getMessage());
            CrashUtil.getSingleton().saveException(e);
            DeveloperLog.LogD("Adt-Banner", e);
        }
    }

    private void setUpBanner(final Bitmap bitmap) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    RelativeLayout.LayoutParams lytParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    mLytBanner.setLayoutParams(lytParams);


                    ImageView bannerView = new ImageView(mContext);
                    bannerView.setImageBitmap(bitmap);
                    mLytBanner.addView(bannerView);

                    //Sets bannerAd's width and height
                    //Adds ImageView and ClickListener                    
                    bannerView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
                    int w = mLytBanner.getWidth();
                    if (w == 0) {
                        w = mContext.getResources().getDisplayMetrics().widthPixels;
                    }
                    bannerView.getLayoutParams().height = w * DEFAULT_HEIGHT / DEFAULT_WIDTH;
                    bannerView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    bannerView.setOnClickListener(BannerImp.this);

                    setUpAdLogo();

                    callbackAdReadyOnUIThread();
                } catch (Exception e) {
                    callbackAdShowFailedOnUIThread(e.getMessage());
                    CrashUtil.getSingleton().saveException(e);
                    DeveloperLog.LogD("Adt-Banner", e);
                }
            }
        };
        HandlerUtil.runOnUiThread(runnable);
    }

    /**
     * Draws logo based on adMark
     */
    private void setUpAdLogo() {
        final AdMark adMark = mAdBean.getAdMark();
        if (adMark != null) {
            if (!TextUtils.isEmpty(adMark.getLogo())) {
                if (Cache.existCache(mContext, adMark.getLogo())) {
                    drawLogo(ImageUtils.getBitmap(Cache.getCacheFile(mContext, adMark.getLogo(), null)),
                            adMark.getLink());
                } else {
                    Response response = AdRequest.get().url(adMark.getLogo()).connectTimeout(3000)
                            .readTimeout(6000).syncRequest();
                    if (response == null || response.code() != 200) {
                        drawLogo(null, adMark.getLink());
                    } else {
                        try {
                            drawLogo(ImageUtils.getBitmap(response.body().stream()), adMark.getLink());
                        } catch (Exception e) {
                            drawLogo(null, adMark.getLink());
                        }
                    }
                }
            } else {
                drawLogo(null, adMark.getLink());
            }
        }
    }

    private void drawLogo(Bitmap bitmap, String link) {
        AdMarketView mobyAdMarketView = new AdMarketView(mContext, bitmap, link);
        mLytBanner.addView(mobyAdMarketView);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mobyAdMarketView.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
        params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        mobyAdMarketView.bringToFront();
    }

    @Override
    public void onLoadAdSuccess(AdBean adBean) {
        super.onLoadAdSuccess(adBean);
        if (adBean == null) {
            callbackAdErrorOnUIThread(ErrorCode.ERROR_NO_FILL);
            return;
        }
        mAdBean = adBean;
        drawBanner(adBean);
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        try {
            AdReport.impReport(mContext, mPlacementId, mAdBean, false);
            mAdManager.onAdShowed();
        } catch (Exception e) {
            DeveloperLog.LogE("adt-banner onViewAttachedToWindow ", e);
            CrashUtil.getSingleton().saveException(e);
            callbackAdShowFailedOnUIThread(e.getLocalizedMessage());
        }
    }

    @Override
    public void onViewDetachedFromWindow(View v) {

    }

    @Override
    public void onClick(View v) {
        AdReport.CLKReport(mContext, mPlacementId, mAdBean);
        PUtils.doClick(mContext, mPlacementId, mAdBean);
        callbackAdClickOnUIThread();
    }

    @Override
    public void onRequestSuccess(Response response) {
        try {
            if (response.code() == 200) {
                setUpBanner(ImageUtils.getBitmap(response.body().stream()));
            } else {
                callbackAdErrorOnUIThread(ErrorCode.ERROR_AD_RESOURCE_EMPTY);
            }
        } catch (Exception e) {
            CrashUtil.getSingleton().saveException(e);
            DeveloperLog.LogD("Adt-Banner", e);
            callbackAdShowFailedOnUIThread(e.getMessage());
        } finally {
            IOUtil.closeQuietly(response);
        }
    }

    @Override
    public void onRequestFailed(String error) {
        callbackAdErrorOnUIThread(error);
    }
}
