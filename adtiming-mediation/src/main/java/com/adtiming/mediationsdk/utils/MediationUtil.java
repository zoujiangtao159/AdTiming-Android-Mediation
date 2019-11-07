// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils;

import android.util.SparseArray;

import com.adtiming.mediationsdk.core.Instance;
import com.adtiming.mediationsdk.utils.cache.DataCache;
import com.adtiming.mediationsdk.utils.event.EventId;
import com.adtiming.mediationsdk.utils.event.EventUploadManager;
import com.adtiming.mediationsdk.utils.model.BaseInstance;
import com.adtiming.mediationsdk.utils.model.Placement;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 */
public class MediationUtil {

    private static final String ADAPTER_PKG_PATH = "com.adtiming.mediationsdk.mobileads";
    //testing instance
    private static Map<String, BaseInstance[]> testInstanceMap = new HashMap<>();

    public static void setTestInstance(String placementId, BaseInstance[] testInstances) {
        testInstanceMap.put(placementId, testInstances);
    }

    public static void cleanTestInstance() {
        testInstanceMap.clear();
    }

    private static Map<String, BaseInstance[]> getTestInstanceMap() {
        return testInstanceMap;
    }

    private MediationUtil() {
    }

    /**
     * 
     */
    static String getClassName(String name, String type) {
        return ADAPTER_PKG_PATH +
                "." +
                name +
                type;
    }

    static String getAdType(int adIndex) {
        String adType;
        switch (adIndex) {
            case 0:
                adType = Constants.ADTYPE_BANNER;
                break;
            case 1:
                adType = Constants.ADTYPE_NATIVE;
                break;
            default:
                adType = Constants.ADTYPE_INIT;
                break;
        }
        return adType;
    }

    public static List<Instance> getAbsIns(JSONObject clInfo, Placement placement) {

        JSONArray campaigns = clInfo.optJSONArray("campaigns");
        if (campaigns != null && campaigns.length() > 0) {
            //saves ads data to memory
            DataCache.getInstance().setMEM(placement.getId() + "-campaigns", campaigns);
        }

        Instance[] test = (Instance[]) getTestInstanceMap().get(placement.getId());
        //for testing
        if (test != null && test.length > 0) {
            Instance[] tmp = splitAbsIns((test));
            return new ArrayList<>(Arrays.asList(tmp));
        }

        JSONArray insArray = clInfo.optJSONArray("ins");
        if (insArray == null || insArray.length() <= 0) {
            return Collections.emptyList();
        }

        SparseArray<BaseInstance> insMap = placement.getInsMap();
        if (insMap == null || insMap.size() <= 0) {
            return Collections.emptyList();
        }

        List<Instance> instancesList = new ArrayList<>();
        for (int i = 0; i < insArray.length(); i++) {
            BaseInstance ins = insMap.get(insArray.optInt(i));
            if (ins != null) {
                ins.setIndex(i);
                instancesList.add((Instance) ins);
            } else {
                JSONObject jsonObject = PlacementUtils.placementEventParams(placement.getId());
                JsonUtil.put(jsonObject, "iid", insArray.optInt(i));
                EventUploadManager.getInstance().uploadEvent(EventId.INSTANCE_NOT_FOUND, jsonObject);
            }
        }
        //
        if (instancesList.size() == 0) {
            return Collections.emptyList();
        }
        return instancesList;
    }

    /**
     * Parses mediation order, and saves ad campaigns to memory
     */
    public static Instance[] getInstances(JSONObject clInfo, Placement placement, int bs) {
        if (bs == 0 || placement == null) {
            return new Instance[0];
        }

        JSONArray campaigns = clInfo.optJSONArray("campaigns");
        if (campaigns != null && campaigns.length() > 0) {
            //saves ads data to memory
            DataCache.getInstance().setMEM(placement.getId() + "-campaigns", campaigns);
        }

        Instance[] test = (Instance[]) getTestInstanceMap().get(placement.getId());
        //for testing
        if (test != null && test.length > 0) {
            return splitInsByBs(test, bs);
        }

        JSONArray insArray = clInfo.optJSONArray("ins");
        if (insArray == null || insArray.length() <= 0) {
            return new Instance[0];
        }

        SparseArray<BaseInstance> insMap = placement.getInsMap();
        if (insMap == null || insMap.size() <= 0) {
            return new Instance[0];
        }

        List<Instance> instancesList = new ArrayList<>();
        for (int i = 0; i < insArray.length(); i++) {
            BaseInstance ins = insMap.get(insArray.optInt(i));
            if (ins != null) {
                instancesList.add((Instance) ins);
            }
        }
        //
        if (instancesList.size() == 0) {
            return new Instance[0];
        }

        return splitInsByBs(instancesList.toArray(new Instance[instancesList.size()]), bs);
    }

    private static Instance[] splitInsByBs(Instance[] origin, int bs) {
        Instance[] result = Arrays.copyOf(origin, origin.length);
        int len = origin.length;
        int grpIndex = 0;
        for (int a = 0; a < len; a++) {
            Instance i = result[a];

            i.setIndex(a);

            //when index of instance >= group index, increase group index
            if (bs != 0) {
                if (a >= (grpIndex + 1) * bs) {
                    grpIndex++;
                }

                i.setGrpIndex(grpIndex);

                if (a % bs == 0) {
                    i.setFirst(true);
                }
            }
            i.setObject(null);
            i.setStart(0);
        }
        return result;
    }

    private static Instance[] splitAbsIns(Instance[] origin) {
        //shallow copy!!!
        Instance[] result = Arrays.copyOf(origin, origin.length);
        int len = origin.length;
        for (int a = 0; a < len; a++) {
            Instance i = result[a];

            //resets instance's state if init failed or load failed
            Instance.MEDIATION_STATE state = i.getMediationState();
            if (state == Instance.MEDIATION_STATE.INIT_FAILED) {
                i.setMediationState(Instance.MEDIATION_STATE.NOT_INITIATED);
            } else if (state == Instance.MEDIATION_STATE.LOAD_FAILED) {
                i.setMediationState(Instance.MEDIATION_STATE.NOT_AVAILABLE);
            }
            DeveloperLog.LogD("ins state : " + i.getMediationState().toString());
            if (state != Instance.MEDIATION_STATE.AVAILABLE) {
                i.setObject(null);
                i.setStart(0);
            }
        }
        return result;
    }
}
