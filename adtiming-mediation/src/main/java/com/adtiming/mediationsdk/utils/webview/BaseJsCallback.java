// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.webview;

import android.webkit.JavascriptInterface;

/**
 * Base JSInterface callback to client
 */
public interface BaseJsCallback {

    /**
     * callback when ad closes
     */
    @JavascriptInterface
    void close();

    /**
     * callback when ad is clicked
     */
    @JavascriptInterface
    void click();

    /**
     * callback when close btn can be shown
     */
    @JavascriptInterface
    void showClose();

    /**
     * callback when close btn should be closed
     */
    @JavascriptInterface
    void hideClose();

    @JavascriptInterface
    void addEvent(String event);

    @JavascriptInterface
    void wvClick();
}
