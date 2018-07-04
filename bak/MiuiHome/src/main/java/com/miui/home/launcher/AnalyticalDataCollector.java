package com.miui.home.launcher;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import com.xiaomi.mistatistic.sdk.CustomSettings;
import com.xiaomi.mistatistic.sdk.MiStatInterface;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import miui.analytics.Analytics;
import miui.os.Build;

public class AnalyticalDataCollector extends BroadcastReceiver {
    public static final String EVENT_ID_FOLDER_OPEN = ("home_folder_open" + EVENT_ID_LOCALE_SUFFIX);
    public static final String EVENT_ID_GADGET_CLICK = ("gadget_click" + EVENT_ID_LOCALE_SUFFIX);
    public static final String EVENT_ID_GADGET_VIEW = ("gadget_view" + EVENT_ID_LOCALE_SUFFIX);
    public static final String EVENT_ID_LOCALE_SUFFIX = (Build.IS_INTERNATIONAL_BUILD ? "_global" : "_cn");
    public static final String EVENT_ID_RECOMMEND_APP = ("home_recommend_app" + EVENT_ID_LOCALE_SUFFIX);

    public static void init(Context context) {
        MiStatInterface.initialize(context, "1000271", "420100086271", "channel");
        MiStatInterface.setUploadPolicy(1, 0);
        CustomSettings.setUseSystemStatService(true);
        MiStatInterface.enableExceptionCatcher(false);
    }

    public static void trackMusicEvent(Context context, String command, String pkg) {
        Map<String, String> parameters = new HashMap();
        parameters.put("command", command);
        parameters.put("pkg", pkg);
        Analytics xiaomiAnalytics = Analytics.getInstance();
        xiaomiAnalytics.startSession(context);
        xiaomiAnalytics.trackEvent("gadget_track_music", parameters);
        xiaomiAnalytics.endSession();
    }

    public static void setWallpaperEntryType(Context context, String type) {
        MiStatInterface.recordCountEvent("home_set_wallpaper_entry_type", type);
    }

    public static void registerAnalyticalAlarm(Context context) {
        ((AlarmManager) context.getSystemService("alarm")).setRepeating(3, SystemClock.elapsedRealtime(), 86400000, PendingIntent.getBroadcast(context, 0, new Intent(context, AnalyticalDataCollector.class), 268435456));
    }

    public static void setRecommendSwitchChanged(Context context, String type) {
        Analytics xiaomiAnalytics = Analytics.getInstance();
        xiaomiAnalytics.startSession(context);
        xiaomiAnalytics.trackEvent("recommend_switch_behavior", type);
        xiaomiAnalytics.endSession();
    }

    public static void trackChildSettingEnter() {
        MiStatInterface.recordCountEvent("home_child_setting", "enter");
    }

    public static void trackChildAppNum(int num) {
        MiStatInterface.recordCalculateEvent("home_child_setting", "app_num", (long) num);
    }

    public static void trackChildRemainTime(long millSeconds) {
        MiStatInterface.recordCalculateEvent("home_child_setting", "remain", millSeconds / 1000);
    }

    public static void trackEditMode(String type) {
        MiStatInterface.recordCountEvent("home_enter_edit_mode", type);
    }

    public static void trackFolderOpenWithRecommend(String recommendStatus) {
        MiStatInterface.recordCountEvent(EVENT_ID_FOLDER_OPEN, "open_with_recommend_" + recommendStatus);
    }

    public static void trackFolderRecommendAppClick() {
        MiStatInterface.recordCountEvent(EVENT_ID_RECOMMEND_APP, "click");
    }

    public static void trackFolderRecommendAppRefresh() {
        MiStatInterface.recordCountEvent(EVENT_ID_RECOMMEND_APP, "refresh");
    }

    public static void trackForceTouchAdaptedApp(String packageName) {
        MiStatInterface.recordCountEvent("home_force_touch_adapted_app", packageName);
    }

    public static void trackEditingEntryClicked(String itemName) {
        MiStatInterface.recordCountEvent("editing_entry_be_clicked", itemName);
    }

    public static void trackAddWidget() {
        MiStatInterface.recordCountEvent("add_widget", "to_workspace");
    }

    public static void trackWallpaperChanged(String changer) {
        MiStatInterface.recordCountEvent("wallpaper_changed", changer);
    }

    public static void trackLockWallpaperChanged(String changer) {
        MiStatInterface.recordCountEvent("lock_wallpaper_changed", changer);
    }

    public static void trackTransitionEffectChanged(String effect) {
        MiStatInterface.recordCountEvent("transition_effect_changed", effect);
    }

    public static void trackScreenCellsSizeChanged(String currentSize) {
        MiStatInterface.recordCountEvent("screen_cells_size_changed", currentSize);
    }

    public static void trackScreenCellsSize(String deviceName, String currentSize) {
        MiStatInterface.recordCountEvent("screen_cells_size_" + deviceName, currentSize);
    }

    public static void trackUsingMultiSelect() {
        MiStatInterface.recordCountEvent("use_multi_select", "multi_select");
    }

    public static void trackGadgetView(String title) {
        MiStatInterface.recordCountEvent(EVENT_ID_GADGET_VIEW, title);
    }

    public static void trackGadgetClick(String title) {
        MiStatInterface.recordCountEvent(EVENT_ID_GADGET_CLICK, title);
    }

    public static void trackItemMoved(String eventID, String type) {
        MiStatInterface.recordCountEvent(eventID, type);
    }

    public static void enterFreeStyleEditMode(Context context) {
    }

    public static void enterFreeStyleMoveMode(Context context) {
    }

    public static void enterFreeStyle(Context context, String freeStyleName) {
    }

    public static void exitFreeStyle(Context context) {
    }

    public static void launchApps(Context context, Collection<ItemInfo> collection) {
    }

    public static void clickToggle(Context context, ShortcutInfo info) {
    }

    public static void launcherShortcutWidget(Context context, Intent intent) {
    }

    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent();
        serviceIntent.setClass(context, AnalyticalDataCollectorService.class);
        context.startService(serviceIntent);
    }
}
