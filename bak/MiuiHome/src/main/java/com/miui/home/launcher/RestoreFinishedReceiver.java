package com.miui.home.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RestoreFinishedReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        Log.w("Launcher.RestoreFinishedReceiver", "{android.intent.action.RESTORE_FINISH} received.");
        Application.getLauncherApplication(context).setJustRestoreFinished();
    }
}
