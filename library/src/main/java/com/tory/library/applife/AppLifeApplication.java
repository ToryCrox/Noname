package com.tory.library.applife;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

/**
 * @author tory
 * @dae 2018-7-9
 */
public class AppLifeApplication extends Application {

    ApplicationDelegate mApplicationDelegate;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        mApplicationDelegate = new ApplicationDelegate();
        mApplicationDelegate.attachBaseContext(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApplicationDelegate.onCreate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mApplicationDelegate.onConfigurationChanged(newConfig);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        mApplicationDelegate.onTerminate();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mApplicationDelegate.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        mApplicationDelegate.onTrimMemory(level);
    }
}
