package com.market.sdk;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ApkVerifyInfo implements Parcelable {
    public static final Creator<ApkVerifyInfo> CREATOR = new Creator<ApkVerifyInfo>() {
        public ApkVerifyInfo createFromParcel(Parcel source) {
            return new ApkVerifyInfo(source);
        }

        public ApkVerifyInfo[] newArray(int size) {
            return new ApkVerifyInfo[size];
        }
    };
    public String mAppId = "";
    public String mAppName = "";
    public String mInstallerName = "";
    public Intent mIntent;
    public long mNonce = 0;
    public String mPackageName = "";
    public int mStatus = 4;
    public long mTimeStamp = 0;
    public String mToken = "";
    public String mUpdateLog = "";
    public long mUpdateTime = 0;
    public int mVersionCode = 0;
    public String mVersionName = "";

    public ApkVerifyInfo(Parcel in) {
        this.mStatus = in.readInt();
        this.mVersionName = in.readString();
        this.mVersionCode = in.readInt();
        this.mUpdateTime = in.readLong();
        this.mUpdateLog = in.readString();
        this.mNonce = in.readLong();
        this.mTimeStamp = in.readLong();
        this.mAppName = in.readString();
        this.mInstallerName = in.readString();
        this.mAppId = in.readString();
        this.mPackageName = in.readString();
        this.mIntent = (Intent) in.readParcelable(null);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mStatus);
        dest.writeString(this.mVersionName);
        dest.writeInt(this.mVersionCode);
        dest.writeLong(this.mUpdateTime);
        dest.writeString(this.mUpdateLog);
        dest.writeLong(this.mNonce);
        dest.writeLong(this.mTimeStamp);
        dest.writeString(this.mAppName);
        dest.writeString(this.mInstallerName);
        dest.writeString(this.mAppId);
        dest.writeString(this.mPackageName);
        dest.writeParcelable(this.mIntent, 0);
    }
}
