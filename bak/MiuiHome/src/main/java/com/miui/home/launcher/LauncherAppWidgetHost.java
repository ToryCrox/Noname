package com.miui.home.launcher;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;

public class LauncherAppWidgetHost extends AppWidgetHost {
    private Launcher mLauncher;

    public LauncherAppWidgetHost(Context context, Launcher launcher, int hostId) {
        super(context, hostId);
        this.mLauncher = launcher;
    }

    protected AppWidgetHostView onCreateView(Context context, int appWidgetId, AppWidgetProviderInfo appWidget) {
        return new LauncherAppWidgetHostView(context, this.mLauncher);
    }
}
