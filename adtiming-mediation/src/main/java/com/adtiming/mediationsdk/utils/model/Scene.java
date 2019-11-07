// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.model;

import org.json.JSONObject;

public class Scene extends Frequency {
    //Scene ID      0
    private int id;
    //Scene Name    Default
    private String n;
    //isDefault   1
    private int isd;

    public Scene(JSONObject jsonObject) {
        id = jsonObject.optInt("id");
        n = jsonObject.optString("n");
        isd = jsonObject.optInt("isd");
        setFrequencyCap(jsonObject.optInt("fc"));
        setFrequencyUnit(jsonObject.optInt("fu"));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getN() {
        return n;
    }

    public void setN(String n) {
        this.n = n;
    }

    public int getIsd() {
        return isd;
    }

    public void setIsd(int isd) {
        this.isd = isd;
    }

    @Override
    public String toString() {
        return "Scene{" +
                "id=" + id +
                ", n='" + n + '\'' +
                ", isd=" + isd +
                '}';
    }
}
