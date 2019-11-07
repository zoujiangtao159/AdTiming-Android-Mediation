// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.request.network;

/**
 * 
 */
public final class AdRequest {

    public static Request.RequestBuilder get() {
        return Request.newBuilder().method(Request.Method.GET);
    }

    public static Request.RequestBuilder post() {
        return Request.newBuilder().method(Request.Method.POST);
    }
}
