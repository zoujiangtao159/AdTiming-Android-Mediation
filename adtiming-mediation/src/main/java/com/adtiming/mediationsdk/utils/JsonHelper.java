// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils;

import android.text.TextUtils;
import android.util.SparseArray;
import android.util.SparseBooleanArray;

import com.adtiming.mediationsdk.core.Instance;
import com.adtiming.mediationsdk.core.imp.interactivead.IaInstance;
import com.adtiming.mediationsdk.core.imp.interstitialad.IsInstance;
import com.adtiming.mediationsdk.core.imp.rewardedvideo.RvInstance;
import com.adtiming.mediationsdk.utils.cache.DataCache;
import com.adtiming.mediationsdk.utils.crash.CrashUtil;
import com.adtiming.mediationsdk.utils.model.BaseInstance;
import com.adtiming.mediationsdk.utils.model.Config;
import com.adtiming.mediationsdk.utils.model.Events;
import com.adtiming.mediationsdk.utils.model.Mediation;
import com.adtiming.mediationsdk.utils.model.Placement;
import com.adtiming.mediationsdk.utils.model.Scene;
import com.adtiming.mediationsdk.utils.request.network.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Init config response parse helper
 */
public class JsonHelper {

    private JsonHelper() {
    }

    /**
     *
     */
    public static byte[] checkResponse(Response response) {
        byte[] data = null;
        if (response == null) {
            return null;
        }
        if (response.code() != 200) {
            return null;
        }
        try {
            data = response.body().byteArray();
        } catch (Exception e) {
            CrashUtil.getSingleton().saveException(e);
        }
        return data;
    }

    /**
     *
     */
    public static Config formatConfig(String json) {
        Config config = new Config();
        try {
            JSONObject configJson = new JSONObject(json);
            config.setDebug(configJson.optInt("d"));

            //host
            JSONObject hostJson = configJson.optJSONObject("hs");
            if (hostJson != null) {
                config.setTkHost(hostJson.optString("tk"));
                config.setSdkHost(hostJson.optString("sdk"));
                config.setCdn(hostJson.optString("cdn"));
            }
            //Events
            JSONObject events = configJson.optJSONObject("events");
            if (events != null) {
                config.setEvents(new Events(events));
            } else {
                config.setEvents(new Events());
            }
            //
            SparseArray<Mediation> mapps = parseAdNetwork(configJson.optJSONArray("ms"));
            config.setMediations(mapps);
            config.setPlacements(formatPlacement(mapps, configJson.optJSONArray("pls")));
        } catch (Exception e) {
            //
            config = null;
            CrashUtil.getSingleton().saveException(e);
        }

        return config;
    }

    private static Map<String, Placement> formatPlacement(SparseArray<Mediation> mapps, JSONArray placementArray) {
        Map<String, Placement> placementMap = new HashMap<>();
        int len = placementArray.length();
        if (len == 0) {
            return placementMap;
        }
        SparseBooleanArray mainPlacements = new SparseBooleanArray();
        for (int i = 0; i < len; i++) {
            JSONObject placementObject = placementArray.optJSONObject(i);
            String placementId = String.valueOf(placementObject.optInt("id"));
            int adType = placementObject.optInt("t");
            if (!DataCache.getInstance().containsKey("GTPid")) {
                DataCache.getInstance().set("GTPid", placementId);
            }
            Placement placement = new Placement();
            placement.setOriData(placementObject.toString());
            placement.setId(placementId);
            placement.setDm(placementObject.optInt("dm"));
            placement.setVd(placementObject.optInt("vd"));
            placement.setT(adType);
            placement.setMi(placementObject.optInt("mi"));
            placement.setVideo_skip(placementObject.optInt("vk"));
            placement.setIi(placementObject.optInt("ii"));
            placement.setVid(placementObject.optInt("vid"));
            placement.setFrequencyCap(placementObject.optInt("fc"));
            placement.setFrequencyUnit(placementObject.optInt("fu") * 60 * 60 * 1000);
            placement.setFrequencyInterval(placementObject.optInt("fi") * 1000);
            placement.setAp(placementObject.optInt("ap"));
            placement.setRl(placementObject.optInt("rl"));
            placement.setRf(placementObject.optInt("rf"));
            placement.setCs(placementObject.optInt("cs"));
            placement.setBs(placementObject.optInt("bs"));
            placement.setFo(placementObject.optInt("fo"));
            placement.setPt(placementObject.optInt("pt"));
            placement.setMpc(placementObject.optInt("mpc"));
            if (placementObject.has("main")) {
                placement.setMain(placementObject.optInt("main"));
            } else {
                if (!mainPlacements.get(adType, false)) {
                    if (placementObject.optInt("ia") != 1) {
                        placement.setMain(1);
                        mainPlacements.append(adType, true);
                    }
                }
            }
            placement.setScenes(formatScenes(placementObject.optJSONArray("scenes")));
            placement.setInsMap(formatInstances(placementId, mapps, adType, placementObject.optJSONArray("ins")));
            placementMap.put(placementId, placement);
        }
        return placementMap;
    }

    private static Map<String, Scene> formatScenes(JSONArray scenes) {
        Map<String, Scene> sceneMap = new HashMap<>();
        if (scenes != null && scenes.length() > 0) {
            for (int i = 0; i < scenes.length(); i++) {
                Scene scene = new Scene(scenes.optJSONObject(i));
                sceneMap.put(scene.getN(), scene);
            }
        }
        return sceneMap;
    }

    private static SparseArray<BaseInstance> formatInstances(String placementId, SparseArray<Mediation> mapps,
                                                             int adType, JSONArray mPlacementArray) {
        SparseArray<BaseInstance> instanceSparseArray = new SparseArray<>();
        if (mPlacementArray == null || mPlacementArray.length() == 0) {
            return instanceSparseArray;
        }

        int len = mPlacementArray.length();
        for (int i = 0; i < len; i++) {
            JSONObject mPlacementObject = mPlacementArray.optJSONObject(i);
            BaseInstance mInstances = createInstance(adType);
            int instancesId = mPlacementObject.optInt("id");
            int mediationId = mPlacementObject.optInt("m");
            Mediation mediation = mapps.get(mediationId);
            if (mediation == null) {
                continue;
            }
            if (adType == 0 || adType == 1) {
                mInstances.setPath(MediationUtil.getClassName(mediation.getName(), MediationUtil.getAdType(adType)));
            }
            mInstances.setAppKey(mediation.getApp_key());

            String key = mPlacementObject.optString("k");
            mInstances.setId(instancesId);
            if (mediationId == 0) {
                if (TextUtils.isEmpty(key)) {
                    mInstances.setKey(placementId);
                } else {
                    mInstances.setKey(key);
                }
            } else {
                mInstances.setKey(key);
            }
            mInstances.setPlacementId(placementId);
            mInstances.setMediationId(mediationId);
            mInstances.setFrequencyCap(mPlacementObject.optInt("fc"));
            mInstances.setFrequencyUnit(mPlacementObject.optInt("fu") * 60 * 60 * 1000);
            mInstances.setFrequencyInterval(mPlacementObject.optInt("fi") * 1000);
            instanceSparseArray.put(instancesId, mInstances);
        }
        return instanceSparseArray;
    }

    private static BaseInstance createInstance(int adType) {
        switch (adType) {
            case Constants.VIDEO:
                RvInstance rvInstance = new RvInstance();
                rvInstance.setMediationState(Instance.MEDIATION_STATE.NOT_INITIATED);
                return rvInstance;
            case Constants.INTERACTIVE:
                IaInstance iaInstance = new IaInstance();
                iaInstance.setMediationState(Instance.MEDIATION_STATE.NOT_INITIATED);
                return iaInstance;
            case Constants.INTERSTITIAL:
                IsInstance isInstance = new IsInstance();
                isInstance.setMediationState(Instance.MEDIATION_STATE.NOT_INITIATED);
                return isInstance;
            default:
                return new Instance();
        }
    }

    private static SparseArray<Mediation> parseAdNetwork(JSONArray adNetworks) throws Exception {
        SparseArray<Mediation> mediationSparseArray = new SparseArray<>();
        if (adNetworks == null || adNetworks.length() == 0) {
            return mediationSparseArray;
        }
        int len = adNetworks.length();
        for (int i = 0; i < len; i++) {
            JSONObject mediationObject = adNetworks.getJSONObject(i);
            Mediation mediation = new Mediation();
            String appkey = mediationObject.optString("k");
            int id = mediationObject.optInt("id");
            String name = mediationObject.optString("n");
            if (id == 0) {
                mediation.setApp_key(DataCache.getInstance().get("AppKey", String.class));
                name = "Adtiming";
            } else {
                mediation.setApp_key(appkey);
            }
            mediation.setId(id);
            mediation.setName(name);
            mediation.setPath(MediationUtil.getClassName(name, MediationUtil.getAdType(-1)));
            mediationSparseArray.put(id, mediation);
        }
        return mediationSparseArray;
    }
}
