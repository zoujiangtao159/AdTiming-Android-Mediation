// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.request.network;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;

public interface ResponseBody extends Closeable {

    /**
     * Transforms the response data into a string.
     */
    String string() throws IOException;

    /**
     * Transforms the response data into a byte array.
     */
    byte[] byteArray() throws IOException;

    /**
     * Transforms the response data into a stream.
     */
    BufferedInputStream stream() throws IOException;
}
