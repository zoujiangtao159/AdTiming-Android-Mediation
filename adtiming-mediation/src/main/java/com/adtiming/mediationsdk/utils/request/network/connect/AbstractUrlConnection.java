// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.request.network.connect;

import com.adtiming.mediationsdk.utils.request.network.Headers;
import com.adtiming.mediationsdk.utils.request.network.ResponseBody;
import com.adtiming.mediationsdk.utils.request.network.util.NetworkChecker;
import com.adtiming.mediationsdk.utils.error.ErrorCode;
import com.adtiming.mediationsdk.utils.crash.CrashUtil;
import com.adtiming.mediationsdk.utils.request.network.Request;
import com.adtiming.mediationsdk.utils.request.network.RequestBody;
import com.adtiming.mediationsdk.utils.request.network.Response;
import com.adtiming.mediationsdk.utils.request.network.StreamBody;
import com.adtiming.mediationsdk.utils.request.network.exception.ConnectException;
import com.adtiming.mediationsdk.utils.request.network.exception.ReadException;
import com.adtiming.mediationsdk.utils.request.network.exception.WriteException;
import com.adtiming.mediationsdk.utils.IOUtil;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

/**
 * 
 */
public abstract class AbstractUrlConnection {

    private URLConnection mConnection;

    /**
     * 
     */
    public abstract void cancel() throws Exception;

    /**
     * @return 
     */
    abstract int getResponseCode() throws IOException;

    /**
     * @param request 
     * @return 
     * @throws Exception 
     */
    public abstract URLConnection connect(Request request) throws Exception;

    /**
     * constructor
     */
    AbstractUrlConnection() {
    }

    public Response intercept(Request request) throws Exception {
        if (!NetworkChecker.isAvailable(request.getContext())) {
            throw new ConnectException(ErrorCode.ERROR_NETWORK_NOT_AVAILABLE);
        }

        Request.Method method = request.getRequestMethod();
        if (isAllowBody(method)) {
            Headers headers = request.getHeaders();
            RequestBody body = request.getRequestBody();
            if (body != null && headers != null) {
                headers.set(Headers.KEY_CONTENT_LENGTH, Long.toString(body.length()));
                headers.set(Headers.KEY_CONTENT_TYPE, body.contentType());
            }
            mConnection = connect(request);
            writeBody(body);
        } else {
            mConnection = connect(request);
        }
        return readResponse(request);
    }

    private void writeBody(RequestBody body) throws WriteException {
        try {
            if (body == null) {
                return;
            }
            OutputStream stream = mConnection.getOutputStream();
            body.writeTo(IOUtil.toBufferedOutputStream(stream));
            IOUtil.closeQuietly(stream);
        } catch (Exception e) {
            throw new WriteException(e);
        }
    }

    private Response readResponse(Request request) throws ReadException {
        try {
            int code = getResponseCode();
            if (code >= 400) {
                throw new ReadException(String.format("%s RequestCode:%d", mConnection.getURL().toString(), code));
            }
            BufferedInputStream inputStream = IOUtil.toBufferedInputStream(mConnection.getInputStream());
            if (!request.shouldCallbackResponse()) {
                IOUtil.closeQuietly(inputStream);
                inputStream.close();
                cancel();
                return null;
            }

            Headers headers = parseResponseHeaders(mConnection.getHeaderFields());
            String contentType = headers.getContentType();
            ResponseBody body = new StreamBody(contentType, inputStream);
            return Response.newBuilder().code(code).headers(headers).body(body).connection(this).build();
        } catch (SocketTimeoutException e) {
            throw new ReadException(String.format("Read data time out: %1$s.", mConnection.getURL().toString()), e);
        } catch (Exception e) {
            if (e instanceof ReadException) {
                throw new ReadException(e);
            } else {
                Exception exception = new Exception(request.getUrl(), e);
                CrashUtil.getSingleton().saveException(exception);
                throw new ReadException(exception);
            }
        }
    }

    private Headers parseResponseHeaders(Map<String, List<String>> headersMap) {
        Headers headers = new Headers();
        for (Map.Entry<String, List<String>> entry : headersMap.entrySet()) {
            headers.add(entry.getKey(), entry.getValue());
        }
        return headers;
    }

    boolean isAllowBody(Request.Method method) {
        return method.equals(Request.Method.POST);
    }
}
