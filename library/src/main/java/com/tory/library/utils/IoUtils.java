package com.tory.library.utils;

import android.util.Log;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author tory
 * @create 2018/7/22
 * @Describe
 */
public class IoUtils {

    private static final String TAG = "IoUtils";

    /**
     * 关闭流
     * @param c
     */
    public static void closeSilently(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Exception e) {
                Log.e(TAG, "closeSilently", e);
            }
        }
    }
}
