package com.miui.home.launcher.lockwallpaper.mode;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

public class WallpaperInfo {
    public String authority;
    public String content;
    public String cp;
    public String ex;
    public String key;
    public String landingPageUrl;
    public boolean like;
    public String packageName;
    public int pos;
    public boolean supportLike;
    public String tag;
    public String title;
    public String wallpaperUri;

    public String toString() {
        return "WallpaperInfo [authority=" + this.authority + ", key=" + this.key + ", wallpaperUri=" + this.wallpaperUri + ", title=" + this.title + ", content=" + this.content + ", packageName=" + this.packageName + ", landingPageUrl=" + this.landingPageUrl + ", supportLike=" + this.supportLike + ", like=" + this.like + ", tag=" + this.tag + ", cp=" + this.cp + ", pos=" + this.pos + ", ex=" + this.ex + "]";
    }

    public Intent buildIntent() {
        if (TextUtils.isEmpty(this.landingPageUrl)) {
            return null;
        }
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setData(Uri.parse(this.landingPageUrl));
        intent.setFlags(268435456);
        if (TextUtils.isEmpty(this.packageName)) {
            return intent;
        }
        intent.setPackage(this.packageName);
        return intent;
    }
}
