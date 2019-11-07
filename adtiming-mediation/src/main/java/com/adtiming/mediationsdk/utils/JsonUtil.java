// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtil {
    public static JSONObject put(JSONObject jsonObject, String key, Object value) {
        try {
            if (jsonObject == null || value == null) {
                return jsonObject;
            } else if (value instanceof String) {
                if (TextUtils.isEmpty((String) value)) {
                    return jsonObject;
                }
                jsonObject.put(key, value);
            } else if (value instanceof Integer) {
                if (((int) value == 0) && !TextUtils.equals("mid", key)) {
                    return jsonObject;
                }
                jsonObject.put(key, value);
            } else {
                jsonObject.put(key, value);
            }
            return jsonObject;
        } catch (JSONException e) {
            DeveloperLog.LogD("JsonUtil error : ", e);
        }
        return jsonObject;
    }
}
