package com.miui.home.launcher;

import android.appwidget.AppWidgetHostView;
import android.content.ContentValues;
import android.content.Context;

class LauncherAppWidgetInfo extends ItemInfo {
    int appWidgetId;
    AppWidgetHostView hostView;
    String packageName;

    LauncherAppWidgetInfo(int appWidgetId) {
        this.hostView = null;
        this.itemType = 4;
        this.appWidgetId = appWidgetId;
    }

    LauncherAppWidgetInfo(int appWidgetId, LauncherAppWidgetProviderInfo provider) {
        this(appWidgetId);
        this.cellX = provider.cellX;
        this.cellY = provider.cellY;
        this.spanX = provider.spanX;
        this.spanY = provider.spanY;
        this.screenId = provider.screenId;
        this.container = -100;
    }

    public void onAddToDatabase(Context context, ContentValues values) {
        super.onAddToDatabase(context, values);
        values.put("appWidgetId", Integer.valueOf(this.appWidgetId));
    }

    public String toString() {
        return "AppWidget(id=" + Integer.toString(this.appWidgetId) + ")";
    }

    public void unbind() {
        super.unbind();
        this.hostView = null;
    }
}
