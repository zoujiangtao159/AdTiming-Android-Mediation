// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.webview;

import android.webkit.JavascriptInterface;

public interface VideoJsCallback extends BaseJsCallback {

    @JavascriptInterface
    void postMessage(String msg);
}
