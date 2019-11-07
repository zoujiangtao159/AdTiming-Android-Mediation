// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.device;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.webkit.WebSettings;

import com.adtiming.mediationsdk.utils.constant.KeyConstants;
import com.adtiming.mediationsdk.utils.request.network.util.NetworkChecker;
import com.adtiming.mediationsdk.utils.AdtUtil;
import com.adtiming.mediationsdk.utils.Constants;
import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.cache.DataCache;
import com.adtiming.mediationsdk.utils.crash.CrashUtil;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 * 
 * Utils for Android native APIs
 * 
 */
public class DeviceUtil {

    private static String mSessionId;

    private DeviceUtil() {
    }

    /**
     * 
     *
     * @param activity activity
     */
    public static boolean isActivityAvailable(Activity activity) {
        if (activity == null) {
            return false;
        }
        boolean flage = false;
        if (Build.VERSION.SDK_INT >= 17) {
            if (!activity.isDestroyed()) {
                flage = true;
            }
        } else {
            if (!activity.isFinishing()) {
                flage = true;
            }
        }
        return flage;
    }

    public static String today() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);
        Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * device info, app info, and misc. info
     *
     * @param context context
     */
    public static Map<String, Object> getDeviceInfo(Context context) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("PackageName", context.getPackageName());
        map.put("UserAgent", getUserAgent(context));
        map.put("ConnectType", NetworkChecker.getConnectType(context));
        AdvertisingIdClient.AdInfo info = AdvertisingIdClient.getAdvertisingIdInfo(context);
        String gaid = info == null ? "" : info.getId();
        DeveloperLog.LogD("Gaid:" + gaid);
        map.put("AdvertisingId", gaid);
        String fbId = getFacebookId(context);
        DeveloperLog.LogD("fbId:" + fbId);
        if (!TextUtils.isEmpty(fbId)) {
            map.put("FacebookId", fbId);
        }
        map.put("InstallFacebook", isFacebookInstall(context));
        map.put("InstallGP", isGpInstall(context));
        return map;
    }

    /**
     * 
     *
     * @param context context
     */
    public static Map<String, Object> getTelephonyManagerInfo(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        Map<String, Object> map = new HashMap<>();
        if (telephonyManager != null) {
            map.put("NetworkOperator", telephonyManager.getNetworkOperatorName());
            map.put("NetworkIso", telephonyManager.getNetworkCountryIso());
            map.put("SimOperator", telephonyManager.getSimOperatorName());
            map.put("SimIso", telephonyManager.getSimCountryIso());
        }
        return map;
    }

    /**
     * 
     */
    public static Map<String, Object> getBuildInfo() {
        Map<String, Object> map = new HashMap<>();
        map.put("Device", Build.DEVICE);
        map.put("Brand", Build.BRAND);
        map.put("Model", Build.MODEL);
        map.put("Product", Build.PRODUCT);
        map.put("Manufacturer", Build.MANUFACTURER);
        map.put("SDKVersion", Build.VERSION.SDK_INT);
        map.put("OSVersion", Build.VERSION.RELEASE);
        map.put("OSBuild", Build.ID);
        map.put("Display", Build.DISPLAY);
        return map;
    }

    /**
     * 
     *
     * @param context context
     */
    public static Map<String, Object> getResourcesInfo(Context context) {
        Resources resources = context.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        int screenLayout = configuration.screenLayout;
        int densityDpi = displayMetrics.densityDpi;
        //densityLevel
        int densityLevel = 1;
        int low = (DisplayMetrics.DENSITY_MEDIUM + DisplayMetrics.DENSITY_LOW) / 2;
        int high = (DisplayMetrics.DENSITY_MEDIUM + DisplayMetrics.DENSITY_HIGH) / 2;
        if (densityDpi == 0) {
        } else if (densityDpi < low) {
            densityLevel = 0;
        } else if (densityDpi > high) {
            densityLevel = 2;
        }

        //screenSize
        int screenSize = 0;
        switch (screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                screenSize = 1;
                break;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                screenSize = 2;
                break;
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                screenSize = 3;
                break;
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                screenSize = 4;
                break;
            default:
                break;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("DeviceType", (screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE ? 1 : 0);
        map.put("WidthPixels", displayMetrics.widthPixels);
        map.put("HeightPixels", displayMetrics.heightPixels);
        map.put("DensityLevel", densityLevel);
        map.put("ScreenSize", screenSize);
        //
        map.put("ScreenType", screenLayout & 15);
        map.put("xdpi", displayMetrics.xdpi);
        map.put("ydpi", displayMetrics.ydpi);
        map.put("densityDpi", densityDpi);
        return map;
    }

    public static Map<String, Object> getLocaleInfo() {
        Map<String, Object> map = new HashMap<>();
        Locale locale = Locale.getDefault();
        map.put(KeyConstants.RequestBody.KEY_LANG_NAME, locale.getDisplayLanguage());
        map.put(KeyConstants.RequestBody.KEY_LCOUNTRY, Locale.getDefault().getCountry());
        map.put(KeyConstants.RequestBody.KEY_LANG, Locale.getDefault().getLanguage() + Locale.getDefault().getCountry());
        return map;
    }

    public static Map<String, Object> getSystemProperty(Context context) {
        Method getMethod = null;
        Map<String, Object> map = new HashMap<>();
        try {
            map.put("AndroidId", Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");

            getMethod = systemPropertiesClass.getMethod("get", String.class);
            map.put("ro.build.display.id", getMethod.invoke(null, "ro.build.display.id"));
            map.put("os.arch", getMethod.invoke(null, "os.arch"));
            String cpui = getMethod.invoke(null, "ro.product.cpu.abi").toString();
            String cpui2 = getMethod.invoke(null, "ro.product.cpu.abi2").toString();
            map.put("ro.product.cpu.abi", cpui);
            map.put("ro.product.cpu.abi2", cpui2);
            String cpuType = getCPUType(cpui);
            if (TextUtils.isEmpty(cpuType)) {
                cpuType = getCPUType(cpui2);
            }
            map.put("CPUType", cpuType);
        } catch (Exception e) {
            DeveloperLog.LogD("DeviceUtil", e);
            CrashUtil.getSingleton().saveException(e);
        }
        return map;
    }

    public static String getSystemPropertyCPUABI(String systemKey) {
        Method getMethod = null;
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");

            getMethod = systemPropertiesClass.getMethod("get", String.class);
            Object result = getMethod.invoke(null, systemKey);
            if (result != null) {
                return result.toString();
            }
        } catch (Exception e) {
            DeveloperLog.LogD("DeviceUtil", e);
            CrashUtil.getSingleton().saveException(e);
        }
        return null;
    }

    /**
     * @param cpui cupi
     */
    private static String getCPUType(String cpui) {
        String type = "";
        if ("armeabi".equals(cpui) || "armeabi-v7a".equals(cpui)) {
            type = "arm";
        } else if ("arm64-v8a".equals(cpui)) {
            type = "arm64";
        } else {
            type = cpui;
        }
        return type;
    }

    private static String getUserAgent(Context context) {
        String userAgent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                userAgent = WebSettings.getDefaultUserAgent(context);
            } catch (Throwable e) {
                userAgent = System.getProperty("http.agent");
            }
        } else {
            userAgent = System.getProperty("http.agent");
        }
        StringBuilder sb = new StringBuilder();
        if (userAgent != null) {
            for (int i = 0, length = userAgent.length(); i < length; i++) {
                char c = userAgent.charAt(i);
                if (c <= '\u001f' || c >= '\u007f') {
                    sb.append(String.format("\\u%04x", (int) c));
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    public static boolean isSpaceAvailable() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = 0;
        long availableBlocks = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = stat.getBlockSizeLong();
            availableBlocks = stat.getAvailableBlocksLong();
        } else {
            blockSize = stat.getBlockSize();
            availableBlocks = stat.getAvailableBlocks();
        }
        return blockSize * availableBlocks > (100 * 1024 * 1024L);
    }

    public static String getFacebookId(Context context) {
        String facebookId = "";
        String[] projection = {"aid"};
        Cursor cursor = null;
        try {
            if (!isFacebookInstall(context)) {
                return facebookId;
            }
            cursor = context.getContentResolver().query(Uri.parse("content://com.facebook.katana.provider.AttributionIdProvider"),
                    projection, null, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                return facebookId;
            } else {
                facebookId = cursor.getString(cursor.getColumnIndex("aid"));
            }
        } catch (Exception e) {
            DeveloperLog.LogE("DeviceUtil", e);
            DeveloperLog.LogE("Facebook ID get fail");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (facebookId == null) {
            facebookId = "";
        }
        return facebookId;
    }

    /**
     * 
     */
    private static boolean isFacebookInstall(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(Constants.PKG_FB, PackageManager.GET_GIDS);
            return packageInfo != null;
        } catch (Exception e) {
            DeveloperLog.LogD("DeviceUtil", e);
        }
        return false;
    }

    /**
     * 
     *
     * @return 0: google play installed, 1: not
     */
    static int isGpInstall(Context context) {
        int flag = 1;
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(Constants.PKG_GP, PackageManager.GET_GIDS);
            if (packageInfo != null &&
                    ("com.google.android.finsky.application.classic.ClassicApplication".equals(packageInfo.applicationInfo.name) ||
                            "com.google.android.finsky.application.classic.ClassicApplication".equals(packageInfo.applicationInfo.className) ||
                            "com.google.android.finsky.application.classic.ClassicProdApplication".equals(packageInfo.applicationInfo.name) ||
                            "com.google.android.finsky.application.classic.ClassicProdApplication".equals(packageInfo.applicationInfo.className))) {
                flag = 0;
            }
        } catch (Exception e) {
            DeveloperLog.LogD("DeviceUtil", e);
            CrashUtil.getSingleton().saveException(e);
        }
        return flag;
    }

    /**
     * @see KeyConstants.RequestBody#KEY_ZO
     */
    public static int getTimeZoneOffset() {
        return new GregorianCalendar().getTimeZone().getOffset(System.currentTimeMillis());
    }

    /**
     * @see KeyConstants.RequestBody#KEY_TZ
     */
    public static String getTimeZone() {
        return TimeZone.getDefault().getID();
    }

    /**
     * 
     *
     * @see KeyConstants.RequestBody#KEY_APPV
     */
    public static String getVersionName(Context context) {
        if (context == null) {
            return "";
        }
        String verName = "";
        try {
            verName = context.getPackageManager().
                    getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            DeveloperLog.LogE("getVersionName", e);
        }
        return verName;
    }

    /**
     * Free storage in MB
     *
     * @see KeyConstants.RequestBody#KEY_FM
     */
    public static long getFM() {
        File root = Environment.getRootDirectory();
        StatFs sf = new StatFs(root.getPath());
        long blockSize;
        long availCount;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = sf.getBlockSizeLong();
            availCount = sf.getAvailableBlocksLong();
        } else {
            blockSize = sf.getBlockSize();
            availCount = sf.getAvailableBlocks();
        }
        return (availCount * blockSize) / 1024 / 1024;
    }

    /**
     * 
     *
     * @return unique virtual ID
     */
    public static String getUID() {
        // Only API >= 9 devices have android.os.Build.SERIAL
        // http://developer.android.com/reference/android/os/Build.html#SERIAL
        // If the device was upgraded or rooted, this API will return duplicated ID
        String androidId = "";
        try {
            androidId = android.provider.Settings.Secure.getString(AdtUtil.getApplication().getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            androidId = "";
        }
        String serial = "";
        try {
            serial = android.os.Build.class.getField("SERIAL").get(null).toString();
        } catch (Exception exception) {
            serial = "serial";
        }

        // cobines the above with UUID
        return new UUID(androidId.hashCode(), serial.hashCode()).toString();
    }

    public static String getSessionID() {
        if (mSessionId == null) {
            mSessionId = new UUID(getUniquePsuedoID().hashCode(), System.currentTimeMillis()).toString();
        }
        return mSessionId;
    }

    public static String getUniquePsuedoID() {
        String serial = null;

        String mszDevIDShort = "35" +
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
                Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
                Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
                Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
                Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +

                Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +

                Build.USER.length() % 10; //13-digits

        try {
            serial = android.os.Build.class.getField("SERIAL").get(null).toString();
            //API>=9 uses serial
            return new UUID(mszDevIDShort.hashCode(), serial.hashCode()).toString();
        } catch (Exception exception) {
            //serial needs to init
            serial = "serial"; 
        }
        //15-digit ID using hardware info
        return new UUID(mszDevIDShort.hashCode(), serial.hashCode()).toString();

    }

    //
    public static boolean isRoot() {
        String binPath = "/system/bin/su";
        String xBinPath = "/system/xbin/su";

        if (new File(binPath).exists() && isCanExecute(binPath)) {
            return true;
        }
        return new File(xBinPath).exists() && isCanExecute(xBinPath);
    }

    private static boolean isCanExecute(String filePath) {
        java.lang.Process process = null;
        try {
            process = Runtime.getRuntime().exec("ls -l " + filePath);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String str = in.readLine();
            if (str != null && str.length() >= 4) {
                char flag = str.charAt(3);
                return flag == 's' || flag == 'x';
            }
        } catch (IOException e) {
            DeveloperLog.LogE("isCanExecute", e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return false;
    }

    /**
     * 
     */
    private static final String REG_EXC_IP = "^192\\.168\\.(\\d{1}|[1-9]\\d|1\\d{2}|2[0-4]\\d|25\\d)\\.(\\d{1}|[1-9]\\d|1\\d{2}|2[0-4]\\d|25\\d)$";
    /**
     * 
     */
    private static final String REG_EXA_IP = "^10\\.(\\d{1}|[1-9]\\d|1\\d{2}|2[0-4]\\d|25\\d)\\.(\\d{1}|[1-9]\\d|1\\d{2}|2[0-4]\\d|25\\d)\\.(\\d{1}|[1-9]\\d|1\\d{2}|2[0-4]\\d|25\\d)$";
    /**
     * 
     */
    private static final String REG_EXB_IP = "^172\\.(1[6-9]|2\\d|3[0-1])\\.(\\d{1}|[1-9]\\d|1\\d{2}|2[0-4]\\d|25\\d)\\.(\\d{1}|[1-9]\\d|1\\d{2}|2[0-4]\\d|25\\d)$";

    public static String getHostIp() {
        String hostIp;
        Pattern ip = Pattern.compile("(" + REG_EXA_IP + ")|" + "(" + REG_EXB_IP + ")|" + "(" + REG_EXC_IP + ")");
        Enumeration<NetworkInterface> networkInterfaces = null;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            DeveloperLog.LogE("getHostIp", e);
        }
        InetAddress address;
        while (networkInterfaces != null && networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                address = inetAddresses.nextElement();
                String hostAddress = address.getHostAddress();
                Matcher matcher = ip.matcher(hostAddress);
                if (matcher.matches()) {
                    hostIp = hostAddress;
                    return hostIp;
                }

            }
        }
        return null;
    }

    private static final BroadcastReceiver BATTERY_RECEIVER = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    // current battery reading
                    int current = extras.getInt(BatteryManager.EXTRA_LEVEL);
                    // total battery reading
                    int total = extras.getInt(BatteryManager.EXTRA_SCALE);
                    int percent = current * 100 / total;

                    DataCache.getInstance().setMEM(KeyConstants.RequestBody.KEY_BATTERY, percent);

                    int btch = extras.getInt(BatteryManager.EXTRA_STATUS);

                    DataCache.getInstance().setMEM(KeyConstants.RequestBody.KEY_BTCH
                            , btch == BatteryManager.BATTERY_STATUS_FULL
                                    || btch == BatteryManager.BATTERY_STATUS_CHARGING ? 1 : 0);
                    DataCache.getInstance().setMEM(KeyConstants.RequestBody.KEY_LOWP, percent >= 20 ? 0 : 1);
                }
            } else if (Intent.ACTION_BATTERY_LOW.equals(intent.getAction())) {
                DataCache.getInstance().setMEM(KeyConstants.RequestBody.KEY_LOWP, 1);
            }
        }
    };

    public static void startBatteryWatch() {
        if (AdtUtil.getApplication() != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
            filter.addAction(Intent.ACTION_BATTERY_LOW);
            AdtUtil.getApplication().registerReceiver(BATTERY_RECEIVER, filter);
        }
    }

    public static void destroyBatteryWatch() {
        if (AdtUtil.getApplication() != null) {
            AdtUtil.getApplication().unregisterReceiver(BATTERY_RECEIVER);
        }
    }


    public static JSONObject getInstallVending(Context context) {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        int intValue;
        try {
            PackageManager packageManager = context.getPackageManager();
            List<ApplicationInfo> installedApplications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
            Map<String, Integer> hashMap = new HashMap();
            for (ApplicationInfo applicationInfo : installedApplications) {
                String installerPackageName = packageManager.getInstallerPackageName(applicationInfo.packageName);
                Integer num = hashMap.get(installerPackageName);
                if (num == null) {
                    intValue = 1;
                } else {
                    intValue = num.intValue() + 1;
                }
                hashMap.put(installerPackageName, intValue);
            }
            for (Map.Entry<String, Integer> entry : hashMap.entrySet()) {
                String str = "other";
                if (entry.getKey() == null) {
                    map.put("os", entry.getValue());
                } else if (entry.getKey().equals(Constants.PKG_GP)) {
                    map.put("gp", entry.getValue());
                } else if (map.get(str) == null) {
                    map.put(str, entry.getValue());
                } else {
                    map.put(str, entry.getValue() + map.get(str));
                }
            }
        } catch (Exception e) {
        }
        return new JSONObject(map);
    }
}
