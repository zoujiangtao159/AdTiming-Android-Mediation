// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt.video;

interface VideoEvents {
    String START = "start";
    String FIRST_QUARTILE = "firstQuartile";
    String MID_POINT = "midpoint";
    String THIRD_QUARTILE = "thirdQuartile";
    String COMPLETE = "complete";
    String MUTE = "mute";
    String UNMUTE = "unmute";
    String PAUSE = "pause";
    String RESUME = "resume";
    String CLOSE = "close";
    String SKIP = "skip";

    String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";
    String EXTRA_VOLUME_STREAM_TYPE = "android.media.EXTRA_VOLUME_STREAM_TYPE";
}
