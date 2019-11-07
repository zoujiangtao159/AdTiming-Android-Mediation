// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.adtiming.mediationsdk.adt.bean.AdBean;
import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.HandlerUtil;
import com.adtiming.mediationsdk.utils.crash.CrashUtil;
import com.adtiming.mediationsdk.utils.webview.AdWebView;
import com.adtiming.mediationsdk.utils.webview.AdtWebView;
import com.adtiming.mediationsdk.adt.utils.GpUtil;
import com.adtiming.mediationsdk.adt.utils.ResUtil;

import java.lang.ref.SoftReference;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * 
 */
public class BaseActivity extends Activity {
    protected RelativeLayout mLytAd;
    protected AdWebView mAdView;
    protected AdBean mAdBean;
    protected String mPlacementId;
    protected SoftReference<BaseAdListener> mAdListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mLytAd = new RelativeLayout(this);
            setContentView(mLytAd);

            mPlacementId = getIntent().getStringExtra("placementId");
            mAdListener = new SoftReference<>(ListenerMap.getListener(mPlacementId));

            Bundle bundle = getIntent().getBundleExtra("bundle");
            if (bundle == null) {
                callbackAdShowFailedOnUIThread("resource empty");
                callbackAdCloseOnUIThread();
                finish();
                return;
            }
            bundle.setClassLoader(AdBean.class.getClassLoader());
            mAdBean = bundle.getParcelable("ad");
            if (mAdBean == null || mAdBean.getResources() == null) {
                callbackAdShowFailedOnUIThread("resource empty");
                callbackAdCloseOnUIThread();
                finish();
                return;
            }

            String impUrl = mAdBean.getResources().get(0);
            if (TextUtils.isEmpty(impUrl)) {
                callbackAdShowFailedOnUIThread("resource empty");
                callbackAdCloseOnUIThread();
                finish();
                return;
            }

            initViewAndLoad(impUrl);
        } catch (Throwable e) {
            DeveloperLog.LogD("BaseActivity", e);
            CrashUtil.getSingleton().saveException(e);
            callbackAdShowFailedOnUIThread(e.getMessage());
            callbackAdCloseOnUIThread();
            finish();
        }
    }

    protected void initViewAndLoad(String impUrl) {
        mAdView = AdtWebView.getInstance().getAdView();
        if (mAdView.getParent() != null) {
            ViewGroup group = (ViewGroup) mAdView.getParent();
            group.removeView(mAdView);
        }

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        mLytAd.addView(mAdView, params);
        mAdView.setWebViewClient(new AdWebClient());
    }

    /**
     * Ads loading error callback
     *
     * @param error Error info
     */
    protected void callbackAdErrorOnUIThread(final String error) {
        if (mAdListener == null || mAdListener.get() == null) {
            return;
        }
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callbackWhenError(error);
            }
        });
    }

    /**
     * Ads close callback
     */
    protected void callbackAdCloseOnUIThread() {
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callbackWhenClose();
            }
        });
    }

    /**
     * Ads click callback
     */
    protected void callbackAdClickOnUIThread() {
        if (mAdListener == null || mAdListener.get() == null) {
            return;
        }
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callbackWhenClick();
            }
        });
    }

    /**
     * Ads showing error callback
     *
     * @param error Error info
     */
    protected void callbackAdShowFailedOnUIThread(final String error) {
        if (mAdListener == null || mAdListener.get() == null) {
            return;
        }
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callbackWhenShowedFailed(error);
            }
        });
    }

    protected void callbackWhenClose() {
    }

    protected void callbackWhenError(String error) {
    }

    protected void callbackWhenClick() {
    }

    protected void callbackWhenShowedFailed(String error) {
    }

    protected class AdWebClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            try {
                if (GpUtil.isGp(url)) {
                    GpUtil.goGp(view.getContext().getApplicationContext(), url);
                    callbackAdCloseOnUIThread();
                    finish();
                } else if (!url.startsWith("http")) {
                    //Fixme
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
                } else {
                    view.loadUrl(url);
                }
            } catch (Exception e) {
                DeveloperLog.LogD("shouldOverrideUrlLoading error", e);
                CrashUtil.getSingleton().saveException(e);
            }
            return true;
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            WebResourceResponse response = ResUtil.shouldInterceptRequest(view, url);
            if (response == null) {
                DeveloperLog.LogD("response null:" + url);
            }
            return response == null ? super.shouldInterceptRequest(view, url) : response;
        }
    }
}
