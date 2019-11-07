// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.core;

import android.text.TextUtils;

import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.MediationRequest;
import com.adtiming.mediationsdk.utils.cache.DataCache;
import com.adtiming.mediationsdk.utils.request.network.Request;
import com.adtiming.mediationsdk.utils.request.network.Response;

import org.json.JSONObject;

/**
 * 
 */
class IapImp {

    private static final float FLOAT_ACCURACY = 0.0001f;

    static void setIap(final float iapNumber, final String currency) {

        if (FLOAT_ACCURACY > iapNumber) {
            DeveloperLog.LogE("iapNumber  is zero");
            return;
        }

        if (TextUtils.isEmpty(currency)) {
            DeveloperLog.LogE("currency is null");
            return;
        }

        MediationRequest.httpIap(
                String.valueOf(iapNumber),
                currency,
                String.valueOf(getIap()),
                new Request.OnRequestCallback() {
                    @Override
                    public void onRequestSuccess(Response response) {
                        try {
                            if (response == null || response.code() != 200) {
                                DeveloperLog.LogE("iap result error");
                                return;
                            }
                            String resStr = response.body().string();
                            DeveloperLog.LogE(String.format("iap result : %s", resStr));
                            if (!TextUtils.isEmpty(resStr)) {
                                JSONObject resObj = new JSONObject(resStr);
                                saveIap(resObj.optDouble("iap_usd", 0L));
                            }
                        } catch (Exception e) {
                            //e.printStackTrace();
                            DeveloperLog.LogE("save iap data error", e);
                        }
                    }

                    @Override
                    public void onRequestFailed(String error) {
                        DeveloperLog.LogE("http iap error=" + error);
                    }
                });
    }

    private static void saveIap(double iapNumber) {
        try {
            if (FLOAT_ACCURACY > iapNumber) {
                DeveloperLog.LogE("iapNumber  is zero");
                return;
            }

            DataCache.getInstance().set("c_adt_iap_number", String.valueOf(iapNumber));
        } catch (Exception e) {
            DeveloperLog.LogE("saveIap error :", e);
        }
    }


    private static float getIap() {
        try {
            String iapStr = DataCache.getInstance().get("c_adt_iap_number", String.class);
            if (TextUtils.isEmpty(iapStr)) {
                return 0.00f;
            }
            return Float.valueOf(iapStr);
        } catch (Exception e) {
            DeveloperLog.LogE("getIap error :", e);
            return 0.00f;
        }
    }
}
