package com.tory.library.utils;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.tory.library.R;
import com.tory.library.log.LogUtils;

import java.lang.ref.WeakReference;

/**
 * Created by tao.xu2 on 2016/8/19.
 */
public class Utilities {



    public static final boolean ATLEAST_MARSHMALLOW = Build.VERSION.SDK_INT >= 23;

    public static final boolean ATLEAST_LOLLIPOP_MR1 =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;

    public static final boolean ATLEAST_LOLLIPOP =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

    public static final boolean ATLEAST_KITKAT =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

    public static final boolean ATLEAST_JB_MR1 =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;

    public static final boolean ATLEAST_JB_MR2 =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;

    public static boolean isRtl(Resources res) {
        return ATLEAST_JB_MR1 &&
                (res.getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL);
    }

    public static void initSwipeRefresh(SwipeRefreshLayout swipeRefreshLayout) {
        if(swipeRefreshLayout == null) return;
        // 设置下拉圆圈上的颜色，蓝色、绿色、橙色、红色
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    public static void setNightMode(Context context, boolean night) {
        setNightMode(context, night, true);
    }

    public static void setNightMode(@NonNull Context context, boolean night, boolean recreateNow) {
        boolean nowmode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
        boolean modeChange = nowmode == night;
        int mode = night ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        AppCompatDelegate.setDefaultNightMode(mode);
        if (modeChange) {
            LogUtils.d("setNightMode","DefaultNightMode not change nightMode:" + nowmode);
            return;
        }
        SettingHelper.getInstance(context).setNightMode(night);
        if (context instanceof AppCompatActivity) {
            final AppCompatActivity activity = (AppCompatActivity) context;
            int delay = recreateNow ? 0 : 300;
            new DealyRecreateHandler(Looper.getMainLooper(), activity).sendEmptyMessageDelayed(0, delay);
        }
    }

    static class DealyRecreateHandler extends Handler {
        private WeakReference<Activity> weakReference;

        public DealyRecreateHandler(Looper looper, Activity activity) {
            super(looper);
            weakReference = new WeakReference<Activity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            Activity activity = weakReference.get();
            if (activity != null && !activity.isDestroyed()) {
                activity.getWindow().setWindowAnimations(R.style.WindowAnimationFadeInOut);
                activity.recreate();
            }
        }
    }

    /**
     * 实现文本复制功能
     * add by wangqianzhou
     *
     * @param content
     */
    public static void copyToClipboar(Context context,String content) {
        // 得到剪贴板管理器
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(content.trim());
    }

    /**
     * 实现粘贴功能
     * add by wangqianzhou
     *
     * @param context
     * @return
     */
    public static String pasteFromClipboar(Context context) {
        // 得到剪贴板管理器
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        return cmb.getText().toString().trim();
    }


    public static final String WEB_URL = "web_url";
    public static final String ACTION_WEB_VIEW = "com.tory.action.WEB_VIEW";
    public static final String ACTION_WEB_VIEW_X5 = "com.tory.action.WEB_VIEW_X5";
    /**
     * 用内部浏览器打开
     * @param context
     * @param url
     */
    public static void startWeb(Context context, String url) {
        Intent intent = new Intent();
        if("1".equals(SettingHelper.getInstance(context).getWebKener())){
            intent.setAction(ACTION_WEB_VIEW);
        }else{
            intent.setAction(ACTION_WEB_VIEW_X5);
        }
        intent.putExtra(WEB_URL, url);
        context.startActivity(intent);
    }

    /**
     * 用外部浏览器打开
     * @param context
     * @param url
     */
    public static void openInBrowser(Context context,String url){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse(url));
        context.startActivity(intent);
    }

}
