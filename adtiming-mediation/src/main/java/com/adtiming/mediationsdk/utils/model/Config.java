// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.model;

import android.util.SparseArray;

import com.adtiming.mediationsdk.utils.JsonUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class Config {
    
    private int debug;
    
    private int mr;
    private String tkHost;
    private String sdkHost;
    private SparseArray<Mediation> mediations;
    
    private Map<String, Placement> placements;

    private String cdn;

    private Events events;

    public int getDebug() {
        return debug;
    }

    public void setDebug(int debug) {
        this.debug = debug;
    }

    public int getMr() {
        return mr;
    }

    public void setMr(int mr) {
        this.mr = mr;
    }

    public Map<String, Placement> getPlacements() {
        return placements;
    }

    public void setPlacements(Map<String, Placement> placements) {
        this.placements = placements;
    }

    public SparseArray<Mediation> getMediations() {
        return mediations;
    }

    public void setMediations(SparseArray<Mediation> mediations) {
        this.mediations = mediations;
    }

    public String getTkHost() {
        return tkHost;
    }

    public void setTkHost(String tkHost) {
        this.tkHost = tkHost;
    }

    public String getSdkHost() {
        return sdkHost;
    }

    public void setSdkHost(String sdkHost) {
        this.sdkHost = sdkHost;
    }

    public String getCdn() {
        return cdn;
    }

    public void setCdn(String cdn) {
        this.cdn = cdn;
    }

    public void setEvents(Events events) {
        this.events = events;
    }

    public Events getEvents() {
        return events;
    }

    public String getJsConfig() {
        JSONObject jsonObject = new JSONObject();
        JsonUtil.put(jsonObject, "mr", mr);
        JsonUtil.put(jsonObject, "d", debug);
        Map<String, String> hosts = new HashMap<>();
        hosts.put("tk", tkHost);
        hosts.put("sdk", sdkHost);
        hosts.put("cdn", cdn);
        JsonUtil.put(jsonObject, "hs", hosts);
        return jsonObject.toString();
    }
}
