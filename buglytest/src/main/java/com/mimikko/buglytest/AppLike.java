package com.mimikko.buglytest;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.leon.channel.helper.ChannelReaderUtil;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.interfaces.BetaPatchListener;
import com.tencent.tinker.loader.app.DefaultApplicationLike;

import java.util.Locale;

/**
 * @author xutao
 * @date 2018/6/13
 */

public class AppLike extends DefaultApplicationLike{

    private static final String TAG = "AppLike";

    public AppLike(Application application, int tinkerFlags, boolean tinkerLoadVerifyFlag, long applicationStartElapsedTime, long applicationStartMillisTime, Intent tinkerResultIntent) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime, applicationStartMillisTime, tinkerResultIntent);
    }


    @Override
    public void onBaseContextAttached(Context base) {
        super.onBaseContextAttached(base);

        //MultiDex.install(base);

        MLog.d(TAG, "onBaseContextAttached="+getCurrentProcessName(base));
        // 安装tinker
        // TinkerManager.installTinker(this); 替换成下面Bugly提供的方法
        Beta.installTinker(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        configTinker();
        String channel = ChannelReaderUtil.getChannel(getApplication());
        if (channel != null){
            Bugly.setAppChannel(getApplication(), channel);
        }
        // 这里实现SDK初始化，appId替换成你的在Bugly平台申请的appId
        // 调试时，将第三个参数改为true
        Bugly.init(getApplication(), Constants.BUGLY_APPID, true);
    }


    private void configTinker() {
        // 设置是否开启热更新能力，默认为true
        Beta.enableHotfix = true;
        // 设置是否自动下载补丁，默认为true
        Beta.canAutoDownloadPatch = true;
        // 设置是否自动合成补丁，默认为true
        Beta.canAutoPatch = true;
        // 设置是否提示用户重启，默认为false
        Beta.canNotifyUserRestart = true;
        // 补丁回调接口
        Beta.betaPatchListener = new BetaPatchListener() {
            @Override
            public void onPatchReceived(String patchFile) {
                Beta.
                MLog.d(TAG, "onPatchReceived patchFile="+patchFile);
            }

            @Override
            public void onDownloadReceived(long savedLength, long totalLength) {
                MLog.d(TAG, "onDownloadReceived "+String.format(Locale.getDefault(), "%s %d%%",
                        Beta.strNotificationDownloading,
                        (int) (totalLength == 0 ? 0 : savedLength * 100 / totalLength)));
            }

            @Override
            public void onDownloadSuccess(String msg) {
                MLog.d(TAG, "onDownloadSuccess msg="+msg);
            }

            @Override
            public void onDownloadFailure(String msg) {
                MLog.d(TAG, "onDownloadFailure msg="+msg);

            }

            @Override
            public void onApplySuccess(String msg) {
                MLog.d(TAG, "onApplySuccess msg="+msg);
            }

            @Override
            public void onApplyFailure(String msg) {
                MLog.d(TAG, "onApplyFailure msg="+msg);
            }

            @Override
            public void onPatchRollback() {
                MLog.d(TAG, "onPatchRollback");
            }
        };
        if(!BuildConfig.DEBUG){
            Bugly.setIsDevelopmentDevice(getApplication(), "beta".equals(BuildConfig.BUILD_TYPE));
        }

    }


    public static String getCurrentProcessName(Context context) {
        String currentProgressName = "";
        int currentPid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcessInfo : activityManager.getRunningAppProcesses()) {
            if (appProcessInfo.pid == currentPid) {
                currentProgressName = appProcessInfo.processName;
                return currentProgressName;
            }
        }
        return currentProgressName;
    }
}
