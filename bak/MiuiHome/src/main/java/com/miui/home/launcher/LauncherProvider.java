package com.miui.home.launcher;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.os.SystemClock;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.provider.MiuiSettings.System;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;
import com.android.internal.util.XmlUtils;
import com.miui.home.R;
import com.miui.home.launcher.common.Utilities;
import com.miui.home.launcher.gadget.GadgetFactory;
import com.miui.home.launcher.gadget.GadgetInfo;
import com.miui.home.launcher.gadget.MamlTools;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import libcore.net.MimeUtils;
import miui.os.Build;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class LauncherProvider extends ContentProvider {
    static final Uri CONTENT_APPWIDGET_RESET_URI = Uri.parse("content://com.miui.home.launcher.settings/appWidgetReset");
    public static final HashSet<String> mLaamPresetPackage = new HashSet();
    private static final ArrayList<ComponentName> mPresetItems = new ArrayList();
    public static final HashSet<ComponentName> mSkippedItems = new HashSet();
    private volatile Object mLock = new Object();
    private DatabaseHelper mOpenHelper;
    private ArrayList<ScreenInfo> mScreens;

    static class DatabaseHelper extends SQLiteOpenHelper {
        private final AppWidgetHost mAppWidgetHost;
        private final ContentValues mContentvalues = new ContentValues();
        private final Context mContext;
        private boolean mDatabaseReady = true;
        private long mMaxId = -1;
        private long mPresetsContainerId = -1;

        DatabaseHelper(Context context) {
            super(context, DeviceConfig.getDatabaseName(), null, 31);
            this.mContext = context;
            this.mAppWidgetHost = new AppWidgetHost(context, 1024);
            SQLiteDatabase db = null;
            int failTime = 0;
            while (db == null) {
                try {
                    db = getWritableDatabase();
                } catch (SQLiteException ex) {
                    Log.d("Launcher.LauncherProvider", "get writable database fail", ex);
                    SystemClock.sleep(50);
                    failTime++;
                    if (failTime > 10) {
                        DeviceConfig.removeInvalidateDatabase(context, false);
                    }
                }
            }
            if (!isDatabaseLegal(db)) {
                this.mDatabaseReady = false;
            } else if (this.mMaxId == -1) {
                this.mMaxId = initializeMaxId(db);
            }
        }

        private boolean isDatabaseLegal(SQLiteDatabase db) {
            if (!isTableExist(db, "favorites") || !isTableExist(db, "screens")) {
                return false;
            }
            if (getItemCountInDB(db, "screens", new String[]{"count(*)"}, null, null) >= 1) {
                return true;
            }
            return false;
        }

        private int getItemCountInDB(SQLiteDatabase db, String table, String[] columns, String selection, String[] selectionArgs) {
            int count = -1;
            Cursor c = null;
            try {
                c = db.query(table, columns, selection, selectionArgs, null, null, null, null);
                if (c != null && c.moveToNext()) {
                    count = c.getInt(0);
                }
                if (c != null) {
                    c.close();
                }
            } catch (Exception e) {
                if (c != null) {
                    c.close();
                }
            } catch (Throwable th) {
                if (c != null) {
                    c.close();
                }
            }
            return count;
        }

        private boolean isTableExist(SQLiteDatabase db, String tableName) {
            boolean isExist = false;
            Cursor cursor = null;
            try {
                cursor = getReadableDatabase().rawQuery("select count(*) from sqlite_master where type='table' and name='" + tableName + "'", null);
                if (cursor != null && cursor.moveToNext()) {
                    isExist = cursor.getInt(0) > 0;
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return isExist;
        }

        public boolean isDatabaseReady() {
            return this.mDatabaseReady;
        }

        private void sendAppWidgetResetNotify() {
            this.mContext.getContentResolver().notifyChange(LauncherProvider.CONTENT_APPWIDGET_RESET_URI, null);
        }

        public void onCreate(SQLiteDatabase db) {
            Log.d("Launcher.LauncherProvider", "creating new launcher database");
            this.mMaxId = 0;
            this.mDatabaseReady = false;
        }

        private void loadDefaultWorkspace(SQLiteDatabase db) {
            if (!(DeviceConfig.isXLargeMode() || this.mAppWidgetHost == null)) {
                this.mAppWidgetHost.deleteHost();
                sendAppWidgetResetNotify();
            }
            this.mMaxId = 1;
            loadFavorites(db);
            loadPresetsApps(db);
            createScreensTable(db);
            updateMaxId(db);
        }

        public void loadDefaultWorkspace() {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
            sp.edit().putBoolean("database_ready_pref_key", false).commit();
            loadDefaultWorkspace(getWritableDatabase());
            this.mDatabaseReady = true;
            sp.edit().putBoolean("database_ready_pref_key", true).commit();
            sp.edit().putLong("home_data_create_time_key", System.currentTimeMillis()).commit();
        }

        public String getPreferenceValue(String key) {
            String result = getValue(key, PreferenceManager.getDefaultSharedPreferences(this.mContext));
            if (TextUtils.isEmpty(result)) {
                result = getValue(key, this.mContext.getSharedPreferences(this.mContext.getPackageName() + "_world_readable_preferences", DeviceConfig.TEMP_SHARE_MODE_FOR_WORLD_READABLE));
            }
            return !TextUtils.isEmpty(result) ? result : "";
        }

        private String getValue(String key, SharedPreferences sp) {
            Object value = sp.getAll().get(key);
            if (value != null) {
                return value.toString();
            }
            return null;
        }

        private void createFavoritesTriggers(SQLiteDatabase db) {
            LauncherProvider.safelyExecSQL(db, "DROP TRIGGER IF EXISTS update_item_on_update_item;");
            LauncherProvider.safelyExecSQL(db, "CREATE TRIGGER update_item_on_update_item  AFTER UPDATE of container ON favorites  WHEN (new.itemType == 0 OR new.itemType == 1) AND (new.container > 0) BEGIN   UPDATE favorites SET itemFlags = (((itemFlags >> 1) <<1) | (SELECT ((itemFlags>>1)&1) from favorites where _id==new.container))     WHERE _id==new._id;  END");
            LauncherProvider.safelyExecSQL(db, "DROP TRIGGER IF EXISTS update_item_on_update_home;");
            LauncherProvider.safelyExecSQL(db, "CREATE TRIGGER update_item_on_update_home  AFTER UPDATE of container ON favorites  WHEN (new.itemType == 0 OR new.itemType == 1) AND (new.container <= 0) BEGIN   UPDATE favorites SET itemFlags = (((itemFlags >> 1) <<1) | 0)     WHERE _id==new._id;  END");
            LauncherProvider.safelyExecSQL(db, "DROP TRIGGER IF EXISTS update_item_on_update_folder;");
            LauncherProvider.safelyExecSQL(db, "CREATE TRIGGER update_item_on_update_folder  AFTER UPDATE of itemFlags ON favorites  WHEN new.itemType == 2  BEGIN   UPDATE favorites SET itemFlags = (((itemFlags >> 1) <<1) | ((new.itemFlags>>1)&1))      WHERE container==new._id;  END");
        }

        private void createScreensTable(SQLiteDatabase db) {
            LauncherProvider.safelyExecSQL(db, "DROP TABLE IF EXISTS screens");
            LauncherProvider.safelyExecSQL(db, "CREATE TABLE screens (_id INTEGER PRIMARY KEY,title TEXT,screenOrder INTEGER NOT NULL DEFAULT -1,screenType INTEGER NOT NULL DEFAULT 0);");
            SQLiteDatabase sQLiteDatabase = db;
            Cursor cursor = sQLiteDatabase.query("favorites", new String[]{"MAX(screen)"}, null, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    int i;
                    int count = cursor.getInt(0) + 1;
                    ContentValues values = new ContentValues();
                    long[] ids = new long[count];
                    for (i = 0; i < count; i++) {
                        values.clear();
                        values.put("screenOrder", Integer.valueOf(i));
                        Cursor widgets = null;
                        Cursor icons = null;
                        int screenType = 0;
                        if (DeviceConfig.isRotatable()) {
                            try {
                                String[] strArr = new String[]{"COUNT()"};
                                String str = "spanX+spanY>2 AND screen=" + i + " AND " + "container" + "=" + -100;
                                widgets = db.query("favorites", strArr, str, null, null, null, null);
                                strArr = new String[]{"COUNT()"};
                                str = "spanX=1 AND spanY=1 AND screen=" + i + " AND " + "container" + "=" + -100 + " AND " + "itemType" + "!=" + 5;
                                icons = db.query("favorites", strArr, str, null, null, null, null);
                                widgets.moveToNext();
                                int widgetCount = widgets.getInt(0);
                                icons.moveToNext();
                                int iconsCount = icons.getInt(0);
                                if (widgetCount > 0 && iconsCount > 0) {
                                    screenType = 0;
                                } else if (widgetCount > 0) {
                                    screenType = 2;
                                } else {
                                    screenType = 1;
                                }
                                if (widgets != null) {
                                    widgets.close();
                                }
                                if (icons != null) {
                                    icons.close();
                                }
                            } catch (Throwable th) {
                                if (cursor != null) {
                                    cursor.close();
                                }
                            }
                        }
                        values.put("screenType", Integer.valueOf(screenType));
                        ids[i] = LauncherProvider.safelyInsertDatabase(db, "screens", null, values);
                    }
                    for (i = count - 1; i >= 0; i--) {
                        values.clear();
                        values.put("screen", Long.valueOf(ids[i]));
                        SQLiteDatabase sQLiteDatabase2 = db;
                        ContentValues contentValues = values;
                        LauncherProvider.safelyUpdateDatabase(sQLiteDatabase2, "favorites", contentValues, "screen=?", new String[]{String.valueOf(i)});
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            long folderId;
            Cursor c;
            SQLiteDatabase sQLiteDatabase;
            Cursor cursor;
            Log.d("Launcher.LauncherProvider", "onUpgrade triggered");
            int version = oldVersion;
            Intent intent = new Intent("android.intent.action.MAIN", null);
            intent.addCategory("android.intent.category.LAUNCHER");
            PackageManager packageManager = this.mContext.getPackageManager();
            if (version < 9) {
                createScreensTable(db);
                version = 9;
            }
            if (version == 9) {
                LauncherProvider.safelyExecSQL(db, "ALTER TABLE favorites ADD COLUMN launchCount INTEGER NOT NULL DEFAULT 1");
                LauncherProvider.safelyExecSQL(db, "ALTER TABLE favorites ADD COLUMN sortMode INTEGER");
                version = 10;
            }
            if (version == 10) {
                LauncherProvider.safelyExecSQL(db, "ALTER TABLE favorites ADD COLUMN itemFlags INTEGER NOT NULL DEFAULT 0");
                version = 11;
            }
            if (version == 11) {
                LauncherProvider.safelyExecSQL(db, "UPDATE favorites SET title='com.miui.home:string/default_folder_title_tools' WHERE title='com.android.launcher:string/default_folder_title_tools';");
                LauncherProvider.safelyExecSQL(db, "UPDATE favorites SET title='com.miui.home:string/default_folder_title_recommend' WHERE title='com.android.launcher:string/default_folder_title_recommend';");
                upgradeComponentName(db, ComponentName.unflattenFromString("com.miui.camera/.Camera"), ComponentName.unflattenFromString("com.android.camera/.Camera"));
                upgradeComponentName(db, ComponentName.unflattenFromString("com.android.gallery/.ui.MainActivity"), ComponentName.unflattenFromString("com.miui.gallery/.app.Gallery"));
                upgradeComponentName(db, ComponentName.unflattenFromString("com.miui.player/.drawerActivityGroup.MainActivityGroup"), ComponentName.unflattenFromString("com.miui.player/.ui.MusicBrowserActivity"));
                upgradeComponentName(db, ComponentName.unflattenFromString("com.android.settings/.Settings"), ComponentName.unflattenFromString("com.android.settings/.MiuiSettings"));
                upgradeComponentName(db, ComponentName.unflattenFromString("com.miui.supermarket/.MainActivity"), ComponentName.unflattenFromString("com.xiaomi.market/.ui.MainTabActivity"));
                upgradeComponentName(db, ComponentName.unflattenFromString("com.android.deskclock/.DeskClock"), ComponentName.unflattenFromString("com.android.deskclock/.DeskClockTabActivity"));
                LauncherProvider.safelyExecSQL(db, "UPDATE favorites SET intent='#Intent;action=android.intent.action.MAIN;category=android.intent.category.LAUNCHER;launchFlags=0x10200000;component=com.android.deskclock/.DeskClockTabActivity;end' WHERE intent='#Intent;action=android.intent.action.MAIN;category=android.intent.category.LAUNCHER;launchFlags=0x10200000;component=com.android.deskclock/.DeskClock;end';");
                upgradeComponentName(db, ComponentName.unflattenFromString("com.android.contacts/.TwelveKeyDialer"), ComponentName.unflattenFromString("com.android.contacts/.activities.TwelveKeyDialer"));
                upgradeComponentName(db, ComponentName.unflattenFromString("com.android.contacts/.DialtactsContactsEntryActivity"), ComponentName.unflattenFromString("com.android.contacts/.activities.PeopleActivity"));
                version = 12;
            }
            if (version == 12) {
                if (queryIdByTitle(db, "com.miui.home:string/default_folder_title_security") == -1) {
                    long vsId = queryInstalledComponentId(db, "com.android.settings/.VirusScanActivity");
                    long naId = queryInstalledComponentId(db, "com.wali.miui.networkassistant/.NetworkAssistantActivity");
                    long passId = queryInstalledComponentId(db, "com.android.settings/.MiuiPasswordGuardActivity");
                    long bkId = queryInstalledComponentId(db, "com.miui.backup/.BackupActivity");
                    long antId = queryInstalledComponentId(db, "com.miui.antispam/.firewall.FirewallTab");
                    long auId = queryInstalledComponentId(db, "com.lbe.security.miui/com.lbe.security.ui.MainActivity");
                    long bgmId = queryInstalledComponentId(db, "com.lbe.security.miui/com.lbe.security.ui.AutoStartAppList");
                    if (vsId == -1 && passId == -1 && bgmId == -1) {
                        this.mContentvalues.clear();
                        this.mContentvalues.put("title", "com.miui.home:string/default_folder_title_security");
                        this.mContentvalues.put("itemType", Integer.valueOf(2));
                        this.mContentvalues.put("container", Integer.valueOf(-100));
                        this.mContentvalues.put("spanX", Integer.valueOf(1));
                        this.mContentvalues.put("spanY", Integer.valueOf(1));
                        this.mContentvalues.put("screen", Integer.valueOf(-1));
                        SQLiteDatabase sQLiteDatabase2 = db;
                        folderId = LauncherProvider.safelyInsertDatabase(sQLiteDatabase2, "favorites", null, this.mContentvalues);
                        intent.setComponent(ComponentName.unflattenFromString("com.android.settings/.VirusScanActivity"));
                        addAppShortcut(db, intent, folderId, 0, packageManager);
                        if (naId != -1) {
                            updateItemContainer(db, naId, folderId, 1);
                        } else {
                            intent.setComponent(ComponentName.unflattenFromString("com.wali.miui.networkassistant/.NetworkAssistantActivity"));
                            addAppShortcut(db, intent, folderId, 1, packageManager);
                        }
                        intent.setComponent(ComponentName.unflattenFromString("com.android.settings/.MiuiPasswordGuardActivity"));
                        addAppShortcut(db, intent, folderId, 2, packageManager);
                        if (bkId != -1) {
                            updateItemContainer(db, bkId, folderId, 3);
                        }
                        if (antId != -1) {
                            updateItemContainer(db, antId, folderId, 4);
                        }
                        if (auId != -1) {
                            updateItemContainer(db, auId, folderId, 5);
                        } else {
                            intent.setComponent(ComponentName.unflattenFromString("com.lbe.security.miui/com.lbe.security.ui.MainActivity"));
                            addAppShortcut(db, intent, folderId, 5, packageManager);
                        }
                        intent.setComponent(ComponentName.unflattenFromString("com.lbe.security.miui/com.lbe.security.ui.AutoStartAppList"));
                        addAppShortcut(db, intent, folderId, 6, packageManager);
                    }
                }
                version = 13;
            }
            if (version == 13) {
                upgradeComponentName(db, ComponentName.unflattenFromString("com.xiaomi.market/.ui.MainTabActivity"), ComponentName.unflattenFromString("com.xiaomi.market/.ui.MarketTabActivity"));
                upgradeComponentName(db, ComponentName.unflattenFromString("com.lbe.security.miui/com.lbe.security.ui.MainActivity"), ComponentName.unflattenFromString("com.android.settings/.permission.PermManageActivity"));
                upgradeComponentName(db, ComponentName.unflattenFromString("com.lbe.security.miui/com.lbe.security.ui.AutoStartAppList"), ComponentName.unflattenFromString("com.android.settings/.BackgroundApplicationsManager"));
                upgradeComponentName(db, ComponentName.unflattenFromString("com.miui.bugreport/.BugReportActivity"), ComponentName.unflattenFromString("com.miui.bugreport/.ui.TypeSelectionActivity"));
                if (DeviceConfig.getCellCountX() == 4 && DeviceConfig.getCellCountY() == 5) {
                    c = null;
                    try {
                        folderId = queryIdByTitle(db, "com.miui.home:string/default_folder_title_security");
                        if (folderId != -1) {
                            intent.setComponent(ComponentName.unflattenFromString("com.xiaomi.xmsf/.payment.MiliCenterEntryActivity"));
                            addAppShortcut(db, intent, folderId, -1, packageManager);
                        }
                        sQLiteDatabase = db;
                        c = sQLiteDatabase.query("favorites", new String[]{"screen"}, "itemType=5 AND appWidgetId=6 AND cellX=0 AND cellY=0", null, null, null, null);
                        if (c.moveToNext()) {
                            int screenId = c.getInt(0);
                            c.close();
                            sQLiteDatabase = db;
                            c = sQLiteDatabase.query("favorites", new String[]{"_id"}, "container=-100 AND screen=" + screenId + " AND " + "cellY" + ">3", null, null, null, null);
                            if (c.getCount() == 0) {
                                LauncherProvider.safelyExecSQL(db, "UPDATE favorites SET cellY=cellY+1 WHERE container=-100 AND screen=" + screenId + " AND " + "cellY" + ">1");
                            }
                        }
                        if (c != null) {
                            c.close();
                        }
                    } catch (Throwable th) {
                        if (c != null) {
                            c.close();
                        }
                    }
                }
                version = 14;
            }
            if (version == 14) {
                c = null;
                try {
                    removeSkippedItems(db);
                    long miCloudId = queryInstalledComponentId(db, "com.xiaomi.xmsf/.account.ui.MiCloudSettingsActivity");
                    long stkId = queryInstalledComponentId(db, "com.android.stk/.StkLauncherActivity");
                    folderId = queryIdByTitle(db, "com.miui.home:string/default_folder_title_security");
                    if (folderId != -1) {
                        sQLiteDatabase = db;
                        c = sQLiteDatabase.query("favorites", new String[]{"_id"}, "container=" + folderId, null, null, null, null);
                        int securityItemCount = c.getCount();
                        if (miCloudId == -1) {
                            intent.setComponent(ComponentName.unflattenFromString("com.xiaomi.xmsf/.account.ui.MiCloudSettingsActivity"));
                            cursor = null;
                            sQLiteDatabase = db;
                            cursor = sQLiteDatabase.query("favorites", new String[]{"_id"}, "container=? AND _id=?", new String[]{String.valueOf(folderId), String.valueOf(stkId)}, null, null, null);
                            if (c.getCount() == 1) {
                                addAppShortcut(db, intent, folderId, securityItemCount - 1, packageManager);
                                updateItemContainer(db, stkId, folderId, securityItemCount);
                            } else {
                                addAppShortcut(db, intent, folderId, securityItemCount, packageManager);
                            }
                            if (cursor != null) {
                                cursor.close();
                            }
                        }
                    }
                    if (c != null) {
                        c.close();
                    }
                    version = 15;
                } catch (Throwable th2) {
                    if (c != null) {
                        c.close();
                    }
                }
            }
            if (version == 15) {
                long id = queryIdByTitle(db, "com.miui.home:string/default_folder_title_recommend");
                if (id != -1) {
                    SQLiteDatabase sQLiteDatabase3 = db;
                    addShortcut(sQLiteDatabase3, "com.android.fileexplorer:drawable/kuaipan_icon", "miui.intent.action.START_KUAIPAN", "com.android.fileexplorer", "com.android.fileexplorer.FileExplorerTabActivity", "com.android.fileexplorer:string/kuaipan_label", id, queryFolderSize(db, id), 0);
                }
                version = 16;
            }
            if (version == 16) {
                folderId = queryIdByTitle(db, "com.miui.home:string/default_folder_title_tools");
                if (-1 != folderId) {
                    int size = queryFolderSize(db, folderId);
                    if (-1 == queryInstalledComponentId(db, "com.miui.transfer/cn.kuaipan.mishare.LogoActivity")) {
                        intent.setComponent(ComponentName.unflattenFromString("com.miui.transfer/cn.kuaipan.mishare.LogoActivity"));
                        addAppShortcut(db, intent, folderId, size, packageManager);
                    }
                }
                version = 17;
            }
            if (version == 17) {
                folderId = queryIdByTitle(db, "com.miui.home:string/default_folder_title_tools");
                if (folderId != -1) {
                    c = null;
                    int toolFolderCellX = -1;
                    int toolFolderCellY = -1;
                    int i = -1;
                    int i2 = -1;
                    int i3 = -1;
                    boolean z = false;
                    try {
                        c = db.rawQuery("SELECT _id, cellX, cellY, screen FROM favorites WHERE _id=" + folderId, null);
                        if (c.moveToNext()) {
                            toolFolderCellX = c.getInt(1);
                            toolFolderCellY = c.getInt(2);
                            i = c.getInt(3);
                        }
                        if (!(toolFolderCellX == -1 || toolFolderCellY == -1 || i == -1 || toolFolderCellX + 1 >= DeviceConfig.getCellCountX() || toolFolderCellY >= DeviceConfig.getCellCountY())) {
                            i2 = toolFolderCellX + 1;
                            i3 = toolFolderCellY;
                            cursor = null;
                            cursor = db.rawQuery("SELECT _id FROM favorites WHERE container=-100 AND screen=" + i + " AND (" + "cellX" + "-" + (i2 + 1) + ")*(" + "cellX" + "+" + "spanX" + "-" + i2 + ")< 0 AND (" + "cellY" + "-" + (i3 + 1) + ")*(" + "cellY" + "+" + "spanY" + "-" + i3 + ")< 0", null);
                            if (c.getCount() == 0) {
                                z = true;
                            }
                            if (cursor != null) {
                                cursor.close();
                            }
                        }
                        if (c != null) {
                            c.close();
                        }
                    } catch (Exception e) {
                        if (c != null) {
                            c.close();
                        }
                    } catch (Throwable th3) {
                        if (c != null) {
                            c.close();
                        }
                    }
                    if (z) {
                        long securityCenterId = queryIdByIntent(db, "#Intent;action=android.intent.action.MAIN;category=android.intent.category.LAUNCHER;launchFlags=0x10200000;component=com.android.settings/com.miui.securitycenter.Main;end");
                        if (securityCenterId != -1) {
                            LauncherProvider.safelyExecSQL(db, "UPDATE favorites SET cellX=" + i2 + ", " + "cellY" + "=" + i3 + ", " + "screen" + "=" + i + ", " + "container" + "=" + -100 + " WHERE " + "_id" + "=" + securityCenterId);
                        } else {
                            intent.setComponent(ComponentName.unflattenFromString("com.android.settings/com.miui.securitycenter.Main"));
                            this.mContentvalues.clear();
                            this.mContentvalues.put("container", Integer.valueOf(-100));
                            this.mContentvalues.put("cellX", Integer.valueOf(i2));
                            this.mContentvalues.put("cellY", Integer.valueOf(i3));
                            this.mContentvalues.put("screen", Integer.valueOf(i));
                            addAppShortcut(db, this.mContentvalues, null, packageManager, intent);
                        }
                    }
                }
                version = 18;
            }
            if (version == 18) {
                try {
                    LauncherProvider.safelyExecSQL(db, "ALTER TABLE screens ADD screenType INTEGER NOT NULL DEFAULT 0");
                } catch (SQLiteException ex) {
                    if (!ex.getMessage().startsWith("duplicate column name: screenType")) {
                        throw ex;
                    }
                }
                version = 19;
            }
            if (version == 19) {
                upgradeComponentName(db, new ComponentName("com.android.settings", "com.miui.securitycenter.Main"), new ComponentName("com.miui.securitycenter", "com.miui.securitycenter.MainActivity"));
                upgradeComponentName(db, new ComponentName("com.miui.weather2", "com.miui.weather2.ActivityWeatherCycle"), new ComponentName("com.miui.weather2", "com.miui.weather2.ActivityWeatherMain"));
                upgradeComponentName(db, new ComponentName("com.xiaomi.xmsf", "com.xiaomi.xmsf.account.ui.MiCloudSettingsActivity"), new ComponentName("com.miui.cloudservice", "com.miui.cloudservice.ui.MiCloudInfoSettingsActivity"));
                upgradeComponentName(db, new ComponentName("com.duokan.phone.remotecontroller", "com.xiaomi.mitv.phone.remotecontroller.HoriWidgetMainActivity"), new ComponentName("com.duokan.phone.remotecontroller", "com.xiaomi.mitv.phone.remotecontroller.HoriWidgetMainActivityV2"));
                upgradeComponentName(db, new ComponentName("com.miui.bugreport", "com.miui.bugreport.ui.TypeSelectionActivity"), new ComponentName("com.miui.bugreport", "com.miui.bugreport.ui.MainActivity"));
                folderId = queryIdByTitle(db, "com.miui.home:string/default_folder_title_tools");
                if (folderId != -1) {
                    if (queryIdByIntent(db, "#Intent;action=android.intent.action.MAIN;category=android.intent.category.LAUNCHER;launchFlags=0x10200000;component=com.xiaomi.account/.ui.MiCloudSettingsActivity;end") == -1) {
                        intent.setComponent(ComponentName.unflattenFromString("com.xiaomi.account/.ui.MiCloudSettingsActivity"));
                        this.mContentvalues.clear();
                        this.mContentvalues.put("container", Long.valueOf(folderId));
                        this.mContentvalues.put("cellX", Integer.valueOf(0));
                        this.mContentvalues.put("cellY", Integer.valueOf(0));
                        this.mContentvalues.put("screen", Integer.valueOf(-1));
                        addAppShortcut(db, this.mContentvalues, null, packageManager, intent);
                    }
                }
                deleteClockBack();
                version = 20;
            }
            if (version == 20) {
                deleteClockBack();
                version = 21;
            }
            if (version == 21) {
                upgradeComponentName(db, new ComponentName("com.miui.fmradio", "com.miui.fmradio.FmRadioActivity"), new ComponentName("com.ximalaya.ting.android", "com.ximalaya.ting.android.activity.login.WelcomeActivity"));
                version = 22;
            }
            if (version == 22) {
                deleteClockBack();
                version = 23;
            }
            if (version == 23) {
                try {
                    LauncherProvider.safelyExecSQL(db, "ALTER TABLE favorites ADD profileId INTEGER NOT NULL DEFAULT 0 ");
                } catch (SQLiteException ex2) {
                    if (!ex2.getMessage().startsWith("duplicate column name:")) {
                        throw ex2;
                    }
                }
                version = 24;
            }
            if (version == 24) {
                db.beginTransaction();
                try {
                    long userSerialNumber = ((UserManager) this.mContext.getSystemService("user")).getSerialNumberForUser(Process.myUserHandle());
                    ContentValues values = new ContentValues();
                    values.put("profileId", Integer.valueOf((int) userSerialNumber));
                    LauncherProvider.safelyUpdateDatabase(db, "favorites", values, null, null);
                    db.setTransactionSuccessful();
                    version = 25;
                } finally {
                    db.endTransaction();
                }
            }
            if (version == 25) {
                LauncherProvider.safelyExecSQL(db, "update favorites set launchCount = launchCount+1 where itemType = 2");
                version = 26;
            }
            if (version == 26) {
                deleteClockBack();
                version = 27;
            }
            if (version == 27) {
                upgradeComponentName(db, new ComponentName("com.miui.barcodescanner", "com.miui.barcodescanner.activity.CaptureActivity"), new ComponentName("com.xiaomi.scanner", "com.xiaomi.scanner.app.ScanActivity"));
                upgradeComponentName(db, new ComponentName("com.android.calculator2", "com.android.calculator2.Calculator"), new ComponentName("com.miui.calculator", "com.miui.calculator.cal.CalculatorActivity"));
                version = 28;
            }
            if (version == 28) {
                if (!Build.IS_TABLET) {
                    sQLiteDatabase2 = db;
                    LauncherProvider.safelyDeleteFromDB(sQLiteDatabase2, "favorites", "uri=?", new String[]{"file:///system/media/theme/default/gadgets/music.mtz"});
                }
                version = 29;
            }
            if (version == 29) {
                deleteClockBack();
                version = 30;
            }
            if (version == 30) {
                upgradeComponentName(db, ComponentName.unflattenFromString("com.miui.gallery/.app.Gallery"), ComponentName.unflattenFromString("com.miui.gallery/.activity.HomePageActivity"));
                upgradeComponentName(db, ComponentName.unflattenFromString("com.miui.bugreport/.ui.MainActivity"), ComponentName.unflattenFromString("com.miui.bugreport/.ui.MainTabActivity"));
                upgradeComponentName(db, ComponentName.unflattenFromString("com.android.deskclock/.DeskClock"), ComponentName.unflattenFromString("com.android.deskclock/.DeskClockTabActivity"));
                version = 31;
            }
            if (false) {
                createFavoritesTriggers(db);
            }
            if (version != newVersion) {
                Log.w("Launcher.LauncherProvider", "Destroying all old data and re-create.");
                onCreate(db);
            }
        }

        private void deleteClockBack() {
            File file = this.mContext.getDir("clock_bak", DeviceConfig.TEMP_SHARE_MODE_FOR_WORLD_READABLE);
            if (file.exists()) {
                File[] childFile = file.listFiles();
                if (!(childFile == null || childFile.length == 0)) {
                    for (File f : childFile) {
                        f.delete();
                    }
                }
                file.delete();
            }
        }

        public static boolean queryItemInFolder(SQLiteDatabase db, long itemId, String folderName) {
            long folderId = queryIdByTitle(db, folderName);
            Cursor c = null;
            if (folderId != -1) {
                try {
                    String[] strArr = new String[]{"_id"};
                    String str = "_id=" + itemId + " AND " + "container" + "=" + folderId;
                    c = db.query("favorites", strArr, str, null, null, null, null);
                    if (c != null && c.getCount() == 1) {
                        return true;
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }
            if (c != null) {
                c.close();
            }
            return false;
        }

        public int queryFolderSize(SQLiteDatabase db, long folderId) {
            Cursor c = null;
            try {
                c = db.rawQuery("select COUNT('_id') from favorites where container=" + folderId, null);
                if (c.moveToNext()) {
                    int i = c.getInt(0);
                    if (c == null) {
                        return i;
                    }
                    c.close();
                    return i;
                }
                if (c != null) {
                    c.close();
                }
                return -1;
            } catch (Exception e) {
                if (c != null) {
                    c.close();
                }
            } catch (Throwable th) {
                if (c != null) {
                    c.close();
                }
            }
        }

        static long queryInstalledComponentId(SQLiteDatabase db, String componentName) {
            return queryIdByIntent(db, "#Intent;action=android.intent.action.MAIN;category=android.intent.category.LAUNCHER;launchFlags=0x10200000;component=" + componentName + ";end");
        }

        static void removeSkippedItems(SQLiteDatabase db) {
            Iterator i$ = LauncherProvider.mSkippedItems.iterator();
            while (i$.hasNext()) {
                if (queryIdByIntent(db, "#Intent;action=android.intent.action.MAIN;category=android.intent.category.LAUNCHER;launchFlags=0x10200000;component=" + ((ComponentName) i$.next()).flattenToShortString() + ";end") != -1) {
                    LauncherProvider.safelyDeleteFromDB(db, "favorites", "_id=?", new String[]{String.valueOf(queryIdByIntent(db, "#Intent;action=android.intent.action.MAIN;category=android.intent.category.LAUNCHER;launchFlags=0x10200000;component=" + ((ComponentName) i$.next()).flattenToShortString() + ";end"))});
                }
            }
        }

        private void addAppShortcut(SQLiteDatabase db, Intent intent, long containerId, int cellX, PackageManager packageManager) {
            this.mContentvalues.clear();
            this.mContentvalues.put("container", Long.valueOf(containerId));
            if (cellX >= 0) {
                this.mContentvalues.put("cellX", Integer.valueOf(cellX));
            }
            addAppShortcut(db, this.mContentvalues, null, packageManager, intent);
        }

        private void updateItemContainer(SQLiteDatabase db, long Id, long containerId, int cellX) {
            this.mContentvalues.clear();
            this.mContentvalues.put("container", Long.valueOf(containerId));
            this.mContentvalues.put("cellX", Integer.valueOf(cellX));
            LauncherProvider.safelyUpdateDatabase(db, "favorites", this.mContentvalues, "_id=?", new String[]{String.valueOf(Id)});
        }

        static void upgradeComponentName(SQLiteDatabase db, ComponentName from, ComponentName to) {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setFlags(270532608);
            intent.addCategory("android.intent.category.LAUNCHER");
            intent.setComponent(to);
            String sql = "UPDATE favorites SET intent='" + intent.toUri(0);
            if (!from.getPackageName().equals(to.getPackageName())) {
                sql = sql + "',iconPackage='" + to.getPackageName();
            }
            intent.setComponent(from);
            LauncherProvider.safelyExecSQL(db, sql + "' WHERE intent='" + intent.toUri(0) + "';");
        }

        public static long queryIdByTitle(SQLiteDatabase db, String title) {
            Cursor c = null;
            long id = -1;
            try {
                SQLiteDatabase sQLiteDatabase = db;
                c = sQLiteDatabase.query("favorites", new String[]{"_id"}, "title=?", new String[]{title}, null, null, null);
                if (c.getCount() != 0 && c.moveToNext()) {
                    id = c.getLong(0);
                }
                if (c != null) {
                    c.close();
                }
                return id;
            } catch (Throwable th) {
                if (c != null) {
                    c.close();
                }
            }
        }

        static long queryIdByIntent(SQLiteDatabase db, String intent) {
            Cursor c = null;
            long id = -1;
            try {
                SQLiteDatabase sQLiteDatabase = db;
                c = sQLiteDatabase.query("favorites", new String[]{"_id"}, "intent=?", new String[]{intent}, null, null, null);
                if (c.getCount() != 0 && c.moveToNext()) {
                    id = c.getLong(0);
                }
                if (c != null) {
                    c.close();
                }
                return id;
            } catch (Throwable th) {
                if (c != null) {
                    c.close();
                }
            }
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (newVersion < 11) {
                super.onDowngrade(db, oldVersion, newVersion);
            }
        }

        public long generateNewId() {
            if (this.mMaxId < 0) {
                throw new RuntimeException("Error: max id was not initialized");
            }
            this.mMaxId++;
            return this.mMaxId;
        }

        public void updateMaxId(SQLiteDatabase db) {
            this.mMaxId = initializeMaxId(db);
        }

        private long initializeMaxId(SQLiteDatabase db) {
            Cursor c = db.rawQuery("SELECT MAX(_id) FROM favorites", null);
            long id = -1;
            if (c != null && c.moveToNext()) {
                id = c.getLong(0);
            }
            if (c != null) {
                c.close();
            }
            if (id != -1) {
                return id;
            }
            throw new RuntimeException("Error: could not query max id");
        }

        private int loadFavorites(SQLiteDatabase db) {
            LauncherProvider.safelyExecSQL(db, "DROP TABLE IF EXISTS favorites");
            LauncherProvider.safelyExecSQL(db, "CREATE TABLE favorites (_id INTEGER PRIMARY KEY,title TEXT,intent TEXT,container INTEGER,screen INTEGER,cellX INTEGER,cellY INTEGER,spanX INTEGER,spanY INTEGER,itemType INTEGER,appWidgetId INTEGER NOT NULL DEFAULT -1,isShortcut INTEGER,iconType INTEGER,iconPackage TEXT,iconResource TEXT,icon BLOB,uri TEXT,displayMode INTEGER,launchCount INTEGER NOT NULL DEFAULT 1,sortMode INTEGER,itemFlags INTEGER NOT NULL DEFAULT 0,profileId INTEGER NOT NULL DEFAULT 0);");
            Intent intent = new Intent("android.intent.action.MAIN", null);
            intent.addCategory("android.intent.category.LAUNCHER");
            ContentValues values = new ContentValues();
            PackageManager packageManager = this.mContext.getPackageManager();
            int i = 0;
            try {
                XmlPullParser parser = this.mContext.getResources().getXml(DeviceConfig.getDefaultWorkspaceXmlId(this.mContext));
                try {
                    AttributeSet attrs = Xml.asAttributeSet(parser);
                    XmlUtils.beginDocument(parser, "favorites");
                    int depth = parser.getDepth();
                    while (true) {
                        int type = parser.next();
                        if ((type == 3 && parser.getDepth() <= depth) || type == 1) {
                            break;
                        } else if (type == 2) {
                            boolean added = false;
                            String name = parser.getName();
                            TypedArray a = this.mContext.obtainStyledAttributes(attrs, R.styleable.Favorite);
                            values.clear();
                            values.put("profileId", Long.valueOf(((UserManager) this.mContext.getSystemService("user")).getSerialNumberForUser(Process.myUserHandle())));
                            String container = a.getString(3);
                            int containerID = TextUtils.isEmpty(container) ? -100 : Integer.parseInt(container);
                            if (containerID == -100) {
                                container = String.valueOf(-100);
                                values.put("screen", a.getString(2));
                            }
                            values.put("cellX", a.getString(4));
                            values.put("cellY", a.getString(5));
                            if (DeviceConfig.isLayoutRtl() && containerID == -100 && ("favorite".equals(name) || "shortcut".equals(name) || "folder".equals(name))) {
                                values.put("cellX", Integer.toString((DeviceConfig.getCellCountX() - Integer.valueOf(a.getString(4)).intValue()) - 1));
                            }
                            values.put("container", container);
                            if ("default".equals(name)) {
                                Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
                                editor.putLong("pref_default_screen", values.getAsLong("screen").longValue());
                                editor.commit();
                            } else if ("favorite".equals(name)) {
                                added = addAppShortcut(db, values, a, packageManager, intent);
                            } else if ("search".equals(name)) {
                                added = addSearchWidget(db, values);
                            } else if ("google_search".equals(name)) {
                                added = addGadget(db, values, 13);
                            } else if ("google_original_search".equals(name)) {
                                added = addGoogleOriginalSearchWidget(db, values);
                            } else if ("clock2x2".equals(name)) {
                                added = addClock2x2(db, values);
                            } else if ("clock2x4".equals(name)) {
                                added = addClock2x4(db, values);
                            } else if ("appwidget".equals(name)) {
                                added = addAppWidget(db, values, a, packageManager);
                            } else if ("shortcut".equals(name)) {
                                added = addUriShortcut(db, values, a);
                            } else if ("folder".equals(name)) {
                                added = addFolder(db, values, a);
                            } else if ("gadget".equals(name)) {
                                added = addMtzGadget(db, values, a);
                            }
                            if (added) {
                                i++;
                            }
                            a.recycle();
                        }
                    }
                } catch (XmlPullParserException e) {
                    Log.w("Launcher.LauncherProvider", "Got exception parsing favorites.", e);
                } catch (IOException e2) {
                    Log.w("Launcher.LauncherProvider", "Got exception parsing favorites.", e2);
                }
                return i;
            } catch (NotFoundException e3) {
                return 0;
            }
        }

        private void dumpDefaultWorkspace(int defaultScreenIndex) {
            if (Environment.getExternalStorageState().equals("mounted") && dumpDefaultWorkspaceImpl(DeviceConfig.getDatabaseName(), DeviceConfig.getDatabaseName() + ".xml", defaultScreenIndex)) {
                Toast.makeText(this.mContext, "dump default workspace succeeded.", 100).show();
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private boolean dumpDefaultWorkspaceImpl(java.lang.String r25, java.lang.String r26, int r27) {
            /*
            r24 = this;
            r13 = "launcher";
            r7 = "favorite";
            r17 = "shortcut";
            r5 = 0;
            r4 = 0;
            r16 = android.os.Environment.getExternalStorageDirectory();	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r14 = new com.android.internal.util.FastXmlSerializer;	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r14.<init>();	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r8 = new java.io.FileOutputStream;	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r19 = new java.lang.StringBuilder;	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r19.<init>();	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r0 = r19;
            r1 = r16;
            r19 = r0.append(r1);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r20 = "/";
            r19 = r19.append(r20);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r0 = r19;
            r1 = r26;
            r19 = r0.append(r1);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r19 = r19.toString();	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r0 = r19;
            r8.<init>(r0);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r19 = "utf-8";
            r0 = r19;
            r14.setOutput(r8, r0);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r19 = 0;
            r20 = 0;
            r20 = java.lang.Boolean.valueOf(r20);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r0 = r19;
            r1 = r20;
            r14.startDocument(r0, r1);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r19 = 0;
            r20 = "favorites";
            r0 = r19;
            r1 = r20;
            r14.startTag(r0, r1);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r19 = 0;
            r20 = "xmlns:launcher";
            r21 = "http://schemas.android.com/apk/res/com.miui.home";
            r0 = r19;
            r1 = r20;
            r2 = r21;
            r14.attribute(r0, r1, r2);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r19 = 0;
            r20 = "default";
            r0 = r19;
            r1 = r20;
            r14.startTag(r0, r1);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r19 = "launcher";
            r20 = "screen";
            r21 = java.lang.String.valueOf(r27);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r0 = r19;
            r1 = r20;
            r2 = r21;
            r14.attribute(r0, r1, r2);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r19 = 0;
            r20 = "default";
            r0 = r19;
            r1 = r20;
            r14.endTag(r0, r1);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r0 = r24;
            r0 = r0.mContext;	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r19 = r0;
            r0 = r19;
            r1 = r25;
            r19 = r0.getDatabasePath(r1);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r15 = r19.getPath();	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r19 = 0;
            r20 = 1;
            r0 = r19;
            r1 = r20;
            r5 = android.database.sqlite.SQLiteDatabase.openDatabase(r15, r0, r1);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r19 = "SELECT * FROM favorites WHERE itemType=2 ORDER BY screen ASC, cellY ASC, cellX ASC";
            r20 = 0;
            r0 = r19;
            r1 = r20;
            r4 = r5.rawQuery(r0, r1);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
        L_0x00b8:
            r19 = r4.moveToNext();	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            if (r19 == 0) goto L_0x0134;
        L_0x00be:
            r19 = 0;
            r20 = "folder";
            r0 = r19;
            r1 = r20;
            r14.startTag(r0, r1);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r19 = "_id";
            r0 = r19;
            r19 = r4.getColumnIndex(r0);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r0 = r19;
            r10 = r4.getInt(r0);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r0 = (long) r10;	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r20 = r0;
            r0 = r24;
            r0 = r0.mPresetsContainerId;	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r22 = r0;
            r19 = (r20 > r22 ? 1 : (r20 == r22 ? 0 : -1));
            if (r19 != 0) goto L_0x00f3;
        L_0x00e4:
            r19 = "launcher";
            r20 = "presets_container";
            r21 = "true";
            r0 = r19;
            r1 = r20;
            r2 = r21;
            r14.attribute(r0, r1, r2);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
        L_0x00f3:
            r19 = "launcher";
            r20 = "title";
            r21 = "title";
            r0 = r21;
            r21 = r4.getColumnIndex(r0);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r0 = r21;
            r21 = r4.getString(r0);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r0 = r19;
            r1 = r20;
            r2 = r21;
            r14.attribute(r0, r1, r2);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r19 = "launcher";
            r0 = r24;
            r1 = r19;
            r0.dumpWorkspaceWritePos(r4, r14, r1);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r19 = 0;
            r20 = "folder";
            r0 = r19;
            r1 = r20;
            r14.endTag(r0, r1);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            goto L_0x00b8;
        L_0x0123:
            r6 = move-exception;
            r6.printStackTrace();	 Catch:{ all -> 0x02e4 }
            if (r4 == 0) goto L_0x012c;
        L_0x0129:
            r4.close();
        L_0x012c:
            if (r5 == 0) goto L_0x0131;
        L_0x012e:
            r5.close();
        L_0x0131:
            r19 = 0;
        L_0x0133:
            return r19;
        L_0x0134:
            r4.close();	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r4 = 0;
            r19 = "SELECT * FROM favorites WHERE itemType!=2 ORDER BY screen ASC, cellY ASC, cellX ASC";
            r20 = 0;
            r0 = r19;
            r1 = r20;
            r4 = r5.rawQuery(r0, r1);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
        L_0x0144:
            r19 = r4.moveToNext();	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            if (r19 == 0) goto L_0x02a5;
        L_0x014a:
            r19 = "itemType";
            r0 = r19;
            r19 = r4.getColumnIndex(r0);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r0 = r19;
            r12 = r4.getInt(r0);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r18 = 0;
            switch(r12) {
                case 0: goto L_0x025b;
                case 1: goto L_0x025f;
                case 2: goto L_0x015d;
                case 3: goto L_0x015d;
                case 4: goto L_0x015d;
                case 5: goto L_0x0263;
                default: goto L_0x015d;
            };	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
        L_0x015d:
            if (r18 == 0) goto L_0x0144;
        L_0x015f:
            r19 = 0;
            r0 = r19;
            r1 = r18;
            r14.startTag(r0, r1);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r19 = 1;
            r0 = r19;
            if (r12 == r0) goto L_0x0170;
        L_0x016e:
            if (r12 != 0) goto L_0x0285;
        L_0x0170:
            r19 = "intent";
            r0 = r19;
            r19 = r4.getColumnIndex(r0);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r0 = r19;
            r19 = r4.getString(r0);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r20 = 0;
            r11 = android.content.Intent.parseUri(r19, r20);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            if (r11 == 0) goto L_0x0240;
        L_0x0186:
            r19 = r11.getComponent();	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            if (r19 == 0) goto L_0x0240;
        L_0x018c:
            r19 = "launcher";
            r20 = "packageName";
            r21 = r11.getComponent();	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r21 = r21.getPackageName();	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r0 = r19;
            r1 = r20;
            r2 = r21;
            r14.attribute(r0, r1, r2);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r19 = "launcher";
            r20 = "className";
            r21 = r11.getComponent();	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r21 = r21.getClassName();	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r0 = r19;
            r1 = r20;
            r2 = r21;
            r14.attribute(r0, r1, r2);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r19 = r11.getPackage();	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r19 = com.miui.home.launcher.LauncherSettings.isRetainedPackage(r19);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            if (r19 == 0) goto L_0x01cf;
        L_0x01c0:
            r19 = "launcher";
            r20 = "retained";
            r21 = "true";
            r0 = r19;
            r1 = r20;
            r2 = r21;
            r14.attribute(r0, r1, r2);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
        L_0x01cf:
            r19 = "launcher";
            r0 = r24;
            r1 = r19;
            r0.dumpWorkspaceWritePos(r4, r14, r1);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r19 = 1;
            r0 = r19;
            if (r12 != r0) goto L_0x0240;
        L_0x01de:
            r19 = "launcher";
            r20 = "title";
            r21 = "title";
            r0 = r21;
            r21 = r4.getColumnIndex(r0);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r0 = r21;
            r21 = r4.getString(r0);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r0 = r19;
            r1 = r20;
            r2 = r21;
            r14.attribute(r0, r1, r2);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r19 = "launcher";
            r20 = "action";
            r21 = r11.getAction();	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r0 = r19;
            r1 = r20;
            r2 = r21;
            r14.attribute(r0, r1, r2);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r19 = "launcher";
            r20 = "iconResource";
            r21 = "iconResource";
            r0 = r21;
            r21 = r4.getColumnIndex(r0);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r0 = r21;
            r21 = r4.getString(r0);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r0 = r19;
            r1 = r20;
            r2 = r21;
            r14.attribute(r0, r1, r2);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r19 = r11.getData();	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            if (r19 == 0) goto L_0x0240;
        L_0x022b:
            r19 = "launcher";
            r20 = "uri";
            r21 = r11.getData();	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r21 = r21.toString();	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r0 = r19;
            r1 = r20;
            r2 = r21;
            r14.attribute(r0, r1, r2);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
        L_0x0240:
            r19 = 0;
            r0 = r19;
            r1 = r18;
            r14.endTag(r0, r1);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            goto L_0x0144;
        L_0x024b:
            r6 = move-exception;
            r6.printStackTrace();	 Catch:{ all -> 0x02e4 }
            if (r4 == 0) goto L_0x0254;
        L_0x0251:
            r4.close();
        L_0x0254:
            if (r5 == 0) goto L_0x0131;
        L_0x0256:
            r5.close();
            goto L_0x0131;
        L_0x025b:
            r18 = "favorite";
            goto L_0x015d;
        L_0x025f:
            r18 = "shortcut";
            goto L_0x015d;
        L_0x0263:
            r19 = "appWidgetId";
            r0 = r19;
            r19 = r4.getColumnIndex(r0);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r0 = r19;
            r9 = r4.getInt(r0);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r19 = 3;
            r0 = r19;
            if (r9 != r0) goto L_0x027b;
        L_0x0277:
            r18 = "search";
            goto L_0x015d;
        L_0x027b:
            r19 = 6;
            r0 = r19;
            if (r9 != r0) goto L_0x015d;
        L_0x0281:
            r18 = "clock2x4";
            goto L_0x015d;
        L_0x0285:
            r19 = 5;
            r0 = r19;
            if (r12 != r0) goto L_0x0240;
        L_0x028b:
            r19 = "launcher";
            r0 = r24;
            r1 = r19;
            r0.dumpWorkspaceWritePos(r4, r14, r1);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            goto L_0x0240;
        L_0x0295:
            r6 = move-exception;
            r6.printStackTrace();	 Catch:{ all -> 0x02e4 }
            if (r4 == 0) goto L_0x029e;
        L_0x029b:
            r4.close();
        L_0x029e:
            if (r5 == 0) goto L_0x0131;
        L_0x02a0:
            r5.close();
            goto L_0x0131;
        L_0x02a5:
            r19 = 0;
            r20 = "favorites";
            r0 = r19;
            r1 = r20;
            r14.endTag(r0, r1);	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r14.endDocument();	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r8.close();	 Catch:{ IllegalArgumentException -> 0x0123, IllegalStateException -> 0x024b, IOException -> 0x0295, SQLiteException -> 0x02c4, URISyntaxException -> 0x02d4 }
            r19 = 1;
            if (r4 == 0) goto L_0x02bd;
        L_0x02ba:
            r4.close();
        L_0x02bd:
            if (r5 == 0) goto L_0x0133;
        L_0x02bf:
            r5.close();
            goto L_0x0133;
        L_0x02c4:
            r6 = move-exception;
            r6.printStackTrace();	 Catch:{ all -> 0x02e4 }
            if (r4 == 0) goto L_0x02cd;
        L_0x02ca:
            r4.close();
        L_0x02cd:
            if (r5 == 0) goto L_0x0131;
        L_0x02cf:
            r5.close();
            goto L_0x0131;
        L_0x02d4:
            r6 = move-exception;
            r6.printStackTrace();	 Catch:{ all -> 0x02e4 }
            if (r4 == 0) goto L_0x02dd;
        L_0x02da:
            r4.close();
        L_0x02dd:
            if (r5 == 0) goto L_0x0131;
        L_0x02df:
            r5.close();
            goto L_0x0131;
        L_0x02e4:
            r19 = move-exception;
            if (r4 == 0) goto L_0x02ea;
        L_0x02e7:
            r4.close();
        L_0x02ea:
            if (r5 == 0) goto L_0x02ef;
        L_0x02ec:
            r5.close();
        L_0x02ef:
            throw r19;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.miui.home.launcher.LauncherProvider.DatabaseHelper.dumpDefaultWorkspaceImpl(java.lang.String, java.lang.String, int):boolean");
        }

        private void dumpWorkspaceWritePos(Cursor c, XmlSerializer out, String nameSpace) throws IllegalArgumentException, IllegalStateException, IOException {
            int container = c.getInt(c.getColumnIndex("container"));
            if (container == -100) {
                out.attribute(nameSpace, "screen", String.valueOf(c.getInt(c.getColumnIndex("screen")) - 1));
            } else {
                out.attribute(nameSpace, "container", String.valueOf(container));
            }
            out.attribute(nameSpace, "x", String.valueOf(c.getInt(c.getColumnIndex("cellX"))));
            out.attribute(nameSpace, "y", String.valueOf(c.getInt(c.getColumnIndex("cellY"))));
        }

        private boolean addFolder(SQLiteDatabase db, ContentValues values, TypedArray a) {
            String folderName = a.getString(9);
            if ("com.miui.home:string/default_folder_title_claro".equals(folderName) && "MX".equals(Build.getRegion())) {
                values.put("title", "com.miui.home:string/default_folder_title_telcel");
            } else {
                values.put("title", folderName);
            }
            values.put("itemType", Integer.valueOf(2));
            values.put("spanX", Integer.valueOf(1));
            values.put("spanY", Integer.valueOf(1));
            long id = LauncherProvider.safelyInsertDatabase(db, "favorites", null, values);
            if (a.getBoolean(14, false)) {
                this.mPresetsContainerId = id;
            }
            return true;
        }

        private boolean addMtzGadget(SQLiteDatabase db, ContentValues values, TypedArray a) {
            String uri = a.getString(10);
            if (uri != null) {
                GadgetInfo info = new GadgetInfo(Uri.parse(uri));
                try {
                    if (info.loadMtzGadget()) {
                        values.put("spanX", Integer.valueOf(info.spanX));
                        values.put("spanY", Integer.valueOf(info.spanY));
                        values.put("itemType", Integer.valueOf(info.itemType));
                        values.put("appWidgetId", Integer.valueOf(info.getGadgetId()));
                        values.put("uri", info.getMtzUri().toString());
                        LauncherProvider.safelyInsertDatabase(db, "favorites", null, values);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }

        private boolean addAppShortcut(SQLiteDatabase db, ContentValues values, TypedArray a, PackageManager packageManager, Intent intent) {
            ComponentName cn;
            ActivityInfo info = null;
            String packageName = a != null ? a.getString(1) : intent.getComponent().getPackageName();
            String className = a != null ? a.getString(0) : intent.getComponent().getClassName();
            if (a != null) {
                try {
                    if (a.getBoolean(13, false)) {
                        cn = new ComponentName(packageName, className);
                        intent.setComponent(cn);
                        intent.setFlags(270532608);
                        values.put("intent", intent.toUri(0));
                        if (info != null) {
                            values.put("title", info.loadLabel(packageManager).toString());
                        }
                        values.put("itemType", Integer.valueOf(0));
                        values.put("iconPackage", packageName);
                        values.put("spanX", Integer.valueOf(1));
                        values.put("spanY", Integer.valueOf(1));
                        LauncherProvider.safelyInsertDatabase(db, "favorites", null, values);
                        return true;
                    }
                } catch (NameNotFoundException e) {
                    Log.w("Launcher.LauncherProvider", "Unable to add favorite: " + packageName + "/" + className);
                    return false;
                }
            }
            try {
                cn = new ComponentName(packageName, className);
                info = packageManager.getActivityInfo(cn, 0);
            } catch (NameNotFoundException e2) {
                cn = new ComponentName(packageManager.currentToCanonicalPackageNames(new String[]{packageName})[0], className);
                info = packageManager.getActivityInfo(cn, 0);
            }
            intent.setComponent(cn);
            intent.setFlags(270532608);
            values.put("intent", intent.toUri(0));
            if (info != null) {
                values.put("title", info.loadLabel(packageManager).toString());
            }
            values.put("itemType", Integer.valueOf(0));
            values.put("iconPackage", packageName);
            values.put("spanX", Integer.valueOf(1));
            values.put("spanY", Integer.valueOf(1));
            LauncherProvider.safelyInsertDatabase(db, "favorites", null, values);
            return true;
        }

        private boolean addGoogleOriginalSearchWidget(SQLiteDatabase db, ContentValues values) {
            for (AppWidgetProviderInfo info : AppWidgetManager.getInstance(this.mContext).getInstalledProviders()) {
                Log.i("Launcher.provider", info.provider.getClassName());
                if (info.provider != null && "com.google.android.googlequicksearchbox.SearchWidgetProvider".equals(info.provider.getClassName())) {
                    LauncherAppWidgetProviderInfo itemInfo = new LauncherAppWidgetProviderInfo(info);
                    DeviceConfig.calcWidgetSpans(itemInfo);
                    return addAppWidget(db, values, new ComponentName(info.provider.getPackageName(), info.provider.getClassName()), itemInfo.spanX, itemInfo.spanY);
                }
            }
            return false;
        }

        private boolean addSearchWidget(SQLiteDatabase db, ContentValues values) {
            return addGadget(db, values, 3);
        }

        private boolean addClock2x2(SQLiteDatabase db, ContentValues values) {
            return addGadget(db, values, 5);
        }

        private boolean addClock2x4(SQLiteDatabase db, ContentValues values) {
            return addGadget(db, values, 6);
        }

        private boolean addGadget(SQLiteDatabase db, ContentValues values, int gadgetId) {
            GadgetInfo info = GadgetFactory.getInfo(gadgetId);
            values.put("spanX", Integer.valueOf(info.spanX));
            values.put("spanY", Integer.valueOf(info.spanY));
            values.put("itemType", Integer.valueOf(5));
            values.put("appWidgetId", Integer.valueOf(gadgetId));
            LauncherProvider.safelyInsertDatabase(db, "favorites", null, values);
            return true;
        }

        private boolean addAppWidget(SQLiteDatabase db, ContentValues values, TypedArray a, PackageManager packageManager) {
            String packageName = a.getString(1);
            String className = a.getString(0);
            if (packageName == null || className == null) {
                return false;
            }
            boolean hasPackage = true;
            ComponentName cn = new ComponentName(packageName, className);
            try {
                packageManager.getReceiverInfo(cn, 0);
            } catch (Exception e) {
                cn = new ComponentName(packageManager.currentToCanonicalPackageNames(new String[]{packageName})[0], className);
                try {
                    packageManager.getReceiverInfo(cn, 0);
                } catch (Exception e2) {
                    hasPackage = false;
                }
            }
            if (!hasPackage) {
                return false;
            }
            return addAppWidget(db, values, cn, a.getInt(6, 0), a.getInt(7, 0));
        }

        private boolean addAppWidget(SQLiteDatabase db, ContentValues values, ComponentName cn, int spanX, int spanY) {
            boolean allocatedAppWidgets = false;
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.mContext);
            try {
                int appWidgetId = this.mAppWidgetHost.allocateAppWidgetId();
                values.put("itemType", Integer.valueOf(4));
                values.put("spanX", Integer.valueOf(spanX));
                values.put("spanY", Integer.valueOf(spanY));
                values.put("appWidgetId", Integer.valueOf(appWidgetId));
                LauncherProvider.safelyInsertDatabase(db, "favorites", null, values);
                allocatedAppWidgets = true;
                appWidgetManager.bindAppWidgetId(appWidgetId, cn);
                return true;
            } catch (RuntimeException ex) {
                Log.e("Launcher.LauncherProvider", "Problem allocating appWidgetId", ex);
                return allocatedAppWidgets;
            }
        }

        private boolean addUriShortcut(SQLiteDatabase db, ContentValues values, TypedArray a) {
            String iconRes = a.getString(12);
            if (iconRes == null) {
                return false;
            }
            Intent intent = new Intent();
            values.put("iconType", Integer.valueOf(0));
            values.put("iconResource", iconRes);
            values.put("iconPackage", iconRes.substring(0, iconRes.indexOf(58)));
            values.put("spanX", Integer.valueOf(1));
            values.put("spanY", Integer.valueOf(1));
            intent.setAction(a.getString(11));
            intent.setClassName(a.getString(1), a.getString(0));
            intent.setFlags(270532608);
            String uri = a.getString(10);
            if (uri != null) {
                intent.setData(Uri.parse(uri));
            }
            intent.addCategory("android.intent.category.DEFAULT");
            values.put("intent", intent.toUri(0));
            values.put("title", a.getString(9));
            values.put("itemType", Integer.valueOf(1));
            values.put("iconType", Integer.valueOf(0));
            if (a.getBoolean(13, false)) {
                values.put("isShortcut", Integer.valueOf(1));
            }
            LauncherProvider.safelyInsertDatabase(db, "favorites", null, values);
            return true;
        }

        private boolean addShortcut(SQLiteDatabase db, String iconRes, String action, String packageName, String className, String title, long container, int cellX, int cellY) {
            if (iconRes == null) {
                return false;
            }
            Intent intent = new Intent();
            ContentValues values = new ContentValues();
            values.put("iconType", Integer.valueOf(0));
            values.put("iconResource", iconRes);
            values.put("iconPackage", iconRes.substring(0, iconRes.indexOf(58)));
            values.put("container", Long.valueOf(container));
            values.put("cellX", Integer.valueOf(cellX));
            values.put("cellY", Integer.valueOf(cellY));
            values.put("spanX", Integer.valueOf(1));
            values.put("spanY", Integer.valueOf(1));
            intent.setAction(action);
            intent.setClassName(packageName, className);
            intent.setFlags(270532608);
            intent.addCategory("android.intent.category.DEFAULT");
            values.put("intent", intent.toUri(0));
            values.put("title", title);
            values.put("itemType", Integer.valueOf(1));
            values.put("iconType", Integer.valueOf(0));
            LauncherProvider.safelyInsertDatabase(db, "favorites", null, values);
            return true;
        }

        private int loadPresetsApps(SQLiteDatabase db) {
            if (this.mPresetsContainerId < 0) {
                return 0;
            }
            File f = new File(LauncherSettings.PRESET_APPS_PATH);
            int counter = 100;
            if (!f.isDirectory()) {
                return 100;
            }
            File[] flist = f.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String filename) {
                    return filename.toLowerCase().endsWith(".apk");
                }
            });
            if (flist == null) {
                return 100;
            }
            PackageManager pm = this.mContext.getPackageManager();
            ContentValues values = new ContentValues();
            Intent intent = new Intent("android.intent.action.VIEW");
            Resources pRes = this.mContext.getResources();
            for (File file : flist) {
                PackageInfo pinfo = pm.getPackageArchiveInfo(file.getAbsolutePath(), 0);
                if (pinfo != null) {
                    ApplicationInfo ainfo = pinfo.applicationInfo;
                    AssetManager assmgr = new AssetManager();
                    assmgr.addAssetPath(file.getAbsolutePath());
                    Resources resources = new Resources(assmgr, pRes.getDisplayMetrics(), pRes.getConfiguration());
                    CharSequence label = null;
                    if (pinfo.applicationInfo.labelRes != 0) {
                        try {
                            label = resources.getText(ainfo.labelRes);
                        } catch (NotFoundException e) {
                        }
                    }
                    if (label == null) {
                        label = ainfo.nonLocalizedLabel != null ? ainfo.nonLocalizedLabel : ainfo.packageName;
                    }
                    Drawable icon = null;
                    if (ainfo.icon != 0) {
                        try {
                            icon = resources.getDrawable(ainfo.icon);
                        } catch (NotFoundException e2) {
                        }
                    }
                    if (icon == null) {
                        icon = this.mContext.getPackageManager().getDefaultActivityIcon();
                    }
                    if (icon != null) {
                        if (label != null) {
                            values.put("title", label.toString());
                            values.put("container", Long.valueOf(this.mPresetsContainerId));
                            values.put("iconPackage", pinfo.packageName);
                            values.put("spanX", Integer.valueOf(1));
                            values.put("spanY", Integer.valueOf(1));
                            values.put("cellX", Integer.valueOf(counter));
                            values.put("cellY", Integer.valueOf(0));
                            intent.setFlags(270532608);
                            intent.setDataAndType(Uri.fromFile(file), MimeUtils.guessMimeTypeFromExtension("apk"));
                            values.put("intent", intent.toUri(0));
                            values.put("itemType", Integer.valueOf(1));
                            values.put("itemFlags", Integer.valueOf(1));
                            values.put("iconType", Integer.valueOf(1));
                            ItemInfo.writeBitmap(values, Utilities.createIconBitmap(icon));
                            LauncherProvider.safelyInsertDatabase(db, "favorites", null, values);
                            values.clear();
                            counter++;
                        }
                        assmgr.close();
                    }
                }
            }
            return counter;
        }
    }

    static class SqlArguments {
        public final String[] args;
        public final long id;
        public final String table;
        public final String where;

        SqlArguments(Uri url, String where, String[] args) {
            if (url.getPathSegments().size() == 1) {
                this.table = selectTable((String) url.getPathSegments().get(0));
                this.where = where;
                this.args = args;
                this.id = -1;
            } else if (url.getPathSegments().size() != 2) {
                throw new IllegalArgumentException("Invalid URI: " + url);
            } else if (TextUtils.isEmpty(where)) {
                this.table = selectTable((String) url.getPathSegments().get(0));
                this.id = ContentUris.parseId(url);
                if ("favorites".equals(this.table)) {
                    this.where = "favorites._id=" + this.id;
                } else {
                    this.where = "screens._id=" + this.id;
                }
                this.args = null;
            } else {
                throw new UnsupportedOperationException("WHERE clause not supported: " + url);
            }
        }

        SqlArguments(Uri url) {
            if (url.getPathSegments().size() == 1) {
                this.table = selectTable((String) url.getPathSegments().get(0));
                this.where = null;
                this.args = null;
                this.id = -1;
                return;
            }
            throw new IllegalArgumentException("Invalid URI: " + url);
        }

        private String selectTable(String param) {
            return param;
        }
    }

    public Object getLock() {
        return this.mLock;
    }

    public boolean onCreate() {
        loadLaamPresetPackages();
        loadPresetItems();
        resetDatabaseIfNeeded();
        this.mOpenHelper = new DatabaseHelper(getContext());
        Application.getLauncherApplication(getContext()).setLauncherProvider(this);
        return true;
    }

    private void loadLaamPresetPackages() {
        mLaamPresetPackage.add("com.telcel.contenedor");
        mLaamPresetPackage.add("com.claroColombia.contenedor");
        mLaamPresetPackage.add("com.portal");
        mLaamPresetPackage.add("com.claro.claromusica.latam");
        mLaamPresetPackage.add("com.telcel.imk");
        mLaamPresetPackage.add("com.gameloft.android.gdc");
        mLaamPresetPackage.add("com.naranya.claroapps");
        mLaamPresetPackage.add("com.dla.android");
    }

    public void loadSkippedItems(Context context) {
        mSkippedItems.clear();
        if (!Build.IS_INTERNATIONAL_BUILD) {
            mSkippedItems.add(new ComponentName("com.google.android.gms", "com.google.android.gms.app.settings.GoogleSettingsActivity"));
            mSkippedItems.add(new ComponentName("com.google.android.gms", "com.google.android.gms.common.settings.GoogleSettingsActivity"));
        }
        mSkippedItems.add(new ComponentName("com.qualcomm.qti.modemtestmode", "com.qualcomm.qti.modemtestmode.MbnFileActivate"));
        mSkippedItems.add(new ComponentName("com.google.android.googlequicksearchbox", "com.google.android.googlequicksearchbox.SearchActivity"));
        mSkippedItems.add(new ComponentName("com.google.android.googlequicksearchbox", "com.google.android.handsfree.HandsFreeLauncherActivity"));
        mSkippedItems.add(new ComponentName("com.google.android.inputmethod.pinyin", "com.google.android.apps.inputmethod.libs.framework.core.LauncherActivity"));
        mSkippedItems.add(new ComponentName("com.opera.max.oem.xiaomi", "com.opera.max.ui.v2.MainActivity"));
        mSkippedItems.add(new ComponentName("com.google.android.inputmethod.latin", "com.android.inputmethod.latin.setup.SetupActivity"));
        if (DeviceConfig.needHideThemeManager(context)) {
            mSkippedItems.add(new ComponentName("com.android.thememanager", "com.android.thememanager.ThemeResourceTabActivity"));
        }
    }

    private void loadPresetItems() {
        mPresetItems.add(new ComponentName("com.xiaomi.market", "com.xiaomi.market.ui.MarketTabActivity"));
        mPresetItems.add(new ComponentName("com.duokan.reader", "com.duokan.reader.DkReaderActivity"));
        mPresetItems.add(new ComponentName("com.xiaomi.smarthome", "com.xiaomi.smarthome.StartupActivity"));
        mPresetItems.add(new ComponentName("com.yidian.xiaomi", "com.yidian.xiaomi.ui.guide.UserGuideActivity"));
    }

    public static List<ComponentName> getPresetItems() {
        return new ArrayList(mPresetItems);
    }

    public static boolean isSkippedItem(ComponentName cn) {
        return mSkippedItems.contains(cn);
    }

    private void resetDatabaseIfNeeded() {
        if (this.mOpenHelper != null) {
            this.mOpenHelper.close();
            this.mScreens = null;
        }
    }

    public String getType(Uri uri) {
        SqlArguments args = new SqlArguments(uri, null, null);
        if (TextUtils.isEmpty(args.where)) {
            return "vnd.android.cursor.dir/" + args.table;
        }
        return "vnd.android.cursor.item/" + args.table;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (!isReady()) {
            return null;
        }
        synchronized (this.mLock) {
            try {
                SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
                if ("preference".equals(args.table)) {
                    Cursor ret = new MatrixCursor(new String[]{"value"});
                    for (int i = 0; i < projection.length; i++) {
                        ret.addRow(new String[]{this.mOpenHelper.getPreferenceValue(projection[i])});
                    }
                    return ret;
                }
                SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
                qb.setTables(args.table);
                Cursor result = qb.query(this.mOpenHelper.getWritableDatabase(), projection, args.where, args.args, null, null, sortOrder);
                result.setNotificationUri(getContext().getContentResolver(), uri);
                return result;
            } catch (SQLiteFullException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public Uri insert(Uri uri, ContentValues initialValues) {
        synchronized (this.mLock) {
            try {
                if (isReady()) {
                    SqlArguments args = new SqlArguments(uri);
                    SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
                    if ("favorites".equals(args.table)) {
                        Integer screenId = initialValues.getAsInteger("screen");
                        Integer container = initialValues.getAsInteger("container");
                        if (screenId == null && container == null) {
                            return null;
                        } else if (screenId.intValue() != -1 && container.intValue() == -100 && getScreenInfo(loadScreens(db), (long) screenId.intValue()).screenType == 1) {
                            DeviceConfig.portraitCellPosition(initialValues);
                        }
                    }
                    long rowId = safelyInsertDatabase(db, args.table, null, initialValues);
                    if (rowId <= 0) {
                        return null;
                    }
                    uri = ContentUris.withAppendedId(uri, rowId);
                    return uri;
                }
                return null;
            } catch (SQLiteFullException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public int bulkInsert(Uri uri, ContentValues[] values) {
        int i = -1;
        synchronized (this.mLock) {
            SQLiteDatabase db;
            try {
                if (isReady()) {
                    SqlArguments args = new SqlArguments(uri);
                    db = this.mOpenHelper.getWritableDatabase();
                    db.beginTransaction();
                    for (ContentValues safelyInsertDatabase : values) {
                        if (safelyInsertDatabase(db, args.table, null, safelyInsertDatabase) < 0) {
                            db.endTransaction();
                            return 0;
                        }
                    }
                    db.setTransactionSuccessful();
                    db.endTransaction();
                    this.mOpenHelper.initializeMaxId(db);
                    return values.length;
                }
                return i;
            } catch (SQLiteFullException e) {
                e.printStackTrace();
                return i;
            } catch (Throwable th) {
                db.endTransaction();
            }
        }
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        synchronized (this.mLock) {
            try {
                if (isReady()) {
                    SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
                    SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
                    int r = safelyDeleteFromDB(db, args.table, args.where, args.args);
                    this.mOpenHelper.updateMaxId(db);
                    return r;
                }
                return -1;
            } catch (SQLiteFullException e) {
                e.printStackTrace();
                return -1;
            }
        }
    }

    public static final void safelyExecSQL(SQLiteDatabase db, String sql) {
        if (db != null) {
            try {
                db.execSQL(sql);
            } catch (SQLiteException e) {
                Log.d("Launcher.LauncherProvider", "exec sql fail", e);
            }
        }
    }

    public static final int safelyDeleteFromDB(SQLiteDatabase db, String table, String whereClause, String[] whereArgs) {
        if (db == null) {
            return 0;
        }
        int i = 0;
        try {
            return db.delete(table, whereClause, whereArgs);
        } catch (SQLiteException e) {
            Log.d("Launcher.LauncherProvider", "delete from db fail", e);
            return i;
        }
    }

    public static final int safelyUpdateDatabase(SQLiteDatabase db, String table, ContentValues values, String whereClause, String[] whereArgs) {
        if (db == null) {
            return 0;
        }
        int i = 0;
        try {
            return db.update(table, values, whereClause, whereArgs);
        } catch (SQLiteException e) {
            Log.d("Launcher.LauncherProvider", "update db fail", e);
            return i;
        }
    }

    public static final long safelyInsertDatabase(SQLiteDatabase db, String table, String nullColumnHack, ContentValues values) {
        if (db == null) {
            return -1;
        }
        long j = -1;
        try {
            return db.insert(table, nullColumnHack, values);
        } catch (SQLiteException e) {
            Log.d("Launcher.LauncherProvider", "insert to db fail", e);
            return j;
        }
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int i;
        synchronized (this.mLock) {
            SQLiteDatabase db;
            try {
                if (isReady()) {
                    SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
                    db = this.mOpenHelper.getWritableDatabase();
                    if ("packages".equals(args.table)) {
                        String packageName = (String) values.get("name");
                        if (Boolean.TRUE.equals(values.getAsBoolean("delete"))) {
                            ScreenUtils.removePackage(getContext(), db, packageName, values.getAsLong("profileId").longValue());
                        }
                        this.mOpenHelper.updateMaxId(db);
                        i = 0;
                    } else {
                        if ("screens".equals(args.table)) {
                            ArrayList<String> bulkValues = values.getStringArrayList("screenOrder");
                            if (bulkValues == null) {
                                Log.e("Launcher.LauncherProvider", "Invalid resorder request: " + (bulkValues == null ? "null" : bulkValues.toString()));
                                i = 0;
                            } else {
                                i = 0;
                                db.beginTransaction();
                                for (int order = 0; order < bulkValues.size(); order++) {
                                    ContentValues update = new ContentValues();
                                    update.put("screenOrder", Integer.valueOf(i));
                                    i += safelyUpdateDatabase(db, args.table, update, "_id=?", new String[]{(String) bulkValues.get(order)});
                                }
                                db.setTransactionSuccessful();
                                db.endTransaction();
                                this.mScreens = null;
                            }
                        } else {
                            if ("favorites".equals(args.table) && selection == null && values != null) {
                                Long container = values.getAsLong("container");
                                Long sid = values.getAsLong("screen");
                                if (sid != null) {
                                    ScreenInfo si = getScreenInfo(loadScreens(db), sid.longValue());
                                    if (si != null && si.screenType == 1) {
                                        DeviceConfig.portraitCellPosition(values);
                                    }
                                }
                            }
                            i = safelyUpdateDatabase(db, args.table, values, args.where, args.args);
                        }
                    }
                } else {
                    i = -1;
                }
            } catch (SQLiteFullException e) {
                e.printStackTrace();
                i = -1;
            } catch (Throwable th) {
                db.endTransaction();
            }
        }
        return i;
    }

    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        SQLiteDatabase db;
        try {
            if (!isReady()) {
                return null;
            }
            ContentProviderResult[] result;
            synchronized (this.mLock) {
                db = this.mOpenHelper.getWritableDatabase();
                db.beginTransaction();
                result = super.applyBatch(operations);
                db.setTransactionSuccessful();
                db.endTransaction();
            }
            return result;
        } catch (SQLiteFullException e) {
            e.printStackTrace();
            return null;
        } catch (Throwable th) {
            db.endTransaction();
        }
    }

    public Bundle call(String method, String request, Bundle args) {
        if (method.equals("dumpDefaultWorkspace")) {
            this.mOpenHelper.dumpDefaultWorkspace(Integer.parseInt(request));
        }
        boolean result;
        Bundle bundle;
        if (method.equals("isInRecommendFolder") || method.equals("isInSysToolFolder")) {
            result = false;
            String componentName = args.getString("componentName");
            if (!TextUtils.isEmpty(componentName)) {
                SQLiteDatabase db = this.mOpenHelper.getReadableDatabase();
                long itemId = DatabaseHelper.queryInstalledComponentId(db, componentName);
                if (itemId != -1) {
                    String folderTitle = null;
                    if (method.equals("isInRecommendFolder")) {
                        folderTitle = "com.miui.home:string/default_folder_title_recommend";
                    } else if (method.equals("isInSysToolFolder")) {
                        folderTitle = "com.miui.home:string/default_folder_title_tools";
                    }
                    result = DatabaseHelper.queryItemInFolder(db, itemId, folderTitle);
                }
            }
            bundle = new Bundle();
            bundle.putBoolean("result_boolean", result);
            return bundle;
        } else if ("setLockWallpaperAuthority".equals(method)) {
            result = System.putString(getContext().getContentResolver(), "lock_wallpaper_provider_authority", request);
            String pkg = getCallingPackage();
            Log.d("Launcher.LauncherProvider", "set authority " + request + " by " + pkg);
            if ("com.mfashiongallery.emag".equals(pkg)) {
                WallpaperUtils.setProviderClosedByUser(getContext().getApplicationContext(), TextUtils.isEmpty(request));
            }
            bundle = new Bundle();
            bundle.putBoolean("result_boolean", result);
            return bundle;
        } else {
            String path;
            if ("getWallPaperPath".equals(method)) {
                path = WallpaperUtils.getWallpaperSourcePath("pref_key_current_wallpaper_path");
                if (!TextUtils.isEmpty(path)) {
                    bundle = new Bundle();
                    bundle.putString("result_string", path);
                    return bundle;
                }
            } else if ("getLockScreenPath".equals(method)) {
                context = getContext().getApplicationContext();
                path = context.getSharedPreferences(context.getPackageName() + "_world_readable_preferences", DeviceConfig.TEMP_SHARE_MODE_FOR_WORLD_READABLE).getString("pref_key_lock_wallpaper_path", "");
                if (!TextUtils.isEmpty(path)) {
                    bundle = new Bundle();
                    bundle.putString("result_string", path);
                    return bundle;
                }
            } else if ("getLockWallpaperInfo".equals(method)) {
                String currentWallpaperInfo = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext()).getString("currentWallpaperInfo", null);
                if (!TextUtils.isEmpty(currentWallpaperInfo)) {
                    bundle = new Bundle();
                    bundle.putString("result_json", currentWallpaperInfo);
                    return bundle;
                }
            } else if ("getHomePreview".equals(method)) {
                int wallpaperColorMode = args.getInt("wallpaperColorMode");
                context = getContext().getApplicationContext();
                Bitmap homePreview = Utilities.createBitmapSafely(DeviceConfig.getDeviceWidth(), DeviceConfig.getDeviceHeight(), Config.ARGB_8888);
                WallpaperUtils.correctHomeScreenPreview(wallpaperColorMode, homePreview);
                bundle = new Bundle();
                bundle.putByteArray("result_bitmap", Utilities.flattenBitmap(homePreview));
                return bundle;
            } else if ("getScreenCellsOptionList".equals(method)) {
                ArrayList<CharSequence> result2 = ScreenUtils.getScreenCellsSizeOptions(getContext());
                bundle = new Bundle();
                bundle.putCharSequenceArrayList("result_charsequence_arraylist", result2);
                return bundle;
            } else if ("getLockScreenPreview".equals(method)) {
                Bitmap lockScreenSnapshot = MamlTools.snapshootLockscreen(getContext().getApplicationContext(), args.getInt("wallpaperColorMode"));
                bundle = new Bundle();
                bundle.putByteArray("result_bitmap", Utilities.flattenBitmap(lockScreenSnapshot));
                return bundle;
            }
            return null;
        }
    }

    private ArrayList<ScreenInfo> loadScreens(SQLiteDatabase db) {
        if (this.mScreens == null) {
            this.mScreens = ScreenUtils.loadScreens(db);
        }
        return this.mScreens;
    }

    static ScreenInfo getScreenInfo(ArrayList<ScreenInfo> screens, long id) {
        if (screens != null) {
            Iterator i$ = screens.iterator();
            while (i$.hasNext()) {
                ScreenInfo si = (ScreenInfo) i$.next();
                if (id == si.screenId) {
                    return si;
                }
            }
        }
        return null;
    }

    public long generateNewId() {
        return this.mOpenHelper.generateNewId();
    }

    public boolean isReady() {
        return this.mOpenHelper.isDatabaseReady();
    }

    public void loadDefaultWorkspace() {
        this.mOpenHelper.loadDefaultWorkspace();
    }

    public long queryIdByTitle(String title) {
        return DatabaseHelper.queryIdByTitle(this.mOpenHelper.getWritableDatabase(), title);
    }

    public long queryInstalledComponentId(String componentName) {
        return DatabaseHelper.queryInstalledComponentId(this.mOpenHelper.getWritableDatabase(), componentName);
    }

    public int queryFolderSize(long id) {
        return this.mOpenHelper.queryFolderSize(this.mOpenHelper.getWritableDatabase(), id);
    }
}
