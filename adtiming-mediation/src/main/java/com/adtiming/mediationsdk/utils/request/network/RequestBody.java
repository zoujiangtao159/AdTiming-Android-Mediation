// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.request.network;

import java.io.IOException;
import java.io.OutputStream;

public interface RequestBody {

    /**
     * Returns the size of the data.
     */
    long length();

    /**
     * Gets the content type of data.
     */
    String contentType();

    /**
     * OutData data.
     */
    void writeTo(OutputStream writer) throws IOException;
}
