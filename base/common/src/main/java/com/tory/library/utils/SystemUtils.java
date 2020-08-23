package com.tory.library.utils;

import java.lang.reflect.Method;

/**
 * Created by tao.xu2 on 2016/8/8.
 */
public class SystemUtils {

    public static String getProperty(String key) {
        try {
            Class c = Class.forName("android.os.SystemProperties");
            Method m = c.getDeclaredMethod("get", String.class);
            m.setAccessible(true);
            return (String) m.invoke(null, key);
        } catch (Exception e) {
            return null;
        }
    }

}
