// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.error;

public class AdTimingError {
    private int code;
    private String message;
    private int internalCode;

    public AdTimingError(int code, String message, int internalCode) {
        this.code = code;
        this.message = message;
        this.internalCode = internalCode;
    }

    @Override
    public String toString() {
        if (internalCode == -1) {
            return "AdTimingError{" +
                    "code:" + code +
                    ", message:" + message +
                    "}";
        }
        return "AdTimingError{" +
                "code:" + code +
                ", message:" + message +
                ", internalCode:" + internalCode +
                "}";
    }
}
