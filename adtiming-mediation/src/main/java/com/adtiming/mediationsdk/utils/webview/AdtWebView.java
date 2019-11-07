// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.webkit.ValueCallback;

import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.HandlerUtil;
import com.adtiming.mediationsdk.utils.AdtUtil;
import com.adtiming.mediationsdk.utils.crash.CrashUtil;

public class AdtWebView implements View.OnAttachStateChangeListener {

    private AdWebView mAdView;

    private static final class AdtWebViewHolder {
        private static AdtWebView sInstance = new AdtWebView();
    }

    private AdtWebView() {
    }

    public static AdtWebView getInstance() {
        return AdtWebViewHolder.sInstance;
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
                    mAdView.addOnAttachStateChangeListener(AdtWebView.this);
                } catch (Throwable e) {
                    DeveloperLog.LogD("AdtWebView", e);
                    CrashUtil.getSingleton().saveException(e);
                }
            }
        });
    }

    public boolean isSupport(final Context context) {
        try {
            mAdView = new AdWebView(context.getApplicationContext());
        } catch (Throwable e) {
            DeveloperLog.LogD("AdtWebView", e);
            CrashUtil.getSingleton().saveException(e);
            return false;
        }
        return true;
    }

    public AdWebView getAdView() {
        return mAdView;
    }

    public synchronized void evaluateJavascript(final AdWebView webView, final String javascript) {
        if (webView == null || TextUtils.isEmpty(javascript)) {
            return;
        }
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    webView.loadUrl(javascript);
                } else {
                    webView.evaluateJavascript(javascript, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            DeveloperLog.LogD("moby-js", "evaluateJs : " + javascript + " result is : " + value);
                        }
                    });
                }
            }
        });
    }

    @SuppressLint({"AddJavascriptInterface", "SetJavaScriptEnabled"})
    public void addJsInterface(AdWebView webView, AdJSInterface jsInterface, String name) {
        if (jsInterface != null) {
            webView.removeJavascriptInterface(name);
            webView.addJavascriptInterface(jsInterface, name);
        }
    }

    public void destroy(AdWebView adWebView, String jsName) {
        if (adWebView == null) {
            return;
        }
        adWebView.loadUrl("about:blank");
        adWebView.removeAllViews();
        adWebView.removeJavascriptInterface(jsName);
        adWebView.setWebViewClient(null);
        adWebView.setWebChromeClient(null);
        adWebView.freeMemory();
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
