package com.miui.home.launcher;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class InstallWidgetReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent data) {
        if ("com.miui.home.launcher.action.INSTALL_WIDGET".equals(data.getAction())) {
            final Launcher launcher = Application.getLauncherApplication(context).getLauncher();
            final Intent intentData = data;
            final Context runningContext = context;
            if (launcher == null) {
                Log.e("InstallWidgetReceiver", "Launcher is not running,process later");
            } else {
                launcher.runOnUiThread(new Runnable() {
                    public void run() {
                        if (launcher.isWorkspaceLoading()) {
                            launcher.getWorkspace().postDelayed(this, 100);
                            return;
                        }
                        AppWidgetManager widgetManager = AppWidgetManager.getInstance(runningContext);
                        ComponentName providerName = (ComponentName) intentData.getParcelableExtra("miui.intent.extra.provider_component_name");
                        if (providerName != null) {
                            AppWidgetProviderInfo pInfo = null;
                            for (AppWidgetProviderInfo info : widgetManager.getInstalledProviders()) {
                                if (providerName.equals(info.provider)) {
                                    pInfo = info;
                                    break;
                                }
                            }
                            if (pInfo != null) {
                                LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo = new LauncherAppWidgetProviderInfo(pInfo);
                                DeviceConfig.calcWidgetSpans(launcherAppWidgetProviderInfo);
                                final AppWidgetHost host = launcher.getAppWidgetHost();
                                AppWidgetManager manager = AppWidgetManager.getInstance(launcher);
                                final int appWidgetId = host.allocateAppWidgetId();
                                try {
                                    manager.bindAppWidgetId(appWidgetId, pInfo.provider);
                                    final LauncherAppWidgetInfo widgetInfo = new LauncherAppWidgetInfo(appWidgetId, launcherAppWidgetProviderInfo);
                                    launcher.addItemToWorkspace(widgetInfo, -1, -100, 0, 0, new Runnable() {
                                        public void run() {
                                            if (widgetInfo.screenId != -1) {
                                                ComponentName resultReceiver = (ComponentName) intentData.getParcelableExtra("miui.intent.extra.result_receiver_component_name");
                                                Intent result = new Intent("miui.intent.action.BIND_WIDGET_COMPLETED");
                                                result.putExtra("miui.intent.extra.bind_widget_result", new long[]{intentData.getLongExtra("android.intent.extra.UID", -1), (long) appWidgetId});
                                                result.setComponent(resultReceiver);
                                                runningContext.sendBroadcast(result);
                                                return;
                                            }
                                            host.deleteAppWidgetId(appWidgetId);
                                        }
                                    });
                                } catch (IllegalArgumentException e) {
                                    Log.e("InstallWidgetReceiver", "Error when bind app widget");
                                }
                            }
                        }
                    }
                });
            }
        }
    }
}
