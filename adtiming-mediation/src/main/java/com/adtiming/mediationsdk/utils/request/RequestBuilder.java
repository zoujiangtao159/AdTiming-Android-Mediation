// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.request;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.adtiming.mediationsdk.utils.constant.KeyConstants;
import com.adtiming.mediationsdk.utils.device.DeviceUtil;
import com.adtiming.mediationsdk.utils.device.SensorManager;
import com.adtiming.mediationsdk.utils.request.network.util.NetworkChecker;
import com.adtiming.mediationsdk.utils.AdtUtil;
import com.adtiming.mediationsdk.utils.Constants;
import com.adtiming.mediationsdk.utils.DensityUtil;
import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.Gzip;
import com.adtiming.mediationsdk.utils.JsonUtil;
import com.adtiming.mediationsdk.utils.event.Event;
import com.adtiming.mediationsdk.utils.cache.DataCache;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

/**
 * RequestBuilder
 *
 * 
 */
public class RequestBuilder {

    /**
     * 
     */
    public static final class REQUEST_TYPE {
        public static final int REQUEST_TYPE_DANMAKU = 0;
        public static final int REQUEST_TYPE_LR = 1;
        public static final int REQUEST_TYPE_VPC = 2;
        public static final int REQUEST_TYPE_IR = 3;
        public static final int REQUEST_TYPE_IAP = 4;
    }

    private static final Pattern REG_UNMATCHED_PERCENTAGE = Pattern.compile("(?i)%(?![\\da-f]{2})");

    private static class Param {
        private String name, value;

        private Param(String name, Object value) {
            this.name = name;
            this.value = value == null ? "" : value.toString();
        }
    }

    private List<Param> ps;

    public RequestBuilder() {
        this.ps = new ArrayList<>();
    }

    public RequestBuilder(int initialCapacity) {
        this.ps = new ArrayList<>(initialCapacity);
    }

    public RequestBuilder(List<Param> ps) {
        this.ps = ps;
    }

    public RequestBuilder p(String name, Object value) {
        ps.add(new Param(name, value));
        return this;
    }

    public String format(String charset) {
        return format(ps, charset);
    }

    public static String format(List<Param> ps, String charset) {
        try {
            final StringBuilder result = new StringBuilder();
            for (Param p : ps) {
                final String encodedName = URLEncoder.encode(p.name, charset);
                final String encodedValue = URLEncoder.encode(p.value, charset);
                if (result.length() > 0) {
                    result.append('&');
                }
                result.append(encodedName);
                if (encodedValue != null) {
                    result.append('=').append(encodedValue);
                }
            }
            return result.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public String format() {
        return format("UTF-8");
    }

    public static RequestBuilder from(String query) {
        return new RequestBuilder(parse(query));
    }

    public static List<Param> parse(String query) {
        query = REG_UNMATCHED_PERCENTAGE.matcher(query).replaceAll("%25");
        String[] qs = query.split("&", -1);
        List<Param> list = new ArrayList<>(qs.length);
        for (String s : qs) {
            int ei = s.indexOf('=');
            String n, v = null;
            if (ei == -1) {
                n = s;
            } else {
                n = s.substring(0, ei);
                v = s.substring(ei + 1);
            }
            try {
                list.add(new Param(URLDecoder.decode(n, "UTF-8"), v == null ? null : URLDecoder.decode(v, "UTF-8")));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return list;
    }

    public List<Param> params() {
        return ps;
    }


    public static byte[] buildRequestBody(int type, String... extras) throws Exception {
        byte[] bytes = null;
        switch (type) {
            case REQUEST_TYPE.REQUEST_TYPE_DANMAKU:
                bytes = buildDanmakuRequestBody();
                break;
            case REQUEST_TYPE.REQUEST_TYPE_LR:
                bytes = buildLRRequestBody(extras);
                break;
            case REQUEST_TYPE.REQUEST_TYPE_VPC:
                bytes = buildVPCRequestBody(extras);
                break;
            case REQUEST_TYPE.REQUEST_TYPE_IR:
                bytes = buildIRRequestBody(extras);
                break;
            case REQUEST_TYPE.REQUEST_TYPE_IAP:
                bytes = buildIAPequestBody(extras);
                break;
            default:
                break;
        }
        return bytes;
    }

    public static String buildDanmakuUrl(String... extras) {
        if (TextUtils.isEmpty(extras[0])) {
            return "";
        }
        Locale loc = Locale.getDefault();
        String language = loc.getLanguage().toLowerCase(Locale.US);
        if (language.length() == 0) {
            language = "en";
        }
        return extras[0] + "/danmaku?" + new RequestBuilder(2)
                .p("p", extras[1])
                .p("lang", language)
                .p("sdkv", Constants.SDK_V)
                .p("mv", Constants.VERSION)
                .p("m", extras[2])
                .p("ap", extras[3])
                .p("pp", DataCache.getInstance().get("PackageName", String.class))
                .format();
    }

    public static String buildLRUrl(String... extras) {
        if (TextUtils.isEmpty(extras[0])) {
            return "";
        }
        if (TextUtils.isEmpty(extras[1])) {
            return "";
        }

        return extras[0] + "/lr?" + new RequestBuilder()
                .p("v", Constants.API_VERSION_9)
                .p("plat", Constants.DEVICE_PLATFORM)
                .p("mv", Constants.VERSION)
                .p("sdkv", Constants.SDK_V)
                .p("t", extras[1])
                .format();
    }

    public static String buildVPCUrl(String host) {
        if (TextUtils.isEmpty(host)) {
            return "";
        }
        return host + "/vpc?" + new RequestBuilder()
                .p("v", 5)
                .p("plat", Constants.DEVICE_PLATFORM)
                .p("sdkv", Constants.SDK_V)
                .p("k", DataCache.getInstance().get("AppKey", String.class))
                .p("mv", Constants.VERSION)
                .format();
    }

    public static String buildConfigUrl(String... extras) {
        //
        return "https://sdk.adtiming.com/init?" + new RequestBuilder()
                .p("v", Constants.API_VERSION_9)
                .p("plat", Constants.DEVICE_PLATFORM)
                .p("sdkv", Constants.SDK_V)
                .p("k", extras[0])
                .format();
    }

    public static String buildCLUrl(String host) {
        if (TextUtils.isEmpty(host)) {
            return "";
        }
        return host + "/cl?v=" + Constants.API_VERSION_9 +
                "&plat=" + Constants.DEVICE_PLATFORM +
                "&sdkv=" + Constants.SDK_V;
    }

    public static String buildIRUrl(String host) {
        if (TextUtils.isEmpty(host)) {
            return "";
        }

        return host + "/ir?" + new RequestBuilder()
                .p("v", 1)
                .p("plat", Constants.DEVICE_PLATFORM)
                .p("sdkv", Constants.SDK_V)
                .format();
    }

    public static String buildIAPUrl(String host) {
        if (TextUtils.isEmpty(host)) {
            return "";
        }

        return host + "/iap?" + new RequestBuilder()
                .p("v", 1)
                .p("plat", Constants.DEVICE_PLATFORM)
                .p("sdkv", Constants.SDK_V)
                .p("k", DataCache.getInstance().get("AppKey", String.class))
                .p("mv", Constants.VERSION)
                .format();
    }

    public static String buildEventUrl(String eventUrl) {
        if (TextUtils.isEmpty(eventUrl)) {
            return "";
        }
        return eventUrl + "?" + new RequestBuilder()
                .p("v", 1)
                .p("plat", Constants.DEVICE_PLATFORM)
                .p("sdkv", Constants.SDK_V)
                .format();
    }

    private static byte[] buildDanmakuRequestBody() {
        return "".getBytes();
    }

    private static byte[] buildLRRequestBody(String... extras) throws Exception {
        JSONObject jsonObject = getRequestBodyBaseJson();
        JsonUtil.put(jsonObject, KeyConstants.RequestBody.KEY_PID, Integer.parseInt(extras[0]));
        JsonUtil.put(jsonObject, KeyConstants.RequestBody.KEY_SCENE, Integer.parseInt(extras[1]));
        JsonUtil.put(jsonObject, KeyConstants.RequestBody.KEY_ACT, Integer.parseInt(extras[2]));
        JsonUtil.put(jsonObject, KeyConstants.RequestBody.KEY_MID, Integer.parseInt(extras[3]));
        JsonUtil.put(jsonObject, KeyConstants.RequestBody.KEY_IID, Integer.parseInt(extras[4]));
        DeveloperLog.LogD("lr params:" + jsonObject.toString());
        return Gzip.inGZip(jsonObject.toString().getBytes(Charset.forName("UTF-8")));
    }

    private static byte[] buildVPCRequestBody(String... extras) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("t", Long.toString(System.currentTimeMillis()));
        params.put("p", extras[0]);
        params.put("content", extras[1]);
        return Gzip.inGZip(new JSONObject(params).toString().getBytes(Charset.forName("UTF-8")));
    }

    /**
     * All keys must present! Values can be null
     */
    public static byte[] buildConfigRequestBody(JSONArray adapters) throws Exception {
        long start = System.currentTimeMillis();
        DeviceUtil.startBatteryWatch();

        Context context = AdtUtil.getApplication();
        JSONObject body = getRequestBodyBaseJson();
        body.put(KeyConstants.RequestBody.KEY_W, DensityUtil.getPhoneWidth(context));
        body.put(KeyConstants.RequestBody.KEY_H, DensityUtil.getPhoneHeight(context));
        body.put(KeyConstants.RequestBody.KEY_TZ, DeviceUtil.getTimeZone());
        body.put(KeyConstants.RequestBody.KEY_BUILD, Build.DISPLAY);
        body.put(KeyConstants.RequestBody.KEY_LIP, DeviceUtil.getHostIp());
        body.put(KeyConstants.RequestBody.KEY_ADNS, adapters);

        JSONObject androidBody = new JSONObject();
        androidBody.put(KeyConstants.Android.KEY_DEVICE, Build.DEVICE);
        androidBody.put(KeyConstants.Android.KEY_PRODUCE, Build.PRODUCT);
        androidBody.put(KeyConstants.Android.KEY_SD, DensityUtil.getScreenSize(context));
        androidBody.put(KeyConstants.Android.KEY_SS, DensityUtil.getScreenSize(context));
        androidBody.put(KeyConstants.Android.KEY_CPU_ABI, DeviceUtil.getSystemPropertyCPUABI(KeyConstants.Device.KEY_RO_CPU_ABI));
        androidBody.put(KeyConstants.Android.KEY_CPU_ABI2, DeviceUtil.getSystemPropertyCPUABI(KeyConstants.Device.KEY_RO_CPU_ABI2)/*"arm64-v8a"*/);
        androidBody.put(KeyConstants.Android.KEY_CPU_ABI_LIST, DeviceUtil.getSystemPropertyCPUABI(KeyConstants.Device.KEY_RO_CPU_ABI_LIST));
        androidBody.put(KeyConstants.Android.KEY_CPU_ABI_LIST_32, DeviceUtil.getSystemPropertyCPUABI(KeyConstants.Device.KEY_RO_CPU_ABI_LIST_32));
        androidBody.put(KeyConstants.Android.KEY_CPU_ABI_LIST_64, DeviceUtil.getSystemPropertyCPUABI(KeyConstants.Device.KEY_RO_CPU_ABI_LIST_64));
        androidBody.put(KeyConstants.Android.KEY_API_LEVEL, Build.VERSION.SDK_INT);
        androidBody.put(KeyConstants.Android.KEY_D_DPI, DensityUtil.getDensityDpi(context));
        androidBody.put(KeyConstants.Android.KEY_DIM_SIZE, DensityUtil.getScreenSize(context));
        androidBody.put(KeyConstants.Android.KEY_XDP, Integer.toString(DensityUtil.getXdpi(context)));
        androidBody.put(KeyConstants.Android.KEY_YDP, Integer.toString(DensityUtil.getYdpi(context)));
        androidBody.put(KeyConstants.Android.KEY_DFPID, DeviceUtil.getUniquePsuedoID());
        androidBody.put(KeyConstants.Android.KEY_TIME_ZONE, DeviceUtil.getTimeZone());
        androidBody.put(KeyConstants.Android.KEY_ARCH, DeviceUtil.getSystemPropertyCPUABI(KeyConstants.Device.KEY_RO_ARCH));
        androidBody.put(KeyConstants.Android.KEY_CHIPNAME, DeviceUtil.getSystemPropertyCPUABI(KeyConstants.Device.KEY_RO_CHIPNAME));
        androidBody.put(KeyConstants.Android.KEY_BRIDGE, DeviceUtil.getSystemPropertyCPUABI(KeyConstants.Device.KEY_RO_BRIDGE));
        androidBody.put(KeyConstants.Android.KEY_BRIDGE_EXEC, DeviceUtil.getSystemPropertyCPUABI(KeyConstants.Device.KEY_RO_BRIDGE_EXEC)/*"0"*/);
        androidBody.put(KeyConstants.Android.KEY_ZYGOTE, DeviceUtil.getSystemPropertyCPUABI(KeyConstants.Device.KEY_RO_ZYGOTE));
        androidBody.put(KeyConstants.Android.KEY_MOCK_LOCATION, DeviceUtil.getSystemPropertyCPUABI(KeyConstants.Device.KEY_RO_MOCK_LOCATION));
        androidBody.put(KeyConstants.Android.KEY_ISA_ARM, DeviceUtil.getSystemPropertyCPUABI(KeyConstants.Device.KEY_RO_ISA_ARM)/*"default"*/);
        androidBody.put(KeyConstants.Android.KEY_BUILD_USER, DeviceUtil.getSystemPropertyCPUABI(KeyConstants.Device.KEY_RO_BUILD_USER));
        androidBody.put(KeyConstants.Android.KEY_KERNEL_QEMU, DeviceUtil.getSystemPropertyCPUABI(KeyConstants.Device.KEY_RO_KERNEL_QEMU));
        androidBody.put(KeyConstants.Android.KEY_HARDWARE, DeviceUtil.getSystemPropertyCPUABI(KeyConstants.Device.KEY_RO_HARDWARE));
        androidBody.put(KeyConstants.Android.KEY_NATIVE_BRIDGE, DeviceUtil.getSystemPropertyCPUABI(KeyConstants.Device.KEY_NATIVE_BRIDGE)/*"1"*/);
        androidBody.put(KeyConstants.Android.KEY_ISA_X86_FEATURES, "");//"default"      ???????
        androidBody.put(KeyConstants.Android.KEY_ISA_X86_VARIANT, "");//"cortex-a53.a57"     ???????
        androidBody.put(KeyConstants.Android.KEY_ISA_ARM_FEATURES, "");//"default"     ???????
        androidBody.put(KeyConstants.Android.KEY_ISA_ARM_VARIANT, "");//"cortex-a53"     ???????
        androidBody.put(KeyConstants.Android.KEY_ISA_ARM64_FEATURES, "");//"default"     ???????
        androidBody.put(KeyConstants.Android.KEY_ISA_ARM64_VARIANT, "");//"cortex-a53"     ???????

        JSONArray sensorArray = SensorManager.getSingleton().getSensorData();
        androidBody.put(KeyConstants.Android.KEY_SENSOR_SIZE, sensorArray != null ? sensorArray.length() : 0);
        androidBody.put(KeyConstants.Android.KEY_SENSORS, sensorArray != null ? sensorArray : "");

        androidBody.put(KeyConstants.Android.KEY_FB_ID, DeviceUtil.getFacebookId(context));
        androidBody.put(KeyConstants.Android.KEY_AS, DeviceUtil.getInstallVending(context));
        body.put(KeyConstants.RequestBody.KEY_ANDROID, androidBody);

        DeviceUtil.destroyBatteryWatch();
        DeveloperLog.LogD("init params:" + body.toString());

        byte[] zipByte = Gzip.inGZip(body.toString().getBytes(Charset.forName("UTF-8")));
        DeveloperLog.LogD("buildConfigRequestBody cast : " + (System.currentTimeMillis() - start));
        return zipByte;
    }

    private static JSONObject getRequestBodyBaseJson() throws Exception {
        JSONObject body = new JSONObject();
        Map<String, Object> map = DeviceUtil.getLocaleInfo();
        Context context = AdtUtil.getApplication();
        body.put(KeyConstants.RequestBody.KEY_TS, System.currentTimeMillis());
        body.put(KeyConstants.RequestBody.KEY_ZO, DeviceUtil.getTimeZoneOffset());
        body.put(KeyConstants.RequestBody.KEY_SESSION, DeviceUtil.getSessionID());
        body.put(KeyConstants.RequestBody.KEY_UID, DeviceUtil.getUID());
        body.put(KeyConstants.RequestBody.KEY_DID, DataCache.getInstance().get("AdvertisingId", String.class));
        body.put(KeyConstants.RequestBody.KEY_DTYPE, 2);
        body.put(KeyConstants.RequestBody.KEY_JB, DeviceUtil.isRoot() ? 1 : 0);
        body.put(KeyConstants.RequestBody.KEY_LANG, map.get(KeyConstants.RequestBody.KEY_LANG));
        body.put(KeyConstants.RequestBody.KEY_LANG_NAME, map.get(KeyConstants.RequestBody.KEY_LANG_NAME));
        body.put(KeyConstants.RequestBody.KEY_LCOUNTRY, map.get(KeyConstants.RequestBody.KEY_LCOUNTRY));
        body.put(KeyConstants.RequestBody.KEY_BUNDLE, context != null ? context.getPackageName() : "");
        body.put(KeyConstants.RequestBody.KEY_MAKE, Build.MANUFACTURER);
        body.put(KeyConstants.RequestBody.KEY_BRAND, Build.BRAND);
        body.put(KeyConstants.RequestBody.KEY_MODEL, Build.MODEL);
        body.put(KeyConstants.RequestBody.KEY_OSV, Build.VERSION.RELEASE);
        body.put(KeyConstants.RequestBody.KEY_APPV, DeviceUtil.getVersionName(context));
        body.put(KeyConstants.RequestBody.KEY_CONT, NetworkChecker.getConnectType(context));
        body.put(KeyConstants.RequestBody.KEY_CARRIER, NetworkChecker.getNetworkOperator(context));
        body.put(KeyConstants.RequestBody.KEY_FM, DeviceUtil.getFM());
        body.put(KeyConstants.RequestBody.KEY_BATTERY, DataCache.getInstance().get(KeyConstants.RequestBody.KEY_BATTERY, Integer.TYPE));
        body.put(KeyConstants.RequestBody.KEY_BTCH, DataCache.getInstance().get(KeyConstants.RequestBody.KEY_BTCH, Integer.TYPE));
        body.put(KeyConstants.RequestBody.KEY_LOWP, DataCache.getInstance().get(KeyConstants.RequestBody.KEY_LOWP, Integer.TYPE));
        return body;
    }

    public static byte[] buildCLRequestBody(String... extras) throws Exception {
        long start = System.currentTimeMillis();
        JSONObject body = getRequestBodyBaseJson();
        body.put(KeyConstants.RequestBody.KEY_PID, extras[0]);
        body.put(KeyConstants.RequestBody.KEY_W, extras[1]);
        body.put(KeyConstants.RequestBody.KEY_H, extras[2]);

        String placementKey = extras[0];

        if (TextUtils.isEmpty(extras[4])) {
            body.put(KeyConstants.RequestBody.KEY_BA, extras[4]);
        }
        String iapStr = DataCache.getInstance().get("c_adt_iap_number", String.class);
        if (TextUtils.isEmpty(iapStr)) {
            iapStr = "0.00";
        }
        body.put(KeyConstants.RequestBody.KEY_IAP, iapStr);
        body.put(KeyConstants.RequestBody.KEY_IMPRTIMES, Integer.valueOf(extras[5]));
        //no adt flag, 0:needs ADT,   1:no need
        if (DataCache.getInstance().containsKey(placementKey + "-campaigns")
                || Boolean.valueOf(extras[6])) {
            body.put(KeyConstants.RequestBody.KEY_NA, 1);
        } else {
            body.put(KeyConstants.RequestBody.KEY_NA, 0);
        }
        body.put(KeyConstants.RequestBody.KEY_NG, String.valueOf(DataCache.getInstance().get("InstallGP", Integer.class)));
        body.put("act", extras[3]);
        DeveloperLog.LogD("request cl params : " + body.toString());
        byte[] zipByte = Gzip.inGZip(body.toString().getBytes(Charset.forName("UTF-8")));
        DeveloperLog.LogD("buildCLRequestBody cast : " + (System.currentTimeMillis() - start));
        return zipByte;
    }

    private static byte[] buildIAPequestBody(String... extras) throws Exception {
        Map<String, String> params = new HashMap<>();

        params.put("cur", extras[0]);
        params.put("iap", extras[1]);
        params.put("iapt", extras[2]);
        params.put("cts", String.valueOf(System.currentTimeMillis()));

        JSONObject object = new JSONObject(params);

        DeveloperLog.LogD("iap params : " + object.toString());
        byte[] zipByte = Gzip.inGZip(object.toString().getBytes(Charset.forName("UTF-8")));
        return zipByte;
    }

    private static byte[] buildIRRequestBody(String... extras) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("did", DataCache.getInstance().get("AdvertisingId", String.class));
        params.put("ct", DataCache.getInstance().get("ConnectType", String.class));
        params.put("ca", DataCache.getInstance().get("NetworkOperator", String.class));
        params.put("lang", DataCache.getInstance().get("LangCode", String.class));
        params.put("make", DataCache.getInstance().get("Manufacturer", String.class));
        params.put("brand", DataCache.getInstance().get("Brand", String.class));
        params.put("model", DataCache.getInstance().get("Model", String.class));
        params.put("osv", DataCache.getInstance().get("OSVersion", String.class));
        params.put("pid", extras[0]);
        params.put("iid", extras[1]);
        params.put("status", extras[2]);
        params.put("msg", extras[3]);
        params.put("ctt", extras[4]);
        params.put("pt", extras[5]);
        params.put("bs", extras[6]);
        params.put("idx", extras[7]);
        params.put("mid", extras[8]);
        params.put("ts", String.valueOf(System.currentTimeMillis()));
        JSONObject object = new JSONObject(params);
        DeveloperLog.LogD("IR report params : " + object.toString());
        return Gzip.inGZip(object.toString().getBytes(Charset.forName("UTF-8")));
    }


    public static byte[] buildEventRequestBody(ConcurrentLinkedQueue<Event> events) throws Exception {
        JSONObject body = getRequestBodyBaseJson();
        body.put(KeyConstants.RequestBody.KEY_APPK, DataCache.getInstance().get("AppKey", String.class));
        JSONArray jsonEvents = new JSONArray();
        for (Event e : events) {
            jsonEvents.put(e.toJSONObject());
        }
        body.put("events", jsonEvents);
        DeveloperLog.LogD("event report params : " + body.toString());
        return Gzip.inGZip(body.toString().getBytes(Charset.forName("UTF-8")));
    }
}
