package com.tory.noname.main.utils;

import android.content.Context;

import com.facebook.stetho.Stetho;

/**
 * Created by tao.xu2 on 2017/3/31.
 */

public class StethoReflection {

    public void initStetho(Context context){
        L.d("StethoReflection initStetho context="+context);
        //chrome://inspect
        Stetho.initializeWithDefaults(context);
    }
}
