// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.model;

import com.adtiming.mediationsdk.utils.Constants;

/**
 * This class includes placement info with adType
 *
 * 
 */
public class PlacementInfo {
    private String mId;
    private int mWidth;
    private int mHeight;
    private int mAdType;

    public PlacementInfo(String id) {
        mId = id;
    }

    public String getId() {
        return mId;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }


    public int getAdType() {
        return mAdType;
    }

    public PlacementInfo getPlacementInfo(int adType) {
        mAdType = adType;
        switch (adType) {
            case Constants.BANNER:
                mWidth = 640;
                mHeight = 100;
                break;
            case Constants.NATIVE:
                mWidth = 1200;
                mHeight = 627;
                break;
            case Constants.INTERSTITIAL:
                mWidth = 768;
                mHeight = 1024;
                break;
            case Constants.VIDEO:
            case Constants.INTERACTIVE:
                break;
            default:
                break;
        }
        return this;
    }

    @Override
    public String toString() {
        return "PlacementInfo{" +
                "mId='" + mId + '\'' +
                ", mWidth=" + mWidth +
                ", mHeight=" + mHeight +
                ", mAdType=" + mAdType +
                '}';
    }
}
