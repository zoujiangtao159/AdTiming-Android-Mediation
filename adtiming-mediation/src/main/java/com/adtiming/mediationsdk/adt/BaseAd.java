// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt;

public abstract class BaseAd {

    public abstract void loadAd();

    public abstract void setAdListener(BaseAdListener listener);

    public abstract void destroy();

    public boolean isReady() {
        return false;
    }

    public void show() {
    }
}
