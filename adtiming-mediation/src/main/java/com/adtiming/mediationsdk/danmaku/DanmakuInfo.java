// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.danmaku;

import java.util.LinkedList;

class DanmakuInfo {
    private int type;
    private LinkedList<String> danmakus;
    private int[] colors;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public LinkedList<String> getDanmakus() {
        return danmakus;
    }

    public void setDanmakus(LinkedList<String> danmakus) {
        this.danmakus = danmakus;
    }

    public int[] getColors() {
        return colors;
    }

    public void setColors(int[] colors) {
        this.colors = colors;
    }
}
