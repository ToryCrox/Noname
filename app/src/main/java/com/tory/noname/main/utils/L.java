package com.tory.noname.main.utils;

import android.util.Log;

import com.tory.noname.BuildConfig;

/**
 * @Author: Tory
 * Create: 2016/9/11
 * Update: 2016/9/11
 */
public class L {

    public static final String TAG = "NoName";
    public static final boolean FORCE_DEBUG = BuildConfig.DEBUG;
    public static final boolean DEBUG = FORCE_DEBUG || Log.isLoggable(TAG,Log.DEBUG);
    public static void d(String msg){
        if(DEBUG){
            Log.d(TAG,msg);
        }
    }

    public static void d(String tag,String msg){
        if(DEBUG){
            Log.d(TAG,tag +"  "+msg);
        }
    }

    public static void i(String tag,String msg){
        if(DEBUG){
            Log.i(TAG,tag +"  "+msg);
        }
    }

    public static void w(String msg){
        Log.w(TAG,msg);
    }

    public static void w(String tag,String msg){
            Log.w(TAG,tag +"  "+msg);
    }

    public static void e(String tag,String msg){
        Log.e(TAG,tag +"  "+msg);
    }

    public static void e(String tag,String msg,Throwable e){
        Log.e(TAG,tag +"  "+msg, e);
    }
}
