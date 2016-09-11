package com.tory.noname.utils;

import android.util.Log;

/**
 * @Author: Tory
 * Create: 2016/9/11
 * Update: 2016/9/11
 */
public class L {

    public static final String TAG = "NoName";
    public static final boolean DEBUG = true;

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

    public static void w(String tag,String msg){
            Log.w(TAG,tag +"  "+msg);
    }

    public static void e(String tag,String msg){
            Log.e(TAG,tag +"  "+msg);
    }
}
