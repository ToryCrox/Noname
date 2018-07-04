package com.miui.home.launcher;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import com.miui.home.launcher.LauncherSettings.Favorites;
import com.miui.home.launcher.gadget.GadgetFactory;
import com.miui.home.launcher.gadget.GadgetInfo;
import com.miui.home.launcher.setting.PortableUtils;
import com.miui.home.launcher.setting.PortableUtils.LauncherApps_Callback;
import com.miui.home.launcher.upsidescene.data.FreeStyle;
import com.miui.home.launcher.upsidescene.data.FreeStyleSerializer;
import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import miui.content.res.IconCustomizer;
import miui.os.Build;

public class LauncherModel extends BroadcastReceiver {
    private static HashSet<ItemInfo> sDelayedUpdateBuffer = null;
    public static boolean sLoadingMissingPreset = false;
    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());
    private static final HandlerThread sWorkerThread = new HandlerThread("launcher-loader");
    private AllAppsList mAllAppsList = new AllAppsList();
    private final Object mAllAppsListLock = new Object();
    private final LauncherApplication mApp;
    final ArrayList<LauncherAppWidgetInfo> mAppWidgets = new ArrayList();
    private WeakReference<Callbacks> mCallbacks;
    final HashMap<Long, FolderInfo> mFolders = new HashMap();
    final ArrayList<GadgetInfo> mGadgets = new ArrayList();
    private DeferredHandler mHandler = new DeferredHandler();
    private IconLoader mIconLoader;
    final HashSet<String> mInstalledShortcutWidgets = new HashSet();
    final ArrayList<ItemInfo> mItems = new ArrayList();
    private LauncherAppsCallback mLauncherAppsCallback = new LauncherAppsCallback();
    final HashMap<ComponentAndUser, Long> mLoadedApps = new HashMap();
    final HashSet<PackageAndUser> mLoadedPackages = new HashSet();
    final HashSet<String> mLoadedPresetPackages = new HashSet();
    final HashSet<String> mLoadedUris = new HashSet();
    private LoaderTask mLoaderTask;
    private final Object mLock = new Object();
    final HashSet<String> mProgressingPackages = new HashSet();
    private Intent mShortcutWidgetQueryIntent = new Intent("miui.intent.action.ICON_PANEL").addCategory("android.intent.category.DEFAULT");
    private boolean mWorkspaceLoaded;

    public interface Callbacks {
        void bindAppWidget(LauncherAppWidgetInfo launcherAppWidgetInfo);

        void bindAppsChanged(ArrayList<RemoveInfo> arrayList, ArrayList<ShortcutInfo> arrayList2, Intent intent);

        void bindFolders(HashMap<Long, FolderInfo> hashMap);

        void bindFreeStyleLoaded(FreeStyle freeStyle);

        void bindGadget(GadgetInfo gadgetInfo);

        void bindIconsChanged(ArrayList<String> arrayList);

        void bindItems(ArrayList<ItemInfo> arrayList, int i, int i2);

        void finishBindingMissingItems();

        void finishBindingSavedItems();

        void finishLoading();

        int getCurrentWorkspaceScreen();

        boolean isReadyToBinding();

        void reloadWidgetPreview();

        void startBinding();

        void startLoading();
    }

    private abstract class DataCarriedRunnable implements Runnable {
        protected Object mData;
        protected Object mData2;

        DataCarriedRunnable(Object data) {
            this.mData = data;
        }

        DataCarriedRunnable(Object data, Object data2) {
            this.mData = data;
            this.mData2 = data2;
        }
    }

    public static class ComponentAndUser {
        public final ComponentName componentName;
        public final UserHandle user;

        public ComponentAndUser(ComponentName cn, UserHandle u) {
            this.componentName = cn;
            this.user = u;
        }

        public int hashCode() {
            return this.componentName.hashCode() ^ this.user.hashCode();
        }

        public boolean equals(Object obj) {
            ComponentAndUser other = (ComponentAndUser) obj;
            return this.componentName.equals(other.componentName) && this.user.equals(other.user);
        }
    }

    private class LauncherAppsCallback extends LauncherApps_Callback {
        private LauncherAppsCallback() {
        }

        public void onPackageChanged(final String packageName, final UserHandle user) {
            LauncherModel.this.runOnWorkerThreadAndCheckForReady(new Runnable() {
                public void run() {
                    Intent intent = new Intent("android.intent.action.PACKAGE_CHANGED");
                    intent.putExtra("android.intent.extra.PACKAGES", packageName);
                    intent.setData(Uri.fromParts("package", packageName, null));
                    LauncherModel.this.onReceiveBackground(LauncherModel.this.mApp.getLauncher(), intent, user);
                }
            });
        }

        public void onPackageRemoved(final String packageName, final UserHandle user) {
            LauncherModel.this.runOnWorkerThreadAndCheckForReady(new Runnable() {
                public void run() {
                    Intent intent = new Intent("android.intent.action.PACKAGE_REMOVED");
                    intent.putExtra("android.intent.extra.PACKAGES", packageName);
                    intent.setData(Uri.fromParts("package", packageName, null));
                    LauncherModel.this.onReceiveBackground(LauncherModel.this.mApp.getLauncher(), intent, user);
                }
            });
        }

        public void onPackageAdded(final String packageName, final UserHandle user) {
            LauncherModel.this.runOnWorkerThreadAndCheckForReady(new Runnable() {
                public void run() {
                    Intent intent = new Intent("android.intent.action.PACKAGE_ADDED");
                    intent.putExtra("android.intent.extra.PACKAGES", packageName);
                    intent.setData(Uri.fromParts("package", packageName, null));
                    LauncherModel.this.onReceiveBackground(LauncherModel.this.mApp.getLauncher(), intent, user);
                }
            });
        }

        public void onPackagesAvailable(final String[] packageNames, final UserHandle user, boolean replacing) {
            LauncherModel.this.runOnWorkerThreadAndCheckForReady(new Runnable() {
                public void run() {
                    Intent intent = new Intent("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE");
                    intent.putStringArrayListExtra("android.intent.extra.changed_package_list", new ArrayList(Arrays.asList(packageNames)));
                    LauncherModel.this.onReceiveBackground(LauncherModel.this.mApp.getLauncher(), intent, user);
                }
            });
        }

        public void onPackagesUnavailable(final String[] packageNames, final UserHandle user, boolean replacing) {
            LauncherModel.this.runOnWorkerThreadAndCheckForReady(new Runnable() {
                public void run() {
                    Intent intent = new Intent("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE");
                    intent.putStringArrayListExtra("android.intent.extra.changed_package_list", new ArrayList(Arrays.asList(packageNames)));
                    LauncherModel.this.onReceiveBackground(LauncherModel.this.mApp.getLauncher(), intent, user);
                }
            });
        }
    }

    private class LoaderTask implements Runnable {
        private final ContentResolver mContentResolver;
        private Context mContext;
        private HashSet<ComponentAndUser> mInstalledComponents = new HashSet();
        private boolean mIsJustRestoreFinished;
        private boolean mIsLaunching;
        private boolean mLoadAndBindStepFinished;
        private final PackageManager mManager;
        private boolean mStopped;
        private boolean mWaitingForCloudAppRestore = false;

        LoaderTask(Context context, boolean isLaunching, boolean isJustRestoreFinished) {
            this.mContext = context;
            this.mIsLaunching = isLaunching;
            this.mContentResolver = context.getContentResolver();
            this.mManager = context.getPackageManager();
            this.mIsJustRestoreFinished = isJustRestoreFinished;
        }

        boolean isLaunching() {
            return this.mIsLaunching;
        }

        private void loadAndBindMissingIcons() {
            if (!this.mStopped) {
                final Callbacks oldCallbacks = LauncherModel.this.mCallbacks != null ? (Callbacks) LauncherModel.this.mCallbacks.get() : null;
                if (oldCallbacks == null) {
                    Log.e("Launcher.Model", "No callback to call back");
                } else if (this.mInstalledComponents.size() == 0) {
                    Log.e("Launcher.Model", "No main activity found, the system is so clean");
                } else {
                    HashSet<PackageAndUser> updatedPackages = new HashSet();
                    Iterator i$ = this.mInstalledComponents.iterator();
                    while (i$.hasNext()) {
                        ComponentAndUser info = (ComponentAndUser) i$.next();
                        if (!this.mStopped) {
                            PackageAndUser packageAndUser = new PackageAndUser(info.componentName.getPackageName(), info.user);
                            if (!(updatedPackages.contains(packageAndUser) || LauncherModel.this.mLoadedApps.containsKey(info))) {
                                updatedPackages.add(packageAndUser);
                            }
                        } else {
                            return;
                        }
                    }
                    if (Build.IS_CM_CUSTOMIZATION) {
                        boolean missingAllPreset = true;
                        if (updatedPackages.size() >= 0) {
                            List<ComponentName> presetList = LauncherProvider.getPresetItems();
                            for (int i = 2; i >= 0; i--) {
                                if (!containsPackageIgnoreUser(updatedPackages, ((ComponentName) presetList.get(i)).getPackageName())) {
                                    missingAllPreset = false;
                                    break;
                                }
                            }
                            if (missingAllPreset && insertPresetToDefaultScreen(presetList, updatedPackages)) {
                                LauncherModel.this.stopLoaderLocked();
                                LauncherModel.this.startLoader(this.mContext, true);
                                LauncherModel.this.mHandler.cancel();
                                return;
                            }
                        }
                    }
                    i$ = updatedPackages.iterator();
                    while (i$.hasNext()) {
                        PackageAndUser pkgAndUser = (PackageAndUser) i$.next();
                        synchronized (LauncherModel.this.mAllAppsListLock) {
                            LauncherModel.this.mAllAppsList.updatePackage(this.mContext, pkgAndUser.packageName, true, true, pkgAndUser.user);
                            ProgressManager.getManager(this.mContext).onAppInProgressInstalled(pkgAndUser.packageName);
                        }
                    }
                    if (!this.mStopped) {
                        ArrayList<RemoveInfo> removed = new ArrayList();
                        ArrayList<ShortcutInfo> added = new ArrayList();
                        if (LauncherModel.this.mAllAppsList.removed.size() > 0) {
                            removed.addAll(LauncherModel.this.mAllAppsList.removed);
                            LauncherModel.this.mAllAppsList.removed.clear();
                        }
                        if (LauncherModel.this.mAllAppsList.added.size() > 0) {
                            added.addAll(LauncherModel.this.mAllAppsList.added);
                            LauncherModel.this.mAllAppsList.added.clear();
                        }
                        LauncherModel.this.mHandler.post(new DataCarriedRunnable(removed, added) {
                            public void run() {
                                Callbacks callbacks = LoaderTask.this.tryGetCallbacks(oldCallbacks);
                                if (!LoaderTask.this.mStopped && callbacks != null) {
                                    Log.d("Launcher.Model", "Finally updating for missing icons");
                                    callbacks.bindAppsChanged((ArrayList) this.mData, (ArrayList) this.mData2, null);
                                }
                            }
                        });
                        LauncherModel.this.onRemoveItems(removed);
                        LauncherModel.this.onLoadShortcuts(added);
                    }
                    LauncherModel.this.mHandler.post(new Runnable() {
                        public void run() {
                            Iterator i$ = ProgressManager.getManager(LoaderTask.this.mContext).getAllAppProgressKeys().iterator();
                            while (i$.hasNext()) {
                                String name = (String) i$.next();
                                if (!LauncherModel.this.mProgressingPackages.contains(name)) {
                                    ShortcutInfo info = new ShortcutInfo();
                                    info.intent = new Intent();
                                    info.intent.setComponent(new ComponentName(name, "invalidClassName"));
                                    if (ProgressManager.getManager(LoaderTask.this.mContext).bindAppProgressItem(info, true)) {
                                        LauncherModel.this.mProgressingPackages.add(name);
                                    }
                                }
                            }
                        }
                    });
                    LauncherModel.this.mHandler.post(new Runnable() {
                        public void run() {
                            Callbacks callbacks = LoaderTask.this.tryGetCallbacks(oldCallbacks);
                            if (!LoaderTask.this.mStopped && callbacks != null) {
                                callbacks.finishBindingMissingItems();
                            }
                        }
                    });
                    LauncherModel.sLoadingMissingPreset = false;
                }
            }
        }

        private boolean containsPackageIgnoreUser(HashSet<PackageAndUser> packageSet, String packageName) {
            if (TextUtils.isEmpty(packageName) || packageSet == null) {
                return false;
            }
            Iterator<PackageAndUser> itr = packageSet.iterator();
            while (itr.hasNext()) {
                if (packageName.equals(((PackageAndUser) itr.next()).packageName)) {
                    return true;
                }
            }
            return false;
        }

        private boolean insertPresetToDefaultScreen(List<ComponentName> presetList, HashSet<PackageAndUser> missingItems) {
            LauncherModel.sLoadingMissingPreset = true;
            long defaultScreenId = Application.getLauncherApplication(this.mContext).getLauncher().getWorldReadableSharedPreference().getLong("pref_default_screen", -1);
            if (defaultScreenId != -1) {
                if (!ScreenUtils.makeLastRowEmpty(this.mContext, defaultScreenId, 0)) {
                    return false;
                }
                int lastCellX = DeviceConfig.getCellCountX() - 1;
                int cellY = DeviceConfig.getCellCountY() - 1;
                boolean hasRecommendFolder = false;
                for (FolderInfo info : LauncherModel.this.mFolders.values()) {
                    if (info.getTitle(this.mContext) != null) {
                        if (info.getTitle(this.mContext).equals(LauncherModel.loadTitle(this.mContext, "com.miui.home:string/default_folder_title_recommend"))) {
                            hasRecommendFolder = true;
                        }
                    }
                }
                if (!hasRecommendFolder) {
                    ScreenUtils.addFolderToScreen(this.mContext, "com.miui.home:string/default_folder_title_recommend", defaultScreenId, lastCellX, cellY);
                    lastCellX--;
                }
                int cellX = 0;
                for (int i = 0; i < presetList.size() && cellX <= lastCellX; i++) {
                    ComponentName cn = (ComponentName) presetList.get(i);
                    if (missingItems.contains(new PackageAndUser(cn.getPackageName(), Process.myUserHandle()))) {
                        ScreenUtils.addItemToScreen(this.mContext, cn, defaultScreenId, cellX, cellY);
                        cellX++;
                    }
                }
            }
            return true;
        }

        private void loadAndBindWorkspace(boolean isLoadingForTheFirstTime) {
            boolean loaded;
            synchronized (this) {
                loaded = LauncherModel.this.mWorkspaceLoaded;
                LauncherModel.this.mWorkspaceLoaded = true;
            }
            Log.d("Launcher.Model", "loadAndBindWorkspace loaded=" + loaded);
            synchronized (LauncherModel.this.mLock) {
                loadWorkspace(isLoadingForTheFirstTime);
                if (this.mStopped) {
                    LauncherModel.this.mWorkspaceLoaded = false;
                    return;
                }
                bindWorkspace();
            }
        }

        private boolean loadDBComplete() {
            return PreferenceManager.getDefaultSharedPreferences(this.mContext).getBoolean("database_ready_pref_key", true);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            /*
            r14 = this;
            r5 = r14.mContext;
            r0 = com.miui.home.launcher.Application.getLauncherApplication(r5);
            r6 = 0;
        L_0x0008:
            r5 = r0.getLauncherProvider();
            if (r5 == 0) goto L_0x0022;
        L_0x000e:
            r5 = r0.getLauncherProvider();
            r5 = r5.isReady();
            if (r5 != 0) goto L_0x003e;
        L_0x0018:
            r5 = r0.hasBroughtToForeground();
            if (r5 != 0) goto L_0x003e;
        L_0x001e:
            r5 = r14.mStopped;
            if (r5 != 0) goto L_0x003e;
        L_0x0022:
            r10 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
            r5 = (r6 > r10 ? 1 : (r6 == r10 ? 0 : -1));
            if (r5 < 0) goto L_0x0035;
        L_0x0028:
            r5 = r0.getLauncherProvider();
            if (r5 != 0) goto L_0x0035;
        L_0x002e:
            r5 = android.os.Process.myPid();
            android.os.Process.killProcess(r5);
        L_0x0035:
            r10 = 50;
            r6 = r6 + r10;
            r10 = 50;
            android.os.SystemClock.sleep(r10);
            goto L_0x0008;
        L_0x003e:
            r5 = r14.mStopped;
            if (r5 == 0) goto L_0x0043;
        L_0x0042:
            return;
        L_0x0043:
            r4 = r14.loadDBComplete();
            r3 = 0;
            r5 = r0.getLauncherProvider();
            r5 = r5.isReady();
            if (r5 == 0) goto L_0x0054;
        L_0x0052:
            if (r4 != 0) goto L_0x005c;
        L_0x0054:
            r3 = 1;
            r5 = r0.getLauncherProvider();
            r5.loadDefaultWorkspace();
        L_0x005c:
            r5 = r0.getLauncherProvider();
            r5.loadSkippedItems(r0);
            r8 = android.os.SystemClock.uptimeMillis();
            r5 = com.miui.home.launcher.LauncherModel.this;
            r10 = r5.mLock;
            monitor-enter(r10);
            r5 = r14.mIsLaunching;	 Catch:{ all -> 0x009d }
            if (r5 == 0) goto L_0x009a;
        L_0x0072:
            r5 = 0;
        L_0x0073:
            android.os.Process.setThreadPriority(r5);	 Catch:{ all -> 0x009d }
            monitor-exit(r10);	 Catch:{ all -> 0x009d }
            r5 = com.miui.home.launcher.LauncherModel.this;
            r10 = r5.mLock;
            monitor-enter(r10);
            r5 = com.miui.home.launcher.LauncherModel.this;	 Catch:{ all -> 0x0097 }
            r5 = r5.mCallbacks;	 Catch:{ all -> 0x0097 }
            if (r5 == 0) goto L_0x00a0;
        L_0x0086:
            r5 = com.miui.home.launcher.LauncherModel.this;	 Catch:{ all -> 0x0097 }
            r5 = r5.mCallbacks;	 Catch:{ all -> 0x0097 }
            r5 = r5.get();	 Catch:{ all -> 0x0097 }
            r5 = (com.miui.home.launcher.LauncherModel.Callbacks) r5;	 Catch:{ all -> 0x0097 }
            r1 = r5;
        L_0x0093:
            if (r1 != 0) goto L_0x00a2;
        L_0x0095:
            monitor-exit(r10);	 Catch:{ all -> 0x0097 }
            goto L_0x0042;
        L_0x0097:
            r5 = move-exception;
            monitor-exit(r10);	 Catch:{ all -> 0x0097 }
            throw r5;
        L_0x009a:
            r5 = 10;
            goto L_0x0073;
        L_0x009d:
            r5 = move-exception;
            monitor-exit(r10);	 Catch:{ all -> 0x009d }
            throw r5;
        L_0x00a0:
            r1 = 0;
            goto L_0x0093;
        L_0x00a2:
            r5 = com.miui.home.launcher.LauncherModel.this;	 Catch:{ all -> 0x0097 }
            r5 = r5.mHandler;	 Catch:{ all -> 0x0097 }
            r5.cancel();	 Catch:{ all -> 0x0097 }
            r5 = com.miui.home.launcher.LauncherModel.this;	 Catch:{ all -> 0x0097 }
            r5 = r5.mHandler;	 Catch:{ all -> 0x0097 }
            r11 = new com.miui.home.launcher.LauncherModel$LoaderTask$4;	 Catch:{ all -> 0x0097 }
            r11.<init>(r1);	 Catch:{ all -> 0x0097 }
            r5.post(r11);	 Catch:{ all -> 0x0097 }
            monitor-exit(r10);	 Catch:{ all -> 0x0097 }
            r5 = "Launcher.Model";
            r10 = "step 1: loading workspace";
            android.util.Log.d(r5, r10);
            r14.loadAndBindWorkspace(r3);
            r5 = r14.mStopped;
            if (r5 == 0) goto L_0x00f2;
        L_0x00c8:
            r5 = com.miui.home.launcher.LauncherModel.this;
            r10 = r5.mLock;
            monitor-enter(r10);
            r5 = 0;
            android.os.Process.setThreadPriority(r5);	 Catch:{ all -> 0x01c9 }
            monitor-exit(r10);	 Catch:{ all -> 0x01c9 }
            r5 = 0;
            r14.mContext = r5;
            r5 = com.miui.home.launcher.LauncherModel.this;
            r10 = r5.mLock;
            monitor-enter(r10);
            r5 = com.miui.home.launcher.LauncherModel.this;	 Catch:{ all -> 0x01cc }
            r5 = r5.mLoaderTask;	 Catch:{ all -> 0x01cc }
            if (r5 != r14) goto L_0x00ec;
        L_0x00e6:
            r5 = com.miui.home.launcher.LauncherModel.this;	 Catch:{ all -> 0x01cc }
            r11 = 0;
            r5.mLoaderTask = r11;	 Catch:{ all -> 0x01cc }
        L_0x00ec:
            monitor-exit(r10);	 Catch:{ all -> 0x01cc }
            r5 = 0;
            r14.mStopped = r5;
            goto L_0x0042;
        L_0x00f2:
            r5 = com.miui.home.launcher.LauncherModel.this;
            r10 = r5.mLock;
            monitor-enter(r10);
            r5 = r14.mIsLaunching;	 Catch:{ all -> 0x0154 }
            if (r5 == 0) goto L_0x0109;
        L_0x00fd:
            r5 = "Launcher.Model";
            r11 = "Setting thread priority to BACKGROUND";
            android.util.Log.d(r5, r11);	 Catch:{ all -> 0x0154 }
            r5 = 10;
            android.os.Process.setThreadPriority(r5);	 Catch:{ all -> 0x0154 }
        L_0x0109:
            monitor-exit(r10);	 Catch:{ all -> 0x0154 }
            r5 = 0;
            r5 = java.lang.String.valueOf(r5);
            r10 = r14.mContext;
            r10 = r10.getContentResolver();
            r11 = "extra_micloudapp_provisioned";
            r10 = android.provider.Settings.System.getString(r10, r11);
            r5 = r5.equals(r10);
            if (r5 == 0) goto L_0x0146;
        L_0x0121:
            r5 = "Launcher.Model";
            r10 = "step 2: loading restoring items from cloudAppBackup";
            android.util.Log.d(r5, r10);
            r2 = new com.miui.home.launcher.LauncherModel$LoaderTask$5;
            r2.<init>();
            r5 = 1;
            r14.mWaitingForCloudAppRestore = r5;
            r5 = com.miui.home.launcher.LauncherModel.this;
            r10 = r5.mLock;
            monitor-enter(r10);
            r5 = com.miui.home.launcher.LauncherModel.this;	 Catch:{ all -> 0x0157 }
            r5 = r5.mHandler;	 Catch:{ all -> 0x0157 }
            r11 = new com.miui.home.launcher.LauncherModel$LoaderTask$6;	 Catch:{ all -> 0x0157 }
            r11.<init>(r2);	 Catch:{ all -> 0x0157 }
            r5.post(r11);	 Catch:{ all -> 0x0157 }
            monitor-exit(r10);	 Catch:{ all -> 0x0157 }
        L_0x0146:
            r5 = r14.mWaitingForCloudAppRestore;
            if (r5 == 0) goto L_0x015a;
        L_0x014a:
            r5 = r14.mStopped;
            if (r5 != 0) goto L_0x015a;
        L_0x014e:
            r10 = 50;
            android.os.SystemClock.sleep(r10);
            goto L_0x0146;
        L_0x0154:
            r5 = move-exception;
            monitor-exit(r10);	 Catch:{ all -> 0x0154 }
            throw r5;
        L_0x0157:
            r5 = move-exception;
            monitor-exit(r10);	 Catch:{ all -> 0x0157 }
            throw r5;
        L_0x015a:
            r5 = "Launcher.Model";
            r10 = "step 3: loading missing icons";
            android.util.Log.d(r5, r10);
            r5 = com.miui.home.launcher.LauncherModel.this;
            r10 = r5.mLock;
            monitor-enter(r10);
            r14.loadAndBindMissingIcons();	 Catch:{ all -> 0x01b3 }
            monitor-exit(r10);	 Catch:{ all -> 0x01b3 }
            r5 = "Launcher.Model";
            r10 = new java.lang.StringBuilder;
            r10.<init>();
            r11 = "finish loading using ";
            r10 = r10.append(r11);
            r12 = android.os.SystemClock.uptimeMillis();
            r12 = r12 - r8;
            r10 = r10.append(r12);
            r11 = " ms";
            r10 = r10.append(r11);
            r10 = r10.toString();
            android.util.Log.d(r5, r10);
            r5 = com.miui.home.launcher.LauncherModel.this;
            r10 = r5.mLock;
            monitor-enter(r10);
            r5 = com.miui.home.launcher.LauncherModel.this;	 Catch:{ all -> 0x01b0 }
            r5 = r5.mCallbacks;	 Catch:{ all -> 0x01b0 }
            if (r5 == 0) goto L_0x01b6;
        L_0x019e:
            r5 = com.miui.home.launcher.LauncherModel.this;	 Catch:{ all -> 0x01b0 }
            r5 = r5.mCallbacks;	 Catch:{ all -> 0x01b0 }
            r5 = r5.get();	 Catch:{ all -> 0x01b0 }
            r5 = (com.miui.home.launcher.LauncherModel.Callbacks) r5;	 Catch:{ all -> 0x01b0 }
            r1 = r5;
        L_0x01ab:
            if (r1 != 0) goto L_0x01b8;
        L_0x01ad:
            monitor-exit(r10);	 Catch:{ all -> 0x01b0 }
            goto L_0x0042;
        L_0x01b0:
            r5 = move-exception;
            monitor-exit(r10);	 Catch:{ all -> 0x01b0 }
            throw r5;
        L_0x01b3:
            r5 = move-exception;
            monitor-exit(r10);	 Catch:{ all -> 0x01b3 }
            throw r5;
        L_0x01b6:
            r1 = 0;
            goto L_0x01ab;
        L_0x01b8:
            r5 = com.miui.home.launcher.LauncherModel.this;	 Catch:{ all -> 0x01b0 }
            r5 = r5.mHandler;	 Catch:{ all -> 0x01b0 }
            r11 = new com.miui.home.launcher.LauncherModel$LoaderTask$7;	 Catch:{ all -> 0x01b0 }
            r11.<init>(r1);	 Catch:{ all -> 0x01b0 }
            r5.post(r11);	 Catch:{ all -> 0x01b0 }
            monitor-exit(r10);	 Catch:{ all -> 0x01b0 }
            goto L_0x00c8;
        L_0x01c9:
            r5 = move-exception;
            monitor-exit(r10);	 Catch:{ all -> 0x01c9 }
            throw r5;
        L_0x01cc:
            r5 = move-exception;
            monitor-exit(r10);	 Catch:{ all -> 0x01cc }
            throw r5;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.miui.home.launcher.LauncherModel.LoaderTask.run():void");
        }

        public void stopLocked() {
            this.mStopped = true;
        }

        Callbacks tryGetCallbacks(Callbacks oldCallbacks) {
            synchronized (LauncherModel.this.mLock) {
                if (this.mStopped) {
                    return null;
                } else if (LauncherModel.this.mCallbacks == null) {
                    return null;
                } else {
                    Callbacks callbacks = (Callbacks) LauncherModel.this.mCallbacks.get();
                    if (callbacks != oldCallbacks) {
                        return null;
                    } else if (callbacks == null) {
                        Log.w("Launcher.Model", "no mCallbacks");
                        return null;
                    } else {
                        return callbacks;
                    }
                }
            }
        }

        private void loadFolder(Cursor c) {
            FolderInfo folderInfo = LauncherModel.findOrMakeUserFolder(LauncherModel.this.mFolders, c.getLong(0));
            if (folderInfo != null) {
                folderInfo.load(this.mContext, c);
                if (folderInfo.container == -100 || folderInfo.container == -101) {
                    LauncherModel.this.mItems.add(folderInfo);
                }
                LauncherModel.this.mFolders.put(Long.valueOf(folderInfo.id), folderInfo);
            }
        }

        private void loadAppWidget(Cursor c, ArrayList<Long> itemsToRemove) {
            int appWidgetId = c.getInt(9);
            long id = c.getLong(0);
            AppWidgetProviderInfo provider = AppWidgetManager.getInstance(this.mContext).getAppWidgetInfo(appWidgetId);
            if (provider == null || provider.provider == null || provider.provider.getPackageName() == null) {
                Log.e("Launcher.Model", "Deleting widget that isn't installed anymore: id=" + id + " appWidgetId=" + appWidgetId);
                if (!this.mManager.isSafeMode()) {
                    itemsToRemove.add(Long.valueOf(id));
                    return;
                }
                return;
            }
            LauncherAppWidgetInfo appWidgetInfo = new LauncherAppWidgetInfo(appWidgetId);
            appWidgetInfo.id = id;
            appWidgetInfo.load(this.mContext, c);
            appWidgetInfo.packageName = provider.provider.toShortString();
            if (appWidgetInfo.container != -100) {
                Log.e("Launcher.Model", "Widget found where container != CONTAINER_DESKTOP -- ignoring!");
            } else {
                LauncherModel.this.mAppWidgets.add(appWidgetInfo);
            }
        }

        private void loadGadget(Cursor c, ArrayList<Long> itemsToRemove) {
            int gadgetId = c.getInt(9);
            long id = c.getLong(0);
            GadgetInfo gadgetInfo = GadgetFactory.getInfo(gadgetId);
            if (gadgetInfo != null) {
                gadgetInfo.load(this.mContext, c);
                if (gadgetInfo.isValid()) {
                    LauncherModel.this.mGadgets.add(gadgetInfo);
                } else {
                    gadgetInfo = null;
                }
            }
            if (gadgetInfo == null && !this.mManager.isSafeMode()) {
                itemsToRemove.add(Long.valueOf(id));
            }
        }

        private void loadShortcut(Cursor c, int itemType, ArrayList<Long> itemsToRemove, RemovedComponentInfoList removedList) {
            try {
                ShortcutInfo info;
                FolderInfo folderInfo;
                Intent intent = Intent.parseUri(c.getString(1), 0);
                long id = c.getLong(0);
                UserHandle user = ((UserManager) LauncherModel.this.mApp.getLauncher().getSystemService("user")).getUserForSerialNumber((long) c.getInt(20));
                if (itemType == 0) {
                    ComponentName cn = intent.getComponent();
                    if (user == null || !this.mInstalledComponents.contains(new ComponentAndUser(cn, user))) {
                        if (!LauncherSettings.isRetainedPackage(cn.getPackageName())) {
                            if (this.mIsJustRestoreFinished || user == null || !ScreenUtils.isPackageDisabled(this.mContext, cn.getPackageName())) {
                                removedList.recordRemovedInfo(c, cn);
                                itemsToRemove.add(Long.valueOf(id));
                                Log.w("Launcher.Model", "Remove:" + cn + ". mIsJustRestoreFinished:" + this.mIsJustRestoreFinished);
                                return;
                            }
                            return;
                        }
                        return;
                    }
                }
                if (itemType == 0) {
                    info = LauncherModel.this.getShortcutInfo(this.mManager, intent, this.mContext, c, 4, 2, 20);
                    if (info == null || !LauncherModel.this.mLoadedApps.containsKey(new ComponentAndUser(intent.getComponent(), info.getUser()))) {
                        LauncherModel.this.mLoadedApps.put(new ComponentAndUser(intent.getComponent(), info.getUser()), Long.valueOf(id));
                    } else {
                        info = null;
                    }
                } else {
                    info = LauncherModel.this.getShortcutInfo(intent, c, this.mContext, itemType, 3, 5, 6, 4, 2, 20);
                }
                if (info != null) {
                    info.intent = intent;
                    info.load(this.mContext, c);
                    if (info.isPresetApp()) {
                        Uri uri = intent.getData();
                        String path = uri == null ? null : uri.getPath();
                        if (!(TextUtils.isEmpty(path) || new File(path).exists())) {
                            info = null;
                        }
                    }
                }
                if (info != null) {
                    LauncherModel.this.updateSavedIcon(this.mContext, info, c, 4);
                    if (info.container == -100 || info.container == -101) {
                        LauncherModel.this.mItems.add(info);
                    } else {
                        folderInfo = (FolderInfo) LauncherModel.this.mFolders.get(Long.valueOf(info.container));
                        if (folderInfo != null) {
                            folderInfo.add(info);
                        } else {
                            LauncherModel.this.mItems.add(info);
                        }
                    }
                    LauncherModel.this.onLoadShortcut(info);
                }
                if (info == null) {
                    Log.e("Launcher.Model", "Error loading shortcut " + id + ", removing it");
                    itemsToRemove.add(Long.valueOf(id));
                } else if (ProgressManager.isProgressType(info)) {
                    Log.i("Launcher.Model", "load progress shortcut " + info.id);
                    if (!ScreenUtils.isAlreadyInstalled(info.getPackageName(), this.mContext) && ProgressManager.getManager(this.mContext).bindAppProgressItem(info, true) && info.intent.getComponent().getClassName().equals("invalidClassName")) {
                        LauncherModel.this.mProgressingPackages.add(info.getPackageName());
                        return;
                    }
                    ProgressManager.getManager(this.mContext).removeProgressingInfo(info.getPackageName());
                    LauncherModel.this.mProgressingPackages.remove(info.getPackageName());
                    LauncherModel.this.mItems.remove(info);
                    itemsToRemove.add(Long.valueOf(info.id));
                    folderInfo = (FolderInfo) LauncherModel.this.mFolders.get(Long.valueOf(info.container));
                    if (folderInfo != null) {
                        folderInfo.remove(info);
                    }
                }
            } catch (URISyntaxException e) {
            }
        }

        private void loadWorkspace(boolean isLoadingForTheFirstTime) {
            Iterator i$;
            long t = SystemClock.uptimeMillis();
            RemovedComponentInfoList removedComponentInfoList = new RemovedComponentInfoList(this.mContext);
            LauncherModel.this.mItems.clear();
            LauncherModel.this.mAppWidgets.clear();
            LauncherModel.this.mGadgets.clear();
            LauncherModel.this.mFolders.clear();
            LauncherModel.this.mLoadedApps.clear();
            LauncherModel.this.mLoadedUris.clear();
            LauncherModel.this.mProgressingPackages.clear();
            LauncherModel.this.mLoadedPackages.clear();
            LauncherModel.this.mLoadedPresetPackages.clear();
            this.mInstalledComponents.clear();
            LauncherModel.this.mInstalledShortcutWidgets.clear();
            for (ComponentAndUser info : PortableUtils.launcherApps_getActivityList(this.mContext, null, null)) {
                String packageName = info.componentName.getPackageName();
                String className = info.componentName.getClassName();
                if (!(TextUtils.isEmpty(packageName) || TextUtils.isEmpty(className) || LauncherProvider.isSkippedItem(info.componentName))) {
                    this.mInstalledComponents.add(info);
                }
            }
            Intent intent = new Intent("miui.intent.action.ICON_PANEL");
            intent.addCategory("android.intent.category.DEFAULT");
            for (ResolveInfo ri : this.mManager.queryIntentActivities(intent, 0)) {
                LauncherModel.this.mInstalledShortcutWidgets.add(ri.activityInfo.packageName);
            }
            ArrayList<Long> itemsToRemove = new ArrayList();
            if (!this.mStopped) {
                Cursor c = this.mContentResolver.query(Favorites.CONTENT_URI, ItemQuery.COLUMNS, "itemType=?", new String[]{String.valueOf(2)}, "cellY ASC, cellX ASC, itemType ASC");
                if (!this.mStopped) {
                    loadItems(c, itemsToRemove, removedComponentInfoList);
                    if (!this.mStopped) {
                        c = this.mContentResolver.query(Favorites.getJoinContentUri(" JOIN screens ON favorites.screen=screens._id"), ItemInfo.getColumnsWithScreenType(), "container=? AND itemType!=?", new String[]{String.valueOf(-100), String.valueOf(2)}, "screens.screenOrder ASC, cellY ASC, cellX ASC, itemType ASC");
                        if (!this.mStopped) {
                            loadItems(c, itemsToRemove, removedComponentInfoList);
                            if (!this.mStopped) {
                                c = this.mContentResolver.query(Favorites.CONTENT_URI, ItemQuery.COLUMNS, "container!=? AND itemType!=?", new String[]{String.valueOf(-100), String.valueOf(2)}, "cellX ASC");
                                if (!this.mStopped) {
                                    loadItems(c, itemsToRemove, removedComponentInfoList);
                                    if (isLoadingForTheFirstTime) {
                                        LauncherModel.this.removeEmptyFolder(itemsToRemove);
                                    }
                                    if (!itemsToRemove.isEmpty()) {
                                        removedComponentInfoList.writeBackToFile();
                                        ContentProviderClient client = this.mContentResolver.acquireContentProviderClient(Favorites.CONTENT_URI);
                                        i$ = itemsToRemove.iterator();
                                        while (i$.hasNext()) {
                                            long id = ((Long) i$.next()).longValue();
                                            Log.d("Launcher.Model", "Removed id = " + id);
                                            try {
                                                client.delete(Favorites.getContentUri(id), null, null);
                                            } catch (RemoteException e) {
                                                Log.w("Launcher.Model", "Could not remove id = " + id);
                                            } catch (SQLiteException e2) {
                                                Log.w("Launcher.Model", "Could not remove id(database readonly) = " + id);
                                            }
                                        }
                                        client.release();
                                    }
                                    Log.d("Launcher.Model", "load workspace in " + (SystemClock.uptimeMillis() - t) + "ms");
                                }
                            }
                        }
                    }
                }
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void loadItems(android.database.Cursor r5, java.util.ArrayList<java.lang.Long> r6, com.miui.home.launcher.RemovedComponentInfoList r7) {
            /*
            r4 = this;
        L_0x0000:
            r2 = r4.mStopped;	 Catch:{ all -> 0x0021 }
            if (r2 != 0) goto L_0x0032;
        L_0x0004:
            r2 = r5.moveToNext();	 Catch:{ all -> 0x0021 }
            if (r2 == 0) goto L_0x0032;
        L_0x000a:
            r2 = 8;
            r1 = r5.getInt(r2);	 Catch:{ Exception -> 0x0018 }
            switch(r1) {
                case 0: goto L_0x0014;
                case 1: goto L_0x0014;
                case 2: goto L_0x0026;
                case 3: goto L_0x0013;
                case 4: goto L_0x002a;
                case 5: goto L_0x002e;
                case 6: goto L_0x0013;
                case 7: goto L_0x0013;
                case 8: goto L_0x0013;
                case 9: goto L_0x0013;
                case 10: goto L_0x0013;
                case 11: goto L_0x0014;
                default: goto L_0x0013;
            };	 Catch:{ Exception -> 0x0018 }
        L_0x0013:
            goto L_0x0000;
        L_0x0014:
            r4.loadShortcut(r5, r1, r6, r7);	 Catch:{ Exception -> 0x0018 }
            goto L_0x0000;
        L_0x0018:
            r0 = move-exception;
            r2 = "Launcher.Model";
            r3 = "Desktop items loading interrupted:";
            android.util.Log.w(r2, r3, r0);	 Catch:{ all -> 0x0021 }
            goto L_0x0000;
        L_0x0021:
            r2 = move-exception;
            r5.close();
            throw r2;
        L_0x0026:
            r4.loadFolder(r5);	 Catch:{ Exception -> 0x0018 }
            goto L_0x0000;
        L_0x002a:
            r4.loadAppWidget(r5, r6);	 Catch:{ Exception -> 0x0018 }
            goto L_0x0000;
        L_0x002e:
            r4.loadGadget(r5, r6);	 Catch:{ Exception -> 0x0018 }
            goto L_0x0000;
        L_0x0032:
            r5.close();
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.miui.home.launcher.LauncherModel.LoaderTask.loadItems(android.database.Cursor, java.util.ArrayList, com.miui.home.launcher.RemovedComponentInfoList):void");
        }

        private void bindWorkspace() {
            long t = SystemClock.uptimeMillis();
            final Callbacks oldCallbacks = (Callbacks) LauncherModel.this.mCallbacks.get();
            if (oldCallbacks == null) {
                Log.w("Launcher.Model", "LoaderThread running with no launcher");
                return;
            }
            Callbacks callbacks = tryGetCallbacks(oldCallbacks);
            if (callbacks != null) {
                while (!callbacks.isReadyToBinding() && !this.mStopped) {
                    SystemClock.sleep(100);
                }
            }
            if (!this.mStopped) {
                synchronized (LauncherModel.this.mLock) {
                    LauncherAppWidgetInfo widget;
                    GadgetInfo gadget;
                    LauncherModel.this.mHandler.post(new Runnable() {
                        public void run() {
                            Callbacks callbacks = LoaderTask.this.tryGetCallbacks(oldCallbacks);
                            if (callbacks != null) {
                                callbacks.startBinding();
                            }
                        }
                    });
                    LauncherModel.this.mHandler.post(new DataCarriedRunnable(new HashMap(LauncherModel.this.mFolders)) {
                        public void run() {
                            Callbacks callbacks = LoaderTask.this.tryGetCallbacks(oldCallbacks);
                            if (callbacks != null) {
                                callbacks.bindFolders((HashMap) this.mData);
                            }
                        }
                    });
                    ArrayList<ItemInfo> itemClone = new ArrayList(LauncherModel.this.mItems);
                    int N = itemClone.size();
                    int i = 0;
                    while (i < N) {
                        final int start = i;
                        final int chunkSize = i + 16 <= N ? 16 : N - i;
                        LauncherModel.this.mHandler.post(new DataCarriedRunnable(itemClone) {
                            public void run() {
                                Callbacks callbacks = LoaderTask.this.tryGetCallbacks(oldCallbacks);
                                if (callbacks != null) {
                                    callbacks.bindItems((ArrayList) this.mData, start, start + chunkSize);
                                }
                            }
                        });
                        i += 16;
                    }
                    LauncherModel.this.mHandler.post(new Runnable() {
                        public void run() {
                            Log.d("Launcher.Model", "Going to start binding widgets soon.");
                        }
                    });
                    int currentScreen = oldCallbacks.getCurrentWorkspaceScreen();
                    ArrayList<LauncherAppWidgetInfo> appWidgetsClone = new ArrayList(LauncherModel.this.mAppWidgets);
                    N = appWidgetsClone.size();
                    if (currentScreen != -1) {
                        for (i = 0; i < N; i++) {
                            widget = (LauncherAppWidgetInfo) appWidgetsClone.get(i);
                            if (widget.screenId == ((long) currentScreen)) {
                                LauncherModel.this.mHandler.post(new DataCarriedRunnable(widget) {
                                    public void run() {
                                        Callbacks callbacks = LoaderTask.this.tryGetCallbacks(oldCallbacks);
                                        if (callbacks != null) {
                                            callbacks.bindAppWidget((LauncherAppWidgetInfo) this.mData);
                                        }
                                    }
                                });
                            }
                        }
                    }
                    for (i = 0; i < N; i++) {
                        widget = (LauncherAppWidgetInfo) appWidgetsClone.get(i);
                        if (widget.screenId != ((long) currentScreen)) {
                            LauncherModel.this.mHandler.post(new DataCarriedRunnable(widget) {
                                public void run() {
                                    Callbacks callbacks = LoaderTask.this.tryGetCallbacks(oldCallbacks);
                                    if (callbacks != null) {
                                        callbacks.bindAppWidget((LauncherAppWidgetInfo) this.mData);
                                    }
                                }
                            });
                        }
                    }
                    ArrayList<GadgetInfo> arrayList = new ArrayList(LauncherModel.this.mGadgets);
                    N = arrayList.size();
                    for (i = 0; i < N; i++) {
                        gadget = (GadgetInfo) arrayList.get(i);
                        if (gadget.screenId == ((long) currentScreen)) {
                            LauncherModel.this.mHandler.post(new DataCarriedRunnable(gadget) {
                                public void run() {
                                    Callbacks callbacks = LoaderTask.this.tryGetCallbacks(oldCallbacks);
                                    if (callbacks != null) {
                                        callbacks.bindGadget((GadgetInfo) this.mData);
                                    }
                                }
                            });
                        }
                    }
                    for (i = 0; i < N; i++) {
                        gadget = (GadgetInfo) arrayList.get(i);
                        if (gadget.screenId != ((long) currentScreen)) {
                            LauncherModel.this.mHandler.post(new DataCarriedRunnable(gadget) {
                                public void run() {
                                    Callbacks callbacks = LoaderTask.this.tryGetCallbacks(oldCallbacks);
                                    if (callbacks != null) {
                                        callbacks.bindGadget((GadgetInfo) this.mData);
                                    }
                                }
                            });
                        }
                    }
                    LauncherModel.this.mHandler.post(new Runnable() {
                        public void run() {
                            Callbacks callbacks = LoaderTask.this.tryGetCallbacks(oldCallbacks);
                            if (callbacks != null) {
                                callbacks.finishBindingSavedItems();
                            }
                        }
                    });
                    final long j = t;
                    LauncherModel.this.mHandler.post(new Runnable() {
                        public void run() {
                            Log.d("Launcher.Model", "bind workspace in " + (SystemClock.uptimeMillis() - j) + "ms");
                        }
                    });
                }
            }
        }

        public void dumpState() {
            Log.d("Launcher.Model", "mLoader.mLoaderThread.mContext=" + this.mContext);
            Log.d("Launcher.Model", "mLoader.mLoaderThread.mIsLaunching=" + this.mIsLaunching);
            Log.d("Launcher.Model", "mLoader.mLoaderThread.mStopped=" + this.mStopped);
            Log.d("Launcher.Model", "mLoader.mLoaderThread.mLoadAndBindStepFinished=" + this.mLoadAndBindStepFinished);
        }
    }

    static class PackageAndUser {
        public final String packageName;
        public final UserHandle user;

        public PackageAndUser(String pkgName, UserHandle u) {
            this.packageName = pkgName;
            this.user = u;
        }

        public int hashCode() {
            return this.packageName.hashCode() ^ this.user.hashCode();
        }

        public boolean equals(Object obj) {
            PackageAndUser other = (PackageAndUser) obj;
            return this.packageName.equals(other.packageName) && this.user.equals(other.user);
        }
    }

    static {
        sWorkerThread.start();
    }

    LauncherModel(LauncherApplication app, IconLoader iconLoader) {
        this.mApp = app;
        this.mIconLoader = iconLoader;
    }

    public Object getLocker() {
        return this.mLock;
    }

    public Drawable getFallbackIcon() {
        return this.mIconLoader.getDefaultIcon();
    }

    public static void runOnWorkerThread(Runnable r, long delay) {
        if (sWorkerThread.getThreadId() == Process.myTid() && delay == 0) {
            r.run();
        } else {
            sWorker.postDelayed(r, delay);
        }
    }

    public static void runOnWorkerThread(Runnable r) {
        runOnWorkerThread(r, 0);
    }

    static void resizeItemInDatabase(Context context, ItemInfo item, int cellX, int cellY, int spanX, int spanY) {
        item.spanX = spanX;
        item.spanY = spanY;
        item.cellX = cellX;
        item.cellY = cellY;
        ContentValues values = new ContentValues();
        values.put("container", Long.valueOf(item.container));
        values.put("spanX", Integer.valueOf(spanX));
        values.put("spanY", Integer.valueOf(spanY));
        values.put("cellX", Integer.valueOf(cellX));
        values.put("cellY", Integer.valueOf(cellY));
        updateItemInDatabase(context, item.id, values);
    }

    static void updateItemInDatabase(Context context, long id, final ContentValues values) {
        final Uri uri = Favorites.getContentUri(id);
        final ContentResolver cr = context.getContentResolver();
        runOnWorkerThread(new Runnable() {
            public void run() {
                if (cr.update(uri, values, null, null) < 0) {
                    throw new RuntimeException("update Item in database failed.");
                }
            }
        });
    }

    static void applyBatch(Context context, final String authority, final ArrayList<ContentProviderOperation> operations) {
        final ContentResolver cr = context.getContentResolver();
        runOnWorkerThread(new Runnable() {
            public void run() {
                try {
                    cr.applyBatch(authority, operations);
                } catch (RemoteException e) {
                    throw new RuntimeException("applyBatch failed with RemoteException.");
                } catch (OperationApplicationException e2) {
                    throw new RuntimeException("applyBatch failed with OperationApplicationException.");
                }
            }
        });
    }

    static int dropDragObjectIntoFolder(Context context, ShortcutInfo creater, DragObject d, FolderInfo folder) {
        ArrayList<ContentProviderOperation> ops = new ArrayList();
        int count = folder.getAdapter(context).getCount();
        if (creater != null) {
            folder.add(creater);
            int posCounter = count + 1;
            ops.add(makeMoveItemOperation(creater, folder.id, -1, -1, count, 0));
            count = posCounter;
        }
        boolean addSucceeded;
        do {
            ItemInfo info = d.getDragInfo();
            if (!(info instanceof ShortcutInfo) || folder.contains((ShortcutInfo) info)) {
                addSucceeded = false;
            } else {
                folder.add((ShortcutInfo) info);
                posCounter = count + 1;
                ops.add(makeMoveItemOperation(info, folder.id, -1, -1, count, 0));
                addSucceeded = true;
                count = posCounter;
            }
            if (d.isLastObject()) {
                break;
            }
        } while (d.nextDragView(addSucceeded));
        if (!ops.isEmpty()) {
            applyBatch(context, "com.miui.home.launcher.settings", ops);
            folder.notifyDataSetChanged();
        }
        return ops.size();
    }

    static void updateFolderItems(Context context, FolderInfo folder) {
        ArrayList<ContentProviderOperation> ops = new ArrayList();
        Iterator i$ = folder.contents.iterator();
        while (i$.hasNext()) {
            ShortcutInfo info = (ShortcutInfo) i$.next();
            ops.add(makeMoveItemOperation(info, folder.id, -1, -1, info.cellX, info.cellY));
        }
        if (!ops.isEmpty()) {
            applyBatch(context, "com.miui.home.launcher.settings", ops);
        }
    }

    static void moveItemInDatabase(Context context, ItemInfo item, long container, long screenId, int cellX, int cellY) {
        item.cellX = cellX;
        item.cellY = cellY;
        item.container = container;
        item.screenId = screenId;
        updateItemInDatabase(context, item.id, getItemPostionValues(item));
    }

    static ContentProviderOperation makeMoveItemOperation(ItemInfo item, long container, long screenId, int screenType, int cellX, int cellY) {
        item.cellX = cellX;
        item.cellY = cellY;
        item.container = container;
        item.screenId = screenId;
        return ContentProviderOperation.newUpdate(Favorites.getContentUri(item.id)).withValues(getItemPostionValues(item)).build();
    }

    private static ContentValues getItemPostionValues(ItemInfo item) {
        ContentValues values = new ContentValues();
        values.put("container", Long.valueOf(item.container));
        values.put("cellX", Integer.valueOf(item.cellX));
        values.put("cellY", Integer.valueOf(item.cellY));
        values.put("spanX", Integer.valueOf(item.spanX));
        values.put("spanY", Integer.valueOf(item.spanY));
        values.put("screen", Long.valueOf(item.screenId));
        return values;
    }

    public boolean hasShortcutWidgetActivity(String packageName) {
        boolean contains;
        synchronized (this.mLock) {
            contains = this.mInstalledShortcutWidgets.contains(packageName);
        }
        return contains;
    }

    static void updateTitleInDatabase(Context context, long id, CharSequence title) {
        ContentValues values = new ContentValues();
        values.put("title", title.toString());
        updateItemInDatabase(context, id, values);
    }

    static boolean flashDelayedUpdateItemFlags(final Context context) {
        if (sDelayedUpdateBuffer == null || sDelayedUpdateBuffer.isEmpty()) {
            return false;
        }
        runOnWorkerThread(new Runnable() {
            public void run() {
                synchronized (LauncherModel.sDelayedUpdateBuffer) {
                    ArrayList<ContentProviderOperation> ops = new ArrayList();
                    Iterator i$ = LauncherModel.sDelayedUpdateBuffer.iterator();
                    while (i$.hasNext()) {
                        ItemInfo info = (ItemInfo) i$.next();
                        ContentValues values = new ContentValues();
                        values.put("launchCount", Integer.valueOf(info.launchCount));
                        values.put("itemFlags", Integer.valueOf(info.itemFlags));
                        ops.add(ContentProviderOperation.newUpdate(Favorites.getContentUri(info.id)).withValues(values).build());
                    }
                    LauncherModel.applyBatch(context, "com.miui.home.launcher.settings", ops);
                    AnalyticalDataCollector.launchApps(context, LauncherModel.sDelayedUpdateBuffer);
                    LauncherModel.sDelayedUpdateBuffer.clear();
                }
            }
        });
        return true;
    }

    public static boolean updateItemFlagsInDatabaseDelayed(Context context, ItemInfo item) {
        if (sDelayedUpdateBuffer == null) {
            sDelayedUpdateBuffer = new HashSet();
        }
        synchronized (sDelayedUpdateBuffer) {
            sDelayedUpdateBuffer.add(item);
        }
        return true;
    }

    static void updateItemInDatabase(Context context, ItemInfo item) {
        ContentValues values = new ContentValues();
        item.onAddToDatabase(context, values);
        Log.d("Launcher.Model", String.format("Update item in database (%d, %d) of screen %d under container %d", new Object[]{Integer.valueOf(item.cellX), Integer.valueOf(item.cellY), Long.valueOf(item.screenId), Long.valueOf(item.container)}));
        updateItemInDatabase(context, item.id, values);
    }

    static void updateItemUserInDatabase(Context context, ItemInfo item) {
        ContentValues values = new ContentValues();
        item.upateUserToDatabase(context, values);
        updateItemInDatabase(context, item.id, values);
    }

    public void insertItemToDatabase(final Context context, final ItemInfo item) {
        final ContentResolver cr = context.getContentResolver();
        LauncherApplication app = Application.getLauncherApplication(context);
        if (app.getLauncherProvider() != null) {
            item.id = app.getLauncherProvider().generateNewId();
            Log.d("Launcher.Model", String.format("Insert item to database (%d, %d) of screen %d", new Object[]{Integer.valueOf(item.cellX), Integer.valueOf(item.cellY), Long.valueOf(item.screenId)}));
            runOnWorkerThread(new Runnable() {
                public void run() {
                    ContentValues values = new ContentValues();
                    item.onAddToDatabase(context, values);
                    cr.insert(Favorites.CONTENT_URI, values);
                    if (item instanceof ShortcutInfo) {
                        LauncherModel.this.onLoadShortcut((ShortcutInfo) item);
                    }
                }
            });
        }
    }

    static void deleteItemFromDatabase(final Context context, final ItemInfo item) {
        final ContentResolver cr = context.getContentResolver();
        Log.d("Launcher.Model", String.format("Deleting item from database (%d, %d) of screen %d", new Object[]{Integer.valueOf(item.cellX), Integer.valueOf(item.cellY), Long.valueOf(item.screenId)}));
        runOnWorkerThread(new Runnable() {
            public void run() {
                cr.delete(Favorites.getContentUri(item.id), null, null);
                if (item instanceof ShortcutInfo) {
                    Application.getLauncherApplication(context).getModel().onRemoveItem((ShortcutInfo) item);
                }
            }
        });
    }

    static void deleteUserFolderContentsFromDatabase(Context context, final FolderInfo info) {
        final ContentResolver cr = context.getContentResolver();
        runOnWorkerThread(new Runnable() {
            public void run() {
                cr.delete(Favorites.getContentUri(info.id), null, null);
            }
        });
    }

    public void initialize(Callbacks callbacks) {
        if (callbacks != null) {
            stopLoader();
        }
        synchronized (this.mLock) {
            this.mCallbacks = new WeakReference(callbacks);
        }
    }

    public LauncherAppsCallback getLauncherAppsCallback() {
        return this.mLauncherAppsCallback;
    }

    public void onReceive(final Context context, final Intent intent) {
        runOnWorkerThreadAndCheckForReady(new Runnable() {
            public void run() {
                LauncherModel.this.onReceiveBackground(context, intent, Process.myUserHandle());
            }
        });
    }

    void runOnWorkerThreadAndCheckForReady(final Runnable runnable) {
        final Launcher launcher = this.mApp.getLauncher();
        if (launcher == null) {
            Log.e("Launcher.Model", "Launcher is not running,process later");
        } else {
            runOnWorkerThread(new Runnable() {
                public void run() {
                    if (!launcher.isDestroyed()) {
                        if (!launcher.isReadyToBinding() || launcher.isWorkspaceLoading()) {
                            LauncherModel.runOnWorkerThread(this, 100);
                        } else {
                            runnable.run();
                        }
                    }
                }
            });
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onReceiveBackground(android.content.Context r35, android.content.Intent r36, android.os.UserHandle r37) {
        /*
        r34 = this;
        r0 = r34;
        r0 = r0.mApp;
        r35 = r0;
        r7 = new java.util.ArrayList;
        r7.<init>();
        r6 = new java.util.ArrayList;
        r6.<init>();
        r0 = r34;
        r4 = r0.mCallbacks;
        if (r4 == 0) goto L_0x002b;
    L_0x0016:
        r0 = r34;
        r4 = r0.mCallbacks;
        r4 = r4.get();
        r4 = (com.miui.home.launcher.LauncherModel.Callbacks) r4;
        r8 = r4;
    L_0x0021:
        if (r8 != 0) goto L_0x002d;
    L_0x0023:
        r4 = "Launcher.Model";
        r5 = "Nobody to tell about the new app.  Launcher is probably loading.";
        android.util.Log.w(r4, r5);
    L_0x002a:
        return;
    L_0x002b:
        r8 = 0;
        goto L_0x0021;
    L_0x002d:
        r4 = "Launcher.Model";
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r10 = "Got broadcast:";
        r5 = r5.append(r10);
        r0 = r36;
        r5 = r5.append(r0);
        r5 = r5.toString();
        android.util.Log.d(r4, r5);
        r0 = r34;
        r0 = r0.mLock;
        r33 = r0;
        monitor-enter(r33);
        r0 = r34;
        r4 = r0.mAllAppsList;	 Catch:{ all -> 0x0096 }
        r4.clear();	 Catch:{ all -> 0x0096 }
        r16 = r36.getAction();	 Catch:{ all -> 0x0096 }
        r4 = "android.intent.action.ACCESS_CONTROL_CHANGED";
        r0 = r16;
        r24 = r4.equals(r0);	 Catch:{ all -> 0x0096 }
        r4 = "com.xiaomi.market.ACTION_HD_ICON_UPDATE";
        r0 = r16;
        r4 = r4.equals(r0);	 Catch:{ all -> 0x0096 }
        if (r4 == 0) goto L_0x0099;
    L_0x006b:
        r4 = "packageName";
        r0 = r36;
        r29 = r0.getStringExtra(r4);	 Catch:{ all -> 0x0096 }
        r4 = android.text.TextUtils.isEmpty(r29);	 Catch:{ all -> 0x0096 }
        if (r4 != 0) goto L_0x00f2;
    L_0x0079:
        miui.content.res.IconCustomizer.clearCustomizedIcons(r29);	 Catch:{ all -> 0x0096 }
        r9 = new java.util.ArrayList;	 Catch:{ all -> 0x0096 }
        r9.<init>();	 Catch:{ all -> 0x0096 }
        r0 = r29;
        r9.add(r0);	 Catch:{ all -> 0x0096 }
        r0 = r34;
        r10 = r0.mHandler;	 Catch:{ all -> 0x0096 }
        r4 = new com.miui.home.launcher.LauncherModel$9;	 Catch:{ all -> 0x0096 }
        r5 = r34;
        r4.<init>(r6, r7, r8, r9);	 Catch:{ all -> 0x0096 }
        r10.post(r4);	 Catch:{ all -> 0x0096 }
        monitor-exit(r33);	 Catch:{ all -> 0x0096 }
        goto L_0x002a;
    L_0x0096:
        r4 = move-exception;
        monitor-exit(r33);	 Catch:{ all -> 0x0096 }
        throw r4;
    L_0x0099:
        r4 = "com.miui.home.ACTION_MOVE_TO_DESKTOP";
        r0 = r16;
        r4 = r4.equals(r0);	 Catch:{ all -> 0x0096 }
        if (r4 == 0) goto L_0x0168;
    L_0x00a3:
        r4 = "componentName";
        r0 = r36;
        r20 = r0.getStringExtra(r4);	 Catch:{ all -> 0x0096 }
        r4 = android.text.TextUtils.isEmpty(r20);	 Catch:{ all -> 0x0096 }
        if (r4 == 0) goto L_0x00bb;
    L_0x00b1:
        r4 = "Launcher.Model";
        r5 = "com.miui.home.ACTION_MOVE_TO_DESKTOP fail with empty componentName";
        android.util.Log.i(r4, r5);	 Catch:{ all -> 0x0096 }
        monitor-exit(r33);	 Catch:{ all -> 0x0096 }
        goto L_0x002a;
    L_0x00bb:
        r19 = android.content.ComponentName.unflattenFromString(r20);	 Catch:{ Exception -> 0x015d }
        if (r19 == 0) goto L_0x00f2;
    L_0x00c1:
        r17 = com.miui.home.launcher.Application.getLauncherApplication(r35);	 Catch:{ Exception -> 0x015d }
        r31 = r17.getLauncherProvider();	 Catch:{ Exception -> 0x015d }
        r0 = r31;
        r1 = r20;
        r26 = r0.queryInstalledComponentId(r1);	 Catch:{ Exception -> 0x015d }
        r4 = -1;
        r4 = (r26 > r4 ? 1 : (r26 == r4 ? 0 : -1));
        if (r4 == 0) goto L_0x00f2;
    L_0x00d7:
        r4 = com.miui.home.launcher.LauncherSettings.Favorites.getContentUri(r26);	 Catch:{ Exception -> 0x015d }
        r5 = 0;
        r10 = 0;
        r0 = r31;
        r0.delete(r4, r5, r10);	 Catch:{ Exception -> 0x015d }
        r0 = r34;
        r4 = r0.mAllAppsList;	 Catch:{ Exception -> 0x015d }
        r5 = r19.getPackageName();	 Catch:{ Exception -> 0x015d }
        r10 = 1;
        r0 = r35;
        r1 = r37;
        r4.updatePackage(r0, r5, r10, r1);	 Catch:{ Exception -> 0x015d }
    L_0x00f2:
        r0 = r34;
        r4 = r0.mAllAppsList;	 Catch:{ all -> 0x0096 }
        r4 = r4.added;	 Catch:{ all -> 0x0096 }
        r4 = r4.size();	 Catch:{ all -> 0x0096 }
        if (r4 <= 0) goto L_0x0107;
    L_0x00fe:
        r0 = r34;
        r4 = r0.mAllAppsList;	 Catch:{ all -> 0x0096 }
        r4 = r4.added;	 Catch:{ all -> 0x0096 }
        r7.addAll(r4);	 Catch:{ all -> 0x0096 }
    L_0x0107:
        r0 = r34;
        r4 = r0.mAllAppsList;	 Catch:{ all -> 0x0096 }
        r4 = r4.removed;	 Catch:{ all -> 0x0096 }
        r4 = r4.size();	 Catch:{ all -> 0x0096 }
        if (r4 <= 0) goto L_0x011c;
    L_0x0113:
        r0 = r34;
        r4 = r0.mAllAppsList;	 Catch:{ all -> 0x0096 }
        r4 = r4.removed;	 Catch:{ all -> 0x0096 }
        r6.addAll(r4);	 Catch:{ all -> 0x0096 }
    L_0x011c:
        r4 = r6.isEmpty();	 Catch:{ all -> 0x0096 }
        if (r4 != 0) goto L_0x0127;
    L_0x0122:
        r0 = r34;
        r0.onRemoveItems(r6);	 Catch:{ all -> 0x0096 }
    L_0x0127:
        r4 = r7.isEmpty();	 Catch:{ all -> 0x0096 }
        if (r4 != 0) goto L_0x0132;
    L_0x012d:
        r0 = r34;
        r0.onLoadShortcuts(r7);	 Catch:{ all -> 0x0096 }
    L_0x0132:
        r0 = r34;
        r4 = r0.mHandler;	 Catch:{ all -> 0x0096 }
        r10 = new com.miui.home.launcher.LauncherModel$10;	 Catch:{ all -> 0x0096 }
        r11 = r34;
        r12 = r6;
        r13 = r7;
        r14 = r8;
        r15 = r36;
        r10.<init>(r12, r13, r14, r15);	 Catch:{ all -> 0x0096 }
        r4.post(r10);	 Catch:{ all -> 0x0096 }
        r0 = r34;
        r4 = r0.mHandler;	 Catch:{ all -> 0x0096 }
        r5 = new com.miui.home.launcher.LauncherModel$11;	 Catch:{ all -> 0x0096 }
        r0 = r34;
        r5.<init>(r8);	 Catch:{ all -> 0x0096 }
        r4.post(r5);	 Catch:{ all -> 0x0096 }
        r0 = r34;
        r4 = r0.mAllAppsList;	 Catch:{ all -> 0x0096 }
        r4.clear();	 Catch:{ all -> 0x0096 }
        monitor-exit(r33);	 Catch:{ all -> 0x0096 }
        goto L_0x002a;
    L_0x015d:
        r22 = move-exception;
        r4 = "Launcher.Model";
        r5 = "com.miui.home.ACTION_MOVE_TO_DESKTOP fail";
        r0 = r22;
        android.util.Log.d(r4, r5, r0);	 Catch:{ all -> 0x0096 }
        goto L_0x00f2;
    L_0x0168:
        r4 = "android.intent.action.PACKAGE_CHANGED";
        r0 = r16;
        r4 = r4.equals(r0);	 Catch:{ all -> 0x0096 }
        if (r4 != 0) goto L_0x0188;
    L_0x0172:
        r4 = "android.intent.action.PACKAGE_REMOVED";
        r0 = r16;
        r4 = r4.equals(r0);	 Catch:{ all -> 0x0096 }
        if (r4 != 0) goto L_0x0188;
    L_0x017c:
        r4 = "android.intent.action.PACKAGE_ADDED";
        r0 = r16;
        r4 = r4.equals(r0);	 Catch:{ all -> 0x0096 }
        if (r4 != 0) goto L_0x0188;
    L_0x0186:
        if (r24 == 0) goto L_0x030e;
    L_0x0188:
        if (r24 == 0) goto L_0x01f8;
    L_0x018a:
        r4 = "android.intent.extra.PACKAGES";
        r0 = r36;
        r29 = r0.getStringExtra(r4);	 Catch:{ all -> 0x0096 }
    L_0x0192:
        r4 = "android.intent.extra.REPLACING";
        r5 = 0;
        r0 = r36;
        r4 = r0.getBooleanExtra(r4, r5);	 Catch:{ all -> 0x0096 }
        if (r4 != 0) goto L_0x01a9;
    L_0x019d:
        r0 = r34;
        r4 = r0.mLoadedPresetPackages;	 Catch:{ all -> 0x0096 }
        r0 = r29;
        r4 = r4.contains(r0);	 Catch:{ all -> 0x0096 }
        if (r4 == 0) goto L_0x0201;
    L_0x01a9:
        r32 = 1;
    L_0x01ab:
        if (r24 == 0) goto L_0x0204;
    L_0x01ad:
        r4 = "android.intent.extra.KEY_EVENT";
        r5 = 0;
        r0 = r36;
        r4 = r0.getIntExtra(r4, r5);	 Catch:{ all -> 0x0096 }
        r5 = 33554432; // 0x2000000 float:9.403955E-38 double:1.6578092E-316;
        r4 = r4 & r5;
        r5 = 33554432; // 0x2000000 float:9.403955E-38 double:1.6578092E-316;
        if (r4 != r5) goto L_0x0204;
    L_0x01bd:
        r25 = 1;
    L_0x01bf:
        r4 = "android.intent.extra.DONT_KILL_APP";
        r5 = 0;
        r0 = r36;
        r21 = r0.getBooleanExtra(r4, r5);	 Catch:{ all -> 0x0096 }
        r4 = android.os.Build.VERSION.SDK_INT;	 Catch:{ all -> 0x0096 }
        r5 = 14;
        if (r4 < r5) goto L_0x01e1;
    L_0x01ce:
        r4 = r36.isExcludingStopped();	 Catch:{ all -> 0x0096 }
        if (r4 == 0) goto L_0x0207;
    L_0x01d4:
        r4 = "android.intent.action.PACKAGE_CHANGED";
        r0 = r16;
        r4 = r4.equals(r0);	 Catch:{ all -> 0x0096 }
        if (r4 == 0) goto L_0x0207;
    L_0x01de:
        r4 = 1;
    L_0x01df:
        r21 = r21 | r4;
    L_0x01e1:
        if (r24 == 0) goto L_0x0209;
    L_0x01e3:
        r4 = "*";
        r0 = r29;
        r4 = r4.equals(r0);	 Catch:{ all -> 0x0096 }
        if (r4 == 0) goto L_0x0209;
    L_0x01ed:
        r4 = 0;
        r0 = r34;
        r1 = r35;
        r0.startLoader(r1, r4);	 Catch:{ all -> 0x0096 }
        monitor-exit(r33);	 Catch:{ all -> 0x0096 }
        goto L_0x002a;
    L_0x01f8:
        r4 = r36.getData();	 Catch:{ all -> 0x0096 }
        r29 = r4.getSchemeSpecificPart();	 Catch:{ all -> 0x0096 }
        goto L_0x0192;
    L_0x0201:
        r32 = 0;
        goto L_0x01ab;
    L_0x0204:
        r25 = 0;
        goto L_0x01bf;
    L_0x0207:
        r4 = 0;
        goto L_0x01df;
    L_0x0209:
        if (r24 == 0) goto L_0x0228;
    L_0x020b:
        r4 = "android.intent.extra.KEY_EVENT";
        r5 = 0;
        r0 = r36;
        r4 = r0.getIntExtra(r4, r5);	 Catch:{ all -> 0x0096 }
        r5 = 33554432; // 0x2000000 float:9.403955E-38 double:1.6578092E-316;
        r4 = r4 & r5;
        r5 = "android.intent.extra.INITIAL_INTENTS";
        r10 = 0;
        r0 = r36;
        r5 = r0.getIntExtra(r5, r10);	 Catch:{ all -> 0x0096 }
        r10 = 33554432; // 0x2000000 float:9.403955E-38 double:1.6578092E-316;
        r5 = r5 & r10;
        if (r4 != r5) goto L_0x0228;
    L_0x0225:
        monitor-exit(r33);	 Catch:{ all -> 0x0096 }
        goto L_0x002a;
    L_0x0228:
        if (r29 == 0) goto L_0x0230;
    L_0x022a:
        r4 = r29.length();	 Catch:{ all -> 0x0096 }
        if (r4 != 0) goto L_0x0233;
    L_0x0230:
        monitor-exit(r33);	 Catch:{ all -> 0x0096 }
        goto L_0x002a;
    L_0x0233:
        miui.content.res.IconCustomizer.clearCustomizedIcons(r29);	 Catch:{ all -> 0x0096 }
        r4 = "android.intent.action.PACKAGE_CHANGED";
        r0 = r16;
        r4 = r4.equals(r0);	 Catch:{ all -> 0x0096 }
        if (r4 == 0) goto L_0x025c;
    L_0x0240:
        r0 = r34;
        r4 = r0.mAllAppsList;	 Catch:{ all -> 0x0096 }
        r0 = r35;
        r1 = r29;
        r2 = r21;
        r3 = r37;
        r4.updatePackage(r0, r1, r2, r3);	 Catch:{ all -> 0x0096 }
        r4 = r35.getApplicationContext();	 Catch:{ all -> 0x0096 }
        r4 = com.market.sdk.MarketManager.getManager(r4);	 Catch:{ all -> 0x0096 }
        r4.updateApplicationEnableState();	 Catch:{ all -> 0x0096 }
        goto L_0x00f2;
    L_0x025c:
        r4 = "android.intent.action.PACKAGE_REMOVED";
        r0 = r16;
        r4 = r4.equals(r0);	 Catch:{ all -> 0x0096 }
        if (r4 == 0) goto L_0x0277;
    L_0x0266:
        if (r32 != 0) goto L_0x00f2;
    L_0x0268:
        r0 = r34;
        r4 = r0.mAllAppsList;	 Catch:{ all -> 0x0096 }
        r0 = r29;
        r1 = r21;
        r2 = r37;
        r4.removePackage(r0, r1, r2);	 Catch:{ all -> 0x0096 }
        goto L_0x00f2;
    L_0x0277:
        r4 = "android.intent.action.PACKAGE_ADDED";
        r0 = r16;
        r4 = r4.equals(r0);	 Catch:{ all -> 0x0096 }
        if (r4 == 0) goto L_0x02eb;
    L_0x0281:
        r0 = r34;
        r4 = r0.mProgressingPackages;	 Catch:{ all -> 0x0096 }
        r0 = r29;
        r4 = r4.contains(r0);	 Catch:{ all -> 0x0096 }
        if (r4 == 0) goto L_0x02b3;
    L_0x028d:
        r4 = android.os.Process.myUserHandle();	 Catch:{ all -> 0x0096 }
        r0 = r37;
        r4 = r0.equals(r4);	 Catch:{ all -> 0x0096 }
        if (r4 == 0) goto L_0x02b3;
    L_0x0299:
        r0 = r34;
        r4 = r0.mAllAppsList;	 Catch:{ all -> 0x0096 }
        r0 = r35;
        r1 = r29;
        r2 = r21;
        r3 = r37;
        r4.updatePackage(r0, r1, r2, r3);	 Catch:{ all -> 0x0096 }
        r0 = r34;
        r4 = r0.mProgressingPackages;	 Catch:{ all -> 0x0096 }
        r0 = r29;
        r4.remove(r0);	 Catch:{ all -> 0x0096 }
        goto L_0x00f2;
    L_0x02b3:
        if (r32 != 0) goto L_0x02c5;
    L_0x02b5:
        r0 = r34;
        r4 = r0.mAllAppsList;	 Catch:{ all -> 0x0096 }
        r5 = 0;
        r0 = r35;
        r1 = r29;
        r2 = r37;
        r4.addPackage(r0, r1, r5, r2);	 Catch:{ all -> 0x0096 }
        goto L_0x00f2;
    L_0x02c5:
        r0 = r34;
        r4 = r0.mAllAppsList;	 Catch:{ all -> 0x0096 }
        r0 = r35;
        r1 = r29;
        r2 = r21;
        r3 = r37;
        r4.updatePackage(r0, r1, r2, r3);	 Catch:{ all -> 0x0096 }
        r0 = r34;
        r4 = r0.mLoadedPresetPackages;	 Catch:{ all -> 0x0096 }
        r0 = r29;
        r4 = r4.contains(r0);	 Catch:{ all -> 0x0096 }
        if (r4 == 0) goto L_0x00f2;
    L_0x02e0:
        r0 = r34;
        r4 = r0.mLoadedPresetPackages;	 Catch:{ all -> 0x0096 }
        r0 = r29;
        r4.remove(r0);	 Catch:{ all -> 0x0096 }
        goto L_0x00f2;
    L_0x02eb:
        if (r24 == 0) goto L_0x00f2;
    L_0x02ed:
        if (r25 == 0) goto L_0x02fd;
    L_0x02ef:
        r0 = r34;
        r4 = r0.mAllAppsList;	 Catch:{ all -> 0x0096 }
        r5 = 1;
        r0 = r29;
        r1 = r37;
        r4.removePackage(r0, r5, r1);	 Catch:{ all -> 0x0096 }
        goto L_0x00f2;
    L_0x02fd:
        r0 = r34;
        r4 = r0.mAllAppsList;	 Catch:{ all -> 0x0096 }
        r0 = r35;
        r1 = r29;
        r2 = r21;
        r3 = r37;
        r4.updatePackage(r0, r1, r2, r3);	 Catch:{ all -> 0x0096 }
        goto L_0x00f2;
    L_0x030e:
        r4 = "android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE";
        r0 = r16;
        r4 = r4.equals(r0);	 Catch:{ all -> 0x0096 }
        if (r4 != 0) goto L_0x0322;
    L_0x0318:
        r4 = "android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE";
        r0 = r16;
        r4 = r4.equals(r0);	 Catch:{ all -> 0x0096 }
        if (r4 == 0) goto L_0x00f2;
    L_0x0322:
        r30 = 0;
        r4 = "android.intent.extra.changed_package_list";
        r0 = r36;
        r30 = r0.getStringArrayExtra(r4);	 Catch:{ all -> 0x0096 }
        r4 = android.os.Build.VERSION.SDK_INT;	 Catch:{ all -> 0x0096 }
        r5 = 22;
        if (r4 < r5) goto L_0x0348;
    L_0x0332:
        if (r30 != 0) goto L_0x0348;
    L_0x0334:
        r4 = "android.intent.extra.changed_package_list";
        r0 = r36;
        r4 = r0.getStringArrayListExtra(r4);	 Catch:{ Exception -> 0x0352 }
        r5 = 0;
        r5 = new java.lang.String[r5];	 Catch:{ Exception -> 0x0352 }
        r4 = r4.toArray(r5);	 Catch:{ Exception -> 0x0352 }
        r0 = r4;
        r0 = (java.lang.String[]) r0;	 Catch:{ Exception -> 0x0352 }
        r30 = r0;
    L_0x0348:
        if (r30 == 0) goto L_0x034f;
    L_0x034a:
        r0 = r30;
        r4 = r0.length;	 Catch:{ all -> 0x0096 }
        if (r4 != 0) goto L_0x035b;
    L_0x034f:
        monitor-exit(r33);	 Catch:{ all -> 0x0096 }
        goto L_0x002a;
    L_0x0352:
        r22 = move-exception;
        r4 = "Launcher.Model";
        r5 = "get changed package list failed";
        android.util.Log.i(r4, r5);	 Catch:{ all -> 0x0096 }
        goto L_0x0348;
    L_0x035b:
        r4 = "android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE";
        r0 = r16;
        r4 = r4.equals(r0);	 Catch:{ all -> 0x0096 }
        if (r4 == 0) goto L_0x039e;
    L_0x0365:
        r18 = r30;
        r0 = r18;
        r0 = r0.length;	 Catch:{ all -> 0x0096 }
        r28 = r0;
        r23 = 0;
    L_0x036e:
        r0 = r23;
        r1 = r28;
        if (r0 >= r1) goto L_0x00f2;
    L_0x0374:
        r29 = r18[r23];	 Catch:{ all -> 0x0096 }
        r0 = r34;
        r4 = r0.mAllAppsList;	 Catch:{ SQLiteException -> 0x0393 }
        r5 = 0;
        r0 = r29;
        r1 = r37;
        r4.removePackage(r0, r5, r1);	 Catch:{ SQLiteException -> 0x0393 }
        r0 = r34;
        r4 = r0.mAllAppsList;	 Catch:{ SQLiteException -> 0x0393 }
        r5 = 0;
        r0 = r35;
        r1 = r29;
        r2 = r37;
        r4.addPackage(r0, r1, r5, r2);	 Catch:{ SQLiteException -> 0x0393 }
        r23 = r23 + 1;
        goto L_0x036e;
    L_0x0393:
        r22 = move-exception;
        r4 = "Launcher.Model";
        r5 = "database didnot ready,ignore this change";
        android.util.Log.d(r4, r5);	 Catch:{ all -> 0x0096 }
        monitor-exit(r33);	 Catch:{ all -> 0x0096 }
        goto L_0x002a;
    L_0x039e:
        r4 = "android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE";
        r0 = r16;
        r4 = r4.equals(r0);	 Catch:{ all -> 0x0096 }
        if (r4 == 0) goto L_0x00f2;
    L_0x03a8:
        r18 = r30;
        r0 = r18;
        r0 = r0.length;	 Catch:{ all -> 0x0096 }
        r28 = r0;
        r23 = 0;
    L_0x03b1:
        r0 = r23;
        r1 = r28;
        if (r0 >= r1) goto L_0x00f2;
    L_0x03b7:
        r29 = r18[r23];	 Catch:{ all -> 0x0096 }
        r0 = r34;
        r4 = r0.mAllAppsList;	 Catch:{ all -> 0x0096 }
        r5 = 0;
        r0 = r29;
        r1 = r37;
        r4.removePackage(r0, r5, r1);	 Catch:{ all -> 0x0096 }
        r23 = r23 + 1;
        goto L_0x03b1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.miui.home.launcher.LauncherModel.onReceiveBackground(android.content.Context, android.content.Intent, android.os.UserHandle):void");
    }

    void forceReload(Context context) {
        boolean isLaunching = resetLoadedState(true);
        this.mHandler.cancel();
        startLoader(context, isLaunching);
    }

    private boolean resetLoadedState(boolean resetWorkspaceLoaded) {
        boolean isLaunching;
        synchronized (this.mLock) {
            isLaunching = stopLoaderLocked();
            if (resetWorkspaceLoaded) {
                this.mWorkspaceLoaded = false;
            }
        }
        return isLaunching;
    }

    private boolean stopLoaderLocked() {
        boolean isLaunching = false;
        LoaderTask oldTask = this.mLoaderTask;
        if (oldTask != null) {
            if (oldTask.isLaunching()) {
                isLaunching = true;
            }
            oldTask.stopLocked();
        }
        return isLaunching;
    }

    public void startLoader(Context context, boolean isLaunching) {
        synchronized (this.mLock) {
            Log.d("Launcher.Model", "startLoader isLaunching=" + isLaunching);
            if (!(this.mCallbacks == null || this.mCallbacks.get() == null)) {
                isLaunching = isLaunching || stopLoaderLocked();
                this.mLoaderTask = new LoaderTask(context, isLaunching, Application.getLauncherApplication(context).isJustRestoreFinished());
                sWorkerThread.setPriority(5);
                sWorker.post(this.mLoaderTask);
            }
        }
    }

    public void stopLoader() {
        if (this.mLoaderTask != null) {
            this.mLoaderTask.stopLocked();
        }
    }

    private void onLoadShortcuts(ArrayList<ShortcutInfo> infoList) {
        synchronized (this.mLock) {
            Iterator i$ = infoList.iterator();
            while (i$.hasNext()) {
                onLoadShortcut((ShortcutInfo) i$.next());
            }
        }
    }

    private String makeUniquelyShortcutKey(ShortcutInfo info) {
        if (info.intent == null) {
            return "";
        }
        Intent i = new Intent(info.intent);
        i.setFlags(0);
        if (i.getCategories() != null) {
            i.getCategories().clear();
        }
        if (i.getComponent() != null) {
            try {
                if ((this.mApp.getPackageManager().getApplicationInfo(i.getComponent().getPackageName(), 0).flags & 1) == 0) {
                    i.setAction(null);
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            if (i.getComponent().getPackageName().equals(i.getPackage())) {
                i.setPackage(null);
            }
        }
        return i.toUri(0) + ";#shortcut_name:" + info.getTitle(this.mApp) + ";end";
    }

    private void onLoadShortcut(ShortcutInfo info) {
        synchronized (this.mLock) {
            if (info.intent != null) {
                this.mLoadedUris.add(makeUniquelyShortcutKey(info));
                String name = info.getPackageName();
                if (ProgressManager.isProgressType(info)) {
                    this.mProgressingPackages.add(name);
                }
                if (name != null) {
                    this.mLoadedPackages.add(new PackageAndUser(name, info.getUser()));
                    if (info.itemType == 0 && !this.mInstalledShortcutWidgets.contains(name)) {
                        this.mShortcutWidgetQueryIntent.setPackage(name);
                        if (!this.mApp.getPackageManager().queryIntentActivities(this.mShortcutWidgetQueryIntent, 0).isEmpty()) {
                            this.mInstalledShortcutWidgets.add(name);
                        }
                    }
                    if (info.isPresetApp()) {
                        this.mLoadedPresetPackages.add(name);
                    }
                }
            }
        }
    }

    private void onRemoveItems(ArrayList<RemoveInfo> removeList) {
        synchronized (this.mLock) {
            Iterator i$ = removeList.iterator();
            while (i$.hasNext()) {
                RemoveInfo info = (RemoveInfo) i$.next();
                onRemovePackage(info);
                this.mLoadedPackages.remove(new PackageAndUser(info.packageName, info.user));
                if (!info.replacing && info.user.equals(Process.myUserHandle())) {
                    this.mInstalledShortcutWidgets.remove(info.packageName);
                }
            }
        }
    }

    private void onRemoveItem(ShortcutInfo info) {
        synchronized (this.mLock) {
            if (info.intent != null) {
                String name = info.getPackageName();
                if (name != null && (info.itemType != 1 || info.isPresetApp())) {
                    this.mLoadedPackages.remove(new PackageAndUser(name, info.getUser()));
                    if (info.getUser().equals(Process.myUserHandle())) {
                        this.mInstalledShortcutWidgets.remove(name);
                    }
                    if (info.isPresetApp()) {
                        this.mLoadedPresetPackages.remove(name);
                    }
                }
                this.mLoadedUris.remove(makeUniquelyShortcutKey(info));
                if (info.getUser().equals(Process.myUserHandle())) {
                    this.mProgressingPackages.remove(name);
                }
            }
        }
    }

    private void onRemovePackage(RemoveInfo info) {
        Iterator i$ = this.mApp.getLauncher().getAllLoadedApps().iterator();
        while (i$.hasNext()) {
            ShortcutInfo shortcutInfo = (ShortcutInfo) i$.next();
            if (!TextUtils.isEmpty(shortcutInfo.getPackageName()) && shortcutInfo.getPackageName().equals(info.packageName) && shortcutInfo.getUser().equals(info.user)) {
                if (shortcutInfo.itemType == 0 || (shortcutInfo.itemType == 1 && !info.replacing)) {
                    onRemoveItem(shortcutInfo);
                }
            }
        }
        if (info.user.equals(Process.myUserHandle())) {
            this.mProgressingPackages.remove(info.packageName);
        }
    }

    public static CharSequence loadTitle(Context context, CharSequence title) {
        if (TextUtils.isEmpty(title) || -1 != Character.digit(title.charAt(0), 10)) {
            return title;
        }
        String strTitle = title.toString();
        Resources res = null;
        if (strTitle.startsWith(context.getPackageName())) {
            res = context.getResources();
        } else {
            try {
                int pkgPos = strTitle.indexOf(58);
                if (-1 != pkgPos) {
                    res = context.getPackageManager().getResourcesForApplication(strTitle.substring(0, pkgPos));
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (res == null) {
            return title;
        }
        int titleID = res.getIdentifier(strTitle, null, null);
        if (titleID != 0) {
            return res.getString(titleID);
        }
        return title;
    }

    public ShortcutInfo getShortcutInfo(PackageManager manager, Intent intent, Context context, Cursor c, int iconIndex, int titleIndex, int profileIdIndex) {
        Drawable icon = null;
        ShortcutInfo info = new ShortcutInfo();
        ComponentName componentName = intent.getComponent();
        if (componentName == null) {
            return null;
        }
        ResolveInfo resolveInfo = manager.resolveActivity(intent, 0);
        if (resolveInfo != null) {
            icon = this.mIconLoader.getIcon(componentName, resolveInfo);
        }
        if (icon == null && c != null) {
            icon = new BitmapDrawable(context.getResources(), getIconBitmapFromCursor(c, iconIndex));
        }
        UserHandle user = ((UserManager) context.getSystemService("user")).getUserForSerialNumber((long) c.getInt(profileIdIndex));
        if (user == null) {
            return null;
        }
        info.setUser(user);
        if (icon == null) {
            icon = getFallbackIcon();
            info.usingFallbackIcon = true;
        }
        info.setIcon(PortableUtils.getUserBadgedIcon(context, icon, info.getUser()));
        if (resolveInfo != null) {
            info.setTitle(resolveInfo.activityInfo.loadLabel(manager), context);
        }
        if (info.getTitle(context) == null && c != null) {
            info.setTitle(c.getString(titleIndex), context);
        }
        if (info.getTitle(context) == null) {
            info.setTitle(componentName.getClassName(), context);
        }
        info.itemType = 0;
        return info;
    }

    ShortcutInfo getShortcutInfo(Intent intent, Cursor c, Context context, int itemType, int iconTypeIndex, int iconPackageIndex, int iconResourceIndex, int iconIndex, int titleIndex, int profileIdIndex) {
        ShortcutInfo info = new ShortcutInfo();
        info.intent = intent;
        info.itemType = itemType;
        info.itemFlags = c.getInt(19);
        String packageName = c.getString(iconPackageIndex);
        info.setTitle(c.getString(titleIndex), context);
        UserHandle user = ((UserManager) context.getSystemService("user")).getUserForSerialNumber((long) c.getInt(profileIdIndex));
        if (user == null) {
            return null;
        }
        info.setUser(user);
        try {
            info.isRetained = c.getInt(21) == 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        info.mIconType = c.getInt(iconTypeIndex);
        Drawable icon;
        switch (info.mIconType) {
            case 0:
                String resourceName = c.getString(iconResourceIndex);
                if (context.getPackageName().equals(packageName)) {
                    info.isRetained = true;
                    String retainedIconName = resourceName.substring(resourceName.lastIndexOf(47) + 1);
                    icon = IconCustomizer.getCustomizedIcon(context, retainedIconName + ".png");
                    if (icon == null) {
                        icon = IconCustomizer.getCustomizedIcon(context, retainedIconName, null, getDrawableFromPackage(context, packageName, resourceName));
                    }
                } else {
                    icon = getDrawableFromPackage(context, packageName, resourceName);
                }
                if (icon != null) {
                    setIcon(info, icon);
                    break;
                }
                return null;
            case 1:
                if (info.isPresetApp()) {
                    icon = IconCustomizer.getCustomizedIcon(context, packageName);
                    if (icon == null) {
                        icon = IconCustomizer.getCustomizedIcon(context, packageName, null, new BitmapDrawable(context.getResources(), getIconBitmapFromCursor(c, iconIndex)));
                    }
                } else {
                    icon = new BitmapDrawable(context.getResources(), getIconBitmapFromCursor(c, iconIndex));
                }
                if (icon != null) {
                    setIcon(info, icon);
                    break;
                }
                return null;
            case 3:
                info.loadToggleInfo(context);
                break;
            case 4:
                String iconUri = c.getString(iconResourceIndex);
                if (iconUri != null) {
                    info.getAppInfo().iconUri = Uri.parse(iconUri);
                    break;
                }
                break;
            case 5:
                info.loadSettingsInfo(context);
                break;
        }
        info.wrapIconWithBorder(context);
        return info;
    }

    private Drawable getDrawableFromPackage(Context context, String packageName, String resourceName) {
        Drawable drawable = null;
        try {
            Resources resources = context.getPackageManager().getResourcesForApplication(packageName);
            if (resources != null) {
                drawable = resources.getDrawable(resources.getIdentifier(resourceName, null, null));
            }
        } catch (Exception e) {
        }
        return drawable;
    }

    private void setIcon(ShortcutInfo info, Drawable icon) {
        if (icon != null) {
            info.setIcon(PortableUtils.getUserBadgedIcon(this.mApp.getLauncher(), icon, info.getUser()));
            return;
        }
        icon = getFallbackIcon();
        info.usingFallbackIcon = true;
    }

    Bitmap getIconBitmapFromCursor(Cursor c, int iconIndex) {
        byte[] data = c.getBlob(iconIndex);
        try {
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        } catch (Exception e) {
            return null;
        }
    }

    ShortcutInfo getProgressItemInfo(Context context, Intent data, String packageName) {
        ShortcutInfo info = new ShortcutInfo();
        info.container = -100;
        info.itemType = 11;
        info.mIconType = 4;
        infoFromShortcutIntent(context, data, info);
        if (info == null || info.intent == null) {
            return null;
        }
        info.spanY = 1;
        info.spanX = 1;
        info.isLandscapePos = DeviceConfig.isScreenOrientationLandscape();
        info.wrapIconWithBorder(context);
        return info;
    }

    ShortcutInfo getShortcutInfo(Context context, Intent data, CellInfo cellInfo) {
        long j = -1;
        int i = 0;
        if (data == null) {
            return null;
        }
        ShortcutInfo info = new ShortcutInfo();
        infoFromShortcutIntent(context, data, info);
        if (info == null || info.intent == null) {
            return null;
        }
        long j2;
        int i2;
        if (!(info.mIconType == 2 || info.mIconType == 3)) {
            synchronized (this.mLock) {
                if (info.itemType != 1) {
                    ComponentName component = info.intent.getComponent();
                    if (!(component == null || this.mLoadedPackages.contains(new PackageAndUser(component.getPackageName(), info.getUser())))) {
                        return null;
                    }
                }
                if (this.mLoadedUris.contains(makeUniquelyShortcutKey(info))) {
                    return null;
                }
            }
        }
        if (cellInfo != null) {
            j2 = (long) cellInfo.container;
        } else {
            j2 = -1;
        }
        info.container = j2;
        info.spanY = 1;
        info.spanX = 1;
        if (cellInfo != null) {
            j = cellInfo.screenId;
        }
        info.screenId = j;
        if (cellInfo != null) {
            i2 = cellInfo.cellX;
        } else {
            i2 = 0;
        }
        info.cellX = i2;
        if (cellInfo != null) {
            i = cellInfo.cellY;
        }
        info.cellY = i;
        info.isLandscapePos = DeviceConfig.isScreenOrientationLandscape();
        info.wrapIconWithBorder(context);
        return info;
    }

    private ShortcutInfo infoFromShortcutIntent(Context context, Intent data, ShortcutInfo info) {
        Parcelable extra;
        info.intent = (Intent) data.getParcelableExtra("android.intent.extra.shortcut.INTENT");
        info.setTitle(data.getStringExtra("android.intent.extra.shortcut.NAME"), context);
        info.mIconType = 0;
        if (info.intent == null) {
            Log.e("Launcher.Model", "Can't construct ShorcutInfo with null intent");
            return null;
        } else if ("com.miui.action.TOGGLE_SHURTCUT".equals(info.intent.getAction())) {
            info.loadToggleInfo(context);
            info.mIconType = 3;
            return info;
        } else if ("com.miui.action.SETTINGS_SHURTCUT".equals(info.intent.getAction())) {
            info.loadSettingsInfo(context);
            info.mIconType = 5;
            return info;
        } else if ("com.miui.action.DOWNLOADING_APP".equals(info.intent.getAction())) {
            info.mIconType = 4;
            extra = data.getParcelableExtra("iconUri");
            if (extra == null || !(extra instanceof Uri)) {
                return info;
            }
            info.getAppInfo().iconUri = (Uri) extra;
            return info;
        } else {
            Parcelable bitmap = data.getParcelableExtra("android.intent.extra.shortcut.ICON");
            Drawable icon = null;
            if (bitmap == null || !(bitmap instanceof Bitmap)) {
                extra = data.getParcelableExtra("android.intent.extra.shortcut.ICON_RESOURCE");
                if (extra != null && (extra instanceof ShortcutIconResource)) {
                    try {
                        info.iconResource = (ShortcutIconResource) extra;
                        Resources resources = context.getPackageManager().getResourcesForApplication(info.iconResource.packageName);
                        icon = resources.getDrawable(resources.getIdentifier(info.iconResource.resourceName, null, null));
                    } catch (Exception e) {
                        Log.w("Launcher.Model", "Could not load shortcut icon: " + extra);
                    }
                }
            } else {
                icon = new BitmapDrawable(context.getResources(), (Bitmap) bitmap);
                info.mIconType = 1;
            }
            if (icon == null) {
                icon = getFallbackIcon();
                info.usingFallbackIcon = true;
            } else if (data.getBooleanExtra("android.intent.extra.CUSTOMIZED_ICON_SHORTCUT", false)) {
                info.itemFlags |= 2;
            }
            info.setIcon(icon);
            if (!(icon instanceof BitmapDrawable)) {
                return info;
            }
            info.setIconBitmap(((BitmapDrawable) icon).getBitmap());
            return info;
        }
    }

    void updateSavedIcon(Context context, ShortcutInfo info, Cursor c, int iconIndex) {
        if (info.onExternalStorage && info.mIconType == 0 && !info.usingFallbackIcon) {
            boolean needSave = false;
            byte[] data = c.getBlob(iconIndex);
            if (data != null) {
                try {
                    Bitmap saved = BitmapFactory.decodeByteArray(data, 0, data.length);
                    Drawable loaded = info.getIcon(context, this.mIconLoader, info.getIcon());
                    if (loaded instanceof BitmapDrawable) {
                        if (saved.sameAs(((BitmapDrawable) loaded).getBitmap())) {
                            needSave = false;
                        } else {
                            needSave = true;
                        }
                    }
                } catch (Exception e) {
                    needSave = true;
                }
            } else {
                needSave = true;
            }
            if (needSave) {
                Log.d("Launcher.Model", "going to save icon bitmap for info=" + info);
                updateItemInDatabase(context, info);
            }
        }
    }

    private static FolderInfo findOrMakeUserFolder(HashMap<Long, FolderInfo> folders, long id) {
        FolderInfo folderInfo = (FolderInfo) folders.get(Long.valueOf(id));
        if (folderInfo != null && (folderInfo instanceof FolderInfo)) {
            return folderInfo;
        }
        folderInfo = new FolderInfo();
        folders.put(Long.valueOf(id), folderInfo);
        return folderInfo;
    }

    public void loadFreeStyle() {
        runOnWorkerThread(new Runnable() {
            public void run() {
                LauncherModel.this.loadFreeStyleNow();
            }
        });
    }

    private void loadFreeStyleNow() {
        final FreeStyle freeStyle = new FreeStyleSerializer(this.mApp).load();
        final Callbacks callbacks = this.mCallbacks == null ? null : (Callbacks) this.mCallbacks.get();
        if (callbacks != null) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    callbacks.bindFreeStyleLoaded(freeStyle);
                }
            });
        }
    }

    public void dumpState() {
        Log.d("Launcher.Model", "mCallbacks=" + this.mCallbacks);
        if (this.mLoaderTask != null) {
            this.mLoaderTask.dumpState();
        } else {
            Log.d("Launcher.Model", "mLoaderTask=null");
        }
    }

    private void removeEmptyFolder(ArrayList<Long> itemsToRemove) {
        Iterator folderIterator = this.mFolders.entrySet().iterator();
        while (folderIterator.hasNext()) {
            FolderInfo folderInfo = (FolderInfo) ((Entry) folderIterator.next()).getValue();
            if (folderInfo.count() == 0) {
                folderIterator.remove();
                itemsToRemove.add(Long.valueOf(folderInfo.id));
                Iterator itemIterator = this.mItems.iterator();
                while (itemIterator.hasNext()) {
                    if (((ItemInfo) itemIterator.next()).id == folderInfo.id) {
                        itemIterator.remove();
                        break;
                    }
                }
            }
        }
    }
}
