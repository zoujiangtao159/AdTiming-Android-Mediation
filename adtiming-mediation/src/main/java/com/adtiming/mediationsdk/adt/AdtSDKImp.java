// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt;

import android.content.Context;

import com.adtiming.mediationsdk.adt.utils.webview.ActWebView;

final class AdtSDKImp {

    static void initialize(final Context context) {
        ActWebView.getInstance().init(context);
    }
}
