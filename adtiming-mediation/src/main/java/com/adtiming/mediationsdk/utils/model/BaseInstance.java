// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.model;

import android.text.TextUtils;

public class BaseInstance extends Frequency {
    //Instances Id
    protected int id;

    //Mediation Id
    protected int mediationId;
    //placement key
    protected String key;
    //placement template path
    private String path;
    //group index
    private int grpIndex;
    //own index
    protected int index;
    //is 1st in the group
    private boolean isFirst;

    //data for instance storage
    private Object object;

    private long start;

    private String appKey;

    protected long mInitStart;
    protected long mLoadStart;
    protected long mShowStart;

    protected String mPlacementId;

    public BaseInstance() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getMediationId() {
        return mediationId;
    }

    public void setMediationId(int mediationId) {
        this.mediationId = mediationId;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setGrpIndex(int grpIndex) {
        this.grpIndex = grpIndex;
    }

    public int getGrpIndex() {
        return grpIndex;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public void setFirst(boolean first) {
        isFirst = first;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Object getObject() {
        return object;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getStart() {
        return start;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setPlacementId(String placementId) {
        mPlacementId = placementId;
    }

    public String getPlacementId() {
        return mPlacementId;
    }

    @Override
    public String toString() {
        return "Ins{" +
                "id=" + id +
                ", index=" + index +
                ", pid=" + mPlacementId +
                ", mId=" + mediationId +
                '}';
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + mediationId;
        result = prime * result + (TextUtils.isEmpty(key) ? 0 : key.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (getClass() != obj.getClass()) {
            return false;
        } else if (this == obj) {
            return true;
        }
        BaseInstance other = (BaseInstance) obj;
        return TextUtils.equals(key, other.key) && id == other.id;
    }
}
