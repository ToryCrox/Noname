package com.tory.noname.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

/**
 * @Author: Tory
 * Create: 2016/9/15
 * Update: 2016/9/15
 */
public class SpHelper {
    public static final String SHARED_PATH = "sp_shared";
    private static SpHelper sInstance;
    private SharedPreferences sp;

    private SpHelper(Context context,String filepath){
        if(filepath == null){
            sp = PreferenceManager.getDefaultSharedPreferences(context);
        }else{
            sp = context.getSharedPreferences(filepath,Context.MODE_PRIVATE);
        }
    }

    private SpHelper(Context context){
        this(context,null);
    }



    public static SpHelper getInstance(Context context){
        if(sInstance == null){
            synchronized (SpHelper.class){
                sInstance = new SpHelper(context);
            }
        }
        return sInstance;
    }

    public static SpHelper newInstance(Context context,String filepath){
        return new SpHelper(context,filepath);
    }

    public int getInt(String key) {
        if (key != null && !key.equals("")) {
            return sp.getInt(key, 0);
        }
        return 0;
    }

    public long getLong(String key) {
        if (key != null && !key.equals("")) {
            return sp.getLong(key, 0);
        }
        return 0;
    }

    public String getString(String key) {
        if (key != null && !key.equals("")) {
            return sp.getString(key, null);
        }
        return null;
    }

    public boolean getBoolean(String key) {
        if (key != null && !key.equals("")) {
            return sp.getBoolean(key, false);
        }
        return true;
    }

    public float getFloat(String key) {
        if (key != null && !key.equals("")) {
            return sp.getFloat(key, 0);
        }
        return 0;
    }

    public void putString(String key, String value) {
        if (key != null && !key.equals("")) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(key, value);
            editor.commit();
        }
    }

    public void putInt(String key, int value) {
        if (key != null && !key.equals("")) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(key, value);
            editor.commit();
        }
    }

    public void put(String key, boolean value) {
        if (key != null && !key.equals("")) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(key, value);
            editor.commit();
        }
    }

    public void put(String key, long value) {
        if (key != null && !key.equals("")) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putLong(key, value);
            editor.commit();
        }
    }

    public void put(String key, Float value) {
        if (key != null && !key.equals("")) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putFloat(key, value);
            editor.commit();
        }
    }
}
