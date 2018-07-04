package com.miui.home.launcher.structures;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public class ForceTouchItem {
    private String mDesc;
    private Drawable mDrawableIcon;
    private Intent mIntent;
    private String mTitle;
    private String mType;

    public String getTitle() {
        return this.mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getDesc() {
        return this.mDesc;
    }

    public void setDesc(String desc) {
        this.mDesc = desc;
    }

    public Drawable getDrawableIcon() {
        return this.mDrawableIcon;
    }

    public void setDrawableIcon(Drawable drawableIcon) {
        this.mDrawableIcon = drawableIcon;
    }

    public Intent getIntent() {
        return this.mIntent;
    }

    public void setIntent(Intent intent) {
        this.mIntent = intent;
    }

    public void setType(String type) {
        this.mType = type;
    }

    public String getType() {
        return this.mType;
    }
}
