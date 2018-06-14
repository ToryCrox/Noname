package com.mimikko.buglytest;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.leon.channel.helper.ChannelReaderUtil;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.tinker.loader.app.DefaultApplicationLike;

/**
 * @author xutao
 * @date 2018/6/13
 */

public class AppLike extends DefaultApplicationLike{

    public AppLike(Application application, int tinkerFlags, boolean tinkerLoadVerifyFlag, long applicationStartElapsedTime, long applicationStartMillisTime, Intent tinkerResultIntent) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime, applicationStartMillisTime, tinkerResultIntent);
    }


    @Override
    public void onBaseContextAttached(Context base) {
        super.onBaseContextAttached(base);

        //MultiDex.install(base);

        // 安装tinker
        // TinkerManager.installTinker(this); 替换成下面Bugly提供的方法
        Beta.installTinker(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        String channel = ChannelReaderUtil.getChannel(getApplication());
        if (channel != null){
            Bugly.setAppChannel(getApplication(), channel);
        }
        // 这里实现SDK初始化，appId替换成你的在Bugly平台申请的appId
        // 调试时，将第三个参数改为true
        Bugly.init(getApplication(), Constants.BUGLY_APPID, true);
    }
}
