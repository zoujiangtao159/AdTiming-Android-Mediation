// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.request.network;

import com.adtiming.mediationsdk.utils.DeveloperLog;


class AsyncReq extends BaseTask implements Runnable {

    private OnTaskCallback mCallback;

    AsyncReq(Request request) {
        super(request);
    }

    public void setCallback(OnTaskCallback mCallback) {
        this.mCallback = mCallback;
    }

    @Override
    public void run() {
        try {
            if (mConnection == null) {
                if (mCallback != null) {
                    mCallback.onError("not http connection");
                }
                return;
            }
            Response response = mConnection.intercept(mRequest);
            if (response == null) {
                if (mCallback != null) {
                    mCallback.onError("response is null");
                }
            } else {
                if (mCallback != null) {
                    mCallback.onSuccess(response);
                }
            }
        } catch (Exception e) {
            if (mCallback == null) {
                return;
            }
            mCallback.onError(e.getMessage());
            DeveloperLog.LogD("AsyncReq", e);
        } finally {
            if (mConnection != null) {
                try {
                    mConnection.cancel();
                } catch (Exception e) {
                    DeveloperLog.LogD("AsyncReq", e);
                }
            }
        }
    }

    interface OnTaskCallback {
        void onSuccess(Response response);

        void onError(String error);
    }
}
