package com.tory.library.applife;

import android.app.Application;
import android.content.res.Configuration;

/**
 * @author tory
 * @dae 2018-7-9
 */
public class AppLife implements IAppLife {

    protected Application mApplication;

    @Override
    public void attachBaseContext(Application application) {
        mApplication = application;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onLowMemory() {

    }

    @Override
    public void onTrimMemory(int level) {

    }

    @Override
    public void onTerminate() {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public boolean enable() {
        return true;
    }
}
