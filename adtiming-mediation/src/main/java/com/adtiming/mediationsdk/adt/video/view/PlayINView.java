// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt.video.view;

import android.annotation.SuppressLint;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.webkit.ValueCallback;

import com.adtiming.mediationsdk.utils.AdtUtil;
import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.HandlerUtil;
import com.adtiming.mediationsdk.utils.crash.CrashUtil;
import com.adtiming.mediationsdk.utils.webview.AdJSInterface;
import com.adtiming.mediationsdk.utils.webview.AdWebView;

public final class PlayINView implements View.OnAttachStateChangeListener {

    private AdWebView mAdView;

    private static final class PIWebViewHolder {
        private static PlayINView sInstance = new PlayINView();
    }

    private PlayINView() {
    }

    public static PlayINView getInstance() {
        return PIWebViewHolder.sInstance;
    }

    public void init() {
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mAdView == null) {
                        mAdView = new AdWebView(AdtUtil.getApplication());
                    }
                    mAdView.loadUrl("about:blank");
                    mAdView.addOnAttachStateChangeListener(PlayINView.this);
                    mAdView.setVisibility(View.GONE);
                } catch (Throwable e) {
                    DeveloperLog.LogD("AdtWebView", e);
                    CrashUtil.getSingleton().saveException(e);
                }
            }
        });
    }

    public AdWebView getAdView() {
        return mAdView;
    }

    public synchronized void evaluateJavascript(final String javascript) {
        if (mAdView == null || TextUtils.isEmpty(javascript)) {
            return;
        }
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    mAdView.loadUrl(javascript);
                } else {
                    mAdView.evaluateJavascript(javascript, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            DeveloperLog.LogD("pi-js", "evaluateJs : " + javascript + " result is : " + value);
                        }
                    });
                }
            }
        });
    }

    @SuppressLint({"AddJavascriptInterface", "SetJavaScriptEnabled"})
    public void addJsInterface(AdJSInterface jsInterface, String name) {
        if (jsInterface != null) {
            mAdView.removeJavascriptInterface(name);
            mAdView.addJavascriptInterface(jsInterface, name);
        }
    }

    public void destroy(String jsName) {
        if (mAdView == null) {
            return;
        }
        mAdView.loadUrl("about:blank");
        mAdView.removeAllViews();
        mAdView.removeJavascriptInterface(jsName);
        mAdView.setWebViewClient(null);
        mAdView.setWebChromeClient(null);
        mAdView.freeMemory();
        mAdView.destroy();
        mAdView = null;
    }

    @Override
    public void onViewAttachedToWindow(View v) {

    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        if (mAdView != null) {
            mAdView.clearHistory();
        }
    }
}
