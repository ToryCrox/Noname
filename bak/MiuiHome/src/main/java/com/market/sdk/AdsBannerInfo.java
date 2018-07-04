package com.market.sdk;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class AdsBannerInfo implements Parcelable {
    public static final Creator<AdsBannerInfo> CREATOR = new Creator<AdsBannerInfo>() {
        public AdsBannerInfo createFromParcel(Parcel source) {
            return new AdsBannerInfo(source);
        }

        public AdsBannerInfo[] newArray(int size) {
            return new AdsBannerInfo[size];
        }
    };
    public Uri iconUri;
    public String iconUrl;
    public Uri uri;

    public int describeContents() {
        return 0;
    }

    public AdsBannerInfo(Parcel source) {
        this.iconUrl = source.readString();
        this.uri = (Uri) Uri.CREATOR.createFromParcel(source);
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.iconUrl);
        Uri.writeToParcel(dest, this.uri);
    }
}
