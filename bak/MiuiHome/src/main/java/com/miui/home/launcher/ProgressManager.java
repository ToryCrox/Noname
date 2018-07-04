package com.miui.home.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.miui.Shell;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings.System;
import android.text.TextUtils;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class ProgressManager extends BroadcastReceiver {
    private static boolean mObservingUri = false;
    private static ProgressManager sProgressManager;
    private ApplicationProgressProcessor mApplicationProcessor;
    private Runnable mCheckProgressTask = new Runnable() {
        public void run() {
            ProgressManager.this.checkProgress();
            if (ProgressManager.mObservingUri) {
                ProgressManager.this.mProgressHandler.postDelayed(this, 500);
            }
        }
    };
    private Launcher mLauncher;
    private Handler mProgressHandler;
    private HandlerThread mProgressThread;
    private long mQueryCheckCode = 0;
    private ArrayList<WeakReference<ProgressProcessor>> mWallpaperProcessor = new ArrayList();

    public interface ProgressProcessor {
        void checkProgress(Context context);

        void clear();

        void handleProgressUpdate(Context context, Intent intent);

        boolean isStop();
    }

    public ProgressManager(Context context) {
        if (this.mProgressThread == null) {
            this.mProgressThread = new HandlerThread("ProgressThread");
            this.mProgressThread.start();
        }
        if (this.mProgressHandler == null) {
            this.mProgressHandler = new Handler(this.mProgressThread.getLooper());
        }
        this.mApplicationProcessor = new ApplicationProgressProcessor(context);
    }

    public static ProgressManager getManager(Context context) {
        if (sProgressManager == null) {
            sProgressManager = new ProgressManager(context);
        }
        return sProgressManager;
    }

    public void addWallpaperProcessor(ProgressProcessor processor) {
        synchronized (this.mWallpaperProcessor) {
            this.mWallpaperProcessor.add(new WeakReference(processor));
        }
        startObserving();
    }

    public void removeWallpaperProcessor(ProgressProcessor processor) {
        synchronized (this.mWallpaperProcessor) {
            Iterator<WeakReference<ProgressProcessor>> iterator = this.mWallpaperProcessor.iterator();
            while (iterator.hasNext()) {
                WeakReference<ProgressProcessor> ref = (WeakReference) iterator.next();
                if (ref.get() != null && ref.get() == processor) {
                    iterator.remove();
                }
            }
        }
    }

    public void onDestroy() {
        synchronized (this.mWallpaperProcessor) {
            if (this.mApplicationProcessor != null) {
                this.mApplicationProcessor.clear();
            }
            Iterator<WeakReference<ProgressProcessor>> it = this.mWallpaperProcessor.iterator();
            while (it.hasNext()) {
                ProgressProcessor processor = (ProgressProcessor) ((WeakReference) it.next()).get();
                if (processor != null) {
                    processor.clear();
                }
            }
        }
    }

    public void onLauncherPaused() {
        if (this.mApplicationProcessor != null) {
            this.mApplicationProcessor.stop();
        }
    }

    public void onLauncherResume() {
        if (this.mApplicationProcessor != null) {
            this.mApplicationProcessor.start();
        }
    }

    public void setLauncher(Launcher launcher) {
        this.mLauncher = launcher;
        this.mApplicationProcessor.setLauncher(launcher);
    }

    public void onReceive(Context context, Intent intent) {
        if (this.mLauncher != null && !this.mLauncher.isWorkspaceLoading()) {
            String action = intent.getAction();
            if ("android.intent.action.APPLICATION_PROGRESS_UPDATE".equals(action)) {
                if (this.mApplicationProcessor != null) {
                    this.mApplicationProcessor.handleProgressUpdate(context, intent);
                }
            } else if ("android.intent.action.WALLPAPER_PROGRESS_UPDATE".equals(action)) {
                synchronized (this.mWallpaperProcessor) {
                    Iterator<WeakReference<ProgressProcessor>> it = this.mWallpaperProcessor.iterator();
                    while (it.hasNext()) {
                        ProgressProcessor processor = (ProgressProcessor) ((WeakReference) it.next()).get();
                        if (processor != null) {
                            processor.handleProgressUpdate(context, intent);
                        }
                    }
                }
            }
            if (!this.mLauncher.isWorkspaceLoading()) {
                startObserving();
            }
        }
    }

    public void onLoadingFinished() {
        if (this.mLauncher != null) {
            this.mLauncher.sendBroadcast(new Intent("launcher_progress_manager_ready"));
            this.mQueryCheckCode = 0;
            if (this.mApplicationProcessor != null) {
                this.mApplicationProcessor.onLoadingFinished();
            }
            startObserving();
        }
    }

    private void startObserving() {
        if (!mObservingUri) {
            mObservingUri = true;
            this.mCheckProgressTask.run();
        }
    }

    public static boolean isServerEnableShareProgressStatus(Context context, String server) {
        return String.valueOf(true).equals(System.getString(context.getContentResolver(), server + ".enable_share_progress_status"));
    }

    public static String generateServiceProgressChangedKey(String appProgressServer) {
        return "shared_value.pro_change." + appProgressServer;
    }

    public static String generateProgressSharedStatusKey(String server, String packageName) {
        return server + "_" + packageName + "_status";
    }

    public static boolean isProgressType(ShortcutInfo info) {
        return info.itemType == 11 || info.progressStatus != -5;
    }

    public ArrayList<String> getAllAppProgressKeys() {
        if (this.mApplicationProcessor == null) {
            return null;
        }
        return this.mApplicationProcessor.getAllProgressKeys();
    }

    public boolean bindAppProgressItem(ShortcutInfo info, boolean isRecorded) {
        if (this.mApplicationProcessor == null) {
            return false;
        }
        boolean success = this.mApplicationProcessor.bindProgressItem(info, isRecorded);
        if (!success) {
            return success;
        }
        startObserving();
        return success;
    }

    public void removeProgressingInfo(String key) {
        if (this.mApplicationProcessor != null) {
            this.mApplicationProcessor.removeProgressingInfo(key);
        }
    }

    public void onAppInProgressInstalled(final String key) {
        this.mProgressHandler.post(new Runnable() {
            public void run() {
                ProgressManager.this.removeProgressingInfo(key);
            }
        });
    }

    private void checkProgress() {
        synchronized (this.mWallpaperProcessor) {
            this.mQueryCheckCode = new Random().nextLong();
            if (!(this.mApplicationProcessor == null || this.mApplicationProcessor.isEmpty() || this.mApplicationProcessor.isStop())) {
                this.mApplicationProcessor.checkProgress(this.mLauncher);
            }
            Iterator<WeakReference<ProgressProcessor>> it = this.mWallpaperProcessor.iterator();
            while (it.hasNext()) {
                ProgressProcessor processor = (ProgressProcessor) ((WeakReference) it.next()).get();
                if (!(processor == null || processor.isStop())) {
                    processor.checkProgress(this.mLauncher);
                }
            }
            if (this.mWallpaperProcessor.isEmpty() && this.mApplicationProcessor.isEmpty()) {
                mObservingUri = false;
            }
        }
    }

    public static int queryProgressSharedValue(String server, String progressKey) {
        if (TextUtils.isEmpty(server) || TextUtils.isEmpty(progressKey)) {
            return -100;
        }
        return (int) Shell.getRuntimeSharedValue(generateProgressSharedStatusKey(server, progressKey));
    }

    public long getCheckCode() {
        return this.mQueryCheckCode;
    }

    public void queryProgressByBroadcast(Context context, String server, String[] progressKey) {
        if (context != null) {
            Intent intent = new Intent("android.intent.action.APPLICATION_PROGRESS_QUERY");
            intent.setPackage(server);
            intent.putExtra("android.intent.extra.update_progress_key", progressKey);
            intent.putExtra("android.intent.extra.update_progress_check_code", this.mQueryCheckCode);
            context.sendBroadcast(intent);
        }
    }

    public void queryProgressByBroadcast(Context context, String server, String progressKey) {
        if (context != null) {
            Intent intent = new Intent("android.intent.action.APPLICATION_PROGRESS_QUERY");
            intent.setPackage(server);
            intent.putExtra("android.intent.extra.update_progress_key", progressKey);
            intent.putExtra("android.intent.extra.update_progress_check_code", this.mQueryCheckCode);
            context.sendBroadcast(intent);
        }
    }

    public void loadingProgressFromCloudAppBackup(Context context, Runnable callback) {
        if (this.mApplicationProcessor != null) {
            this.mApplicationProcessor.loadingProgressFromCloudAppBackup(context, callback);
        }
    }

    public void onProgressIconClicked(ShortcutInfo info) {
        if (this.mApplicationProcessor != null) {
            this.mApplicationProcessor.onProgressIconClicked(info);
        }
    }

    public void onProgressIconDeleted(ShortcutInfo info) {
        if (this.mApplicationProcessor != null) {
            this.mApplicationProcessor.onProgressIconDeleted(info);
        }
    }
}
