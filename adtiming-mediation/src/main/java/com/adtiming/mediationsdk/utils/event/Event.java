// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.event;

import android.os.Parcel;
import android.os.Parcelable;

import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.JsonUtil;

import org.json.JSONObject;

/**
 * Event reporting
 *
 * 
 */
public class Event implements Parcelable {

    private long ts;//app timestamp in mills;  1567479919643; required
    private int eid;//EventId;  100   ; required
    private String msg;//event message
    private int pid;//palcementID;  1111
    private int mid;//Mediation ID; 1
    private int iid;//instance ID;  2222
    private String adapterv;//Adapter Version;  3.0.1
    private String msdkv;//AdNetwork SDK Version;  4.2.0
    private int scene;//SceneID;  0
    private int ot;//Orientation, [1:portrait; 2:landscape];  1
    private int duration;//in seconds;  6
    private int priority;//instance load priority;  2
    private int cs;//cached stock size;  3

    Event() throws Exception {
        this((String) null);
    }

    Event(String jsonStr) throws Exception {
        this(new JSONObject(jsonStr));
    }

    Event(JSONObject jsonObject) {
        if (jsonObject == null) {
            return;
        }
        parseFromJson(jsonObject);
    }

    private Event(Parcel in) {
        ts = in.readLong();
        eid = in.readInt();
        msg = in.readString();
        pid = in.readInt();
        mid = in.readInt();
        iid = in.readInt();
        adapterv = in.readString();
        msdkv = in.readString();
        scene = in.readInt();
        ot = in.readInt();
        duration = in.readInt();
        priority = in.readInt();
        cs = in.readInt();
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    private void parseFromJson(JSONObject jsonObject) {
        try {
            ts = jsonObject.optLong("ts");
            eid = jsonObject.optInt("eid");
            msg = jsonObject.optString("msg");
            pid = jsonObject.optInt("pid");
            mid = jsonObject.optInt("mid");
            iid = jsonObject.optInt("iid");
            adapterv = jsonObject.optString("adapterv");
            msdkv = jsonObject.optString("msdkv");
            scene = jsonObject.optInt("scene");
            ot = jsonObject.optInt("ot");
            duration = jsonObject.optInt("duration");
            priority = jsonObject.optInt("priority");
            cs = jsonObject.optInt("cs");
        } catch (Exception e) {
            DeveloperLog.LogD("parse Event from json ", e);
        }
    }

    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            JsonUtil.put(jsonObject, "ts", ts);
            JsonUtil.put(jsonObject, "eid", eid);
            JsonUtil.put(jsonObject, "msg", msg);
            JsonUtil.put(jsonObject, "pid", pid);
            JsonUtil.put(jsonObject, "mid", mid);
            JsonUtil.put(jsonObject, "iid", iid);
            JsonUtil.put(jsonObject, "adapterv", adapterv);
            JsonUtil.put(jsonObject, "msdkv", msdkv);
            JsonUtil.put(jsonObject, "scene", scene);
            JsonUtil.put(jsonObject, "ot", ot);
            JsonUtil.put(jsonObject, "duration", duration);
            JsonUtil.put(jsonObject, "priority", priority);
            JsonUtil.put(jsonObject, "cs", cs);
        } catch (Exception e) {
            DeveloperLog.LogD("Event to json ", e);
        }
        return jsonObject;
    }

    String toJson() {
        return toJSONObject().toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(ts);
        dest.writeInt(eid);
        dest.writeString(msg);
        dest.writeInt(pid);
        dest.writeInt(mid);
        dest.writeInt(iid);
        dest.writeString(adapterv);
        dest.writeString(msdkv);
        dest.writeInt(scene);
        dest.writeInt(ot);
        dest.writeInt(duration);
        dest.writeInt(priority);
        dest.writeInt(cs);
    }

    @Override
    public String toString() {
        return toJson();
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public int getEid() {
        return eid;
    }

    public void setEid(int eid) {
        this.eid = eid;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getMid() {
        return mid;
    }

    public void setMid(int mid) {
        this.mid = mid;
    }

    public int getIid() {
        return iid;
    }

    public void setIid(int iid) {
        this.iid = iid;
    }

    public String getAdapterv() {
        return adapterv;
    }

    public void setAdapterv(String adapterv) {
        this.adapterv = adapterv;
    }

    public String getMsdkv() {
        return msdkv;
    }

    public void setMsdkv(String msdkv) {
        this.msdkv = msdkv;
    }

    public int getScene() {
        return scene;
    }

    public void setScene(int scene) {
        this.scene = scene;
    }

    public int getOt() {
        return ot;
    }

    public void setOt(int ot) {
        this.ot = ot;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getCs() {
        return cs;
    }

    public void setCs(int cs) {
        this.cs = cs;
    }

}
