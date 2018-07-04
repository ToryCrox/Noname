package com.xiaomi.analytics;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.util.Log;
import com.xiaomi.analytics.LogEvent.LogType;
import com.xiaomi.analytics.a.a.a;
import com.xiaomi.analytics.a.a.b;
import com.xiaomi.analytics.a.a.e;

public class Analytics {
    public static void trackSystem(Context context, String str, Action action) throws Exception {
        if (isSystemPackage(context) || isPlatformSignature(context)) {
            Intent intent = new Intent();
            intent.setClassName("com.miui.analytics", "com.miui.analytics.EventService");
            String str2 = "key";
            if (str == null) {
                str = "";
            }
            intent.putExtra(str2, str);
            intent.putExtra("content", action.getContent().toString());
            intent.putExtra("extra", action.getExtra().toString());
            if (context.getApplicationContext() != null) {
                intent.putExtra("appid", context.getPackageName());
            }
            if (action instanceof AdAction) {
                intent.putExtra("type", LogType.TYPE_AD.value());
            } else {
                intent.putExtra("type", LogType.TYPE_EVENT.value());
            }
            context.startService(intent);
            return;
        }
        throw new IllegalArgumentException("App is not allowed to use this method to track event, except system or platform signed apps. Use getTracker instead.");
    }

    private static boolean isPlatformSignature(Context context) {
        boolean a = e.a(b.a(context, context.getPackageName()));
        Log.d(a.c("Analytics"), String.format("%s is platform signatures : %b", new Object[]{context.getPackageName(), Boolean.valueOf(a)}));
        return a;
    }

    private static boolean isSystemPackage(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        return (applicationInfo == null || (applicationInfo.flags & 1) == 0) ? false : true;
    }
}
