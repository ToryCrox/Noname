package com.miui.home.launcher;

import android.content.Context;
import miui.external.ApplicationDelegate;

public class Application extends miui.external.Application {
    private static Application sInstance;

    public ApplicationDelegate onCreateApplicationDelegate() {
        return new LauncherApplication();
    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        sInstance = this;
        DeviceConfig.Init(base, false);
    }

    public static LauncherApplication getLauncherApplication(Context context) {
        return (LauncherApplication) ((Application) context.getApplicationContext()).getApplicationDelegate();
    }

    public static Application getInstance() {
        return sInstance;
    }
}
