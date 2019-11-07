// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.model;

import java.util.Map;

public class AdtConfig {
    private String mTkHost;
    private Map<String, PlacementConfig> mPlacementConfigs;

    public void setTkHost(String tkHost) {
        this.mTkHost = tkHost;
    }

    public String getTkHost() {
        return mTkHost;
    }

    public void setPlacementConfigs(Map<String, PlacementConfig> placementConfigs) {
        this.mPlacementConfigs = placementConfigs;
    }

    public Map<String, PlacementConfig> getPlacementConfigs() {
        return mPlacementConfigs;
    }

    public static class PlacementConfig {
        private String mPid;
        private int mVideoDuration;
        private int mVideoSkip;

        public void setPid(String pid) {
            this.mPid = pid;
        }

        public String getPid() {
            return mPid;
        }

        public void setVideoDuration(int videoDuration) {
            this.mVideoDuration = videoDuration;
        }

        public int getVideoDuration() {
            return mVideoDuration;
        }

        public void setVideoSkip(int videoSkip) {
            this.mVideoSkip = videoSkip;
        }

        public int getVideoSkip() {
            return mVideoSkip;
        }
    }
}
