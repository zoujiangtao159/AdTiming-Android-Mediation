// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.request.network;

import com.adtiming.mediationsdk.utils.IOUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class ByteRequestBody implements RequestBody {

    private final byte[] mBody;
    private final Charset mCharset;
    private final String mContentType;

    public ByteRequestBody(byte[] body) {
        this(body, Charset.forName("UTF-8"));
    }

    public ByteRequestBody(byte[] body, Charset charset) {
        this(body, charset, Headers.VALUE_APPLICATION_STREAM);
    }

    public ByteRequestBody(byte[] body, String contentType) {
        this(body, Charset.forName("UTF-8"), contentType);
    }

    public ByteRequestBody(byte[] body, Charset charset, String contentType) {
        this.mBody = body;
        this.mCharset = charset;
        this.mContentType = contentType;
    }

    @Override
    public long length() {
        return mBody.length;
    }

    @Override
    public String contentType() {
        return mContentType;
    }

    @Override
    public void writeTo(OutputStream writer) throws IOException {
        IOUtil.write(writer, mBody);
    }
}
