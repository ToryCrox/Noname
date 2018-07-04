package com.miui.home.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class UninstallShortcutReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent data) {
        if ("com.miui.home.launcher.action.UNINSTALL_SHORTCUT".equals(data.getAction())) {
            final LauncherApplication app = Application.getLauncherApplication(context);
            final Launcher launcher = app.getLauncher();
            if (launcher == null || !launcher.isReadyToBinding()) {
                Log.e("Launcher.UninstallShortcutReceiver", "Launcher is not ready,process later");
                return;
            }
            final Context context2 = context;
            final Intent intent = data;
            launcher.runOnUiThread(new Runnable() {
                public void run() {
                    if (app.getModel() != null && !launcher.isDestroyed()) {
                        if (launcher.isWorkspaceLoading()) {
                            launcher.getWorkspace().postDelayed(this, 100);
                        } else {
                            launcher.uninstallShortcut(context2, intent);
                        }
                    }
                }
            });
        }
    }
}
