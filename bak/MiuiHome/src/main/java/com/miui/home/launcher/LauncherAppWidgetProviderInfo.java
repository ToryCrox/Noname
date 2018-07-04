package com.miui.home.launcher;

import android.appwidget.AppWidgetProviderInfo;

public class LauncherAppWidgetProviderInfo extends ItemInfo {
    public int mWidgetCategory;
    public AppWidgetProviderInfo providerInfo;

    public LauncherAppWidgetProviderInfo(AppWidgetProviderInfo providerInfo) {
        this.mWidgetCategory = -1000;
        this.itemType = 6;
        this.providerInfo = providerInfo;
    }

    public LauncherAppWidgetProviderInfo clone() {
        return (LauncherAppWidgetProviderInfo) super.clone();
    }
}
