package com.miui.home.launcher;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Shader.TileMode;
import android.miui.Shell;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Log;
import com.miui.home.R;
import com.miui.home.launcher.ProgressManager.ProgressProcessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ApplicationProgressProcessor implements ProgressProcessor {
    private static Paint sPaint = new Paint();
    private static LightingColorFilter sProgressFilter = new LightingColorFilter(8421504, 0);
    private final HashMap<String, ArrayList<String>> mAllServersProgressMap = new HashMap();
    private final HashMap<String, JSONObject> mAllServersStatusTitleMap = new HashMap();
    private String mCloudBackupPackageName = "";
    private final HashMap<String, ShortcutInfo> mCurrentProgressingMap = new HashMap();
    private Runnable mFinishWaitingCallback;
    private boolean mIsStop = false;
    private Launcher mLauncher;
    private ProgressInfoList mProgressInfoList;
    private final HashMap<String, Long> mServerChangedValues = new HashMap();
    private final HashMap<String, Integer> mServerNoObservingCount = new HashMap();
    private boolean mWaitingForCloudAppBackup = false;

    public ApplicationProgressProcessor(Context context) {
        if (this.mProgressInfoList == null) {
            this.mProgressInfoList = new ProgressInfoList(context);
            this.mProgressInfoList.getStatusTitleMap(this.mAllServersStatusTitleMap);
        }
    }

    void setLauncher(Launcher launcher) {
        this.mLauncher = launcher;
    }

    public static void drawProgressIcon(Context context, Canvas canvas, Bitmap icon, int progressPercent) {
        sPaint.setShader(new BitmapShader(icon, TileMode.CLAMP, TileMode.CLAMP));
        sPaint.setColorFilter(null);
        int top = (int) ((((float) icon.getHeight()) * ((float) progressPercent)) / 100.0f);
        canvas.drawRect(0.0f, 0.0f, (float) icon.getWidth(), (float) top, sPaint);
        sPaint.setColorFilter(sProgressFilter);
        canvas.drawRect(0.0f, (float) top, (float) icon.getWidth(), (float) icon.getHeight(), sPaint);
    }

    public void handleProgressUpdate(Context context, Intent intent) {
        String action = intent.getAction();
        String senderPackageName = intent.getStringExtra("com.miui.home.extra.server_name");
        if (TextUtils.isEmpty(senderPackageName)) {
            senderPackageName = intent.getSender();
        }
        try {
            this.mServerNoObservingCount.put(senderPackageName, Integer.valueOf(0));
            if ("android.intent.action.APPLICATION_PROGRESS_UPDATE".equals(action)) {
                if (intent.getStringArrayExtra("android.intent.extra.update_application_progress_title") != null) {
                    long checkCode = intent.getLongExtra("android.intent.extra.update_progress_check_code", 0);
                    if (checkCode == ProgressManager.getManager(this.mLauncher).getCheckCode() || checkCode == 0) {
                        updateProgress(intent.getStringArrayExtra("android.intent.extra.update_progress_key"), intent.getStringArrayExtra("android.intent.extra.update_application_progress_title"), intent.getIntArrayExtra("android.intent.extra.update_progress_status"), intent.getStringArrayExtra("android.intent.extra.update_application_progress_icon_uri"), senderPackageName);
                    }
                } else if (ProgressManager.isServerEnableShareProgressStatus(context, senderPackageName)) {
                    String packageName = intent.getStringExtra("android.intent.extra.update_progress_key");
                    String titleJson = intent.getStringExtra("android.intent.extra.update_progress_status_title_map");
                    int status = intent.getIntExtra("android.intent.extra.update_progress_status", -1000);
                    String uri = intent.getStringExtra("android.intent.extra.update_application_progress_icon_uri");
                    if (!TextUtils.isEmpty(titleJson)) {
                        updateStatusTitleMap(senderPackageName, titleJson);
                    }
                    updateProgress(senderPackageName, packageName, status, uri, false);
                }
            }
        } catch (NullPointerException ex) {
            Log.w("Launcher.ApplicationProgressManager", "problem while receiving progress info", ex);
        }
    }

    private void updateStatusTitleMap(String server, String titleJson) {
        try {
            JSONObject json = new JSONObject(titleJson);
            if (!this.mAllServersStatusTitleMap.containsKey(server) || !((JSONObject) this.mAllServersStatusTitleMap.get(server)).toString().equals(titleJson)) {
                this.mAllServersStatusTitleMap.put(server, json);
                this.mProgressInfoList.recordStatusTitleMap(server, titleJson);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i("Launcher.ApplicationProgressManager", "server " + server + " send wrong title");
        }
    }

    public void clear() {
        synchronized (this.mAllServersProgressMap) {
            this.mCurrentProgressingMap.clear();
            this.mAllServersProgressMap.clear();
        }
    }

    public void onLoadingFinished() {
        ArrayList<String> allServers = new ArrayList(this.mAllServersProgressMap.keySet());
        if (!allServers.isEmpty()) {
            Iterator i$ = allServers.iterator();
            while (i$.hasNext()) {
                String server = (String) i$.next();
                ProgressManager.getManager(this.mLauncher).queryProgressByBroadcast(this.mLauncher, server, (String[]) ((ArrayList) this.mAllServersProgressMap.get(server)).toArray(new String[0]));
            }
        }
    }

    public void stop() {
        this.mIsStop = true;
    }

    public void start() {
        this.mIsStop = false;
        final ArrayList<String> allServers = new ArrayList(this.mAllServersProgressMap.keySet());
        if (!allServers.isEmpty()) {
            new AsyncTask<Void, Void, Void>() {
                protected Void doInBackground(Void... params) {
                    List<RunningAppProcessInfo> list = ((ActivityManager) ApplicationProgressProcessor.this.mLauncher.getSystemService("activity")).getRunningAppProcesses();
                    ArrayList<String> notExistServers = new ArrayList(allServers);
                    for (RunningAppProcessInfo info : list) {
                        Iterator i$ = allServers.iterator();
                        while (i$.hasNext()) {
                            String server = (String) i$.next();
                            if (info.processName.equals(server)) {
                                notExistServers.remove(server);
                                break;
                            }
                        }
                    }
                    Iterator it = notExistServers.iterator();
                    while (it.hasNext()) {
                        server = (String) it.next();
                        if (ApplicationProgressProcessor.this.mAllServersProgressMap.get(server) != null) {
                            ProgressManager.getManager(ApplicationProgressProcessor.this.mLauncher).queryProgressByBroadcast(ApplicationProgressProcessor.this.mLauncher, server, (String[]) ((ArrayList) ApplicationProgressProcessor.this.mAllServersProgressMap.get(server)).toArray(new String[0]));
                        }
                    }
                    return null;
                }
            }.execute(new Void[0]);
        }
    }

    public boolean isStop() {
        return this.mIsStop;
    }

    public ArrayList<String> getAllProgressKeys() {
        return this.mProgressInfoList.getAllProgressPackages();
    }

    public boolean bindProgressItem(ItemInfo info, boolean isRecorded) {
        if (!(info instanceof ShortcutInfo)) {
            return false;
        }
        ShortcutInfo shortcut = (ShortcutInfo) info;
        if (!isRecorded && info.id != -1) {
            onNewProgressStart(shortcut.getAppInfo().pkgName, shortcut.progressTitle, shortcut.getAppInfo().iconUri, shortcut.appProgressServer, false);
            return true;
        } else if (!this.mProgressInfoList.getProgressInfo(shortcut.getPackageName(), shortcut)) {
            return false;
        } else {
            shortcut.progressStatus = -1;
            shortcut.progressTitle = mapStatusToTitle(shortcut.progressStatus, shortcut.appProgressServer);
            if (shortcut.id != -1) {
                addProgressingInfo(shortcut);
                return true;
            } else if (this.mCurrentProgressingMap.containsKey(shortcut.getPackageName())) {
                return false;
            } else {
                return onNewProgressStart(shortcut.getPackageName(), shortcut.progressTitle, shortcut.getAppInfo().iconUri, shortcut.appProgressServer, false);
            }
        }
    }

    private boolean onNewProgressStart(String packageName, String title, Uri iconUri, String server, boolean isLast) {
        ShortcutInfo info = this.mLauncher.getFirstAppInfo(packageName, true);
        if (!ScreenUtils.isAlreadyInstalled(packageName, this.mLauncher) && info == null) {
            return addNewProgressItem(packageName, title, iconUri, server, isLast);
        }
        if (info == null || "com.miui.cloudbackup".equals(server)) {
            return false;
        }
        return addUpdatingProgressItem(info, packageName, title, iconUri, server, isLast);
    }

    public boolean isEmpty() {
        return this.mAllServersProgressMap.isEmpty();
    }

    public void removeProgressingInfo(String packageName) {
        synchronized (this.mAllServersProgressMap) {
            if (this.mProgressInfoList.containsAppProgress(packageName)) {
                this.mProgressInfoList.removeProgressInfo(packageName);
            }
            if (this.mCurrentProgressingMap.containsKey(packageName)) {
                ShortcutInfo info = (ShortcutInfo) this.mCurrentProgressingMap.get(packageName);
                this.mCurrentProgressingMap.remove(packageName);
                ArrayList<String> list = (ArrayList) this.mAllServersProgressMap.get(info.appProgressServer);
                if (list != null) {
                    list.remove(info.getPackageName());
                    if (list.isEmpty()) {
                        this.mAllServersProgressMap.remove(info.appProgressServer);
                        this.mServerChangedValues.remove(info.appProgressServer);
                    }
                }
            }
        }
    }

    private boolean addUpdatingProgressItem(ShortcutInfo info, String packageName, String title, Uri iconUri, String server, boolean isLast) {
        if (this.mLauncher == null) {
            return false;
        }
        info.getAppInfo().pkgName = packageName;
        info.progressStatus = -1;
        info.appProgressServer = server;
        addProgressingInfo(info);
        final ShortcutInfo shortcutInfo = info;
        final String str = title;
        final boolean z = isLast;
        final String str2 = server;
        this.mLauncher.getWorkspace().post(new Runnable() {
            public void run() {
                if (shortcutInfo.getBuddyIconView() != null) {
                    shortcutInfo.progressTitle = str;
                    shortcutInfo.getBuddyIconView().updateInfo(ApplicationProgressProcessor.this.mLauncher, shortcutInfo);
                    if (z && ApplicationProgressProcessor.this.isWaitingFor(str2)) {
                        ApplicationProgressProcessor.this.finishWaiting();
                        if (ApplicationProgressProcessor.this.mFinishWaitingCallback != null) {
                            ApplicationProgressProcessor.this.mFinishWaitingCallback.run();
                        }
                    }
                }
            }
        });
        return true;
    }

    private boolean addNewProgressItem(String packageName, String title, Uri iconUri, String server, boolean isLast) {
        Intent intent = new Intent();
        intent.putExtra("android.intent.extra.shortcut.NAME", packageName);
        intent.putExtra("iconUri", iconUri);
        Intent shortcutIntent = new Intent();
        shortcutIntent.setAction("com.miui.action.DOWNLOADING_APP");
        shortcutIntent.setComponent(new ComponentName(packageName, "invalidClassName"));
        intent.putExtra("android.intent.extra.shortcut.INTENT", shortcutIntent);
        LauncherModel model = Application.getLauncherApplication(this.mLauncher).getModel();
        if (model == null) {
            return false;
        }
        ShortcutInfo info = model.getProgressItemInfo(this.mLauncher, intent, packageName);
        if (info == null) {
            return false;
        }
        if (this.mWaitingForCloudAppBackup) {
            ShortcutInfo removedInfo = new RemovedComponentInfoList(this.mLauncher).getRemovedInfo(info.intent.getComponent());
            if (removedInfo != null) {
                info.copyPosition(removedInfo);
            }
        }
        info.getAppInfo().pkgName = packageName;
        info.progressStatus = -1;
        info.appProgressServer = server;
        info.progressTitle = title;
        addProgressingInfo(info);
        if (this.mLauncher == null) {
            return false;
        }
        Runnable callback = null;
        if (isLast && isWaitingFor(server)) {
            callback = this.mFinishWaitingCallback;
            finishWaiting();
        }
        if (info.container == -101) {
            this.mLauncher.addItemToHotseats(info, info.cellX, this.mWaitingForCloudAppBackup, callback);
        } else {
            this.mLauncher.addItemToWorkspace(info, info.screenId, info.container, info.cellX, info.cellY, this.mWaitingForCloudAppBackup, callback);
        }
        return true;
    }

    private void onProgressFinished(String packageName) {
        final ShortcutInfo removedInfo = (ShortcutInfo) this.mCurrentProgressingMap.get(packageName);
        removeProgressingInfo(packageName);
        if (removedInfo != null && this.mLauncher != null && this.mLauncher.getWorkspace() != null) {
            this.mLauncher.getWorkspace().post(new Runnable() {
                public void run() {
                    ApplicationProgressProcessor.this.mLauncher.onProgressFinished(removedInfo);
                }
            });
        }
    }

    private void updateInfo(ShortcutInfo info, int status, String progressTitle, Uri uri) {
        if (this.mLauncher != null && this.mLauncher.getWorkspace() != null) {
            final ShortcutInfo shortcutInfo = info;
            final int i = status;
            final String str = progressTitle;
            final Uri uri2 = uri;
            this.mLauncher.getWorkspace().post(new Runnable() {
                public void run() {
                    shortcutInfo.updateStatus(ApplicationProgressProcessor.this.mLauncher, i, str, uri2);
                }
            });
        }
    }

    private Uri parseUri(String uri) {
        if (uri == null || TextUtils.isEmpty(uri) || !uri.startsWith("file")) {
            return null;
        }
        return Uri.parse(uri);
    }

    private void updateProgress(String[] packageNames, String[] titles, int[] status, String[] uris, String server) {
        int length = packageNames.length;
        if (titles.length == length && status.length == length && uris.length == length) {
            int i = 0;
            while (i < length) {
                updateProgress(server, packageNames[i], status[i], titles[i], parseUri(uris[i]), i == length + -1);
                i++;
            }
        }
    }

    private void updateProgress(String server, String packageName, int status, String title, Uri uri, boolean isLast) {
        if (this.mCurrentProgressingMap.containsKey(packageName)) {
            ShortcutInfo info = (ShortcutInfo) this.mCurrentProgressingMap.get(packageName);
            if ((status == -5 || status == -100) && (TextUtils.isEmpty(info.appProgressServer) || info.appProgressServer.equals(server))) {
                onProgressFinished(packageName);
            } else if (info.appProgressServer != null) {
                if (!(info.appProgressServer.equals(server) || status == -100 || status == -5)) {
                    attachProgressToNewServer(info, server);
                }
                updateInfo(info, status, title, uri);
            }
        } else if (status != -4 && status != -5 && status != -100) {
            onNewProgressStart(packageName, title, uri, server, isLast);
        }
    }

    private void updateProgress(String server, String packageName, int status, String uri, boolean isLast) {
        updateProgress(server, packageName, status, mapStatusToTitle(status, server), parseUri(uri), isLast);
    }

    private void updateProgressStatus(String server, String packageName, int status) {
        ShortcutInfo info = (ShortcutInfo) this.mCurrentProgressingMap.get(packageName);
        if (info == null) {
            return;
        }
        if (info.appProgressServer == null || info.appProgressServer.equals(server)) {
            updateProgress(server, packageName, status, mapStatusToTitle(status, server), info.getAppInfo().iconUri, false);
        }
    }

    private String mapStatusToTitle(int status, String server) {
        String result = null;
        JSONObject titles = (JSONObject) this.mAllServersStatusTitleMap.get(server);
        if (titles != null) {
            if (status >= 0 && status <= 100) {
                status = -2;
            }
            try {
                result = LauncherModel.loadTitle(this.mLauncher, titles.getString(String.valueOf(status))).toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private void attachProgressToNewServer(ShortcutInfo info, String server) {
        synchronized (this.mAllServersProgressMap) {
            onProgressIconDeleted(info);
            removeProgressingInfo(info.getPackageName());
            info.appProgressServer = server;
            addProgressingInfo(info);
        }
    }

    public void checkProgress(Context context) {
        synchronized (this.mAllServersProgressMap) {
            ArrayList<String> allServers = new ArrayList(this.mAllServersProgressMap.keySet());
            if (allServers.isEmpty()) {
                return;
            }
            Iterator i$ = allServers.iterator();
            while (i$.hasNext()) {
                boolean needQuery;
                String server = (String) i$.next();
                Long changedValue = Long.valueOf(Shell.getRuntimeSharedValue(ProgressManager.generateServiceProgressChangedKey(server)));
                if (this.mServerChangedValues.containsKey(server) && ((Long) this.mServerChangedValues.get(server)).equals(changedValue)) {
                    needQuery = false;
                } else {
                    needQuery = true;
                }
                if (!needQuery) {
                    this.mServerNoObservingCount.put(server, Integer.valueOf(((Integer) this.mServerNoObservingCount.get(server)).intValue() + 1));
                }
                if (needQuery || ((Integer) this.mServerNoObservingCount.get(server)).intValue() > 5) {
                    this.mServerChangedValues.put(server, changedValue);
                    if (ProgressManager.isServerEnableShareProgressStatus(context, server) && this.mAllServersStatusTitleMap.keySet().contains(server)) {
                        queryProgressSharedValue(server);
                    } else {
                        ProgressManager.getManager(context).queryProgressByBroadcast(this.mLauncher, server, (String[]) ((ArrayList) this.mAllServersProgressMap.get(server)).toArray(new String[0]));
                    }
                    this.mServerNoObservingCount.put(server, Integer.valueOf(0));
                }
            }
        }
    }

    private void queryProgressSharedValue(String server) {
        ArrayList<String> packageNames = new ArrayList((Collection) this.mAllServersProgressMap.get(server));
        if (packageNames != null && packageNames.size() != 0) {
            Iterator i$ = packageNames.iterator();
            while (i$.hasNext()) {
                String name = (String) i$.next();
                updateProgressStatus(server, name, ProgressManager.queryProgressSharedValue(server, name));
            }
        }
    }

    private void addProgressingInfo(ShortcutInfo info) {
        synchronized (this.mAllServersProgressMap) {
            this.mCurrentProgressingMap.put(info.getPackageName(), info);
            if (!this.mAllServersProgressMap.containsKey(info.appProgressServer)) {
                this.mAllServersProgressMap.put(info.appProgressServer, new ArrayList());
            }
            if (!((ArrayList) this.mAllServersProgressMap.get(info.appProgressServer)).contains(info.getPackageName())) {
                ((ArrayList) this.mAllServersProgressMap.get(info.appProgressServer)).add(info.getPackageName());
            }
            if (!this.mProgressInfoList.containsAppProgress(info.getPackageName())) {
                this.mProgressInfoList.recordProgressInfo(info);
            }
        }
    }

    public void onProgressIconClicked(ShortcutInfo info) {
        if (this.mCurrentProgressingMap.containsKey(info.getPackageName())) {
            Intent intent = new Intent("com.miui.home.action.on_click");
            intent.setPackage(info.appProgressServer);
            intent.putExtra("android.intent.extra.update_progress_key", info.getPackageName());
            this.mLauncher.sendBroadcast(intent);
        }
    }

    public void onProgressIconDeleted(ShortcutInfo info) {
        if (this.mCurrentProgressingMap.containsKey(info.getPackageName())) {
            Intent intent = new Intent("com.miui.home.action.on_delete");
            intent.setPackage(info.appProgressServer);
            intent.putExtra("android.intent.extra.update_progress_key", info.getPackageName());
            this.mLauncher.sendBroadcast(intent);
            updateInfo(info, -100, "", null);
        }
    }

    public void loadingProgressFromCloudAppBackup(Context context, Runnable callback) {
        this.mFinishWaitingCallback = callback;
        String[] packageNames = getStringArray(System.getString(context.getContentResolver(), "micloudappbackup_pkgnames"));
        String[] uris = getIconUriArray(packageNames);
        this.mCloudBackupPackageName = System.getString(context.getContentResolver(), "micloudappbackup_server");
        if (packageNames == null || uris == null || packageNames.length != uris.length || TextUtils.isEmpty(this.mCloudBackupPackageName)) {
            this.mCloudBackupPackageName = "com.miui.cloudbackup";
            if (this.mFinishWaitingCallback != null) {
                this.mFinishWaitingCallback.run();
                return;
            }
            return;
        }
        String[] titles = new String[packageNames.length];
        Arrays.fill(titles, context.getResources().getString(R.string.cloud_app_restore_title));
        int[] status = new int[packageNames.length];
        Arrays.fill(status, -1);
        this.mWaitingForCloudAppBackup = true;
        updateProgress(packageNames, titles, status, uris, this.mCloudBackupPackageName);
        this.mLauncher.getWorkspace().postDelayed(this.mFinishWaitingCallback, 10000);
    }

    private String[] getIconUriArray(String[] packageNames) {
        if (packageNames == null) {
            return null;
        }
        String[] iconUris = new String[packageNames.length];
        for (int i = 0; i < packageNames.length; i++) {
            iconUris[i] = getIconUri(packageNames[i]);
        }
        return iconUris;
    }

    private String getIconUri(String pkgName) {
        return "file:///data/user/" + UserHandle.myUserId() + "/com.miui.cloudbackup/files/cloud/icon/" + pkgName + ".png";
    }

    private String[] getStringArray(String list) {
        if (TextUtils.isEmpty(list)) {
            return null;
        }
        JSONArray restore = null;
        try {
            restore = (JSONArray) new JSONTokener(list).nextValue();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (restore == null) {
            return null;
        }
        String[] result = new String[restore.length()];
        for (int i = 0; i < result.length; i++) {
            try {
                result[i] = restore.getString(i);
            } catch (JSONException e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    private boolean isWaitingFor(String server) {
        return this.mWaitingForCloudAppBackup && this.mCloudBackupPackageName.equals(server);
    }

    private void finishWaiting() {
        this.mLauncher.getWorkspace().removeCallbacks(this.mFinishWaitingCallback);
        this.mWaitingForCloudAppBackup = false;
    }
}
