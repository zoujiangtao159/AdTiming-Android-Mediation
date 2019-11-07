// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.request.network;


import android.content.Context;
import android.text.TextUtils;

import com.adtiming.mediationsdk.utils.error.ErrorCode;
import com.adtiming.mediationsdk.utils.IOUtil;

public class Request {

    private final Method mMethod;
    private final Headers mHeaders;
    private final int mConnectTimeout;
    private final int mReadTimeout;
    private final String mUrl;
    private final RequestBody mRequestBody;
    private final boolean isInstanceFollowRedirects;
    private final boolean isCheckChain;
    private final OnRequestCallback mCallback;
    private final Object mTag;
    private boolean mShouldCallbackResponse;

    private Context mContext;


    public enum Method {
        GET("GET"),
        POST("POST");

        private final String value;

        Method(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }


    public interface OnRequestCallback {
        void onRequestSuccess(Response response);

        void onRequestFailed(String error);
    }

    static RequestBuilder newBuilder() {
        return new RequestBuilder();
    }

    private Request(RequestBuilder builder) {
        mMethod = builder.mMethod;
        mHeaders = builder.mHeaders;
        mConnectTimeout = builder.mConnectTimeout;
        mReadTimeout = builder.mReadTimeout;
        mUrl = builder.mUrl;
        mRequestBody = builder.mRequestBody;
        isInstanceFollowRedirects = builder.isInstanceFollowRedirects;
        isCheckChain = builder.isCheckChain;
        mCallback = builder.mCallback;
        mTag = builder.mTag;
    }

    /**
     * Gets url.
     */
    public String getUrl() {
        return mUrl;
    }

    /**
     * Gets method.
     */
    public Method getRequestMethod() {
        return mMethod;
    }

    /**
     * Gets headers.
     */
    public Headers getHeaders() {
        return mHeaders;
    }

    public RequestBody getRequestBody() {
        return mRequestBody;
    }

    /**
     * Gets the connection timeout time, Unit is a millisecond.
     */
    public int getConnectTimeout() {
        return mConnectTimeout;
    }

    /**
     * Gets the readResponse timeout time, Unit is a millisecond.
     */
    public int getReadTimeout() {
        return mReadTimeout;
    }

    /**
     * Gets the connection can follow redirects
     */
    public boolean isInstanceFollowRedirects() {
        return isInstanceFollowRedirects;
    }

    public boolean isCheckChain() {
        return isCheckChain;
    }

    public Context getContext() {
        return mContext;
    }

    public boolean shouldCallbackResponse() {
        return mShouldCallbackResponse || mCallback != null;
    }

    /**
     * Gets tag.
     */
    public Object getTag() {
        return mTag;
    }

    private void performRequest(Context context) {
        if (context == null) {
            callbackError(mCallback, ErrorCode.ERROR_CONTEXT);
            return;
        }

        if (TextUtils.isEmpty(mUrl)) {
            callbackError(mCallback, "request need a valid url, current is empty");
            return;
        }

        mContext = context;

        AsyncReq asyncReq = new AsyncReq(this);
        asyncReq.setCallback(new AsyncReq.OnTaskCallback() {
            @Override
            public void onSuccess(Response response) {
                if (mCallback != null) {
                    mCallback.onRequestSuccess(response);
                } else {
                    IOUtil.closeQuietly(response);
                }
            }

            @Override
            public void onError(String error) {
                if (mCallback != null) {
                    mCallback.onRequestFailed(error);
                }
            }
        });
        ReqExecutor.execute(asyncReq);
    }


    private Response syncRequest() {
        mShouldCallbackResponse = true;
        return new SyncReq(this).start();
    }

    private void callbackError(OnRequestCallback callback, String error) {
        if (callback == null) {
            throw new IllegalArgumentException(error);
        } else {
            callback.onRequestFailed(error);
        }
    }

    public static class RequestBuilder {
        private Method mMethod;
        private Headers mHeaders;
        private int mConnectTimeout;
        private int mReadTimeout;
        private String mUrl;
        private RequestBody mRequestBody;
        private OnRequestCallback mCallback;
        private boolean isInstanceFollowRedirects;
        private boolean isCheckChain;
        private Object mTag;

        public RequestBuilder method(Method method) {
            mMethod = method;
            return this;
        }

        public RequestBuilder headers(Headers headers) {
            mHeaders = headers;
            return this;
        }

        public RequestBuilder connectTimeout(int timeout) {
            mConnectTimeout = timeout;
            return this;
        }

        public RequestBuilder readTimeout(int readTimeout) {
            mReadTimeout = readTimeout;
            return this;
        }

        public RequestBuilder url(String url) {
            mUrl = url;
            return this;
        }

        public RequestBuilder body(RequestBody body) {
            mRequestBody = body;
            return this;
        }

        public RequestBuilder instanceFollowRedirects(boolean redirects) {
            isInstanceFollowRedirects = redirects;
            return this;
        }

        public RequestBuilder isCheckChain(boolean isCheckChain) {
            this.isCheckChain = isCheckChain;
            return this;
        }

        public RequestBuilder callback(OnRequestCallback callback) {
            mCallback = callback;
            return this;
        }

        public RequestBuilder tag(Object tag) {
            mTag = tag;
            return this;
        }

        public Response syncRequest() {
            return new Request(this).syncRequest();
        }

        public void performRequest(Context context) {
            new Request(this).performRequest(context);
        }
    }
}
