package com.miui.home.launcher;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.Log;
import com.miui.home.launcher.LauncherSettings.Favorites;
import com.miui.home.launcher.setting.PortableUtils;
import java.util.ArrayList;
import java.util.List;

public class ScreenUtils {

    static final class ScreenInfo {
        long screenId;
        int screenOrder;
        int screenType;

        public ScreenInfo(long id, int order, int type) {
            this.screenId = id;
            this.screenOrder = order;
            this.screenType = type;
        }
    }

    private interface ScreensQuery {
        public static final String[] COLUMNS = new String[]{"_id", "screenOrder", "screenType"};
    }

    public static boolean isActivityExist(Context context, ComponentName cn) {
        Intent intent = new Intent();
        intent.setComponent(cn);
        if (context.getPackageManager().queryIntentActivities(intent, 0).size() == 0) {
            return false;
        }
        return true;
    }

    public static boolean isAlreadyInstalled(String packageName, Context context) {
        Intent mainIntent = new Intent("android.intent.action.MAIN", null);
        mainIntent.setPackage(packageName);
        return context.getPackageManager().queryIntentActivities(mainIntent, 64).size() > 0;
    }

    public static CharSequence getProviderName(Context context, String name) {
        CharSequence title = "";
        ApplicationInfo info = findApplicationInfo(context, name);
        if (info != null) {
            title = info.loadLabel(context.getPackageManager());
        }
        if (!TextUtils.isEmpty(title)) {
            return title;
        }
        List<ResolveInfo> launcherApps = findActivitiesForPackage(context, name, Process.myUserHandle());
        if (launcherApps.size() > 0) {
            return ((ResolveInfo) launcherApps.get(0)).activityInfo.loadLabel(context.getPackageManager());
        }
        return title;
    }

    private static ArrayList<Long> getPackageItemIds(Context context, SQLiteDatabase db, String packageName, long profileId) {
        ArrayList<Long> ids = new ArrayList();
        SQLiteDatabase sQLiteDatabase = db;
        Cursor cursor = sQLiteDatabase.query("favorites", new String[]{"_id"}, "iconPackage=? AND profileId=? AND (itemType IN (0, 11) OR itemFlags&1!= 0)", new String[]{packageName, String.valueOf(profileId)}, null, null, null);
        while (cursor != null) {
            try {
                if (!cursor.moveToNext()) {
                    break;
                }
                ids.add(Long.valueOf(cursor.getLong(0)));
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return ids;
    }

    static void removePackage(Context context, SQLiteDatabase db, String packageName, long profileId) {
        ArrayList<Long> ids = getPackageItemIds(context, db, packageName, profileId);
        for (int i = 0; i < ids.size(); i++) {
            deleteFavorite(db, ((Long) ids.get(i)).longValue());
        }
    }

    public static boolean makeLastRowEmpty(Context context, long sid, int stype) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor c = null;
        int sx = (stype == 1 || stype == 0) ? DeviceConfig.getCellCountX() : DeviceConfig.getWidgetCellCountX();
        int emptyRowNum = -1;
        int rowNum = DeviceConfig.getCellCountY() - 1;
        loop0:
        while (rowNum >= 0) {
            int i = 0;
            try {
                c = contentResolver.query(Favorites.CONTENT_URI, new String[]{"_id", "intent"}, "container=-100 AND screen=" + sid + " AND (" + "cellX" + "-" + sx + ")*(" + "cellX" + "+" + "spanX" + ")< 0 AND (" + "cellY" + "-" + (rowNum + 1) + ")*(" + "cellY" + "+" + "spanY" + "-" + rowNum + ")< 0 AND " + "_id" + "!=" + -1 + " AND ((" + "itemType" + "<=" + 1 + ") OR " + "itemType" + ">" + 1 + ")", null, null);
                i = c.getCount();
                if (i == 0) {
                    emptyRowNum = rowNum;
                    break loop0;
                }
                rowNum--;
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }
        if (emptyRowNum == -1) {
            return false;
        }
        String selection = "container=-100 AND screen=" + sid + " AND " + "cellY" + ">" + emptyRowNum;
        ContentValues values = new ContentValues();
        try {
            c = contentResolver.query(Favorites.CONTENT_URI, new String[]{"_id", "cellY"}, selection, null, null);
            while (c != null && c.moveToNext()) {
                String select = "_id=" + c.getLong(0);
                values.clear();
                values.put("cellY", Integer.valueOf(c.getInt(1) - 1));
                contentResolver.update(Favorites.CONTENT_URI, values, select, null);
            }
            if (c != null) {
                c.close();
            }
            return true;
        } catch (Throwable th) {
            if (c != null) {
                c.close();
            }
        }
    }

    public static void addItemToScreen(Context context, ComponentName cn, long sid, int cellX, int cellY) {
        Intent mainIntent = getLaunchableIntent();
        mainIntent.setComponent(cn);
        ResolveInfo info = context.getPackageManager().resolveActivity(mainIntent, 64);
        if (info != null) {
            ShortcutInfo shortcut = new ShortcutInfo(context, info, Process.myUserHandle());
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.LAUNCHER");
            intent.setComponent(new ComponentName(info.activityInfo.applicationInfo.packageName, info.activityInfo.name));
            intent.setFlags(270532608);
            intent.putExtra("profile", Process.myUserHandle());
            shortcut.intent = intent;
            shortcut.setTitle(info.loadLabel(context.getPackageManager()), context);
            shortcut.cellX = cellX;
            shortcut.cellY = cellY;
            shortcut.screenId = sid;
            shortcut.container = -100;
            LauncherApplication app = Application.getLauncherApplication(context);
            if (app.getLauncherProvider() != null) {
                shortcut.id = app.getLauncherProvider().generateNewId();
                ContentValues values = new ContentValues();
                shortcut.onAddToDatabase(context, values);
                context.getContentResolver().insert(Favorites.CONTENT_URI, values);
            }
        }
    }

    public static void addFolderToScreen(Context context, String name, long sid, int cellX, int cellY) {
        FolderInfo folderInfo = new FolderInfo();
        folderInfo.isLandscapePos = DeviceConfig.isScreenOrientationLandscape();
        folderInfo.setTitle("com.miui.home:string/default_folder_title_recommend", context);
        folderInfo.container = -100;
        folderInfo.screenId = sid;
        folderInfo.cellX = cellX;
        folderInfo.cellY = cellY;
        LauncherApplication app = Application.getLauncherApplication(context);
        if (app.getLauncherProvider() != null) {
            folderInfo.id = app.getLauncherProvider().generateNewId();
            ContentValues values = new ContentValues();
            folderInfo.onAddToDatabase(context, values);
            context.getContentResolver().insert(Favorites.CONTENT_URI, values);
        }
    }

    static boolean isPackageDisabled(Context context, String packageName) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(packageName, 256);
            if (pi == null || pi.gids == null || pi.gids.length == 0 || pi.applicationInfo.enabled) {
                return false;
            }
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    private static void deleteFavorite(SQLiteDatabase db, long id) {
        LauncherProvider.safelyDeleteFromDB(db, "favorites", "_id=?", new String[]{String.valueOf(id)});
    }

    static Intent getLaunchableIntent() {
        Intent mainIntent = new Intent("android.intent.action.MAIN", null);
        mainIntent.addCategory("android.intent.category.LAUNCHER");
        return mainIntent;
    }

    static List<ResolveInfo> findActivitiesForPackage(Context context, String packageName, UserHandle user) {
        Intent mainIntent = getLaunchableIntent();
        mainIntent.setPackage(packageName);
        return PortableUtils.resolveActivityAsUser(context, mainIntent, 64, user.getIdentifier());
    }

    static ApplicationInfo findApplicationInfo(Context context, String packageName) {
        ApplicationInfo result = null;
        try {
            result = context.getPackageManager().getApplicationInfo(packageName, 256);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void findActivities2Id(Context context, String packageName, List<ResolveInfo> matches, List<Long> ids, UserHandle user) {
        Throwable th;
        ArrayList<Long> extraIds = new ArrayList();
        ArrayList<ResolveInfo> orderedMatches = new ArrayList();
        Cursor cursor = context.getContentResolver().query(Favorites.CONTENT_URI, new String[]{"_id", "intent", "profileId"}, "iconPackage=? AND (itemType IN (0, 11) OR itemFlags&1!= 0)", new String[]{packageName}, null);
        int index = 0;
        while (cursor != null) {
            int index2;
            try {
                if (!cursor.moveToNext()) {
                    break;
                }
                long id = cursor.getLong(0);
                String intent = cursor.getString(1);
                long j = (long) cursor.getInt(2);
                if (user.equals(((UserManager) context.getSystemService("user")).getUserForSerialNumber(j))) {
                    int i = 0;
                    boolean find = false;
                    while (i < matches.size()) {
                        ResolveInfo info = (ResolveInfo) matches.get(i);
                        if (intent.contains("component=" + new ComponentName(info.activityInfo.packageName, info.activityInfo.name).flattenToShortString() + ';')) {
                            matches.remove(info);
                            index2 = index + 1;
                            try {
                                orderedMatches.add(index, info);
                                ids.add(Long.valueOf(id));
                                find = true;
                                break;
                            } catch (Throwable th2) {
                                th = th2;
                            }
                        } else {
                            i++;
                        }
                    }
                    index2 = index;
                    if (!find) {
                        extraIds.add(Long.valueOf(id));
                    }
                    index = index2;
                }
            } catch (Throwable th3) {
                th = th3;
                index2 = index;
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        ids.addAll(extraIds);
        matches.addAll(0, orderedMatches);
        return;
        if (cursor != null) {
            cursor.close();
        }
        throw th;
    }

    static ArrayList<CharSequence> getScreenCellsSizeOptions(Context context) {
        ArrayList<CharSequence> options = new ArrayList();
        int cellXDef = DeviceConfig.getCellCountXDef(context);
        int cellYDef = DeviceConfig.getCellCountYDef(context);
        int cellXMin = DeviceConfig.getCellCountXMin(context);
        int cellXMax = DeviceConfig.getCellCountXMax(context);
        int cellYMin = DeviceConfig.getCellCountYMin(context);
        int cellYMax = DeviceConfig.getCellCountYMax(context);
        if (!(cellXMin == cellXMax && cellYMin == cellYMax)) {
            for (int i = cellXMin; i <= cellXMax; i++) {
                for (int j = cellYMin; j <= cellYMax; j++) {
                    options.add(i + "x" + j);
                }
            }
            if ((cellXDef > cellXMax || cellYDef > cellYMax) && options.size() != 0) {
                options.add(cellXDef + "x" + cellYDef);
            }
        }
        return options;
    }

    public static boolean parseCellsSize(String cellsConfig, int[] cells) {
        try {
            String[] cellsSize = cellsConfig.split("x");
            cells[0] = Integer.parseInt(cellsSize[0]);
            cells[1] = Integer.parseInt(cellsSize[1]);
            return true;
        } catch (Exception e) {
            Log.i("Launcher.cells", "cells config string invalidate");
            return false;
        }
    }

    static ArrayList<ScreenInfo> loadScreens(SQLiteDatabase db) {
        Cursor c = db.query("screens", ScreensQuery.COLUMNS, null, null, null, null, "screenOrder ASC");
        try {
            ArrayList<ScreenInfo> result = new ArrayList(c.getCount());
            while (c.moveToNext()) {
                result.add(new ScreenInfo(c.getLong(0), c.getInt(1), c.getInt(2)));
            }
            return result;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }
}
