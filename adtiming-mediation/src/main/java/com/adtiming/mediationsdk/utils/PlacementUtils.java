// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils;

import android.net.Uri;
import android.text.TextUtils;

import com.adtiming.mediationsdk.utils.model.Config;
import com.adtiming.mediationsdk.utils.model.ImpRecord;
import com.adtiming.mediationsdk.utils.model.Placement;
import com.adtiming.mediationsdk.core.Instance;
import com.adtiming.mediationsdk.utils.cache.DataCache;
import com.adtiming.mediationsdk.utils.crash.CrashUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 
 */
public class PlacementUtils {

    public static Map<String, String> getPlacementInfo(String placementId, Instance instances) {
        Config config = DataCache.getInstance().get("Config", Config.class);
        Map<String, String> maps = new HashMap<>();
        maps.put("AppKey", config.getMediations().get(instances.getMediationId()).getApp_key());
        maps.put("PlacementId", placementId);
        maps.put("InstanceKey", instances.getKey());
        maps.put("InstanceId", String.valueOf(instances.getId()));
        return maps;
    }

    /**
     * Gets 1st Placement of the adType in config
     *
     * @return 
     */
    public static Placement getPlacement(int adType) {
        Config config = DataCache.getInstance().get("Config", Config.class);
        if (config == null) {
            return null;
        }

        Set<String> keys = config.getPlacements().keySet();
        for (String key : keys) {
            Placement placement = config.getPlacements().get(key);
            if (placement != null && placement.getT() == adType && placement.getMain() == 1) {
                return placement;
            }
        }
        return null;
    }

    /**
     * Gets the 1st Placement if PlacementId is null
     *
     * @param placementId 
     * @return
     */
    public static Placement getPlacement(String placementId) {
        Config config = DataCache.getInstance().get("Config", Config.class);
        if (config == null) {
            return null;
        }
        return config.getPlacements().get(placementId);
    }


    public static String getBaByPid(Placement placement, int maxImpr) {
        StringBuilder ba = new StringBuilder();
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);
            Date date = new Date();
            String today = dateFormat.format(date);

            ImpRecord impRecord = parseFromJson(DataCache.getInstance().get("ImpRecord", String.class));
            if (impRecord == null) {
                return "";
            }

            Map<String, List<ImpRecord.Imp>> impMap = impRecord.getImpMap();
            String tmpKey = String.valueOf(placement.getId()).trim().concat("_imp");
            if (impMap == null || !impMap.containsKey(tmpKey)) {
                return "";
            }

            List<ImpRecord.Imp> imprs = impMap.get(tmpKey);
            if (imprs == null || imprs.isEmpty()) {
                return "";
            }
            List<ImpRecord.Imp> del_keys = new ArrayList<>();
            for (ImpRecord.Imp imp : imprs) {
                if (imp == null) {
                    continue;
                }

                if (!TextUtils.equals(today, imp.getTime())) {
                    del_keys.add(imp);
                    continue;
                }

                int imprCount = imp.getImpCount();
                long lastImpr = imp.getLashImpTime();

                int imprInterval = placement.getIi();
                if (imprCount >= maxImpr ||
                        System.currentTimeMillis() - lastImpr < imprInterval * 1000) {
                    ba = ba.append(imp.getPkgName()).append(",");
                }
            }

            //removing
            for (ImpRecord.Imp del_key : del_keys) {
                imprs.remove(del_key);
            }
            impMap.put(tmpKey, imprs);
            impRecord.setImpMap(impMap);

            DataCache.getInstance().set("ImpRecord", Uri.encode(transformToString(impRecord)));
        } catch (Throwable t) {
            DeveloperLog.LogD("PlacementUtils", t);
            CrashUtil.getSingleton().saveException(t);
        }
        if (ba.length() > 0) {
            return ba.substring(0, ba.length() - 1);
        } else {
            return ba.toString();
        }
    }

    public static JSONObject placementEventParams(String placementId) {
        JSONObject jsonObject = new JSONObject();
        JsonUtil.put(jsonObject, "pid", placementId);
        return jsonObject;
    }

    protected static ImpRecord parseFromJson(String s) {
        if (TextUtils.isEmpty(s)) {
            return null;
        }
        ImpRecord impRecord = new ImpRecord();
        try {
            DeveloperLog.LogD("PlacementUtils imp string : " + Uri.decode(s));
            JSONObject object = new JSONObject(Uri.decode(s));
            Map<String, List<ImpRecord.Imp>> impMap = new HashMap<>();
            Iterator<String> keys = object.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                List<ImpRecord.Imp> impList = jsonToImps(object.optJSONArray(key));
                if (impList != null && !impList.isEmpty()) {
                    impMap.put(key, impList);
                }
            }
            impRecord.setImpMap(impMap);
            return impRecord;
        } catch (JSONException e) {
            DeveloperLog.LogD("PlacementUtils", e);
            CrashUtil.getSingleton().saveException(e);
            return null;
        }
    }

    private static List<ImpRecord.Imp> jsonToImps(JSONArray array) {
        if (array == null || array.length() == 0) {
            return null;
        }
        List<ImpRecord.Imp> list = new ArrayList<>();
        int len = array.length();
        for (int i = 0; i < len; i++) {
            JSONObject object = array.optJSONObject(i);
            ImpRecord.Imp imp = new ImpRecord.Imp();
            imp.setLashImpTime(object.optLong("last_imp_time"));
            imp.setImpCount(object.optInt("imp_count"));
            imp.setTime(object.optString("time"));
            imp.setPkgName(object.optString("pkg_name"));
            imp.setPlacmentId(object.optString("placement_id"));
            imp.setCampaignId(object.optString("campaign_id"));
            list.add(imp);
        }
        return list;
    }

    protected static String transformToString(ImpRecord impRecord) {
        try {
            JSONObject object = new JSONObject();
            Map<String, List<ImpRecord.Imp>> impMap = impRecord.getImpMap();
            Set<Map.Entry<String, List<ImpRecord.Imp>>> impEntrys = impMap.entrySet();
            for (Map.Entry<String, List<ImpRecord.Imp>> entry : impEntrys) {
                List<ImpRecord.Imp> list = entry.getValue();
                if (list == null || list.isEmpty()) {
                    continue;
                }
                JSONArray array = new JSONArray();
                for (ImpRecord.Imp imp : list) {
                    JSONObject o = new JSONObject();
                    o.put("campaign_id", imp.getCampaignId());
                    o.put("imp_count", imp.getImpCount());
                    o.put("last_imp_time", imp.getLashImpTime());
                    o.put("pkg_name", imp.getPkgName());
                    o.put("placement_id", imp.getPlacmentId());
                    o.put("time", imp.getTime());

                    array.put(o);
                }
                object.put(entry.getKey(), array);
            }
            return object.toString();
        } catch (Exception e) {
            DeveloperLog.LogD("PlacementUtils", e);
            CrashUtil.getSingleton().saveException(e);
            return null;
        }
    }

    public static void savePlacementImprCount(String placementId) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);
            Date date = new Date();

            String today = dateFormat.format(date);

            ImpRecord impRecord = parseFromJson(DataCache.getInstance().get("DayImpRecord", String.class));
            if (impRecord == null) {
                impRecord = new ImpRecord();
            }

            Map<String, List<ImpRecord.Imp>> impsMap = impRecord.getImpMap();
            if (impsMap == null) {
                impsMap = new HashMap<>();
            }

            String tpmKey = placementId.trim().concat("day_impr");

            List<ImpRecord.Imp> imps = impsMap.get(tpmKey);

            if (imps != null && !imps.isEmpty()) {
                ImpRecord.Imp imp = imps.get(0);
                if (imp == null) {
                    imp = new ImpRecord.Imp();
                    imp.setTime(today);
                    imp.setImpCount(1);
                } else {
                    if (today.equals(imp.getTime())) {
                        imp.setImpCount(imp.getImpCount() + 1);
                    } else {
                        imp.setTime(today);
                        imp.setImpCount(1);
                    }
                    imps.clear();
                }
                imps.add(imp);
            } else {
                imps = new ArrayList<>();
                ImpRecord.Imp imp = new ImpRecord.Imp();
                imp.setTime(today);
                imp.setImpCount(1);
                imps.add(imp);
            }

            impsMap.put(tpmKey, imps);
            impRecord.setImpMap(impsMap);

            DataCache.getInstance().set("DayImpRecord", Uri.encode(transformToString(impRecord)));
        } catch (Throwable e) {
            DeveloperLog.LogD("PlacementUtils", e);
            CrashUtil.getSingleton().saveException(e);
        }
    }

    public static int getPlacementImprCount(String placementId) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);
            Date date = new Date();

            String today = dateFormat.format(date);

            ImpRecord impRecord = parseFromJson(DataCache.getInstance().get("DayImpRecord", String.class));
            if (impRecord == null) {
                return 0;
            }

            Map<String, List<ImpRecord.Imp>> impsMap = impRecord.getImpMap();
            if (impsMap == null) {
                return 0;
            }

            String tpmKey = placementId.trim().concat("day_impr");

            List<ImpRecord.Imp> imps = impsMap.get(tpmKey);

            if (imps != null && !imps.isEmpty()) {
                ImpRecord.Imp imp = imps.get(0);
                if (imp != null && imp.getTime().equals(today)) {
                    return imp.getImpCount();
                }
            }

            return 0;
        } catch (Throwable e) {
            DeveloperLog.LogD("PlacementUtils", e);
            CrashUtil.getSingleton().saveException(e);
        }
        return 0;
    }
}
