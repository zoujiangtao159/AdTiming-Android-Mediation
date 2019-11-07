// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt.interstitial;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.widget.RelativeLayout;

import com.adtiming.mediationsdk.adt.report.AdReport;
import com.adtiming.mediationsdk.utils.DensityUtil;
import com.adtiming.mediationsdk.utils.webview.AdJSInterface;
import com.adtiming.mediationsdk.utils.webview.AdtWebView;
import com.adtiming.mediationsdk.utils.webview.BaseJsCallback;
import com.adtiming.mediationsdk.adt.BaseActivity;
import com.adtiming.mediationsdk.adt.utils.PUtils;
import com.adtiming.mediationsdk.adt.view.DrawCrossMarkView;

/**
 * 
 */
public class InterstitialActivity extends BaseActivity implements BaseJsCallback {
    private static final String JS_WEBVIEW_RESUME = "javascript:webview_resume()";
    private static final String JS_WEBVIEW_PAUSE = "javascript:webview_pause();";
    private DrawCrossMarkView mDrawCrossMarkView;
    private boolean isBackEnable = true;
    private InterstitialListener mListener;
    private AdJSInterface mJsInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mAdListener.get() != null) {
            mListener = (InterstitialListener) mAdListener.get();
            if (mListener != null) {
                mListener.onInterstitialAdShowed(mPlacementId);
            } else {
                // todo: Report unpredicted error to server
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.loadUrl(JS_WEBVIEW_RESUME);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdView != null) {
            mAdView.loadUrl(JS_WEBVIEW_PAUSE);
        }
    }

    @Override
    protected void initViewAndLoad(String impUrl) {
        super.initViewAndLoad(impUrl);
        if (mJsInterface == null) {
            mJsInterface = new AdJSInterface(mPlacementId,
                    mAdBean.getOriData(), this);
        }
        AdtWebView.getInstance().addJsInterface(mAdView, mJsInterface, "sdk");

        //back button
        mDrawCrossMarkView = new DrawCrossMarkView(this, Color.GRAY);
        mLytAd.addView(mDrawCrossMarkView);
        mDrawCrossMarkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        mDrawCrossMarkView.setVisibility(View.GONE);
        updateCloseBtnStatus();

        int size = DensityUtil.dip2px(this, 20);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.setMargins(30, 30, 30, 30);
        mDrawCrossMarkView.setLayoutParams(params);

        mAdView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        mAdView.loadUrl(impUrl);

        AdReport.impReport(this, mPlacementId, mAdBean, false);
    }

    @Override
    protected void callbackWhenClose() {
        super.callbackWhenClose();
        if (mListener != null) {
            mListener.onInterstitialAdClose(mPlacementId);
        }
    }

    @Override
    protected void callbackWhenError(String error) {
        super.callbackWhenError(error);
        if (mListener != null) {
            mListener.onInterstitialAdFailed(mPlacementId, error);
        }
    }

    @Override
    protected void callbackWhenClick() {
        super.callbackWhenClick();
        if (mListener != null) {
            mListener.onInterstitialAdClicked(mPlacementId);
        }
    }

    private void updateCloseBtnStatus() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (isBackEnable) {
                    if (mDrawCrossMarkView != null) {
                        mDrawCrossMarkView.setVisibility(View.VISIBLE);
                        ObjectAnimator animator = ObjectAnimator.ofFloat(mDrawCrossMarkView, "alpha", 0f, 1f);
                        animator.setDuration(500);
                        animator.start();
                    }
                } else {
                    if (mDrawCrossMarkView != null) {
                        mDrawCrossMarkView.setVisibility(View.GONE);
                    }
                }
            }
        };

        if (mLytAd != null) {
            mLytAd.postDelayed(runnable, 3000);
        }
    }

    @Override
    public void close() {
        callbackAdCloseOnUIThread();
        finish();
    }

    @Override
    public void click() {
        AdReport.CLKReport(this, mPlacementId, mAdBean);
        PUtils.doClick(this, mPlacementId, mAdBean);
        callbackAdClickOnUIThread();
    }

    @Override
    public void showClose() {
        isBackEnable = true;
        updateCloseBtnStatus();
    }

    @Override
    public void hideClose() {
        isBackEnable = false;
        updateCloseBtnStatus();
    }

    @Override
    public void addEvent(String event) {
        if (mListener != null) {
            mListener.onInterstitialAdEvent(mPlacementId, event);
        }
    }

    @Override
    public void wvClick() {
        AdReport.CLKReport(this, mPlacementId, mAdBean);
        callbackAdClickOnUIThread();
    }

    @Override
    public void onBackPressed() {
        if (isBackEnable) {
            callbackAdCloseOnUIThread();
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (mLytAd != null) {
            mLytAd.removeAllViews();
        }
        if (mJsInterface != null) {
            mJsInterface.onDestroy();
            mJsInterface = null;
        }
        if (mAdView != null) {
            mAdView.stopLoading();
            AdtWebView.getInstance().destroy(mAdView, "sdk");
        }

        mAdBean = null;
        mListener = null;
        super.onDestroy();
    }
}
