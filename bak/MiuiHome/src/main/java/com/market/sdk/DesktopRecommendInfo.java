package com.market.sdk;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public class DesktopRecommendInfo implements Parcelable {
    public static final Creator<DesktopRecommendInfo> CREATOR = new Creator<DesktopRecommendInfo>() {
        public DesktopRecommendInfo createFromParcel(Parcel source) {
            return new DesktopRecommendInfo(source);
        }

        public DesktopRecommendInfo[] newArray(int size) {
            return new DesktopRecommendInfo[size];
        }
    };
    public List<AppstoreAppInfo> appInfoList = new ArrayList();
    public String backgroundImageUrl = "";
    public List<AdsBannerInfo> bannerList = new ArrayList();
    public long cacheTime;
    public String description = "";
    public long folderId = -1;
    public String sid = "";

    public int describeContents() {
        return 0;
    }

    public DesktopRecommendInfo(Parcel source) {
        this.folderId = source.readLong();
        source.readTypedList(this.appInfoList, AppstoreAppInfo.CREATOR);
        source.readTypedList(this.bannerList, AdsBannerInfo.CREATOR);
        this.backgroundImageUrl = source.readString();
        this.description = source.readString();
        this.sid = source.readString();
        this.cacheTime = source.readLong();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.folderId);
        dest.writeTypedList(this.appInfoList);
        dest.writeTypedList(this.bannerList);
        dest.writeString(this.backgroundImageUrl);
        dest.writeString(this.description);
        dest.writeString(this.sid);
        dest.writeLong(this.cacheTime);
    }
}
