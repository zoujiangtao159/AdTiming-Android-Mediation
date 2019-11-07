// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdBean implements Parcelable {

    private final String mOriData;
    private final String mCampaignId;
    private final String mAdId;
    private final String mTitle;
    private final String mPkgName;
    private final String mPlayUrl;
    private final String mDescription;
    private final String mAdUrl;
    private final String mVideoUrl;
    private final boolean isWebview;
    private final int mSc;
    private final boolean mCacheVideo;
    private final String mIconUrl;
    private final String mCid;
    private final String mMainimgUrl;
    private final String mApkUrl;
    private final String mResourceMd5;
    private final String mGooglePlayId;
    private final double mRating;
    private final String mAdType;
    private final List<String> mResources;
    private final List<String> mImptrackers;
    private final List<String> mClktrackers;
    private final int mAction;
    private final long mExpire;
    private final int mVpc;
    private final String mVq;
    private HashMap mVes;
    private final String mPiHs;
    private final String mPiId;
    private final String mPiOt;
    private final int mPiCp;

    private String mAppName;
    private int mAppSize;
    private int mRatingCount;
    private List<String> mImgs;
    private AdMark mMk;
    private int mRt;

    protected AdBean(AdBuilder builder) {
        this.mOriData = builder.ori_data;
        this.mCampaignId = builder.campaignId;
        this.mAdId = builder.adId;
        this.mTitle = builder.title;
        this.mPkgName = builder.package_name;
        this.mPlayUrl = builder.play_url;
        this.mDescription = builder.description;
        this.mAdUrl = builder.adUrl;
        this.mVideoUrl = builder.videoUrl;
        this.isWebview = builder.is_webview;
        this.mSc = builder.sc;
        this.mCacheVideo = builder.cacheVideo;
        this.mIconUrl = builder.iconUrl;
        this.mCid = builder.cid;
        this.mMainimgUrl = builder.mainimgUrl;
        this.mApkUrl = builder.apk_url;
        this.mResourceMd5 = builder.resourceMd5;
        this.mGooglePlayId = builder.googlePlayId;
        this.mRating = builder.rating;
        this.mAdType = builder.adType;
        this.mResources = builder.resources;
        this.mImptrackers = builder.imptrackers;
        this.mClktrackers = builder.clktrackers;
        this.mAction = builder.action;
        this.mExpire = builder.expire;
        this.mVpc = builder.vpc;
        this.mVq = builder.vq;
        this.mVes = builder.ves;
        this.mPiHs = builder.pihs;
        this.mPiId = builder.piid;
        this.mPiOt = builder.piot;
        this.mPiCp = builder.picp;
        this.mAppName = builder.appName;
        this.mAppSize = builder.appSize;
        this.mRatingCount = builder.ratingCount;
        this.mImgs = builder.imgs;
        this.mMk = builder.mk;
        this.mRt = builder.rt;
    }

    protected AdBean(Parcel in) {
        mOriData = in.readString();
        mCampaignId = in.readString();
        mAdId = in.readString();
        mTitle = in.readString();
        mPkgName = in.readString();
        mPlayUrl = in.readString();
        mDescription = in.readString();
        mAdUrl = in.readString();
        mVideoUrl = in.readString();
        isWebview = in.readByte() != 0;
        mSc = in.readInt();
        mCacheVideo = in.readByte() != 0;
        mIconUrl = in.readString();
        mCid = in.readString();
        mMainimgUrl = in.readString();
        mApkUrl = in.readString();
        mResourceMd5 = in.readString();
        mGooglePlayId = in.readString();
        mRating = in.readDouble();
        mAdType = in.readString();
        mResources = in.createStringArrayList();
        mImptrackers = in.createStringArrayList();
        mClktrackers = in.createStringArrayList();
        mAction = in.readInt();
        mExpire = in.readLong();
        mVpc = in.readInt();
        mVq = in.readString();
        mVes = in.readHashMap(HashMap.class.getClassLoader());
        mPiHs = in.readString();
        mPiId = in.readString();
        mPiOt = in.readString();
        mPiCp = in.readInt();
        mAppName = in.readString();
        mAppSize = in.readInt();
        mRatingCount = in.readInt();
        mImgs = in.createStringArrayList();
        mMk = in.readParcelable(AdMark.class.getClassLoader());
        mRt = in.readInt();
    }

    public static final Creator<AdBean> CREATOR = new Creator<AdBean>() {
        @Override
        public AdBean createFromParcel(Parcel in) {
            return new AdBean(in);
        }

        @Override
        public AdBean[] newArray(int size) {
            return new AdBean[size];
        }
    };

    @Override
    public String toString() {
        return "AdBean{" +
                "mOriData='" + mOriData + '\'' +
                ", mCampaignId='" + mCampaignId + '\'' +
                ", mAdId='" + mAdId + '\'' +
                ", mTitle='" + mTitle + '\'' +
                ", mPkgName='" + mPkgName + '\'' +
                ", mPlayUrl='" + mPlayUrl + '\'' +
                ", mDescription='" + mDescription + '\'' +
                ", mAdUrl='" + mAdUrl + '\'' +
                ", mVideoUrl='" + mVideoUrl + '\'' +
                ", isWebview=" + isWebview +
                ", mSc=" + mSc +
                ", mCacheVideo=" + mCacheVideo +
                ", mIconUrl='" + mIconUrl + '\'' +
                ", mCid='" + mCid + '\'' +
                ", mMainimgUrl='" + mMainimgUrl + '\'' +
                ", mApkUrl='" + mApkUrl + '\'' +
                ", mResourceMd5='" + mResourceMd5 + '\'' +
                ", mGooglePlayId='" + mGooglePlayId + '\'' +
                ", mRating=" + mRating +
                ", mAdType='" + mAdType + '\'' +
                ", mResources=" + mResources +
                ", mImptrackers=" + mImptrackers +
                ", mClktrackers=" + mClktrackers +
                ", mAction=" + mAction +
                ", mExpire=" + mExpire +
                ", mVpc=" + mVpc +
                ", mVq='" + mVq + '\'' +
                ", mVes=" + mVes +
                ", mPiHs='" + mPiHs + '\'' +
                ", mPiId='" + mPiId + '\'' +
                ", mPiOt='" + mPiOt + '\'' +
                ", mPiCp=" + mPiCp +
                ", mAppName='" + mAppName + '\'' +
                ", mAppSize=" + mAppSize +
                ", mRatingCount=" + mRatingCount +
                ", mImgs=" + mImgs +
                ", mMk=" + mMk +
                ", mRt=" + mRt +
                '}';
    }

    public String getOriData() {
        return mOriData;
    }

    public String getCampaignId() {
        return mCampaignId;
    }

    public String getAdId() {
        return mAdId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getPkgName() {
        return mPkgName;
    }

    public String getPlayUrl() {
        return mPlayUrl;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getAdUrl() {
        return mAdUrl;
    }

    public String getVideoUrl() {
        return mVideoUrl;
    }

    public boolean isWebview() {
        return isWebview;
    }

    public int getSc() {
        return mSc;
    }

    public boolean isCacheVideo() {
        return mCacheVideo;
    }

    public String getIconUrl() {
        return mIconUrl;
    }

    public String getCid() {
        return mCid;
    }

    public String getMainimgUrl() {
        return mMainimgUrl;
    }

    public AdMark getMk() {
        return mMk;
    }

    public String getApkUrl() {
        return mApkUrl;
    }

    public String getResourceMd5() {
        return mResourceMd5;
    }

    public String getGooglePlayId() {
        return mGooglePlayId;
    }

    public double getRating() {
        return mRating;
    }

    public String getAdType() {
        return mAdType;
    }

    public List<String> getResources() {
        return mResources;
    }

    public List<String> getImptrackers() {
        return mImptrackers;
    }

    public List<String> getClktrackers() {
        return mClktrackers;
    }

    public int getAction() {
        return mAction;
    }

    public long getExpire() {
        return mExpire;
    }

    public int getVpc() {
        return mVpc;
    }

    public Map<String, String[]> getVes() {
        return mVes;
    }

    public String getPiHs() {
        return mPiHs;
    }

    public String getPiId() {
        return mPiId;
    }

    public String getPiOt() {
        return mPiOt;
    }

    public int getPiCp() {
        return mPiCp;
    }

    public AdMark getAdMark() {
        return mMk;
    }

    public int getRt() {
        return mRt;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mOriData);
        dest.writeString(mCampaignId);
        dest.writeString(mAdId);
        dest.writeString(mTitle);
        dest.writeString(mPkgName);
        dest.writeString(mPlayUrl);
        dest.writeString(mDescription);
        dest.writeString(mAdUrl);
        dest.writeString(mVideoUrl);
        dest.writeByte((byte) (isWebview ? 1 : 0));
        dest.writeInt(mSc);
        dest.writeByte((byte) (mCacheVideo ? 1 : 0));
        dest.writeString(mIconUrl);
        dest.writeString(mCid);
        dest.writeString(mMainimgUrl);
        dest.writeString(mApkUrl);
        dest.writeString(mResourceMd5);
        dest.writeString(mGooglePlayId);
        dest.writeDouble(mRating);
        dest.writeString(mAdType);
        dest.writeStringList(mResources);
        dest.writeStringList(mImptrackers);
        dest.writeStringList(mClktrackers);
        dest.writeInt(mAction);
        dest.writeLong(mExpire);
        dest.writeInt(mVpc);
        dest.writeString(mVq);
        dest.writeMap(mVes);
        dest.writeString(mPiHs);
        dest.writeString(mPiId);
        dest.writeString(mPiOt);
        dest.writeInt(mPiCp);
        dest.writeString(mAppName);
        dest.writeInt(mAppSize);
        dest.writeInt(mRatingCount);
        dest.writeStringList(mImgs);
        dest.writeParcelable(mMk, 0);
        dest.writeInt(mRt);
    }

    public static class AdBuilder {
        private String ori_data;
        private String campaignId;
        private String adId;
        private String title;
        private String package_name;
        private String play_url;
        private String description;
        private String adUrl;
        private String videoUrl;
        private boolean is_webview;
        private int sc;
        private boolean cacheVideo;
        private String iconUrl;
        private String cid;
        private String mainimgUrl;
        private String apk_url;
        private String resourceMd5;
        private String googlePlayId;
        private double rating;
        private String adType;
        private List<String> resources;
        private List<String> imptrackers;
        private List<String> clktrackers;
        private int action;
        private long expire;
        private int vpc;
        private String vq;
        private HashMap<String, String[]> ves;
        private String pihs;
        private String piid;
        private String piot;
        private int picp;

        private String appName;
        private int appSize;
        private int ratingCount;
        private List<String> imgs;
        private AdMark mk;
        private int rt;

        public AdBuilder oriData(String oriData) {
            this.ori_data = oriData;
            return this;
        }

        public AdBuilder campaignId(String campaignId) {
            this.campaignId = campaignId;
            return this;
        }

        public AdBuilder adId(String adId) {
            this.adId = adId;
            return this;
        }

        public AdBuilder title(String title) {
            this.title = title;
            return this;
        }

        public AdBuilder pkgName(String pkgName) {
            this.package_name = pkgName;
            return this;
        }

        public AdBuilder playUrl(String playUrl) {
            this.play_url = playUrl;
            return this;
        }

        public AdBuilder description(String description) {
            this.description = description;
            return this;
        }

        public AdBuilder adUrl(String adUrl) {
            this.adUrl = adUrl;
            return this;
        }

        public AdBuilder videoUrl(String videoUrl) {
            this.videoUrl = videoUrl;
            return this;
        }

        public AdBuilder isWebview(boolean isWebView) {
            this.is_webview = isWebView;
            return this;
        }

        public AdBuilder sc(int sc) {
            this.sc = sc;
            return this;
        }

        public AdBuilder cacheVideo(boolean isCacheVideo) {
            this.cacheVideo = isCacheVideo;
            return this;
        }

        public AdBuilder iconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
            return this;
        }

        public AdBuilder cid(String cid) {
            this.cid = cid;
            return this;
        }

        public AdBuilder mainImgUrl(String mainImgUrl) {
            this.mainimgUrl = mainImgUrl;
            return this;
        }

        public AdBuilder apkUrl(String apkUrl) {
            this.apk_url = apkUrl;
            return this;
        }

        public AdBuilder resourceMd5(String resourceMd5) {
            this.resourceMd5 = resourceMd5;
            return this;
        }

        public AdBuilder gpId(String gpId) {
            this.googlePlayId = gpId;
            return this;
        }

        public AdBuilder rating(double rating) {
            this.rating = rating;
            return this;
        }

        public AdBuilder adType(String adType) {
            this.adType = adType;
            return this;
        }

        public AdBuilder resources(List<String> resources) {
            this.resources = resources;
            return this;
        }

        public AdBuilder impTrackers(List<String> impTrackers) {
            this.imptrackers = impTrackers;
            return this;
        }

        public AdBuilder clkTrackers(List<String> clkTrackers) {
            this.clktrackers = clkTrackers;
            return this;
        }

        public AdBuilder action(int action) {
            this.action = action;
            return this;
        }

        public AdBuilder expire(long expire) {
            this.expire = expire;
            return this;
        }

        public AdBuilder vpc(int vpc) {
            this.vpc = vpc;
            return this;
        }

        public AdBuilder vq(String vq) {
            this.vq = vq;
            return this;
        }

        public AdBuilder ves(HashMap<String, String[]> ves) {
            this.ves = ves;
            return this;
        }

        public AdBuilder pihs(String pihs) {
            this.pihs = pihs;
            return this;
        }

        public AdBuilder piid(String piid) {
            this.piid = piid;
            return this;
        }

        public AdBuilder picp(int picp) {
            this.picp = picp;
            return this;
        }

        public AdBuilder piot(String piot) {
            this.piot = piot;
            return this;
        }

        public AdBuilder setAppName(String appName) {
            this.appName = appName;
            return this;
        }

        public AdBuilder setAppSize(int appSize) {
            this.appSize = appSize;
            return this;
        }

        public AdBuilder setRatingCount(int ratingCount) {
            this.ratingCount = ratingCount;
            return this;
        }

        public AdBuilder setImgs(List<String> imgs) {
            this.imgs = imgs;
            return this;
        }

        public AdBuilder setMk(AdMark mk) {
            this.mk = mk;
            return this;
        }

        public AdBuilder setRt(int rt) {
            this.rt = rt;
            return this;
        }

        public AdBean build() {
            return new AdBean(this);
        }
    }
}
