package com.miui.home.launcher.common;

import android.util.Log;
import org.json.JSONObject;

public class JsonUtils {
    public static String getStringSafely(JSONObject jo, String name) {
        try {
            return jo.getString(name);
        } catch (Exception e) {
            Log.w("com.miui.home.launcher.common,JsonUtils", e.getMessage(), e);
            return "";
        }
    }
}
