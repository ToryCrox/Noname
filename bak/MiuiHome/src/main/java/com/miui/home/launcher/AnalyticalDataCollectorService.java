package com.miui.home.launcher;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.miui.home.launcher.common.Utilities;
import com.miui.home.launcher.gadget.Gadget;
import com.miui.home.launcher.gadget.GadgetInfo;
import com.xiaomi.mistatistic.sdk.MiStatInterface;
import java.util.ArrayList;
import java.util.Iterator;
import miui.os.Build;

public class AnalyticalDataCollectorService extends IntentService {
    private static final String EVENT_ID_FAVORITE_GADGET_AND_WIDGETS = ("home_favorite_gadget_and_widgets" + AnalyticalDataCollector.EVENT_ID_LOCALE_SUFFIX);

    private static java.util.ArrayList<java.lang.String> getWidgets(android.content.Context r12) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x005a in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:42)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
*/
        /*
        r10 = 0;
        r8 = 0;
        r0 = r12.getContentResolver();	 Catch:{ Exception -> 0x0054, all -> 0x0062 }
        r1 = com.miui.home.launcher.LauncherSettings.Favorites.CONTENT_URI;	 Catch:{ Exception -> 0x0054, all -> 0x0062 }
        r2 = com.miui.home.launcher.ItemQuery.COLUMNS;	 Catch:{ Exception -> 0x0054, all -> 0x0062 }
        r3 = "itemType = ?";	 Catch:{ Exception -> 0x0054, all -> 0x0062 }
        r4 = 1;	 Catch:{ Exception -> 0x0054, all -> 0x0062 }
        r4 = new java.lang.String[r4];	 Catch:{ Exception -> 0x0054, all -> 0x0062 }
        r5 = 0;	 Catch:{ Exception -> 0x0054, all -> 0x0062 }
        r11 = 4;	 Catch:{ Exception -> 0x0054, all -> 0x0062 }
        r11 = java.lang.String.valueOf(r11);	 Catch:{ Exception -> 0x0054, all -> 0x0062 }
        r4[r5] = r11;	 Catch:{ Exception -> 0x0054, all -> 0x0062 }
        r5 = 0;	 Catch:{ Exception -> 0x0054, all -> 0x0062 }
        r8 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x0054, all -> 0x0062 }
        if (r8 == 0) goto L_0x0024;	 Catch:{ Exception -> 0x0054, all -> 0x0062 }
    L_0x001e:
        r0 = r8.getCount();	 Catch:{ Exception -> 0x0054, all -> 0x0062 }
        if (r0 != 0) goto L_0x002b;
    L_0x0024:
        if (r8 == 0) goto L_0x0029;
    L_0x0026:
        r8.close();
    L_0x0029:
        r9 = r10;
    L_0x002a:
        return r9;
    L_0x002b:
        r9 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0054, all -> 0x0062 }
        r9.<init>();	 Catch:{ Exception -> 0x0054, all -> 0x0062 }
    L_0x0030:
        r0 = r8.moveToNext();	 Catch:{ Exception -> 0x0054, all -> 0x0062 }
        if (r0 == 0) goto L_0x005c;	 Catch:{ Exception -> 0x0054, all -> 0x0062 }
    L_0x0036:
        r6 = android.appwidget.AppWidgetManager.getInstance(r12);	 Catch:{ Exception -> 0x0054, all -> 0x0062 }
        r0 = com.miui.home.launcher.ItemQuery.COLUMNS;	 Catch:{ Exception -> 0x0054, all -> 0x0062 }
        r1 = 9;	 Catch:{ Exception -> 0x0054, all -> 0x0062 }
        r0 = r0[r1];	 Catch:{ Exception -> 0x0054, all -> 0x0062 }
        r0 = r8.getColumnIndex(r0);	 Catch:{ Exception -> 0x0054, all -> 0x0062 }
        r0 = r8.getInt(r0);	 Catch:{ Exception -> 0x0054, all -> 0x0062 }
        r7 = r6.getAppWidgetInfo(r0);	 Catch:{ Exception -> 0x0054, all -> 0x0062 }
        if (r7 == 0) goto L_0x0030;	 Catch:{ Exception -> 0x0054, all -> 0x0062 }
    L_0x004e:
        r0 = r7.label;	 Catch:{ Exception -> 0x0054, all -> 0x0062 }
        r9.add(r0);	 Catch:{ Exception -> 0x0054, all -> 0x0062 }
        goto L_0x0030;
    L_0x0054:
        r0 = move-exception;
        if (r8 == 0) goto L_0x005a;
    L_0x0057:
        r8.close();
    L_0x005a:
        r9 = r10;
        goto L_0x002a;
    L_0x005c:
        if (r8 == 0) goto L_0x002a;
    L_0x005e:
        r8.close();
        goto L_0x002a;
    L_0x0062:
        r0 = move-exception;
        if (r8 == 0) goto L_0x0068;
    L_0x0065:
        r8.close();
    L_0x0068:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.miui.home.launcher.AnalyticalDataCollectorService.getWidgets(android.content.Context):java.util.ArrayList<java.lang.String>");
    }

    public AnalyticalDataCollectorService() {
        super("AnalyticalDataCollectorService");
    }

    protected void onHandleIntent(Intent intent) {
        Context context = getApplicationContext();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (!Utilities.isChildrenModeEnabled(context)) {
            Launcher launcher = Application.getLauncherApplication(context).getLauncher();
            if (launcher != null) {
                Iterator i$;
                int defaultFolderNum = 0;
                int userCreatedFolderNum = 0;
                int recommendSwitchOnFolderNum = 0;
                ArrayList<FolderInfo> folderInfoArrayList = launcher.getAllFolders();
                String folderContent = "";
                if (folderInfoArrayList != null) {
                    i$ = folderInfoArrayList.iterator();
                    while (i$.hasNext()) {
                        FolderInfo folderInfo = (FolderInfo) i$.next();
                        if (!(folderInfo == null || folderInfo.getTitle(null) == null)) {
                            if (folderInfo.getTitle(null).toString().equals("com.miui.home:string/default_folder_title_recommend") || folderInfo.getTitle(null).toString().equals("com.miui.home:string/default_folder_title_tools")) {
                                defaultFolderNum++;
                            } else {
                                userCreatedFolderNum++;
                            }
                            String items = "";
                            for (int index = 0; index < folderInfo.contents.size(); index++) {
                                items = items + ((ShortcutInfo) folderInfo.contents.get(index)).getClassName() + ",";
                            }
                            folderContent = folderContent + folderInfo.getTitle(context) + ":" + items + " ";
                            if (folderInfo.isRecommendAppsViewEnable(context)) {
                                recommendSwitchOnFolderNum++;
                            }
                        }
                    }
                }
                MiStatInterface.recordCalculateEvent("home_folder_num", "default", (long) defaultFolderNum);
                MiStatInterface.recordCalculateEvent("home_folder_num", "user_created", (long) userCreatedFolderNum);
                MiStatInterface.recordStringPropertyEvent("home_folder", "home_folder_content", folderContent);
                MiStatInterface.recordCalculateEvent("home_recommend_switch_state", "trun on", (long) recommendSwitchOnFolderNum);
                Workspace workspace = launcher.getWorkspace();
                if (workspace != null) {
                    MiStatInterface.recordCountEvent("home_screen_num", workspace.getScreenCount() + "");
                    recordChangedDefaultScreen(context, workspace);
                }
                ArrayList<ShortcutInfo> toggleArrayList = new ArrayList();
                ArrayList<ShortcutInfo> shortcutInfoArrayList = launcher.getAllLoadedApps();
                int appNum = 0;
                if (shortcutInfoArrayList != null) {
                    i$ = shortcutInfoArrayList.iterator();
                    while (i$.hasNext()) {
                        ShortcutInfo shortcutInfo = (ShortcutInfo) i$.next();
                        if (shortcutInfo != null) {
                            if (shortcutInfo.itemType == 0) {
                                appNum++;
                            }
                            if (shortcutInfo.mIconType == 3 || shortcutInfo.mIconType == 5) {
                                toggleArrayList.add(shortcutInfo);
                            }
                        }
                    }
                }
                MiStatInterface.recordCalculateEvent("home_app_num", "", (long) appNum);
                ArrayList<Gadget> gadgetArrayList = launcher.getAllGadgets();
                ArrayList<GadgetInfo> gadgetInfos = new ArrayList();
                if (gadgetArrayList != null) {
                    i$ = gadgetArrayList.iterator();
                    while (i$.hasNext()) {
                        GadgetInfo gadgetInfo = (GadgetInfo) ((Gadget) i$.next()).getTag();
                        if (!(gadgetInfo == null || gadgetInfo.getGadgetId() == 4)) {
                            gadgetInfos.add(gadgetInfo);
                        }
                    }
                }
                recordGadgetsAndWidgets(gadgetInfos, getWidgets(context), toggleArrayList, context);
                recordLockWallpaperProvider(context);
            }
            DeviceConfig.recordCurrentScreenCells();
            recordScreenCellsConfig(context);
        }
    }

    private static void recordScreenCellsConfig(Context context) {
        MiStatInterface.recordCountEvent("home_screen_cells_locked", String.valueOf(Utilities.isScreenCellsLocked(context)));
        MiStatInterface.recordCountEvent("home_screen_enable_auto_fill_empty", String.valueOf(Utilities.enableAutoFillEmpty(context)));
    }

    private static void recordChangedDefaultScreen(Context context, Workspace workspace) {
        if (context != null && workspace != null) {
            long defaultScreenId = DeviceConfig.getDesignedDefaultScreenId(context);
            if (defaultScreenId == DeviceConfig.INVALIDATE_DEFAULT_SCREEN_ID) {
                return;
            }
            if (defaultScreenId != workspace.getScreenIdByIndex(workspace.getDefaultScreenIndex())) {
                MiStatInterface.recordCountEvent("home_change_default_screen", "changed");
            } else {
                MiStatInterface.recordCountEvent("home_change_default_screen", "no_changed");
            }
        }
    }

    private static void recordGadgetsAndWidgets(ArrayList<GadgetInfo> gadgetInfos, ArrayList<String> widgetNames, ArrayList<ShortcutInfo> toggles, Context context) {
        Iterator i$;
        int i = 0;
        int toggleNum = toggles == null ? 0 : toggles.size();
        int widgetAndGadgetNum = 0 + (gadgetInfos == null ? 0 : gadgetInfos.size());
        if (widgetNames != null) {
            i = widgetNames.size();
        }
        widgetAndGadgetNum += i;
        if (toggleNum == 0 && widgetAndGadgetNum == 0) {
            MiStatInterface.recordCountEvent("home_gadget_and_widgets", "no_toggle_no_widget");
        }
        if (toggleNum > 0 && widgetAndGadgetNum > 0) {
            MiStatInterface.recordCountEvent("home_gadget_and_widgets", "add_toggle_add_widget");
        }
        if (toggleNum > 0 && widgetAndGadgetNum == 0) {
            MiStatInterface.recordCountEvent("home_gadget_and_widgets", "add_toggle_no_widget");
        }
        if (toggleNum == 0 && widgetAndGadgetNum > 0) {
            MiStatInterface.recordCountEvent("home_gadget_and_widgets", "no_toggle_add_widget");
        }
        if (gadgetInfos != null) {
            i$ = gadgetInfos.iterator();
            while (i$.hasNext()) {
                GadgetInfo gadgetInfo = (GadgetInfo) i$.next();
                if (gadgetInfo != null) {
                    String title = gadgetInfo.getTitle(context);
                    String str = EVENT_ID_FAVORITE_GADGET_AND_WIDGETS;
                    if (title == null) {
                        title = "";
                    }
                    MiStatInterface.recordCountEvent(str, title);
                }
            }
        }
        if (widgetNames != null) {
            i$ = widgetNames.iterator();
            while (i$.hasNext()) {
                MiStatInterface.recordCountEvent(EVENT_ID_FAVORITE_GADGET_AND_WIDGETS, (String) i$.next());
            }
        }
        if (toggles != null) {
            i$ = toggles.iterator();
            while (i$.hasNext()) {
                ShortcutInfo shortcutInfo = (ShortcutInfo) i$.next();
                if (shortcutInfo != null) {
                    MiStatInterface.recordCountEvent(EVENT_ID_FAVORITE_GADGET_AND_WIDGETS, shortcutInfo.getTitle(context) == null ? "" : shortcutInfo.getTitle(context).toString());
                }
            }
        }
    }

    private static void recordLockWallpaperProvider(Context context) {
        if (context != null && !Build.IS_TABLET) {
            String type = WallpaperUtils.getLockWallpaperProvider(context);
            if (WallpaperUtils.isKeyguardShowLiveWallpaper()) {
                type = "live";
            } else if (!WallpaperUtils.isDefaultLockStyle()) {
                type = "third_theme";
            }
            MiStatInterface.recordStringPropertyEvent("home_lock_wallpaper", "home_lock_wallpaper_provider", type);
        }
    }
}
