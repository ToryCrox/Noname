package com.tory.library.applife;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

/**
 * @author tory
 * @dae 2018-7-9
 *
 */
public interface IAppLife {

    /**
     * Same as {@link Application#attachBaseContext(Context context)}.
     * 为了准确获取Application这里参数换成application
     */
    void attachBaseContext(Application base);

    /**
     * Same as {@link Application#onCreate()}.
     */
    void onCreate();

    /**
     * Same as {@link Application#onLowMemory()}.
     */
    void onLowMemory();

    /**
     * Same as {@link Application#onTrimMemory(int level)}.
     * @param level
     */
    void onTrimMemory(int level);

    /**
     * Same as {@link Application#onTerminate()}.
     */
    void onTerminate();

    /**
     * Same as {@link Application#onConfigurationChanged(Configuration newconfig)}.
     */
    void onConfigurationChanged(Configuration newConfig);

    /**
     * 执行优先级, see {@link AppLife#priority()}
     * @return
     */
    int priority();

    /**
     * 是否可用，方便随时关闭 see {@link AppLife#enable()}
     * @return
     */
    boolean enable();
}
