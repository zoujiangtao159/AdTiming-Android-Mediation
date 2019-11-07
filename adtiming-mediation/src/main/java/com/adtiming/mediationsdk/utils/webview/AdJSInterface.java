// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.webview;

import android.webkit.JavascriptInterface;

import com.adtiming.mediationsdk.utils.Constants;
import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.cache.DataCache;
import com.adtiming.mediationsdk.utils.crash.CrashUtil;
import com.adtiming.mediationsdk.utils.model.Config;
import com.adtiming.mediationsdk.utils.model.Placement;

import org.json.JSONObject;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;


public class AdJSInterface {
    private String placementId;
    private String ori_data;

    private SoftReference<BaseJsCallback> mCallback;

    public AdJSInterface(String placementId, String ori_data, BaseJsCallback callback) {
        this.placementId = placementId;
        this.ori_data = ori_data;
        this.mCallback = new SoftReference<BaseJsCallback>(callback);
    }

    public void onDestroy() {
        mCallback.clear();
    }

    @JavascriptInterface
    public String getDid() {
        String gaid = "";
        try {
            gaid = DataCache.getInstance().get("AdvertisingId", String.class);
        } catch (Throwable e) {
            DeveloperLog.LogD("AdJSInterface", e);
            CrashUtil.getSingleton().saveException(e);
        }
        return gaid;
    }

    @JavascriptInterface
    public String getDinfo() {
        String dinfo = "";
        try {
            Map<String, String> map = new HashMap<>();
            map.put("did", DataCache.getInstance().get("AdvertisingId", String.class));
            map.put("placement_id", placementId);
            map.put("app_id", DataCache.getInstance().get("PackageName", String.class));
            map.put("make", DataCache.getInstance().get("Manufacturer", String.class));
            map.put("brand", DataCache.getInstance().get("Brand", String.class));
            map.put("model", DataCache.getInstance().get("Model", String.class));
            map.put("osv", DataCache.getInstance().get("OSVersion", String.class));
            map.put("sdkv", Constants.SDK_V);
            map.put("con_type", DataCache.getInstance().get("ConnectType", String.class));
            map.put("carrier", DataCache.getInstance().get("NetworkOperatorName", String.class));
            map.put("lang", DataCache.getInstance().get("Lang", String.class));
            map.put("lang_code", DataCache.getInstance().get("LangCode", String.class));
            dinfo = new JSONObject(map).toString();
        } catch (Throwable e) {
            DeveloperLog.LogD("AdJSInterface", e);
            CrashUtil.getSingleton().saveException(e);
        }
        return dinfo;
    }

    @JavascriptInterface
    public String getInitConfig() {
        Config config = DataCache.getInstance().get("Config", Config.class);
        if (config == null) {
            return "";
        }
        return config.getJsConfig();
    }

    @JavascriptInterface
    public String getCampaign() {
        return ori_data;
    }

    @JavascriptInterface
    public String getPlacement() {
        Config config = DataCache.getInstance().get("Config", Config.class);
        if (config == null) {
            return "";
        }
        Map<String, Placement> placementMap = config.getPlacements();
        if (placementMap == null || !placementMap.containsKey(placementId)) {
            return "";
        }
        return placementMap.get(placementId).getOriData();
    }

    @JavascriptInterface
    public String getPlacementId() {
        return placementId;
    }

    @JavascriptInterface
    public void close() {
        if (mCallback.get() != null) {
            mCallback.get().close();
        }
    }

    @JavascriptInterface
    public void click() {
        if (mCallback.get() != null) {
            mCallback.get().click();
        }
    }

    @JavascriptInterface
    public void wvClick() {
        if (mCallback.get() != null) {
            mCallback.get().wvClick();
        }
    }

    @JavascriptInterface
    public void showClose() {
        if (mCallback.get() != null) {
            mCallback.get().showClose();
        }
    }

    @JavascriptInterface
    public void hideClose() {
        if (mCallback.get() != null) {
            mCallback.get().hideClose();
        }
    }

    public IAJsCallback getCallbackInstance() {
        IAJsCallback callback = (IAJsCallback) mCallback.get();
        return callback != null ? callback : DumbJsCallback.getSingleton();
    }

    @JavascriptInterface
    public boolean isVideoReady() {
        if (mCallback.get() != null) {
            if (mCallback.get() instanceof IAJsCallback) {
                IAJsCallback callback = getCallbackInstance();
                return callback.isVideoReady();
            }
            return false;
        }
        return false;
    }

    @JavascriptInterface
    public boolean playVideo() {
        if (mCallback.get() != null) {
            if (mCallback.get() instanceof IAJsCallback) {
                IAJsCallback callback = getCallbackInstance();
                return callback.playVideo();
            }
            return false;
        }
        return false;
    }

    @JavascriptInterface
    public void loadVideo() {
        if (mCallback.get() != null) {
            if (mCallback.get() instanceof IAJsCallback) {
                IAJsCallback callback = getCallbackInstance();
                callback.loadVideo();
            }
        }
    }

    @JavascriptInterface
    public void postMessage(String msg) {
        BaseJsCallback callback = mCallback.get();
        if (callback != null) {
            if (callback instanceof VideoJsCallback) {
                ((VideoJsCallback) callback).postMessage(msg);
            }
        }
    }

    @JavascriptInterface
    public void addEvent(String event) {
        BaseJsCallback callback = mCallback.get();
        if (callback != null) {
            callback.addEvent(event);
        }
    }
}
