package com.tory.noname;

import com.facebook.stetho.Stetho;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.tory.library.applife.AppLifeApplication;
import com.tory.noname.main.utils.L;
import com.tory.noname.main.utils.SettingHelper;
import com.tory.noname.main.utils.Utilities;

/**
 * @Author: Tory
 * Create: 2016/9/15
 * Update: 2016/9/15
 */
public class MApplication extends AppLifeApplication {
    private static MApplication instance;
    private RefWatcher refWatcher;

    public static MApplication getInstance() {
        return instance;
    }

    public void onCreate() {
        super.onCreate();
        instance = this;
        //chrome://inspect
        Stetho.initializeWithDefaults(this);
        //if(BuildConfig.DEBUG){
        //    ReflectDebugUtil.reflectInitStetho(this);
        //}
        Utilities.setNightMode(this, SettingHelper.getInstance(this).isNightMode());
        refWatcher = setupLeakCanary();
        L.d("StethoReflection sourceDir="+getApplicationInfo().sourceDir);
    }

    private RefWatcher setupLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return RefWatcher.DISABLED;
        }
        return LeakCanary.install(this);
    }
}
