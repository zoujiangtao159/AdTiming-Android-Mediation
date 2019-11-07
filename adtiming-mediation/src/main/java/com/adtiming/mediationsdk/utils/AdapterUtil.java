// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils;

import android.util.Base64;
import android.util.SparseArray;

import com.adtiming.mediationsdk.mediation.CustomAdsAdapter;
import com.adtiming.mediationsdk.mediation.MediationInfo;
import com.adtiming.mediationsdk.utils.model.AdNetwork;
import com.adtiming.mediationsdk.utils.crash.CrashUtil;

import org.json.JSONArray;

import java.lang.reflect.Constructor;

/**
 * 
 */
public class AdapterUtil {

    private static final String ADAPTER = "Adapter";
    private static final String MEDIATION_ADAPTER_BASE_PATH = "com.adtiming.mediationsdk.mobileads.";
    private static SparseArray<CustomAdsAdapter> mAdapters = new SparseArray<>();
    private static SparseArray<String> mAdapterPaths;

    static {
        mAdapterPaths = new SparseArray<>();
        mAdapterPaths.put(MediationInfo.PLAT_ID_0, getAdapterPath(MediationInfo.PLAT_ID_0));
        mAdapterPaths.put(MediationInfo.PLAT_ID_1, getAdapterPath(MediationInfo.PLAT_ID_1));
        mAdapterPaths.put(MediationInfo.PLAT_ID_2, getAdapterPath(MediationInfo.PLAT_ID_2));
        mAdapterPaths.put(MediationInfo.PLAT_ID_3, getAdapterPath(MediationInfo.PLAT_ID_3));
        mAdapterPaths.put(MediationInfo.PLAT_ID_4, getAdapterPath(MediationInfo.PLAT_ID_4));
        mAdapterPaths.put(MediationInfo.PLAT_ID_6, getAdapterPath(MediationInfo.PLAT_ID_6));
        mAdapterPaths.put(MediationInfo.PLAT_ID_7, getAdapterPath(MediationInfo.PLAT_ID_7));
        mAdapterPaths.put(MediationInfo.PLAT_ID_8, getAdapterPath(MediationInfo.PLAT_ID_8));
        mAdapterPaths.put(MediationInfo.PLAT_ID_10, getAdapterPath(MediationInfo.PLAT_ID_10));
        mAdapterPaths.put(MediationInfo.PLAT_ID_11, getAdapterPath(MediationInfo.PLAT_ID_11));
    }

    /**
     * init接口请求参数时调用此方法，然后向mAdapterMap存值
     */
    public static JSONArray getAdns() {
        JSONArray jsonArray = new JSONArray();
        if (mAdapters == null) {
            mAdapters = new SparseArray<>();
        } else {
            mAdapters.clear();
        }
        //traverses to get adapters
        for (int i = 0; i < mAdapterPaths.size(); i++) {
            try {
                CustomAdsAdapter adapter = createAdapter(mAdapterPaths.get(mAdapterPaths.keyAt(i)));

                mAdapters.put(adapter.getAdNetworkId(), adapter);
                AdNetwork unityAdNetwork = getAdNetWork(adapter);
                jsonArray.put(unityAdNetwork.toJson());
            } catch (Exception e) {
                CrashUtil.getSingleton().saveException(e);
                DeveloperLog.LogD("AdapterUtil getAdns : ", e);
            }
        }
        return jsonArray;
    }

    static SparseArray<CustomAdsAdapter> getAdapterMap() {
        return mAdapters;
    }

    public static CustomAdsAdapter getCustomAdsAdapter(int mediationId) {
        if (mAdapters != null) {
            return mAdapters.get(mediationId);
        }
        return null;
    }

    private static AdNetwork getAdNetWork(CustomAdsAdapter adapter) {
        if (adapter != null) {
            return new AdNetwork(adapter.getAdNetworkId(), adapter.getMediationVersion(), adapter.getAdapterVersion());
        } else {
            return null;
        }
    }

    private static CustomAdsAdapter createAdapter(String className) throws Exception {
        Class<? extends CustomAdsAdapter> adapterClass = Class.forName(className)
                .asSubclass(CustomAdsAdapter.class);
        Constructor<?> adapterConstructor = adapterClass.getDeclaredConstructor((Class[]) null);
        adapterConstructor.setAccessible(true);
        return (CustomAdsAdapter) adapterConstructor.newInstance();
    }

    private static String getAdapterPath(int mediationType) {
        String path = "";
        switch (mediationType) {
            case MediationInfo.PLAT_ID_0:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.PLAT_NAME_0)).concat(ADAPTER);
                break;
            case MediationInfo.PLAT_ID_1:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.PLAT_NAME_1)).concat(ADAPTER);
                break;
            case MediationInfo.PLAT_ID_2:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.PLAT_NAME_2)).concat(ADAPTER);
                break;
            case MediationInfo.PLAT_ID_3:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.PLAT_NAME_3)).concat(ADAPTER);
                break;
            case MediationInfo.PLAT_ID_4:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.PLAT_NAME_4)).concat(ADAPTER);
                break;
            case MediationInfo.PLAT_ID_6:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.PLAT_NAME_6)).concat(ADAPTER);
                break;
            case MediationInfo.PLAT_ID_7:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.PLAT_NAME_7)).concat(ADAPTER);
                break;
            case MediationInfo.PLAT_ID_8:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.PLAT_NAME_8)).concat(ADAPTER);
                break;
            case MediationInfo.PLAT_ID_10:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.PLAT_NAME_10)).concat(ADAPTER);
                break;
            case MediationInfo.PLAT_ID_11:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.PLAT_NAME_11)).concat(ADAPTER);
                break;
            default:
                break;
        }
        DeveloperLog.LogD("adapter path is : " + path);
        return path;
    }

    private static String getAdapterName(String platName) {
        return new String(Base64.decode(platName, Base64.NO_WRAP));
    }
}
