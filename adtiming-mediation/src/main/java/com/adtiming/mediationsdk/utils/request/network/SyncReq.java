// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.request.network;

import com.adtiming.mediationsdk.utils.DeveloperLog;

class SyncReq extends BaseTask{

     SyncReq(Request request) {
        super(request);
    }

     Response start() {
        try {
            if (mConnection == null) {
                return null;
            }
            return mConnection.intercept(mRequest);
        } catch (Exception e) {
            DeveloperLog.LogD("SyncReq", e);
            return null;
        }
    }
}
