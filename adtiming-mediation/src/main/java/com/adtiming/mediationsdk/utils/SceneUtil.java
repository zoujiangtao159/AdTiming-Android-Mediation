// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils;

import android.text.TextUtils;

import com.adtiming.mediationsdk.utils.model.Placement;
import com.adtiming.mediationsdk.utils.model.Scene;

import org.json.JSONObject;

import java.util.Map;


/**
 * 
 */
public class SceneUtil {
    public static Scene getScene(Placement placement, String sceneName) {
        if (placement == null || TextUtils.isEmpty(placement.getId())) {
            return null;
        }
        Map<String, Scene> sceneMap = placement.getScenes();
        if (sceneMap != null) {
            Scene sceneValue;
            if (TextUtils.isEmpty(sceneName)) {
                for (Map.Entry<String, Scene> sceneEntry : sceneMap.entrySet()) {
                    sceneValue = sceneEntry.getValue();
                    if (sceneValue != null && 1 == sceneValue.getIsd()) {
                        return sceneValue;
                    }
                }
            } else {
                return sceneMap.get(sceneName);
            }
        }
        return null;
    }

    public static JSONObject sceneCappedReport(String placementId, String scene) {
        JSONObject jsonObject = new JSONObject();
        JsonUtil.put(jsonObject, "pid", placementId);
        JsonUtil.put(jsonObject, "scene", scene);
        return jsonObject;
    }
}
