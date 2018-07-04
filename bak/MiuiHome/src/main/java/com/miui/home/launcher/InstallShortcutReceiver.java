package com.miui.home.launcher;

import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import android.util.Log;
import com.miui.home.launcher.gadget.GadgetFactory;
import java.util.HashSet;

public class InstallShortcutReceiver extends BroadcastReceiver {
    public static final HashSet<String> sSkippedItems = new HashSet();

    static {
        sSkippedItems.add("com.android.vending");
    }

    public void onReceive(final Context context, final Intent data) {
        final String senderPackageName = data.getSender();
        if ("com.android.launcher.action.INSTALL_SHORTCUT".equals(data.getAction()) && !sSkippedItems.contains(senderPackageName)) {
            new Thread() {
                public void run() {
                    try {
                        if (!TextUtils.isEmpty(senderPackageName)) {
                            if (((AppOpsManager) context.getSystemService("appops")).noteOpNoThrow(10017, context.getPackageManager().getApplicationInfo(senderPackageName, 0).uid, senderPackageName) == 0) {
                                InstallShortcutReceiver.this.installShortcut(context, data, senderPackageName);
                            }
                        }
                    } catch (NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    private void installShortcut(Context context, Intent data, String senderPackageName) {
        final LauncherApplication app = Application.getLauncherApplication(context);
        final Launcher launcher = app.getLauncher();
        final Intent intentData = data;
        final Context runningContext = context;
        if (launcher == null || !launcher.isReadyToBinding()) {
            Log.e("InstallShortcutReceiver", "Launcher is not ready,process later");
            return;
        }
        final String str = senderPackageName;
        launcher.runOnUiThread(new Runnable() {
            public void run() {
                LauncherModel model = app.getModel();
                if (model != null && !launcher.isDestroyed()) {
                    if (launcher.isWorkspaceLoading()) {
                        launcher.getWorkspace().postDelayed(this, 100);
                        return;
                    }
                    Intent intent = (Intent) intentData.getParcelableExtra("android.intent.extra.shortcut.INTENT");
                    if (intent == null) {
                        Log.e("InstallShortcutReceiver", "Failed to add shortcut because the extra shortcut intent is missing");
                        return;
                    }
                    if (intent.getAction() == null) {
                        intent.setAction("android.intent.action.VIEW");
                    }
                    if (intent.getAction() == "miui.intent.action.CREATE_QUICK_CLEANUP_SHORTCUT") {
                        launcher.addItemToWorkspace(GadgetFactory.getInfo(12), -1, -100, 0, 0, null);
                    } else if (intent.getAction() == "com.android.securitycenter.CREATE_DEEP_CLEAN_SHORTCUT") {
                        launcher.addItemToWorkspace(GadgetFactory.getInfo(14), -1, -100, 0, 0, null);
                    } else {
                        ShortcutInfo info = model.getShortcutInfo(runningContext, intentData, null);
                        if (info != null) {
                            info.setIconPackage(str);
                            launcher.addItemToWorkspace(info, -1, -100, 0, 0, null);
                            return;
                        }
                        Log.e("InstallShortcutReceiver", "Failed to add shortcut");
                    }
                }
            }
        });
    }
}
