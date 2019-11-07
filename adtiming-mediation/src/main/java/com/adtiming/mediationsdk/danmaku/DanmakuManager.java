// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.danmaku;

import android.app.Activity;
import android.graphics.Color;
import android.text.TextUtils;

import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.IOUtil;
import com.adtiming.mediationsdk.utils.crash.CrashUtil;
import com.adtiming.mediationsdk.utils.request.network.Request;
import com.adtiming.mediationsdk.utils.request.network.Response;
import com.adtiming.mediationsdk.utils.JsonHelper;
import com.adtiming.mediationsdk.utils.MediationRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;

class DanmakuManager implements DanmakuCallback {

    private static final class DMHolder {
        private static final DanmakuManager INSTANCE = new DanmakuManager();
    }

    public static DanmakuManager getSingleton() {
        return DMHolder.INSTANCE;
    }

    public void show(final Activity activity, String placementId, int mediationId, String adPackageName) {
        MediationRequest.httpDanmaku(placementId, mediationId,
                adPackageName, new Request.OnRequestCallback() {
                    @Override
                    public void onRequestSuccess(Response response) {
                        try {
                            if (response.code() != 200) {
                                return;
                            }
                            String json = new String(JsonHelper.checkResponse(response));
                            if (TextUtils.isEmpty(json)) {
                                return;
                            }
                            JSONObject danmakuJson = new JSONObject(json);
                            LinkedList<String> danmakus = new LinkedList<>();
                            JSONArray danmakusJsonArray = danmakuJson.getJSONArray("danmakus");
                            for (int i = 0; i < danmakusJsonArray.length(); i++) {
                                danmakus.add(danmakusJsonArray.optString(i));
                            }
                            JSONArray colorJsonArray = danmakuJson.getJSONArray("colors");
                            int[] colors = new int[colorJsonArray.length()];
                            for (int i = 0; i < colorJsonArray.length(); i++) {
                                colors[i] = Color.parseColor(colorJsonArray.optString(i));
                            }
                            DanmakuInfo danmakuModel = new DanmakuInfo();
                            danmakuModel.setType(danmakuJson.getInt("type"));
                            danmakuModel.setColors(colors);
                            danmakuModel.setDanmakus(danmakus);
                            DanmakuCore.getSingleton().show(activity, danmakuModel, DanmakuManager.this);
                        } catch (Exception e) {
                            DeveloperLog.LogD("Danmaku", "Error:" + e.getMessage());
                            CrashUtil.getSingleton().saveException(e);
                        } finally {
                            IOUtil.closeQuietly(response);
                        }
                    }

                    @Override
                    public void onRequestFailed(String error) {
                        DeveloperLog.LogD("Danmaku", "Error:" + error);
                    }
                });
    }

    public void hide() {
        if (!DanmakuCore.getSingleton().isShow()) {
            return;
        }
        DanmakuCore.getSingleton().hide();
    }

    @Override
    public void onFinish() {
        hide();
    }
}
