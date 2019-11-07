// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.request.network;

import com.adtiming.mediationsdk.utils.request.network.connect.AbstractUrlConnection;
import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.IOUtil;

import java.io.Closeable;
import java.io.IOException;

public final class Response implements Closeable {

    public static Builder newBuilder() {
        return new Builder();
    }

    private final int mCode;
    private final Headers mHeaders;
    private final ResponseBody mBody;
    private final AbstractUrlConnection mConnection;

    private Response(Builder builder) {
        this.mCode = builder.mCode;
        this.mHeaders = builder.mHeaders;
        this.mBody = builder.mBody;
        this.mConnection = builder.mConnection;
    }

    /**
     * Gets the mCode of response.
     */
    public int code() {
        return mCode;
    }

    /**
     * Gets http headers.
     */
    public Headers headers() {
        return mHeaders;
    }

    /**
     * Gets http body.
     */
    public ResponseBody body() {
        return mBody;
    }

    @Override
    public void close() throws IOException {
        try {
            if (mConnection != null) {
                mConnection.cancel();
            }
            IOUtil.closeQuietly(mBody);
        } catch (Exception e) {
            DeveloperLog.LogD("Response close", e);
        }
    }

    @Override
    public String toString() {
        return "Response{" +
                "mCode=" + mCode +
                ", mHeaders=" + mHeaders +
                ", mBody=" + mBody +
                '}';
    }

    public static final class Builder {
        private int mCode;
        private Headers mHeaders;
        private ResponseBody mBody;
        private AbstractUrlConnection mConnection;

        Builder() {
        }

        public Builder code(int code) {
            this.mCode = code;
            return this;
        }

        public Builder headers(Headers headers) {
            this.mHeaders = headers;
            return this;
        }

        public Builder body(ResponseBody body) {
            this.mBody = body;
            return this;
        }

        public Builder connection(AbstractUrlConnection connection) {
            this.mConnection = connection;
            return this;
        }

        public Response build() {
            return new Response(this);
        }
    }
}
