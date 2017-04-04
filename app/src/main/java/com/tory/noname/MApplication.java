package com.tory.noname;

import android.app.Application;

import com.tory.library.reflect.ReflectDebugUtil;
import com.tory.noname.utils.L;
import com.tory.noname.utils.SettingHelper;
import com.tory.noname.utils.Utilities;

/**
 * @Author: Tory
 * Create: 2016/9/15
 * Update: 2016/9/15
 */
public class MApplication extends Application {
    private static MApplication instance;

    public static MApplication getInstance() {
        return instance;
    }

    public void onCreate() {
        super.onCreate();
        instance = this;
        //chrome://inspect
        //Stetho.initializeWithDefaults(this);
        ReflectDebugUtil.reflectInitStetho(this);
        Utilities.setNightMode(this, SettingHelper.getInstance(this).isNightMode());

        L.d("StethoReflection sourceDir="+getApplicationInfo().sourceDir);
    }
}
