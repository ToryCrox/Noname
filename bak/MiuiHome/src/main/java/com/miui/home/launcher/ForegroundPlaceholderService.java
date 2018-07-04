package com.miui.home.launcher;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.IBinder;

public class ForegroundPlaceholderService extends Service {
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        if (VERSION.SDK_INT < 24) {
            startForeground(1, new Notification());
        }
    }
}
