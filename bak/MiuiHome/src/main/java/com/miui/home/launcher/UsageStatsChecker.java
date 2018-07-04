package com.miui.home.launcher;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.os.Build.VERSION;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UsageStatsChecker {
    static final String TAG = UsageStatsChecker.class.getName();

    public void updateNewInstalledApps(Launcher launcher, HashMap<Intent, ShortcutInfo> allLoadedApps, ArrayList<ShortcutInfo> newInstalledApps, long startTime) {
        if (!newInstalledApps.isEmpty()) {
            UsageStatsManager usageStatsManager = (UsageStatsManager) launcher.getSystemService("usagestats");
            List<String> allPackageUsageStats = new ArrayList();
            List<UsageStats> usageStats = usageStatsManager.queryUsageStats(4, startTime, System.currentTimeMillis());
            synchronized (allLoadedApps) {
                if (usageStats != null) {
                    for (UsageStats us : usageStats) {
                        if (VERSION.SDK_INT < 23) {
                            allPackageUsageStats.add(us.getPackageName());
                        } else {
                            try {
                                if (Class.forName("android.app.usage.UsageStats").getField("mLastTimeSystemUsed").getLong(us) > startTime) {
                                    allPackageUsageStats.add(us.getPackageName());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    for (int i = newInstalledApps.size() - 1; i >= 0; i--) {
                        ShortcutInfo info = (ShortcutInfo) newInstalledApps.get(i);
                        if (!(info.intent == null || info.intent.getComponent() == null || !allPackageUsageStats.contains(info.intent.getComponent().getPackageName()))) {
                            info.onLaunch(launcher);
                        }
                    }
                    return;
                }
            }
        }
    }
}
