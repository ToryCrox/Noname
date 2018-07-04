package com.miui.home.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RecommendAppsDownloadReceiver extends BroadcastReceiver {
    private static RecommendAppsDownloadReceiver mInstanse;
    private Launcher mLauncher;

    public static RecommendAppsDownloadReceiver getInstanse() {
        if (mInstanse == null) {
            mInstanse = new RecommendAppsDownloadReceiver();
        }
        return mInstanse;
    }

    public void setLauncher(Launcher launcher) {
        this.mLauncher = launcher;
    }

    public void onReceive(Context context, Intent intent) {
        if ("com.xiaomi.market.DesktopRecommendDownloadStart".equals(intent.getAction()) && this.mLauncher != null) {
            long folderId = intent.getLongExtra("folderId", -1);
            if (this.mLauncher.getFolderInfoById(folderId) != null && this.mLauncher.isFolderShowing() && this.mLauncher.getFolderCling().getFolderId() == folderId) {
                this.mLauncher.getFolderCling().getRecommendScreen().onAppStartDownload(intent.getStringExtra("appId"));
            }
        }
    }
}
