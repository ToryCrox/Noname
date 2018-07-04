package com.miui.home.launcher;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Process;
import android.util.Log;
import java.util.HashSet;
import java.util.Iterator;

public class ApplicationsMessage {
    private final HashSet<String> mAcceptedSenders = new HashSet();
    private ComponentName mLastLaunchApplication;
    private int mLastUserId;
    private Launcher mLauncher;
    private MessageReceiver mMessageReceiver;

    class MessageReceiver extends BroadcastReceiver {
        MessageReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ApplicationsMessage.this.mAcceptedSenders.contains(intent.getSender()) && !ApplicationsMessage.this.mLauncher.isInSnapshotMode()) {
                try {
                    if ("android.intent.action.APPLICATION_MESSAGE_UPDATE".equals(action)) {
                        String name = intent.getStringExtra("android.intent.extra.update_application_component_name");
                        int userId = intent.getIntExtra("userId", Process.myUserHandle().getIdentifier());
                        ComponentName cn = ApplicationsMessage.this.mLauncher.reConstructComponentName(name);
                        if (cn != null && cn.equals(ApplicationsMessage.this.mLastLaunchApplication) && ApplicationsMessage.this.mLastUserId == userId) {
                            ApplicationsMessage.this.mLastLaunchApplication = null;
                            ApplicationsMessage.this.mLastUserId = -1;
                        }
                        if (cn == null) {
                            ApplicationsMessage.this.clearAllMessage();
                        } else {
                            ApplicationsMessage.this.updateMessage(cn, userId, intent.getStringExtra("android.intent.extra.update_application_message_text"), intent.getStringExtra("android.intent.extra.update_application_message_text_background"), intent.getByteArrayExtra("android.intent.extra.update_application_message_icon_tile"));
                        }
                    }
                } catch (NullPointerException ex) {
                    Log.w("Launcher.ApplicationsMessage", "problem while stopping AppWidgetHost during Launcher destruction", ex);
                }
            }
        }
    }

    public ApplicationsMessage(Launcher launcher) {
        this.mLauncher = launcher;
        this.mAcceptedSenders.add("com.android.systemui");
        this.mAcceptedSenders.add("com.android.keyguard");
        this.mAcceptedSenders.add("com.miui.backup");
    }

    private void initialize() {
        IntentFilter updateMessageFilter = new IntentFilter();
        updateMessageFilter.addAction("android.intent.action.APPLICATION_MESSAGE_UPDATE");
        this.mMessageReceiver = new MessageReceiver();
        this.mLauncher.registerReceiver(this.mMessageReceiver, updateMessageFilter);
    }

    public void requestUpdateMessages(boolean firstTime) {
        if (this.mMessageReceiver == null) {
            initialize();
        }
        Intent intent = new Intent("android.intent.action.APPLICATION_MESSAGE_QUERY");
        intent.putExtra("com.miui.extra_update_request_first_time", firstTime);
        this.mLauncher.sendBroadcast(intent);
        if (this.mLastLaunchApplication != null) {
            updateMessage(this.mLastLaunchApplication, this.mLastUserId, null, null, null);
            this.mLastLaunchApplication = null;
        }
    }

    public void updateFolderMessage(FolderInfo info) {
        if (info != null) {
            int updateCount = 0;
            FolderIcon fi = this.mLauncher.getFolderIcon(info);
            if (fi != null) {
                Iterator i$ = new HashSet(info.contents).iterator();
                while (i$.hasNext()) {
                    ShortcutInfo si = (ShortcutInfo) i$.next();
                    if (!si.isEmptyMessage()) {
                        try {
                            updateCount += Integer.parseInt(si.getMessageText());
                        } catch (NumberFormatException e) {
                        }
                    }
                }
                if (updateCount == 0) {
                    fi.setMessage(null);
                } else {
                    fi.setMessage(String.valueOf(updateCount));
                }
            }
        }
    }

    private void clearAllMessage() {
        Iterator i$ = this.mLauncher.getAllLoadedApps().iterator();
        while (i$.hasNext()) {
            updateMessage((ShortcutInfo) i$.next(), null, null, null);
        }
    }

    private void updateMessage(ComponentName componentName, int userId, String text, String textBg, byte[] tile) {
        updateMessage(this.mLauncher.getShortcutInfo(componentName, userId), text, textBg, tile);
    }

    private void updateMessage(ShortcutInfo info, String text, String textBg, byte[] tile) {
        if (info != null && !info.isHideApplicationMessage()) {
            info.setMessage(text, textBg, tile);
            FolderInfo folder = this.mLauncher.getParentFolderInfo(info);
            if (folder != null) {
                folder.notifyDataSetChanged();
                updateFolderMessage(folder);
            }
        }
    }

    public void onLaunchApplication(ComponentName componentName, int userId) {
        this.mLastLaunchApplication = componentName;
        this.mLastUserId = userId;
    }

    public void onDestroy() {
        this.mLauncher.unregisterReceiver(this.mMessageReceiver);
        this.mMessageReceiver = null;
    }
}
