package com.tory.library.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import androidx.annotation.AnyRes;
import androidx.annotation.AttrRes;
import androidx.core.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.ViewConfiguration;

import java.lang.reflect.Method;

/**
 * Created by tao.xu2 on 2016/6/10.
 */
public class SystemConfigUtils {

    public static final String STATUS_BAR_HEIGHT_RES_NAME = "status_bar_height";
    public static final String NAV_BAR_HEIGHT_RES_NAME = "navigation_bar_height";
    public static final String NAV_BAR_HEIGHT_LANDSCAPE_RES_NAME = "navigation_bar_height_landscape";
    public static final String NAV_BAR_WIDTH_RES_NAME = "navigation_bar_width";
    public static final String SHOW_NAV_BAR_RES_NAME = "config_showNavigationBar";

    public static String sNavBarOverride;

    static {
        // Android allows a system property to override the presence of the navigation bar.
        // Used by the emulator.
        // See https://github.com/android/platform_frameworks_base/blob/master/policy/src/com/android/internal/policy/impl/PhoneWindowManager.java#L1076
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                Class c = Class.forName("android.os.SystemProperties");
                Method m = c.getDeclaredMethod("get", String.class);
                m.setAccessible(true);
                sNavBarOverride = (String) m.invoke(null, "qemu.hw.mainkeys");
            } catch (Throwable e) {
                sNavBarOverride = null;
            }
        }
    }


    /**
     * 获取ActionBar的高度
     * @param context
     * @return
     */
    @TargetApi(14)
    public static int getActionBarHeight(Context context) {
        int result = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            TypedValue tv = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
            result = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        }
        return result;
    }

    /**
     * 获取主题中的属性
     * @param context
     * @param attrName
     * @return
     */
    @AnyRes
    public static int getThemeAttr(Context context,@AttrRes int attrName){
        TypedValue tv = new TypedValue();
        context.getTheme().resolveAttribute(attrName, tv, true);
        return tv.resourceId;
    }

    /**
     * 获取主题中的属性的颜色
     * @param context
     * @param attrName
     * @return
     */
    public static int getThemeColor(Context context, @AttrRes int attrName){
        return ContextCompat.getColor(context, getThemeAttr(context, attrName));
    }

    /**
     * 获取状态栏的高度
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context){
        return getInternalDimensionSize(context.getResources(), STATUS_BAR_HEIGHT_RES_NAME);
    }

    /**
     * 获取虚拟导航栏的高度
     * @param context
     * @return
     */
    @TargetApi(14)
    public static  int getNavigationBarHeight(Context context) {
        Resources res = context.getResources();
        int result = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (hasNavBar(context)) {
                String key;
                boolean mInPortrait = (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
                if (mInPortrait) {
                    key = NAV_BAR_HEIGHT_RES_NAME;
                } else {
                    key = NAV_BAR_HEIGHT_LANDSCAPE_RES_NAME;
                }
                return getInternalDimensionSize(res, key);
            }
        }
        return result;
    }

    /**
     * 获取虚拟导航栏的宽度
     * @param context
     * @return
     */
    @TargetApi(14)
    public static  int getNavigationBarWidth(Context context) {
        Resources res = context.getResources();
        int result = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (hasNavBar(context)) {
                return getInternalDimensionSize(res, NAV_BAR_WIDTH_RES_NAME);
            }
        }
        return result;
    }

    /**
     * 检查是否有虚拟导航栏
     * @param context
     * @return
     */
    @TargetApi(14)
    public static  boolean hasNavBar(Context context) {
        Resources res = context.getResources();
        int resourceId = res.getIdentifier(SHOW_NAV_BAR_RES_NAME, "bool", "android");
        if (resourceId != 0) {
            boolean hasNav = res.getBoolean(resourceId);
            // check override flag (see static block)
            if ("1".equals(sNavBarOverride)) {
                hasNav = false;
            } else if ("0".equals(sNavBarOverride)) {
                hasNav = true;
            }
            return hasNav;
        } else { // fallback
            return !ViewConfiguration.get(context).hasPermanentMenuKey();
        }
    }

    /**
     * 获取一些系统隐藏的试题值
     * @param res
     * @param key
     * @return
     */
    public static  int getInternalDimensionSize(Resources res, String key) {
        int resId = res.getIdentifier(key, "dimen", "android");
        int result = resId > 0 ? res.getDimensionPixelSize(resId) : 0;
        return result;
    }

    @SuppressLint("NewApi")
    public static  float getSmallestWidthDp(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        } else {
            // TODO this is not correct, but we don't really care pre-kitkat
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        }
        float widthDp = metrics.widthPixels / metrics.density;
        float heightDp = metrics.heightPixels / metrics.density;
        return Math.min(widthDp, heightDp);
    }
}
