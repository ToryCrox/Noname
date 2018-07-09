package com.tory.library.applife;

import android.app.Application;
import android.content.res.Configuration;
import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.List;

/**
 * @author tory
 * @dae 2018-7-9
 * 帮忙Application分发AppLike
 */
public class ApplicationDelegate implements IAppLife{

    @NonNull
    private List<IAppLife> mAppLifes;

    @Override
    public void attachBaseContext(Application base) {
        mAppLifes = new ManifestParser(base).parse();
        //进行优先级排序，大的在前，小的在后
        Collections.sort(mAppLifes, (o1, o2) -> o2.priority() - o1.priority());
        for (IAppLife iAppLife : mAppLifes) {
            if(iAppLife.enable()){
                iAppLife.attachBaseContext(base);
            }
        }
    }

    @Override
    public void onCreate() {
        for (IAppLife iAppLife : mAppLifes) {
            if(iAppLife.enable()){
                iAppLife.onCreate();
            }
        }
    }

    @Override
    public void onLowMemory() {
        for (IAppLife iAppLife : mAppLifes) {
            if(iAppLife.enable()){
                iAppLife.onLowMemory();
            }
        }
    }

    @Override
    public void onTrimMemory(int level) {
        for (IAppLife iAppLife : mAppLifes) {
            if(iAppLife.enable()){
                iAppLife.onTrimMemory(level);
            }
        }
    }

    @Override
    public void onTerminate() {
        for (IAppLife iAppLife : mAppLifes) {
            if(iAppLife.enable()){
                iAppLife.onTerminate();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        for (IAppLife iAppLife : mAppLifes) {
            if(iAppLife.enable()){
                iAppLife.onConfigurationChanged(newConfig);
            }
        }
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public boolean enable() {
        return false;
    }
}
