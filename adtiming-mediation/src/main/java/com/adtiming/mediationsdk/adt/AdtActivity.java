// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.adtiming.mediationsdk.utils.DensityUtil;
import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.HandlerUtil;
import com.adtiming.mediationsdk.utils.cache.DataCache;
import com.adtiming.mediationsdk.utils.crash.CrashUtil;
import com.adtiming.mediationsdk.utils.webview.AdJSInterface;
import com.adtiming.mediationsdk.utils.webview.AdWebView;
import com.adtiming.mediationsdk.adt.bean.AdBean;
import com.adtiming.mediationsdk.adt.utils.GpUtil;
import com.adtiming.mediationsdk.adt.utils.webview.ActWebView;
import com.adtiming.mediationsdk.adt.view.DrawCrossMarkView;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class AdtActivity extends Activity {

    private RelativeLayout mLytWeb;
    private AdWebView mAdView;
    private AdJSInterface mJsInterface;
    private HandlerUtil.HandlerHolder mHandler;
    private TimeoutRunnable mRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mLytWeb = new RelativeLayout(this);
            setContentView(mLytWeb);
            String placementId = getIntent().getStringExtra("placementId");
            if (getIntent().getExtras() != null) {
                getIntent().getExtras().setClassLoader(AdBean.class.getClassLoader());
            }
            AdBean adBean = getIntent().getParcelableExtra("ad");
            initAndLoad(placementId, adBean);
        } catch (Throwable e) {
            DeveloperLog.LogD("AdtActivity", e);
            CrashUtil.getSingleton().saveException(e);
            finish();
        }
    }


    private void initAndLoad(String placementId, AdBean adBean) {
        if (TextUtils.isEmpty(adBean.getAdUrl())) {
            finish();
            return;
        }

        mAdView = ActWebView.getInstance().getActView();

        if (mJsInterface == null) {
            mJsInterface = new AdJSInterface(placementId,
                    adBean.getOriData(), null);
        }
        ActWebView.getInstance().addJsInterface(mAdView, mJsInterface, "sdk");
        if (mAdView.getParent() != null) {
            ViewGroup vg = (ViewGroup) mAdView.getParent();
            vg.removeView(mAdView);
        }
        mAdView.setWebViewClient(new AdWebClient());
        mLytWeb.addView(mAdView);
        mAdView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
        mAdView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;

        if (adBean.isWebview()) {
            mAdView.setVisibility(View.VISIBLE);
            //Exit button
            DrawCrossMarkView drawCrossMarkView = new DrawCrossMarkView(this, Color.GRAY);
            mLytWeb.addView(drawCrossMarkView);
            drawCrossMarkView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
            int size = DensityUtil.dip2px(this, 20);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.setMargins(30, 30, 30, 30);
            drawCrossMarkView.setLayoutParams(params);
        } else {
            mAdView.setVisibility(View.GONE);
            ProgressBar progressBar = new ProgressBar(this);
            mLytWeb.addView(progressBar);
            int size = DensityUtil.dip2px(this, 40);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            progressBar.setLayoutParams(params);

            sendTimeoutMsg();
        }
        int sceneId = 0;
        if (DataCache.getInstance().containsKey(placementId + "_sceneId")) {
            sceneId = DataCache.getInstance().get(placementId + "_sceneId", Integer.class);
        }
        String adUrl = adBean.getAdUrl();
        if (adUrl.contains("{scene}")) {
            adUrl = adUrl.replace("{scene}", sceneId + "");
        }
        mAdView.loadUrl(adUrl);
    }

    @Override
    protected void onDestroy() {
        if (mLytWeb != null) {
            mLytWeb.removeAllViews();
        }

        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
            mRunnable = null;
            mHandler = null;
        }
        if (mJsInterface != null) {
            mJsInterface.onDestroy();
            mJsInterface = null;
        }
        ActWebView.getInstance().destroy(mAdView, "sdk");
        super.onDestroy();
    }

    private void sendTimeoutMsg() {
        if (mHandler == null) {
            mHandler = new HandlerUtil.HandlerHolder(null);
        }

        if (mRunnable == null) {
            mRunnable = new TimeoutRunnable();
        }
        mHandler.postDelayed(mRunnable, 8000);
    }

    private class AdWebClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            try {
                DeveloperLog.LogD("shouldOverrideUrlLoading:" + url);
                if (GpUtil.isGp(url)) {
                    GpUtil.goGp(view.getContext(), url);
                    finish();
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
                    finish();
                } else {
                    view.loadUrl(url);
                }
            } catch (Exception t) {
                DeveloperLog.LogD("shouldOverrideUrlLoading error", t);
            }
            return true;
        }
    }

    private class TimeoutRunnable implements Runnable {

        @Override
        public void run() {
            finish();
        }
    }
}
