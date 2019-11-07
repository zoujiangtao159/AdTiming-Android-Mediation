// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.crash;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.adtiming.mediationsdk.utils.AdtUtil;
import com.adtiming.mediationsdk.utils.Constants;
import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.WorkExecutor;
import com.adtiming.mediationsdk.utils.cache.DataCache;
import com.adtiming.mediationsdk.utils.request.HeaderUtils;
import com.adtiming.mediationsdk.utils.request.ParamsBuilder;
import com.adtiming.mediationsdk.utils.request.network.AdRequest;
import com.adtiming.mediationsdk.utils.request.network.ByteRequestBody;
import com.adtiming.mediationsdk.utils.request.network.Headers;
import com.adtiming.mediationsdk.utils.Gzip;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.*;

/**
 *
 *
 * 
 */
public class CrashUtil implements Thread.UncaughtExceptionHandler {
    private static final String SP_NAME = "AdTimingCrashSP";
    private SharedPreferences mCrashSP;
    private Thread.UncaughtExceptionHandler mDefaultEH;

    private static class CrashUtilHolder {
        private static final CrashUtil INSTANCE = new CrashUtil();
    }

    private CrashUtil() {
    }

    public static CrashUtil getSingleton() {
        return CrashUtilHolder.INSTANCE;
    }

    public void init() {
        try {
            //inits SP
            mCrashSP = AdtUtil.getApplication().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
            //gets exception handler if set by app
            if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CrashUtil)) {
                mDefaultEH = Thread.getDefaultUncaughtExceptionHandler();
            }
            Thread.setDefaultUncaughtExceptionHandler(this);
        } catch (Exception e) {
            DeveloperLog.LogD("CrashUtil", e);
        }
    }

    /**
     * called for uncaught exceptions
     */
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        try {
            if (throwable == null || throwable instanceof UndeclaredThrowableException) {
                return;
            }
            saveException(throwable);
            //passes to exception handler if set by app developer
            if (mDefaultEH != null && mDefaultEH != this && !(mDefaultEH instanceof CrashUtil)) {
                mDefaultEH.uncaughtException(thread, throwable);
            }
        } catch (Exception e) {
            DeveloperLog.LogD("CrashUtil", e);
        }
    }

    /**
     * saves exceptions
     */
    public void saveException(final Throwable throwable) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (throwable == null || mCrashSP == null) {
                    return;
                }
                //多于10条则不写入
                if (mCrashSP.getAll() != null && mCrashSP.getAll().size() >= 10) {
                    return;
                }
                try {
                    String errorInfo = Constants.SDK_V.concat(":").concat(getStackTraceString(throwable));
                    if (TextUtils.isEmpty(errorInfo)) {
                        return;
                    }
                    SharedPreferences.Editor editor = mCrashSP.edit();
                    editor.putString(Long.toString(System.currentTimeMillis()), errorInfo.trim());
                    editor.apply();
                } catch (Exception e) {
                    DeveloperLog.LogD("CrashUtil", e);
                }
            }
        };
        WorkExecutor.execute(runnable);
    }

    /**
     * uploads Exceptions in separate thread
     */
    public void uploadException(String host, String appKey) {
        if (mCrashSP == null) {
            return;
        }
        if (TextUtils.isEmpty(host)) {
            return;
        }
        if (TextUtils.isEmpty(appKey)) {
            return;
        }
        Map<String, ?> errorMap = mCrashSP.getAll();
        if (errorMap.size() == 0) {
            return;
        }
        try {
            //clears SP after successful uploads
            mCrashSP.edit().clear().apply();
            String xrUrl = host + "/xr?" + new ParamsBuilder()
                    .p("v", "2")
                    .p("k", appKey)
                    .p("sdkv", Constants.SDK_V)
                    .p("mv", Constants.VERSION)
                    .p("mn", "")
                    .p("t", "error")
                    .p("ts", Long.toString(System.currentTimeMillis())).format();
            String model = DataCache.getInstance().get("Model", String.class);
            String make = DataCache.getInstance().get("Make", String.class);
            String brand = DataCache.getInstance().get("Brand", String.class);
            String osv = DataCache.getInstance().get("OSVersion", String.class);
            String advertisingId = DataCache.getInstance().get("AdvertisingId", String.class);
            for (Map.Entry<String, ?> nv : errorMap.entrySet()) {
                String errorInfo = (String) nv.getValue();
                if (TextUtils.isEmpty(errorInfo) || !errorInfo.contains("com.aiming.mdt")) {
                    continue;
                }

                String errorType = getErrorType(errorInfo);
                if (TextUtils.isEmpty(errorType)) {
                    errorType = "UnknownError";
                }
                errorInfo = errorInfo.replaceAll("\u0001", " ");
                String text = TextUtils.join("\u0001", new Object[]{model, advertisingId, errorType, errorInfo, make, brand, osv});
                Headers headers = HeaderUtils.getBaseHeaders();
                AdRequest.post()
                        .body(new ByteRequestBody(Gzip.compress(text.getBytes(Charset.forName("UTF-8")))))
                        .headers(headers)
                        .url(xrUrl)
                        .connectTimeout(30000)
                        .readTimeout(60000)
                        .performRequest(AdtUtil.getApplication());
            }
        } catch (Throwable e) {
            DeveloperLog.LogD("CrashUtil", e);
        }
    }


    private static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }
        Throwable t = new Throwable(Constants.SDK_V, tr);
        while (t != null) {
            if (t instanceof UnknownHostException) {
                return "";
            }
            t = t.getCause();
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    /**
     * matches error type with regex
     */
    private static String getErrorType(String exception) {
        String type = "";
        try {
            Pattern pattern = compile(".*?(Exception|Error|Death)", CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(exception);
            if (matcher.find()) {
                type = matcher.group(0);
            }
            if (!TextUtils.isEmpty(type)) {
                type = type.replaceAll("Caused by:", "").replaceAll(" ", "");
            }
        } catch (Exception e) {
            DeveloperLog.LogD("CrashUtil", e);
        }
        return type;
    }
}
