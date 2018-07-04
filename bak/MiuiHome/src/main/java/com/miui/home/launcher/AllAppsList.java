package com.miui.home.launcher;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import com.miui.home.launcher.LauncherSettings.Favorites;
import java.util.ArrayList;
import java.util.List;

class AllAppsList {
    public ArrayList<ShortcutInfo> added = new ArrayList(3);
    public ArrayList<RemoveInfo> removed = new ArrayList();

    private interface LayoutInfoQuery {
        public static final String[] COLUMNS = new String[]{"screen", "cellX", "cellY", "container", "_id", "itemType", "itemFlags", "iconType"};
    }

    class RemoveInfo {
        public final boolean dontKillApp;
        public final String packageName;
        public final boolean replacing;
        public final UserHandle user;

        public RemoveInfo(String p, boolean r, boolean dontKill, UserHandle u) {
            this.packageName = p;
            this.replacing = r;
            this.dontKillApp = dontKill;
            this.user = u;
        }
    }

    AllAppsList() {
    }

    public void add(ShortcutInfo info) {
        this.added.add(info);
    }

    public void clear() {
        this.added.clear();
        this.removed.clear();
    }

    public void addPackage(Context context, String packageName, boolean loadMissing, UserHandle user) {
        List<ResolveInfo> matches = ScreenUtils.findActivitiesForPackage(context, packageName, user);
        if (matches != null && !matches.isEmpty()) {
            int index = 0;
            ArrayList<Long> ids = new ArrayList();
            ScreenUtils.findActivities2Id(context, packageName, matches, ids, user);
            while (index < matches.size()) {
                ResolveInfo info = (ResolveInfo) matches.get(index);
                if (!LauncherProvider.isSkippedItem(new ComponentName(packageName, info.activityInfo.name))) {
                    addApp(context, info, index < ids.size() ? ((Long) ids.get(index)).longValue() : -1, loadMissing, user);
                }
                index++;
            }
        }
    }

    private void addApp(Context context, ResolveInfo info, long targetId, boolean loadMissing, UserHandle user) {
        ShortcutInfo ai = new ShortcutInfo(context, info, user);
        loadInfo(context, info.activityInfo.packageName, ai, targetId, loadMissing);
        if (loadMissing && ai.screenId == -1 && ai.container == -1) {
            addToDefaultFolder(context, info, ai);
        }
        add(ai);
        loadShortcuts(context, ai.intent, info.activityInfo.packageName, user);
    }

    private void loadInfo(Context context, String packageName, ShortcutInfo info, long targetId, boolean loadMissing) {
        Cursor cursor = context.getContentResolver().query(Favorites.CONTENT_URI, LayoutInfoQuery.COLUMNS, "_id=?", new String[]{String.valueOf(targetId)}, null);
        if (cursor != null) {
            try {
                if (cursor.moveToNext()) {
                    info.id = (long) cursor.getInt(4);
                    info.screenId = (long) cursor.getInt(0);
                    info.cellX = cursor.getInt(1);
                    info.cellY = cursor.getInt(2);
                    info.itemFlags = cursor.getInt(6);
                    info.spanY = 1;
                    info.spanX = 1;
                    info.container = cursor.getLong(3);
                    if (!(info.itemType == cursor.getInt(5) && info.mIconType == cursor.getInt(7))) {
                        LauncherModel.updateItemInDatabase(context, info);
                    }
                    Log.d("Launcher.AllAppsList", String.format("Loaded application %s at (%d, %d) of screen %d under container %d", new Object[]{info.getTitle(context), Integer.valueOf(info.cellX), Integer.valueOf(info.cellY), Long.valueOf(info.screenId), Long.valueOf(info.container)}));
                    if (info.screenId == -1 && info.container == -1) {
                        Log.e("Launcher.AllAppsList", "Can't load postion for app " + info.getTitle(context));
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (loadMissing) {
            ShortcutInfo shortcutInfo = new RemovedComponentInfoList(context).getRemovedInfo(info.intent.getComponent());
            if (shortcutInfo != null) {
                info.copyPosition(shortcutInfo);
            }
        }
        Log.e("Launcher.AllAppsList", "Can't load postion for app " + info.getTitle(context));
        if (cursor != null) {
            cursor.close();
        }
    }

    private void addToDefaultFolder(Context context, ResolveInfo info, ShortcutInfo shortcut) {
        IntentFilter filter = info.filter;
        LauncherProvider provider = Application.getLauncherApplication(context).getLauncherProvider();
        long id = -1;
        if (LauncherModel.sLoadingMissingPreset && !LauncherProvider.getPresetItems().contains(new ComponentName(info.activityInfo.packageName, info.activityInfo.name))) {
            id = provider.queryIdByTitle("com.miui.home:string/default_folder_title_recommend");
        } else if (filter != null) {
            if (filter.hasCategory("miui.intent.category.SYSAPP_TOOL")) {
                id = provider.queryIdByTitle("com.miui.home:string/default_folder_title_tools");
            } else if (filter.hasCategory("miui.intent.category.SYSAPP_SYSTEM")) {
                id = provider.queryIdByTitle("com.miui.home:string/default_folder_title_security");
            } else if (filter.hasCategory("miui.intent.category.SYSAPP_RECOMMEND")) {
                id = provider.queryIdByTitle("com.miui.home:string/default_folder_title_recommend");
            }
        }
        if (id != -1) {
            shortcut.container = id;
            shortcut.mIconType = 0;
            shortcut.cellX = provider.queryFolderSize(id);
            shortcut.cellY = 0;
            Log.d("Launcher.AllAppsList", String.format("Adding new app %s to folder %d, pos (%d)", new Object[]{info.loadLabel(context.getPackageManager()).toString(), Long.valueOf(shortcut.container), Integer.valueOf(shortcut.cellX)}));
        }
    }

    private void loadShortcuts(Context context, Intent intent, String packageName, UserHandle user) {
        ContentResolver cr = context.getContentResolver();
        long serialNum = ((UserManager) context.getSystemService("user")).getSerialNumberForUser(user);
        Cursor cursor = cr.query(Favorites.CONTENT_URI, ItemQuery.COLUMNS, "intent=? AND iconPackage=? AND itemType=1 AND profileId=?", new String[]{intent.toUri(0).toString(), packageName, Long.toString(serialNum)}, null);
        if (cursor != null) {
            try {
                if (cursor.moveToNext()) {
                    ShortcutInfo si = Application.getLauncherApplication(context).getModel().getShortcutInfo(intent, cursor, context, cursor.getInt(8), 3, 5, 6, 4, 2, 20);
                    si.load(context, cursor);
                    si.intent = intent;
                    add(si);
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    public void removePackage(String packageName, boolean dontKill, UserHandle user) {
        removePackage(packageName, false, dontKill, user);
    }

    public void removePackage(String packageName, boolean isReplacing, boolean dontKill, UserHandle user) {
        this.removed.add(new RemoveInfo(packageName, isReplacing, dontKill, user));
    }

    public void updatePackage(Context context, String packageName, boolean dontKill, UserHandle user) {
        updatePackage(context, packageName, dontKill, false, user);
    }

    public void updatePackage(Context context, String packageName, boolean dontKill, boolean loadMissing, UserHandle user) {
        this.removed.add(new RemoveInfo(packageName, true, dontKill, user));
        addPackage(context, packageName, loadMissing, user);
    }
}
