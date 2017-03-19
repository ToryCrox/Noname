package com.tory.library.base;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * @Author: tory
 * Create: 2017/3/19
 * Update: 2017/3/19
 */
public abstract class WeekHandler<T> extends Handler {

    WeakReference<T> mActivity;

    public WeekHandler(T activity){
        super();
        mActivity = new WeakReference<T>(activity);
    }

    @Override
    public void handleMessage(Message msg) {
        T activity = mActivity.get();
        if(activity == null){
            Log.e("WeekHandler","activity is null");
            return;
        }
        handleMessage(activity,msg);
    }

    public abstract void handleMessage(T activity, Message msg);
}
