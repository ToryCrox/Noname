package com.miui.home.launcher;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build.VERSION;
import android.provider.Settings.Global;
import android.util.Log;
import android.view.View;
import com.miui.home.launcher.ShakeMonitor.ShakeConfirmListener;
import com.miui.home.launcher.common.AppCategoryManager;
import com.miui.home.launcher.setting.PortableUtils;
import dalvik.system.VMRuntime;
import miui.external.ApplicationDelegate;

public class LauncherApplication extends ApplicationDelegate implements ShakeConfirmListener {
    private static float sScreenDensity;
    private IconLoader mIconLoader;
    private boolean mJustRestoreFinished = false;
    private Launcher mLauncher;
    LauncherProvider mLauncherProvider;
    private LauncherModel mModel;
    private ShakeMonitor mShakeMonitor;

    public void onCreate() {
        IntentFilter filter;
        boolean provisioned;
        super.onCreate();
        setTheme(getApplicationInfo().theme);
        VMRuntime.getRuntime().clearGrowthLimit();
        sScreenDensity = getResources().getDisplayMetrics().density;
        this.mIconLoader = new IconLoader(this);
        this.mModel = new LauncherModel(this, this.mIconLoader);
        if (VERSION.SDK_INT < 21) {
            filter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
            filter.addAction("android.intent.action.PACKAGE_REMOVED");
            filter.addAction("android.intent.action.PACKAGE_CHANGED");
            filter.addDataScheme("package");
            registerReceiver(this.mModel, filter);
            filter = new IntentFilter();
            filter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE");
            filter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE");
            registerReceiver(this.mModel, filter);
        } else {
            PortableUtils.registerLauncherAppsCallback(this, this.mModel.getLauncherAppsCallback());
        }
        registerReceiver(this.mModel, new IntentFilter("com.xiaomi.market.ACTION_HD_ICON_UPDATE"));
        registerReceiver(this.mModel, new IntentFilter("com.miui.home.ACTION_MOVE_TO_DESKTOP"));
        filter = new IntentFilter();
        this.mShakeMonitor = new ShakeMonitor();
        registerReceiver(ProgressManager.getManager(this), new IntentFilter("android.intent.action.APPLICATION_PROGRESS_UPDATE"), "miui.permission.USE_INTERNAL_GENERAL_API", null);
        registerReceiver(ProgressManager.getManager(this), new IntentFilter("android.intent.action.WALLPAPER_PROGRESS_UPDATE"), "miui.permission.USE_INTERNAL_GENERAL_API", null);
        registerReceiver(RecommendAppsDownloadReceiver.getInstanse(), new IntentFilter("com.xiaomi.market.DesktopRecommendDownloadStart"));
        AppCategoryManager.getInstance().initAppCategoryListAsync(this);
        AnalyticalDataCollector.init(this);
        if (Global.getInt(getContentResolver(), "device_provisioned", 0) != 0) {
            provisioned = true;
        } else {
            provisioned = false;
        }
        Log.d("LauncherApplication", "onCreate:" + provisioned);
        if (!provisioned) {
            WallpaperUtils.setProvisioned(this, false);
        }
    }

    public void onTerminate() {
        super.onTerminate();
        unregisterReceiver(this.mModel);
    }

    public boolean hasBroughtToForeground() {
        if (this.mLauncher == null) {
            return false;
        }
        ComponentName cn = ((RunningTaskInfo) ((ActivityManager) this.mLauncher.getSystemService("activity")).getRunningTasks(1).get(0)).topActivity;
        if (cn == null || !Launcher.class.getName().equals(cn.getClassName())) {
            return false;
        }
        return true;
    }

    LauncherModel setLauncher(Launcher launcher) {
        this.mLauncher = launcher;
        ProgressManager.getManager(this.mLauncher).setLauncher(launcher);
        RecommendAppsDownloadReceiver.getInstanse().setLauncher(launcher);
        this.mModel.initialize(launcher);
        return this.mModel;
    }

    public IconLoader getIconLoader() {
        return this.mIconLoader;
    }

    LauncherModel getModel() {
        return this.mModel;
    }

    void setLauncherProvider(LauncherProvider provider) {
        this.mLauncherProvider = provider;
    }

    LauncherProvider getLauncherProvider() {
        return this.mLauncherProvider;
    }

    Launcher getLauncher() {
        return this.mLauncher;
    }

    public static Launcher getLauncher(Context context) {
        if (context.getApplicationContext() instanceof Application) {
            return Application.getLauncherApplication(context).getLauncher();
        }
        return null;
    }

    public static float getScreenDensity() {
        return sScreenDensity;
    }

    void setJustRestoreFinished() {
        this.mJustRestoreFinished = true;
    }

    boolean isJustRestoreFinished() {
        if (!this.mJustRestoreFinished) {
            return false;
        }
        this.mJustRestoreFinished = false;
        return true;
    }

    public static void startActivity(Context context, Intent intent, View v) {
        Launcher launcher = getLauncher(context);
        if (launcher != null) {
            launcher.startActivity(intent, null, v);
        }
    }

    public static void startActivityForResult(Context context, Intent intent, int requestCode) {
        Launcher launcher = getLauncher(context);
        if (launcher != null) {
            launcher.startActivityForResult(intent, requestCode);
        }
    }

    public void startShakeMonitor() {
        this.mShakeMonitor.start(this, this);
    }

    public void stopShakeMonitor() {
        this.mShakeMonitor.stop(this);
    }

    public void onShake() {
        if (this.mLauncher != null) {
            this.mLauncher.alignCurrentScreen();
        }
    }
}
