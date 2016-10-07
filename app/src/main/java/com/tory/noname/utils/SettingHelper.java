package com.tory.noname.utils;

import android.content.Context;
import android.support.v7.app.AppCompatDelegate;

/**
 * @Author: Tory
 * Create: 2016/9/15
 * Update: 2016/9/15
 */
public class SettingHelper {
    public static final String SHARED_PATH = SpHelper.SHARED_PATH;

    public static final String SP_KEY_MODE_NIGHT = "mode_night";

    private static SettingHelper sInstance;
    private SpHelper mSpHelper;

    public static SettingHelper getInstance(Context context) {
        if(sInstance == null){
            synchronized (SettingHelper.class){
                sInstance = new SettingHelper(context);
            }
        }
        return sInstance;
    }

    private SettingHelper(Context context) {
        mSpHelper = SpHelper.getInstance(context);
    }

    public boolean isNightModeNow(){
        return AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
    }

    public boolean isNightMode(){
        return mSpHelper.getBoolean(SP_KEY_MODE_NIGHT);
    }

    public void setNightMode(boolean night){
        mSpHelper.put(SP_KEY_MODE_NIGHT,night);
    }

    public String getWebKener(){
        return mSpHelper.getString("pf_web_selete_key");
    }
}
