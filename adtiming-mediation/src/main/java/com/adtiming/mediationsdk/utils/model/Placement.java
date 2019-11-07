// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.model;

import android.util.SparseArray;

import java.util.Map;

/**
 * 
 */
public class Placement extends Frequency {
    //id
    private String id;
    //type
    private int t;
    //danmaku
    private int dm;
    //Video duration
    private int vd;
    //max impression
    private int mi;
    //impression interval
    private int ii;
    //video_skippable?
    private int video_skip;
    //video id for interactive ads
    private int vid;
    //auto_preloading 0:off,1:on
    private int ap;

    //Array of Scenes
    private Map<String, Scene> scenes;
    //# of adapters to load at init   2
    private int asl;
    //cache size: # of ready in stock     3
    private int cs;
    //refresh in how many seconds for RewardVideo     30
    private int rf;
    //reload in how many seconds for Banner
    private int rl;
    //max seconds for loading one AdNetwork     30
    private int pt;
    //batchSize for Instance, for Banner&Native   2
    private int bs;
    //Fan Out flag, immediate Ready mode, for Banner&Native     1
    private int fo;
    //Max Parallel loading count    2
    private int mpc;
    //Mediation placement data
    private SparseArray<BaseInstance> insMap;

    private int main;

    private String oriData;

    public void setOriData(String oriData) {
        this.oriData = oriData;
    }

    public String getOriData() {
        return oriData;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getT() {
        return t;
    }

    public void setT(int t) {
        this.t = t;
    }

    public int getDm() {
        return dm;
    }

    public void setDm(int dm) {
        this.dm = dm;
    }

    public int getVd() {
        return vd;
    }

    public void setVd(int vd) {
        this.vd = vd;
    }

    public int getMi() {
        return mi;
    }

    public void setMi(int mi) {
        this.mi = mi;
    }

    public int getIi() {
        return ii;
    }

    public void setIi(int ii) {
        this.ii = ii;
    }

    public int getVideo_skip() {
        return video_skip;
    }

    public void setVideo_skip(int video_skip) {
        this.video_skip = video_skip;
    }

    public int getVid() {
        return vid;
    }

    public void setVid(int vid) {
        this.vid = vid;
    }

    public void setAp(int ap) {
        this.ap = ap;
    }

    public int getAp() {
        return ap;
    }

    public SparseArray<BaseInstance> getInsMap() {
        return insMap;
    }

    public void setInsMap(SparseArray<BaseInstance> insMap) {
        this.insMap = insMap;
    }

    public Map<String, Scene> getScenes() {
        return scenes;
    }

    public void setScenes(Map<String, Scene> scenes) {
        this.scenes = scenes;
    }

    public int getAsl() {
        return asl;
    }

    public void setAsl(int asl) {
        this.asl = asl;
    }

    public int getCs() {
        return cs;
    }

    public void setCs(int cs) {
        this.cs = cs;
    }

    public int getRf() {
        return rf;
    }

    public void setRf(int rf) {
        this.rf = rf;
    }

    public int getRl() {
        return rl;
    }

    public void setRl(int rl) {
        this.rl = rl;
    }

    public int getPt() {
        return pt;
    }

    public void setPt(int pt) {
        this.pt = pt;
    }

    public int getBs() {
        return bs;
    }

    public void setBs(int bs) {
        this.bs = bs;
    }

    public int getFo() {
        return fo;
    }

    public void setFo(int fo) {
        this.fo = fo;
    }

    public int getMpc() {
        return mpc;
    }

    public void setMpc(int mpc) {
        this.mpc = mpc;
    }

    public void setMain(int main) {
        this.main = main;
    }

    public int getMain() {
        return main;
    }

    @Override
    public String toString() {
        return "Placement{" +
                "id=" + id +
                ", t=" + t +
                '}';
    }
}
