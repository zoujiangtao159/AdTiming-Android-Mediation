// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.model;

import java.util.List;
import java.util.Map;

public final class ImpRecord {

    private Map<String, List<Imp>> mImpMap;

    public Map<String, List<Imp>> getImpMap() {
        return mImpMap;
    }

    public void setImpMap(Map<String, List<Imp>> impMap) {
        this.mImpMap = impMap;
    }

    @Override
    public String toString() {
        return "ImpRecord{" +
                "mImpMap=" + mImpMap +
                '}';
    }

    public static class Imp {
        
        private String mPlacementId;
        
        private String mCampaignId;
        //Impression time
        private String mTime;
        //Impression count
        private int mImpCount;
        //Package name
        private String mPkgName;
        //Last Impression Time
        private long mLastImpTime;

        public String getPlacmentId() {
            return mPlacementId;
        }

        public void setPlacmentId(String placementId) {
            this.mPlacementId = placementId;
        }

        public String getCampaignId() {
            return mCampaignId;
        }

        public void setCampaignId(String campaignId) {
            this.mCampaignId = campaignId;
        }

        public String getTime() {
            return mTime;
        }

        public void setTime(String time) {
            this.mTime = time;
        }

        public int getImpCount() {
            return mImpCount;
        }

        public void setImpCount(int impCount) {
            this.mImpCount = impCount;
        }

        public void setPkgName(String pkgName) {
            this.mPkgName = pkgName;
        }

        public String getPkgName() {
            return mPkgName;
        }

        public long getLashImpTime() {
            return mLastImpTime;
        }

        public void setLashImpTime(long lashImpTime) {
            this.mLastImpTime = lashImpTime;
        }

        @Override
        public String toString() {
            return "Imp{" +
                    "mPlacementId='" + mPlacementId + '\'' +
                    ", mCampaignId='" + mCampaignId + '\'' +
                    ", mTime='" + mTime + '\'' +
                    ", mPkgName='" + mPkgName + '\'' +
                    ", mImpCount=" + mImpCount +
                    ", mLastImpTime=" + mLastImpTime +
                    '}';
        }
    }
}
