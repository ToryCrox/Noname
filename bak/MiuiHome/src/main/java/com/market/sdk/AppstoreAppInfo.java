package com.market.sdk;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.market.sdk.utils.VersionUtils;
import java.util.ArrayList;
import java.util.List;
import miui.os.Build;

public class AppstoreAppInfo implements Parcelable {
    public static final Creator<AppstoreAppInfo> CREATOR = new Creator<AppstoreAppInfo>() {
        public AppstoreAppInfo createFromParcel(Parcel source) {
            return new AppstoreAppInfo(source);
        }

        public AppstoreAppInfo[] newArray(int size) {
            return new AppstoreAppInfo[size];
        }
    };
    private static boolean sNeedInmobiParamter;
    public String adInfoPassback;
    public int ads;
    public String appId;
    public Uri appUri;
    public List<String> clickMonitorUrls = new ArrayList();
    public String digest;
    public String experimentalId;
    public String iconMask;
    public Uri iconUri;
    public List<String> impressionMonitorUrls = new ArrayList();
    private volatile long mFlag = -1;
    public String pkgName;
    public String title;
    public List<String> viewMonitorUrls = new ArrayList();

    static {
        if (Build.IS_DEVELOPMENT_VERSION) {
            sNeedInmobiParamter = VersionUtils.isDevVersionLaterThan("6.3.21");
        } else if (Build.IS_STABLE_VERSION) {
            sNeedInmobiParamter = VersionUtils.isStableVersionLaterThan("V7.3.0.0");
        }
    }

    public int describeContents() {
        return 0;
    }

    public AppstoreAppInfo(Parcel source) {
        this.appId = source.readString();
        this.pkgName = source.readString();
        this.title = source.readString();
        this.ads = source.readInt();
        this.digest = source.readString();
        this.experimentalId = source.readString();
        this.iconMask = source.readString();
        this.iconUri = (Uri) Uri.CREATOR.createFromParcel(source);
        this.appUri = (Uri) Uri.CREATOR.createFromParcel(source);
        if (sNeedInmobiParamter) {
            source.readStringList(this.viewMonitorUrls);
            source.readStringList(this.clickMonitorUrls);
            source.readStringList(this.impressionMonitorUrls);
            this.adInfoPassback = source.readString();
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.appId);
        dest.writeString(this.pkgName);
        dest.writeString(this.title);
        dest.writeInt(this.ads);
        dest.writeString(this.digest);
        dest.writeString(this.experimentalId);
        dest.writeString(this.iconMask);
        Uri.writeToParcel(dest, this.iconUri);
        Uri.writeToParcel(dest, this.appUri);
        if (sNeedInmobiParamter) {
            dest.writeStringList(this.viewMonitorUrls);
            dest.writeStringList(this.clickMonitorUrls);
            dest.writeStringList(this.impressionMonitorUrls);
            dest.writeString(this.adInfoPassback);
        }
    }
}
