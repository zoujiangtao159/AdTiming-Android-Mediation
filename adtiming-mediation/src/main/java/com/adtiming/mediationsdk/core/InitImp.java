// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.core;

import android.app.Activity;
import android.text.TextUtils;

import com.adtiming.mediationsdk.danmaku.Danmaku;
import com.adtiming.mediationsdk.danmaku.DanmakuLifecycle;
import com.adtiming.mediationsdk.utils.AdLog;
import com.adtiming.mediationsdk.utils.AdtUtil;
import com.adtiming.mediationsdk.utils.Constants;
import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.HandlerUtil;
import com.adtiming.mediationsdk.utils.IOUtil;
import com.adtiming.mediationsdk.utils.JsonHelper;
import com.adtiming.mediationsdk.utils.JsonUtil;
import com.adtiming.mediationsdk.utils.MediationRequest;
import com.adtiming.mediationsdk.utils.SdkUtil;
import com.adtiming.mediationsdk.utils.WorkExecutor;
import com.adtiming.mediationsdk.utils.cache.Cache;
import com.adtiming.mediationsdk.utils.cache.DataCache;
import com.adtiming.mediationsdk.utils.constant.KeyConstants;
import com.adtiming.mediationsdk.utils.crash.CrashUtil;
import com.adtiming.mediationsdk.utils.device.SensorManager;
import com.adtiming.mediationsdk.utils.error.AdTimingError;
import com.adtiming.mediationsdk.utils.error.ErrorCode;
import com.adtiming.mediationsdk.utils.event.EventId;
import com.adtiming.mediationsdk.utils.event.EventUploadManager;
import com.adtiming.mediationsdk.utils.model.Config;
import com.adtiming.mediationsdk.utils.request.network.Request;
import com.adtiming.mediationsdk.utils.request.network.Response;
import com.adtiming.mediationsdk.utils.webview.AdtWebView;
import com.adtiming.mediationsdk.InitCallback;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public final class InitImp {
    private static AtomicBoolean hasInit = new AtomicBoolean(false);
    private static AtomicBoolean isInitRunning = new AtomicBoolean(false);
    private static InitCallback mCallback;
    private static long sInitStart;

    /**
     * init method
     */
    public static void init(final Activity activity, final String appKey, final InitCallback callback) {
        //
        if (hasInit.get()) {
            return;
        }

        if (isInitRunning.get()) {
            return;
        }

        if (activity == null) {
            AdTimingError error = new AdTimingError(ErrorCode.CODE_INIT_INVALID_REQUEST
                    , ErrorCode.MSG_INIT_INVALID_REQUEST, ErrorCode.CODE_INTERNAL_REQUEST_ACTIVITY);
            AdLog.getSingleton().LogE(error.toString());
            DeveloperLog.LogE(error.toString() + ", init failed because activity is null");
            callbackInitErrorOnUIThread(error);
            return;
        }
        sInitStart = System.currentTimeMillis();
        mCallback = callback;
        AdtUtil.init(activity);
        SensorManager.getSingleton();
        Danmaku.getSingleton().init(activity);
        EventUploadManager.getInstance().init(activity.getApplicationContext());
        EventUploadManager.getInstance().uploadEvent(EventId.INIT_START);
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initOnUIThread(activity, appKey);
            }
        });
    }

    /**
     * init success?
     */
    public static boolean isInit() {
        return hasInit.get();
    }

    static boolean isInitRunning() {
        return isInitRunning.get();
    }

    private static void requestConfig(String appKey) throws Exception {
        DeveloperLog.LogD("Adt init request config");
        //requests Config
        MediationRequest.httpInit(appKey, new InitRequestCallback(appKey));
    }

    private static void initOnUIThread(Activity activity, String appKey) {
        isInitRunning.set(true);
        //WebView available?
        if (!AdtWebView.getInstance().isSupport(activity)) {
            AdTimingError error = new AdTimingError(ErrorCode.CODE_INIT_INVALID_REQUEST
                    , ErrorCode.MSG_INIT_INVALID_REQUEST, ErrorCode.CODE_INTERNAL_REQUEST_WEBVIEW);
            AdLog.getSingleton().LogE(error.toString());
            DeveloperLog.LogE(error.toString() + ", init failed because webview unsupport");
            callbackInitErrorOnUIThread(error);
            return;
        } else {
            //if webview is supported
            AdtWebView.getInstance().init();
        }

        WorkExecutor.execute(new InitAsyncRunnable(appKey));
    }

    /**
     * Inits global utils
     */
    private static void initUtil() throws Exception {
        DataCache.getInstance().init(AdtUtil.getApplication());
        Cache.init();
    }

    private static void doAfterGetConfig(String appKey, Config config) {
        try {
            DeveloperLog.enableDebug(AdtUtil.getApplication(), config.getDebug() == 1);
            AdLog.getSingleton().init(AdtUtil.getApplication());
            EventUploadManager.getInstance().updateReportSettings(config.getEvents());
            //reports error logs
            CrashUtil.getSingleton().uploadException(config.getSdkHost(), appKey);
        } catch (Exception e) {
            DeveloperLog.LogD("doAfterGetConfig  exception : ", e);
            CrashUtil.getSingleton().saveException(e);
        }
    }

    private static void callbackInitErrorOnUIThread(final AdTimingError result) {
        HandlerUtil.runOnUiThread(new InitFailRunnable(result));
    }

    private static void callbackInitSuccessOnUIThread() {
        HandlerUtil.runOnUiThread(new InitSuccessRunnable());
    }

    private static void initCompleteReport(AdTimingError error) {
        JSONObject jsonObject = new JSONObject();
        if (error != null) {
            JsonUtil.put(jsonObject, "msg", error);
        } else {
            JsonUtil.put(jsonObject, "msg", "init success");
        }
        if (sInitStart != 0) {
            int dur = (int) (System.currentTimeMillis() - sInitStart);
            JsonUtil.put(jsonObject, "duration", dur);
        }
        EventUploadManager.getInstance().uploadEvent(EventId.INIT_COMPLETE, jsonObject);
    }

    private static class InitSuccessRunnable implements Runnable {

        @Override
        public void run() {
            DeveloperLog.LogD("Adt init Success ");
            hasInit.set(true);
            isInitRunning.set(false);
            if (mCallback != null) {
                mCallback.onSuccess();
            }
            initCompleteReport(null);
        }
    }

    private static class InitAsyncRunnable implements Runnable {

        private String appKey;

        InitAsyncRunnable(String appKey) {
            this.appKey = appKey;
        }

        @Override
        public void run() {
            try {
                Activity activity = DanmakuLifecycle.getInstance().getActivity();
                //filters banning conditions
                AdTimingError error = SdkUtil.banRun(activity, appKey);
                if (error != null) {
                    callbackInitErrorOnUIThread(error);
                    return;
                }
                initUtil();
                DataCache.getInstance().set(KeyConstants.Storage.KEY_APP_KEY, appKey);
                DataCache.getInstance().set("AdtVersion", Constants.SDK_V);
                requestConfig(appKey);
            } catch (Exception e) {
                DeveloperLog.LogD("initOnAsyncThread  exception : ", e);
                CrashUtil.getSingleton().saveException(e);
                AdTimingError error = new AdTimingError(ErrorCode.CODE_INIT_UNKNOWN_INTERNAL_ERROR
                        , ErrorCode.MSG_INIT_UNKNOWN_INTERNAL_ERROR, ErrorCode.CODE_INTERNAL_UNKNOWN_OTHER);
                AdLog.getSingleton().LogE(error.toString());
                DeveloperLog.LogE(error.toString() + ", initOnAsyncThread");
                callbackInitErrorOnUIThread(error);
            }
        }
    }

    private static class InitFailRunnable implements Runnable {
        private AdTimingError mError;

        InitFailRunnable(AdTimingError result) {
            mError = result;
        }

        @Override
        public void run() {
            DeveloperLog.LogD("Adt init error  " + mError);
            hasInit.set(false);
            isInitRunning.set(false);
            if (mCallback != null) {
                mCallback.onError(mError);
            }
            initCompleteReport(mError);
        }
    }

    private static class InitRequestCallback implements Request.OnRequestCallback {

        private String appKey;

        InitRequestCallback(String appKey) {
            this.appKey = appKey;
        }

        @Override
        public void onRequestSuccess(Response response) {
            try {
                if (response.code() != HttpURLConnection.HTTP_OK) {
                    AdTimingError error = new AdTimingError(ErrorCode.CODE_INIT_SERVER_ERROR
                            , ErrorCode.MSG_INIT_SERVER_ERROR, ErrorCode.CODE_INTERNAL_SERVER_ERROR);
                    AdLog.getSingleton().LogE(error.toString() + ", Adt init response code: " + response.code());
                    DeveloperLog.LogE(error.toString() + "Adt init request config response code not 200 : " + response.code());
                    callbackInitErrorOnUIThread(error);
                    return;
                }

                String requestData = new String(JsonHelper.checkResponse(response), "UTF-8");
                if (TextUtils.isEmpty(requestData)) {
                    AdTimingError error = new AdTimingError(ErrorCode.CODE_INIT_SERVER_ERROR
                            , ErrorCode.MSG_INIT_SERVER_ERROR, ErrorCode.CODE_INTERNAL_SERVER_ERROR);
                    AdLog.getSingleton().LogE(error.toString() + ", Adt init response data is null: " + requestData);
                    DeveloperLog.LogE(error.toString() + ", Adt init response data is null: " + requestData);
                    callbackInitErrorOnUIThread(error);
                    return;
                }
                //adds global data to memory
                Config config = JsonHelper.formatConfig(requestData);
                if (config != null) {
                    DeveloperLog.LogD("Adt init request config success");
                    DataCache.getInstance().setMEM("Config", config);
                    DataCache.getInstance().setMEM("AdtConfig", SdkUtil.getAdtConfig(config));
                    callbackInitSuccessOnUIThread();

                    doAfterGetConfig(appKey, config);
                } else {
                    AdTimingError error = new AdTimingError(ErrorCode.CODE_INIT_SERVER_ERROR
                            , ErrorCode.MSG_INIT_SERVER_ERROR, ErrorCode.CODE_INTERNAL_SERVER_ERROR);
                    AdLog.getSingleton().LogE(error.toString() + ", Adt init format config is null");
                    DeveloperLog.LogE(error.toString() + ", Adt init format config is null");
                    callbackInitErrorOnUIThread(error);
                }
            } catch (Exception e) {
                CrashUtil.getSingleton().saveException(e);
                AdTimingError error = new AdTimingError(ErrorCode.CODE_INIT_SERVER_ERROR
                        , ErrorCode.MSG_INIT_SERVER_ERROR, ErrorCode.CODE_INTERNAL_UNKNOWN_OTHER);
                AdLog.getSingleton().LogE(error.toString());
                DeveloperLog.LogE(error.toString() + ", request config exception:" + e);
                callbackInitErrorOnUIThread(error);
            } finally {
                IOUtil.closeQuietly(response);
            }
        }

        @Override
        public void onRequestFailed(String error) {
            AdTimingError result = new AdTimingError(ErrorCode.CODE_INIT_SERVER_ERROR
                    , ErrorCode.MSG_INIT_SERVER_ERROR, ErrorCode.CODE_INTERNAL_SERVER_FAILED);
            AdLog.getSingleton().LogD("request config failed : " + result + ", error:" + error);
            DeveloperLog.LogD("request config failed : " + result + ", error:" + error);
            callbackInitErrorOnUIThread(result);
        }
    }
}
