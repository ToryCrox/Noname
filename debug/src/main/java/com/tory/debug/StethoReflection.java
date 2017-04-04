package com.tory.debug;

import android.content.Context;
import android.util.Log;

import com.facebook.stetho.Stetho;

/**
 * Created by tao.xu2 on 2017/3/31.
 */

public class StethoReflection {

    private static final String TAG = "StethoReflection";


    public void initStetho(Context context){
        Log.d(TAG,"initStetho context="+context);
        //chrome://inspect
        Stetho.initializeWithDefaults(context);
    }


}
