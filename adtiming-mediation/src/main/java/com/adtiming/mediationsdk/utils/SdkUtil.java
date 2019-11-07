// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils;

import android.Manifest;
import android.app.Activity;
import android.text.TextUtils;

import com.adtiming.mediationsdk.utils.device.DeviceUtil;
import com.adtiming.mediationsdk.utils.device.GdprUtil;
import com.adtiming.mediationsdk.utils.error.AdTimingError;
import com.adtiming.mediationsdk.utils.error.ErrorCode;
import com.adtiming.mediationsdk.utils.model.AdtConfig;
import com.adtiming.mediationsdk.utils.model.Config;
import com.adtiming.mediationsdk.utils.model.Placement;
import com.adtiming.mediationsdk.utils.request.network.util.NetworkChecker;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class SdkUtil {
    private static String[] ADT_PERMISSIONS = new String[]{Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE};

    /**
     * 
     */
    public static AdTimingError banRun(Activity activity, String appKey) {
        AdTimingError error = null;
        //
        if (!DeviceUtil.isActivityAvailable(activity)) {
            error = new AdTimingError(ErrorCode.CODE_INIT_INVALID_REQUEST
                    , ErrorCode.MSG_INIT_INVALID_REQUEST, ErrorCode.CODE_INTERNAL_UNKNOWN_ACTIVITY);
            //init error activity is not available
            AdLog.getSingleton().LogE(error.toString());
            DeveloperLog.LogE(error.toString());
        }
        //
        if (TextUtils.isEmpty(appKey)) {
            error = new AdTimingError(ErrorCode.CODE_INIT_INVALID_REQUEST
                    , ErrorCode.MSG_INIT_INVALID_REQUEST, ErrorCode.CODE_INTERNAL_REQUEST_APPKEY);
            //init error appKey is empty
            AdLog.getSingleton().LogE(error.toString());
            DeveloperLog.LogE(error.toString());
        }
        //
        if (GdprUtil.isGdprSubjected(activity)) {
            error = new AdTimingError(ErrorCode.CODE_INIT_INVALID_REQUEST
                    , ErrorCode.MSG_INIT_INVALID_REQUEST, ErrorCode.CODE_INTERNAL_REQUEST_GDPR);
            //init error gdpr is rejected
            AdLog.getSingleton().LogE(error.toString());
            DeveloperLog.LogE(error.toString());
        }
        //
        if (!PermissionUtil.isGranted(activity, ADT_PERMISSIONS)) {
            error = new AdTimingError(ErrorCode.CODE_INIT_INVALID_REQUEST
                    , ErrorCode.MSG_INIT_INVALID_REQUEST, ErrorCode.CODE_INTERNAL_REQUEST_PERMISSION);
            //init error permission is not granted
            AdLog.getSingleton().LogE(error.toString());
            DeveloperLog.LogE(error.toString());
        }
        //
        if (!NetworkChecker.isAvailable(activity)) {
            error = new AdTimingError(ErrorCode.CODE_INIT_NETWORK_ERROR
                    , ErrorCode.MSG_INIT_NETWORK_ERROR, -1);
            //init error network is not available
            AdLog.getSingleton().LogE(error.toString());
            DeveloperLog.LogE(error.toString());
        }
        return error;
    }

    public static AdtConfig getAdtConfig(Config config) throws Exception {
        AdtConfig adtConfig = new AdtConfig();
        adtConfig.setTkHost(config.getTkHost());
        Map<String, Placement> placementMap = config.getPlacements();
        if (placementMap != null && !placementMap.isEmpty()) {
            Map<String, AdtConfig.PlacementConfig> placementConfigMap = new HashMap<>();
            for (Map.Entry<String, Placement> placementEntry : placementMap.entrySet()) {
                if (placementEntry == null) {
                    continue;
                }
                AdtConfig.PlacementConfig placementConfig = new AdtConfig.PlacementConfig();
                placementConfig.setPid(placementEntry.getKey());
                placementConfig.setVideoSkip(placementEntry.getValue().getVideo_skip());
                placementConfig.setVideoDuration(placementEntry.getValue().getVd());
                placementConfigMap.put(placementEntry.getKey(), placementConfig);
            }
            adtConfig.setPlacementConfigs(placementConfigMap);
        }
        return adtConfig;
    }
}
