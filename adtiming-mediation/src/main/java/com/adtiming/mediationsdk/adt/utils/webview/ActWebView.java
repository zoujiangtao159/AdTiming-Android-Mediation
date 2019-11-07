// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt.utils.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.HandlerUtil;
import com.adtiming.mediationsdk.utils.crash.CrashUtil;
import com.adtiming.mediationsdk.utils.webview.AdJSInterface;
import com.adtiming.mediationsdk.utils.webview.AdWebView;


public final class ActWebView implements View.OnAttachStateChangeListener {

    private AdWebView mActView;

    @Override
    public void onViewAttachedToWindow(View v) {

    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        if (mActView != null) {
            mActView.loadUrl("about:blank");
            mActView.clearHistory();
        }
    }

    private static final class ActWebViewHolder {
        private static ActWebView sInstance = new ActWebView();
    }

    private ActWebView() {
    }

    public static ActWebView getInstance() {
        return ActWebViewHolder.sInstance;
    }

    public void init(final Context context) {
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mActView == null) {
                        mActView = new AdWebView(context.getApplicationContext());
                        mActView.loadUrl("about:blank");
                        mActView.addOnAttachStateChangeListener(ActWebView.this);
                    }
                } catch (Throwable e) {
                    DeveloperLog.LogD("ActWebView", e);
                    CrashUtil.getSingleton().saveException(e);
                }
            }
        });
    }

    public AdWebView getActView() {
        return mActView;
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

        adWebView.stopLoading();
        adWebView.removeAllViews();
        adWebView.removeJavascriptInterface(jsName);
        adWebView.setWebViewClient(null);
        adWebView.setWebChromeClient(null);
        adWebView.freeMemory();
    }
}
