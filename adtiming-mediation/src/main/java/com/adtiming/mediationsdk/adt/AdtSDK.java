// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt;

import android.content.Context;

public final class AdtSDK {

    public static void initializeSdk(Context context) {
        AdtSDKImp.initialize(context);
    }
}
