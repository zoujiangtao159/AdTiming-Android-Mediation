// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt.interactive;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adtiming.mediationsdk.adt.report.AdReport;
import com.adtiming.mediationsdk.utils.AdtUtil;
import com.adtiming.mediationsdk.utils.Constants;
import com.adtiming.mediationsdk.utils.DensityUtil;
import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.HandlerUtil;
import com.adtiming.mediationsdk.utils.cache.Cache;
import com.adtiming.mediationsdk.utils.crash.CrashUtil;
import com.adtiming.mediationsdk.utils.interactive.IAUtil;
import com.adtiming.mediationsdk.utils.interactive.IAVideoListener;
import com.adtiming.mediationsdk.utils.webview.AdJSInterface;
import com.adtiming.mediationsdk.utils.webview.IAJsCallback;
import com.adtiming.mediationsdk.adt.BaseActivity;
import com.adtiming.mediationsdk.adt.utils.GpUtil;

import java.io.File;
import java.net.URL;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * 
 */
public class InteractiveActivity extends BaseActivity implements IAJsCallback, IAVideoListener {

    private static final String JS_ACTIVITY_SHOWED = "javascript:nve.onshow()";
    private static final String JS_ACTIVITY_PAUSED = "javascript:nve.onclose()";
    private static final String JS_VIDEO_READY = "javascript:nve.onplay()";
    private static final String JS_VIDEO_PLAYING = "javascript:nve.onplaying()";
    private static final String JS_VIDEO_ENDED = "javascript:nve.onended()";

    private RelativeLayout titleRLayout;
    private InteractiveTitleView mCloseView;
    private InteractiveListener mListener;

    private AdJSInterface mJsInterface;
    private boolean isVideoReady;
    private boolean isBackPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mLytAd.setBackgroundColor(Color.WHITE);
            if (mAdListener.get() != null) {
                mListener = (InteractiveListener) mAdListener.get();
            }
            if (mListener != null) {
                mListener.onInteractiveAdShowed(mPlacementId);
            }
            IAUtil.getInstance().registerIAVideoListener(this);
            IAUtil.getInstance().notifyLoadVideo();
        } catch (Exception e) {
            DeveloperLog.LogD("InteractiveActivity", e);
            CrashUtil.getSingleton().saveException(e);
            adClose();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //asks if the video ad isready
        IAUtil.getInstance().notifyAskVideoReady();

        InteractiveAdView.getInstance().evaluateJavascript(JS_ACTIVITY_SHOWED);
    }

    @Override
    protected void onPause() {
        InteractiveAdView.getInstance().evaluateJavascript(JS_ACTIVITY_PAUSED);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        try {
            if (mAdView != null && mAdView.canGoBack()) {
                mAdView.goBack();
                isBackPressed = true;
                return;
            }
            callbackAdCloseOnUIThread();
            super.onBackPressed();
        } catch (Exception e) {
            CrashUtil.getSingleton().saveException(e);
        }
    }

    @Override
    protected void onDestroy() {
        DeveloperLog.LogD("interactive onDestroy");
        if (mLytAd != null) {
            mLytAd.removeAllViews();
            mLytAd = null;
        }

        if (titleRLayout != null) {
            titleRLayout.removeAllViews();
            titleRLayout = null;
        }

        if (mJsInterface != null) {
            mJsInterface.onDestroy();
            mJsInterface = null;
        }

        if (mAdView != null) {
            InteractiveAdView.getInstance().destroy("sdk");
        }
        IAUtil.getInstance().unregisterIAVideoListener();
        mListener = null;
        super.onDestroy();
    }

    @Override
    protected void callbackWhenClose() {
        super.callbackWhenClose();
        if (mListener != null) {
            mListener.onInteractiveAdClose(mPlacementId);
        }
    }

    @Override
    protected void callbackWhenError(String error) {
        super.callbackWhenError(error);
        if (mListener != null) {
            mListener.onInteractiveAdFailed(mPlacementId, error);
        }
    }

    @Override
    protected void callbackWhenClick() {
        super.callbackWhenClick();
        if (mListener != null) {
            mListener.onInteractiveAdClicked(mPlacementId);
        }
    }

    @Override
    protected void initViewAndLoad(String impUrl) {
        //adds html5 page title
        initTitleView();

        mAdView = InteractiveAdView.getInstance().getAdView();

        if (mAdView.getParent() != null) {
            ViewGroup group = (ViewGroup) mAdView.getParent();
            group.removeView(mAdView);
        }

        //WebView remote debugging enabled in DEBUG mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)) {
                mAdView.setWebContentsDebuggingEnabled(true);
            }
        }

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        params.topMargin = DensityUtil.dip2px(this, 48);
        mLytAd.addView(mAdView, params);

        mAdView.getSettings().setUseWideViewPort(false);
        mAdView.setWebViewClient(new InteractiveWebClient());
        mAdView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                updateTitle(title);
            }
        });

        setUpJsInterface();
        mAdView.loadUrl(impUrl);

        DeveloperLog.LogD("imp url : " + impUrl);
        AdReport.impReport(AdtUtil.getApplication(), mPlacementId, mAdBean, true);
    }

    private void initTitleView() {
        if (titleRLayout != null) {
            return;
        }
        titleRLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams titleViewParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                DensityUtil.dip2px(this, 48));
        titleViewParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        titleRLayout.setLayoutParams(titleViewParams);
        titleRLayout.setBackgroundColor(Color.WHITE);
        mLytAd.addView(titleRLayout);

        //back button
        InteractiveTitleView backView = new InteractiveTitleView(this);
        backView.setId(InteractiveTitleView.generateViewId());
        backView.setTypeEnum(InteractiveTitleView.TypeEnum.BACK);
        titleRLayout.addView(backView);
        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        RelativeLayout.LayoutParams backParams = new RelativeLayout.LayoutParams(DensityUtil.dip2px(this, 36),
                DensityUtil.dip2px(this, 36));
        backParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        backParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        backView.setLayoutParams(backParams);

        //close button
        mCloseView = new InteractiveTitleView(this);
        mCloseView.setTypeEnum(InteractiveTitleView.TypeEnum.CLOSE);
        titleRLayout.addView(mCloseView);
        mCloseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adClose();
            }
        });

        RelativeLayout.LayoutParams closeParams = new RelativeLayout.LayoutParams(DensityUtil.dip2px(this, 36),
                DensityUtil.dip2px(this, 36));
        closeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        closeParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        mCloseView.setLayoutParams(closeParams);

        //title content
        TextView titleTV = new TextView(this);
        titleTV.setTag("interactive_title");
        titleRLayout.addView(titleTV);
        RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        titleParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        titleTV.setLayoutParams(titleParams);
        //truncates title if too long
        titleTV.setMaxEms(12);
        titleTV.setSingleLine(true);
        titleTV.setEllipsize(TextUtils.TruncateAt.END);
        titleTV.setTextColor(Color.BLACK);
        titleTV.setTextSize(18);

        titleRLayout.bringToFront();
    }

    private void updateTitle(final String title) {
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (titleRLayout != null && mAdView != null) {
                    View view = titleRLayout.findViewWithTag("interactive_title");
                    if (view instanceof TextView) {
                        TextView titleTv = (TextView) view;
                        titleTv.setText(title);
                    }
                }
            }
        });
    }

    private void updateCloseBtnStatus(final boolean visible) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (visible) {
                    if (mCloseView != null) {
                        mCloseView.setVisibility(View.VISIBLE);
                        ObjectAnimator animator = ObjectAnimator.ofFloat(mCloseView,
                                "alpha", 0f, 1f);
                        animator.setDuration(500);
                        animator.start();
                    }
                } else {
                    if (mCloseView != null) {
                        mCloseView.setVisibility(View.GONE);
                    }
                }
            }
        };

        if (mLytAd != null) {
            mLytAd.postDelayed(runnable, 3000);
        }
    }

    private void adClose() {
        callbackAdCloseOnUIThread();
        finish();
    }

    private void setUpJsInterface() {
        if (mJsInterface != null) {
            return;
        }
        mJsInterface = new AdJSInterface(mPlacementId, mAdBean.getOriData(), this);
        InteractiveAdView.getInstance().addJsInterface(mJsInterface, "sdk");
    }

    @JavascriptInterface
    @Override
    public boolean isVideoReady() {
        DeveloperLog.LogD("js isVideoReady");
        IAUtil.getInstance().notifyAskVideoReady();
        return isVideoReady;
    }

    @JavascriptInterface
    @Override
    public boolean playVideo() {
        DeveloperLog.LogD("js playVideo");
        IAUtil.getInstance().notifyPlayVideo();
        return true;
    }

    @Override
    public void loadVideo() {
        DeveloperLog.LogD("js loadVideo");
        IAUtil.getInstance().notifyAskVideoReady();
        if (!isFinishing() && !isVideoReady) {
            IAUtil.getInstance().notifyLoadVideo();
        }
    }

    @JavascriptInterface
    @Override
    public void close() {
        DeveloperLog.LogD("js close");
        adClose();
    }

    @JavascriptInterface
    @Override
    public void click() {

    }

    @JavascriptInterface
    @Override
    public void showClose() {
        updateCloseBtnStatus(true);
    }

    @JavascriptInterface
    @Override
    public void hideClose() {
        updateCloseBtnStatus(false);
    }

    @Override
    public void addEvent(String event) {
        if (mListener != null) {
            mListener.onInteractiveAdEvent(mPlacementId, event);
        }
    }

    @Override
    public void wvClick() {

    }

    @Override
    public void videoReady() {
        DeveloperLog.LogD("video is ready");
        isVideoReady = true;
        InteractiveAdView.getInstance().evaluateJavascript(JS_VIDEO_READY);
    }

    @Override
    public void videoClose() {
        DeveloperLog.LogD("Video is close");
        isVideoReady = false;

        InteractiveAdView.getInstance().evaluateJavascript(JS_VIDEO_ENDED);
    }

    @Override
    public void videoShow() {
        DeveloperLog.LogD("show video");
        InteractiveAdView.getInstance().evaluateJavascript(JS_VIDEO_PLAYING);
    }

    private class InteractiveWebClient extends AdWebClient {

        @Override
        public boolean shouldOverrideUrlLoading(
                WebView view, String url) {
            try {
                DeveloperLog.LogD("shouldOverrideUrlLoading:" + url);
                if (GpUtil.isGp(url)) {
                    GpUtil.goGp(getApplicationContext(), url);
                    adClose();
                } else if (!url.startsWith("http")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        Intent intent = Intent.parseUri(url, Intent.URI_ANDROID_APP_SCHEME);
                        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                    }
                    adClose();
                } else {
                    view.loadUrl(url);
                }
            } catch (Exception t) {
                DeveloperLog.LogD("shouldOverrideUrlLoading error", t);
                CrashUtil.getSingleton().saveException(t);
            }
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (view != null && !isFinishing() && !TextUtils.isEmpty(view.getTitle())) {
                if (isBackPressed && TextUtils.equals(view.getTitle(), "about:blank")) {
                    DeveloperLog.LogD("InteractiveAd" + view.getTitle());
                    adClose();
                    return;
                }
                updateTitle(view.getTitle());
                DeveloperLog.LogD("InteractiveAd-title:" + view.getTitle());
            } else {
                DeveloperLog.LogD("InteractiveAd-title is null");
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            try {
                File header = Cache.getCacheFile(view.getContext(), url, Constants.FILE_HEADER_SUFFIX);
                if (!header.exists()) {
                    super.onPageStarted(view, url, favicon);
                } else {
                    String location = Cache.getValueFromFile(header, Constants.KEY_LOCATION);
                    if (TextUtils.isEmpty(location)) {
                        super.onPageStarted(view, url, favicon);
                    } else {
                        DeveloperLog.LogD("Interactive onPageStarted redirect url : " + location);
                        URL u = new URL(location);
                        view.stopLoading();
                        view.loadUrl(u.toString());
                    }
                }
            } catch (Exception e) {
                super.onPageStarted(view, url, favicon);
                DeveloperLog.LogD("Interactive onPageStarted", e);
                CrashUtil.getSingleton().saveException(e);
            }
        }
    }
}
