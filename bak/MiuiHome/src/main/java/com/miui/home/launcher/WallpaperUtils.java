package com.miui.home.launcher;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog.Builder;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.IContentProvider;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.miui.Shell;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.preference.PreferenceManager;
import android.provider.MiuiSettings;
import android.provider.MiuiSettings.SettingsCloudData;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import com.miui.home.R;
import com.miui.home.launcher.CellLayout.LayoutParams;
import com.miui.home.launcher.common.Utilities;
import com.miui.home.launcher.gadget.Gadget;
import com.miui.home.launcher.gadget.MamlTools;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import miui.app.ProgressDialog;
import miui.content.res.ThemeNativeUtils;
import miui.graphics.BitmapFactory;
import miui.os.Environment;
import miui.os.FileUtils;
import miui.theme.GlobalUtils;
import miui.util.CustomizeUtil;
import miui.util.InputStreamLoader;

public class WallpaperUtils extends BroadcastReceiver {
    public static final String MIUI_WALLPAPER_PATH = (FileUtils.normalizeDirectoryName(Environment.getExternalStorageMiuiDirectory().getAbsolutePath()) + "wallpaper/");
    public static final String SYSTEM_WALLPAPER_RUNTIME_PATH = ("/data/system/users/" + UserHandle.myUserId() + "/wallpaper");
    private static WeakReference<Launcher> mLauncherRef;
    private static ProgressDialog mProgress;
    private static Point mTmpPoint = new Point();
    private static byte[] sBytesForInt = new byte[4];
    private static byte[] sBytesForShort = new byte[2];
    private static int sCurrentStatusBarAreaColorMode = 0;
    private static int sCurrentWallpaperColorMode = 0;
    public static String sDefaultLockWallpaperProvider = "com.xiaomi.tv.gallerylockscreen.lockscreen_magazine_provider";
    private static int[] sHotseatsIconTitleBg;
    private static int[][] sIconTitleBg;
    private static String[] sLocalWallpaperPath = new String[]{MIUI_WALLPAPER_PATH, "/system/media/wallpaper/", "/system/media/lockscreen/"};
    private static final Intent sPickerIntent = new Intent("android.intent.action.GET_CONTENT");
    private static final ArrayList<ComponentName> sPresetWallpaperPicker = new ArrayList();
    private static boolean sSkipNextWallpaperChanged = false;
    private static int sWallpaperHeight;
    private static float sWallpaperScreenSpan;
    private static int sWallpaperWidth;
    private final ArrayList<WallpaperColorChangedListener> mWallpaperColorChangedListeners = new ArrayList();

    public interface WallpaperColorChangedListener {
        void onWallpaperColorChanged();
    }

    private static android.graphics.Bitmap autoCropWallpaper(android.content.Context r10, android.graphics.Bitmap r11, android.graphics.Point r12) {
        /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:79)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
*/
        /*
        r9 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r8 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        if (r11 != 0) goto L_0x0008;
    L_0x0006:
        r1 = 0;
    L_0x0007:
        return r1;
    L_0x0008:
        r1 = 0;
        r6 = r11.getWidth();	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r6 = (float) r6;	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r6 = r6 * r8;	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r7 = r12.x;	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r7 = (float) r7;	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r6 = r6 / r7;	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r7 = r11.getHeight();	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r7 = (float) r7;	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r7 = r7 * r8;	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r8 = r12.y;	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r8 = (float) r8;	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r7 = r7 / r8;	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r5 = java.lang.Math.min(r6, r7);	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r6 = r11.getWidth();	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r6 = (float) r6;	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r7 = r12.x;	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r7 = (float) r7;	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r7 = r7 * r5;	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r6 = r6 - r7;	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r6 = r6 / r9;	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r3 = (int) r6;	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r6 = r11.getHeight();	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r6 = (float) r6;	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r7 = r12.y;	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r7 = (float) r7;	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r7 = r7 * r5;	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r6 = r6 - r7;	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r6 = r6 / r9;	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r4 = (int) r6;	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r0 = new miui.graphics.BitmapFactory$CropOption;	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r0.<init>();	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r6 = new android.graphics.Rect;	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r7 = r11.getWidth();	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r7 = r7 - r3;	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r8 = r11.getHeight();	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r8 = r8 - r4;	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r6.<init>(r3, r4, r7, r8);	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r0.srcBmpDrawingArea = r6;	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r6 = r0.srcBmpDrawingArea;	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r6 = r6.width();	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r7 = r0.srcBmpDrawingArea;	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r7 = r7.height();	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r8 = r11.getConfig();	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        r1 = com.miui.home.launcher.common.Utilities.createBitmapSafely(r6, r7, r8);	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        miui.graphics.BitmapFactory.cropBitmap(r11, r1, r0);	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        if (r11 == 0) goto L_0x0007;
    L_0x0068:
        r11.recycle();
        goto L_0x0007;
    L_0x006c:
        r2 = move-exception;
        r2.printStackTrace();	 Catch:{ OutOfMemoryError -> 0x006c, all -> 0x0076 }
        if (r11 == 0) goto L_0x0007;
    L_0x0072:
        r11.recycle();
        goto L_0x0007;
    L_0x0076:
        r6 = move-exception;
        if (r11 == 0) goto L_0x007c;
    L_0x0079:
        r11.recycle();
    L_0x007c:
        throw r6;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.miui.home.launcher.WallpaperUtils.autoCropWallpaper(android.content.Context, android.graphics.Bitmap, android.graphics.Point):android.graphics.Bitmap");
    }

    public static android.graphics.Bitmap decodeRegion(android.content.Context r12, android.net.Uri r13, android.graphics.Rect r14, int r15, int r16, int r17) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:20:? in {4, 9, 14, 16, 17, 18, 19, 22, 23} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
*/
        /*
        r11 = new miui.util.InputStreamLoader;
        r11.<init>(r12, r13);
        r9 = new android.graphics.BitmapFactory$Options;
        r9.<init>();
        r2 = 90;
        r0 = r17;
        if (r0 == r2) goto L_0x0016;
    L_0x0010:
        r2 = 270; // 0x10e float:3.78E-43 double:1.334E-321;
        r0 = r17;
        if (r0 != r2) goto L_0x0057;
    L_0x0016:
        r2 = (float) r15;
        r4 = r14.height();
        r4 = (float) r4;
        r2 = r2 / r4;
        r0 = r16;
        r4 = (float) r0;
        r5 = r14.width();
        r5 = (float) r5;
        r4 = r4 / r5;
        r2 = java.lang.Math.max(r2, r4);
        r2 = computeSampleSizeLarger(r2);
        r9.inSampleSize = r2;
    L_0x0030:
        r2 = r11.get();	 Catch:{ IOException -> 0x0072, all -> 0x007b }
        r4 = 1;	 Catch:{ IOException -> 0x0072, all -> 0x007b }
        r3 = android.graphics.BitmapRegionDecoder.newInstance(r2, r4);	 Catch:{ IOException -> 0x0072, all -> 0x007b }
        r11.close();
        r2 = android.graphics.Bitmap.Config.ARGB_8888;
        r0 = r16;
        r10 = com.miui.home.launcher.common.Utilities.createBitmapSafely(r15, r0, r2);
        if (r10 == 0) goto L_0x0056;
    L_0x0046:
        r1 = new android.graphics.Canvas;
        r1.<init>(r10);
        r7 = r9.inSampleSize;
        r2 = r17;
        r4 = r14;
        r5 = r15;
        r6 = r16;
        drawInTiles(r1, r2, r3, r4, r5, r6, r7);
    L_0x0056:
        return r10;
    L_0x0057:
        r2 = (float) r15;
        r4 = r14.width();
        r4 = (float) r4;
        r2 = r2 / r4;
        r0 = r16;
        r4 = (float) r0;
        r5 = r14.height();
        r5 = (float) r5;
        r4 = r4 / r5;
        r2 = java.lang.Math.max(r2, r4);
        r2 = computeSampleSizeLarger(r2);
        r9.inSampleSize = r2;
        goto L_0x0030;
    L_0x0072:
        r8 = move-exception;
        r8.printStackTrace();	 Catch:{ IOException -> 0x0072, all -> 0x007b }
        r10 = 0;
        r11.close();
        goto L_0x0056;
    L_0x007b:
        r2 = move-exception;
        r11.close();
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.miui.home.launcher.WallpaperUtils.decodeRegion(android.content.Context, android.net.Uri, android.graphics.Rect, int, int, int):android.graphics.Bitmap");
    }

    static {
        sPickerIntent.addCategory("android.intent.category.OPENABLE");
        sPickerIntent.setType("image/*");
        sPresetWallpaperPicker.clear();
        sPresetWallpaperPicker.add(new ComponentName("com.android.wallpaper.livepicker", "com.android.wallpaper.livepicker.LiveWallpaperActivity"));
    }

    public static void onDestroy() {
        sCurrentWallpaperColorMode = 0;
        sCurrentStatusBarAreaColorMode = 0;
    }

    public static boolean isSystemPresetWallpaper(String wallpaperPath) {
        if (TextUtils.isEmpty(wallpaperPath)) {
            return false;
        }
        if (wallpaperPath.startsWith("/system/media/wallpaper/") || wallpaperPath.startsWith("/system/media/lockscreen/")) {
            return true;
        }
        return false;
    }

    public static void loadLocalWallpaperList(ArrayList<String> wallpaperList) {
        HashSet<String> wallpaperFileMd5Set = new HashSet();
        wallpaperList.clear();
        if (sLocalWallpaperPath != null && sLocalWallpaperPath.length != 0) {
            for (int i = 0; i < sLocalWallpaperPath.length; i++) {
                String[] list = new File(sLocalWallpaperPath[i]).list();
                if (list != null && list.length > 0) {
                    int j = list.length - 1;
                    while (j >= 0) {
                        String wallpaperFileMd5 = Utilities.getFileMd5(new File(sLocalWallpaperPath[i] + list[j]));
                        if ((list[j].endsWith(".jpg") || list[j].endsWith(".png")) && !wallpaperFileMd5Set.contains(wallpaperFileMd5)) {
                            wallpaperList.add(sLocalWallpaperPath[i] + list[j]);
                            wallpaperFileMd5Set.add(wallpaperFileMd5);
                        }
                        j--;
                    }
                }
            }
        }
    }

    public static void loadImagePickerList(Context context, ArrayList<ResolveInfo> pickerList) {
        pickerList.clear();
        Iterator i$ = sPresetWallpaperPicker.iterator();
        while (i$.hasNext()) {
            ComponentName cn = (ComponentName) i$.next();
            Intent presetIntent = new Intent();
            presetIntent.setClassName(cn.getPackageName(), cn.getClassName());
            List<ResolveInfo> presetApps = context.getPackageManager().queryIntentActivities(presetIntent, 0);
            if (presetApps.size() == 1) {
                pickerList.add(presetApps.get(0));
            }
        }
        for (ResolveInfo info : context.getPackageManager().queryIntentActivities(sPickerIntent, 0)) {
            if (info.activityInfo.exported) {
                pickerList.add(info);
            }
        }
    }

    public static Intent getIntent(ResolveInfo info) {
        ComponentName cn = new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
        Intent intent = new Intent(sPickerIntent);
        intent.setComponent(new ComponentName(cn.getPackageName(), cn.getClassName()));
        intent.addFlags(16777216);
        if (cn.getPackageName().equals("com.miui.gallery")) {
            intent.putExtra("pick-need-origin", true);
        }
        return intent;
    }

    public static Intent getThemeManagerWallpaperPickerIntent(Context context, String title, boolean onLine) {
        ComponentName cn = new ComponentName("com.android.thememanager", "com.android.thememanager.activity.ThemeTabActivity");
        if (!ScreenUtils.isActivityExist(context, cn)) {
            return null;
        }
        if (onLine && GlobalUtils.isReligiousArea(context)) {
            return null;
        }
        String extraUri = null;
        if (!TextUtils.isEmpty(title)) {
            extraUri = "&S.REQUEST_RELATED_TITLE=" + title + "&i.REQUEST_HOME_INDEX=" + (onLine ? "1" : "0");
        }
        Intent intent = buildPickerIntent(context, "wallpaper", extraUri);
        intent.setComponent(cn);
        return intent;
    }

    public static Intent getMIWallpaperPickerIntent(Context context) {
        ComponentName cn = new ComponentName("com.android.thememanager", "com.android.thememanager.activity.ThemeTabActivity");
        if (!ScreenUtils.isActivityExist(context, cn)) {
            return null;
        }
        Intent intent = buildPickerIntent(context, "miwallpaper", null);
        intent.setComponent(cn);
        return intent;
    }

    private static Intent buildPickerIntent(Context context, String requestResCode, String extraUri) {
        Intent intent = new Intent();
        String uri = "theme://zhuti.xiaomi.com/list?S.REQUEST_RESOURCE_CODE=" + requestResCode + "&miref=" + context.getPackageName() + "&miback=true";
        if (!TextUtils.isEmpty(extraUri)) {
            uri = uri + extraUri;
        }
        intent.setData(Uri.parse(uri));
        intent.setAction("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addCategory("android.intent.category.BROWSABLE");
        return intent;
    }

    public static void clearWallpaperSrc() {
        setWallpaperSourceUri("pref_key_current_wallpaper_path", null);
        setWallpaperSourceUri("pref_key_lock_wallpaper_path", null);
    }

    private static void setWallpaperSourceUri(String key, String uri) {
        if (mLauncherRef != null && mLauncherRef.get() != null) {
            ((Launcher) mLauncherRef.get()).getWorldReadableSharedPreference().edit().putString(key, uri).commit();
        }
    }

    public static Uri getWallpaperSourceUri(String key) {
        if (mLauncherRef == null || mLauncherRef.get() == null) {
            return null;
        }
        Launcher launcher = (Launcher) mLauncherRef.get();
        String path = launcher.getWorldReadableSharedPreference().getString(key, null);
        if (path == null) {
            path = PreferenceManager.getDefaultSharedPreferences(launcher).getString(key, null);
            setWallpaperSourceUri(key, path);
        }
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        File wallpaper = new File(path);
        if (wallpaper.exists()) {
            return Uri.fromFile(wallpaper);
        }
        return Uri.parse(path);
    }

    public static String getWallpaperSourcePath(String key) {
        Uri uri = getWallpaperSourceUri(key);
        if (uri != null) {
            return uri.getPath();
        }
        return null;
    }

    public void setLauncher(Launcher launcher) {
        mLauncherRef = new WeakReference(launcher);
    }

    public void setOnWallpaperColorChangedListener(WallpaperColorChangedListener listener) {
        this.mWallpaperColorChangedListeners.add(listener);
    }

    public static boolean hasAppliedLightWallpaper() {
        return sCurrentWallpaperColorMode == 2;
    }

    public static boolean hasAppliedProximateLightWallpaper() {
        return sCurrentWallpaperColorMode == 1;
    }

    public static int getCurrentWallpaperColorMode() {
        return sCurrentWallpaperColorMode;
    }

    public static int getIconTitleBgMode(int cellX, int cellY, boolean inDock) {
        if (sHotseatsIconTitleBg == null || sIconTitleBg == null) {
            return sCurrentWallpaperColorMode;
        }
        if (inDock) {
            if (cellX < 0 || cellX >= sHotseatsIconTitleBg.length) {
                return sCurrentWallpaperColorMode;
            }
            return sHotseatsIconTitleBg[cellX];
        } else if (cellX < 0 || cellY < 0 || cellX >= sIconTitleBg.length || cellY >= sIconTitleBg[0].length) {
            return sCurrentWallpaperColorMode;
        } else {
            return sIconTitleBg[cellX][cellY];
        }
    }

    public static boolean hasLightBgForStatusBar() {
        return sCurrentStatusBarAreaColorMode == 2;
    }

    private static void updateCurrentWallpaperSize(String wallpaperPath) {
        if (new File(wallpaperPath).exists()) {
            try {
                Options op = BitmapFactory.getBitmapSize(wallpaperPath);
                sWallpaperWidth = op.outWidth;
                sWallpaperHeight = op.outHeight;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void suggestWallpaperDimension(Launcher launcher, boolean skipNextWallpaperChanged) {
        WallpaperManager wpm = (WallpaperManager) launcher.getSystemService("wallpaper");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(launcher);
        Point p = getScreenSize(launcher);
        sWallpaperScreenSpan = 1.0f;
        if (sWallpaperWidth <= 0 || sWallpaperHeight <= 0) {
            float wallpaperScreenSpanInPref = -1.0f;
            try {
                wallpaperScreenSpanInPref = sp.getFloat("pref_key_wallpaper_screen_span", -1.0f);
            } catch (ClassCastException e) {
                wallpaperScreenSpanInPref = (float) sp.getInt("pref_key_wallpaper_screen_span", -1);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            if (wallpaperScreenSpanInPref == -1.0f) {
                updateCurrentWallpaperSize(launcher.getFileStreamPath("current_wallpaper").getAbsolutePath());
            } else {
                sWallpaperScreenSpan = wallpaperScreenSpanInPref;
            }
        }
        if (sWallpaperWidth > 0 && sWallpaperHeight > 0) {
            sWallpaperScreenSpan = Math.max(1.0f, ((((float) sWallpaperWidth) * ((float) p.y)) / ((float) sWallpaperHeight)) / ((float) p.x));
            sp.edit().putFloat("pref_key_wallpaper_screen_span", Math.max(1.0f, Math.min(sWallpaperScreenSpan, 2.0f))).commit();
        }
        sp.edit().putString("pref_key_wallpaper_scroll_type", sWallpaperScreenSpan > 1.0f ? "byTheme" : "none").commit();
        if (launcher.getDragLayer() != null) {
            launcher.getDragLayer().updateWallpaper();
        }
        System.putFloat(launcher.getContentResolver(), "pref_key_wallpaper_screen_span", sWallpaperScreenSpan);
        wpm.suggestDesiredDimensions((int) (((float) p.x) * sWallpaperScreenSpan), p.y);
        sSkipNextWallpaperChanged = skipNextWallpaperChanged;
    }

    public static boolean isFileExist(String fileName) {
        if (mLauncherRef == null || mLauncherRef.get() == null) {
            return false;
        }
        return ((Launcher) mLauncherRef.get()).getFileStreamPath(fileName).exists();
    }

    public static String copyFile(String srcName, String dstName) {
        if (mLauncherRef == null || mLauncherRef.get() == null) {
            return null;
        }
        Launcher launcher = (Launcher) mLauncherRef.get();
        File srcFile = launcher.getFileStreamPath(srcName);
        File destFile = launcher.getFileStreamPath(dstName);
        android.os.FileUtils.copyFile(srcFile, destFile);
        if (destFile.exists()) {
            return destFile.getAbsolutePath();
        }
        return null;
    }

    public static void varyViewGroupByWallpaper(ViewGroup targetView) {
        int childCount = targetView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = targetView.getChildAt(i);
            if (child instanceof WallpaperColorChangedListener) {
                ((WallpaperColorChangedListener) child).onWallpaperColorChanged();
            }
        }
    }

    public static void onAddViewToGroup(ViewGroup targetView, View child, boolean changedByWallpaper) {
        if (changedByWallpaper && (child instanceof WallpaperColorChangedListener)) {
            ((WallpaperColorChangedListener) child).onWallpaperColorChanged();
        }
    }

    private int getSampleRatio(Bitmap b) {
        if (b.getWidth() < 400 || b.getHeight() < 400) {
            return 1;
        }
        return 5;
    }

    public void onWallpaperChanged() {
        if (mLauncherRef != null && mLauncherRef.get() != null) {
            final Launcher launcher = (Launcher) mLauncherRef.get();
            WallpaperManager wpm = (WallpaperManager) launcher.getSystemService("wallpaper");
            Bitmap b = null;
            if (wpm.getWallpaperInfo() == null) {
                setLockScreenShowLiveWallpaper(false);
                b = wpm.getBitmap();
                final Bitmap currentWallpaper = b;
                if (!(b == null || wallpaperChangedByLauncher(launcher) || sSkipNextWallpaperChanged)) {
                    setWallpaperSourceUri("pref_key_current_wallpaper_path", "");
                    sWallpaperWidth = b.getWidth();
                    sWallpaperHeight = b.getHeight();
                    suggestWallpaperDimension(launcher, true);
                    new AsyncTask<Void, Void, Void>() {
                        protected Void doInBackground(Void... params) {
                            File backFile = launcher.getFileStreamPath("current_wallpaper");
                            if (backFile.exists()) {
                                backFile.delete();
                            }
                            try {
                                BitmapFactory.saveToFile(currentWallpaper, backFile.getAbsolutePath(), false);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    }.execute(new Void[0]);
                }
                sSkipNextWallpaperChanged = false;
            } else {
                Drawable preview = getLiveWallpaperPreview(launcher);
                if (preview != null && (preview instanceof BitmapDrawable)) {
                    b = ((BitmapDrawable) preview).getBitmap();
                }
                PreferenceManager.getDefaultSharedPreferences(launcher).edit().putString("pref_key_wallpaper_scroll_type", "byTheme").commit();
                setWallpaperSourceUri("pref_key_current_wallpaper_path", "");
            }
            if (b != null) {
                final Bitmap wallpaper = b;
                launcher.getWorkspace().post(new Runnable() {
                    public void run() {
                        if (!launcher.isDestroyed()) {
                            if (!launcher.isReadyToBinding() || launcher.isWorkspaceLoading()) {
                                launcher.getWorkspace().postDelayed(this, 100);
                                return;
                            }
                            WallpaperUtils.sCurrentWallpaperColorMode = BitmapFactory.getBitmapColorMode(wallpaper, WallpaperUtils.this.getSampleRatio(wallpaper));
                            WallpaperUtils.this.correctStatusBarAreaColorMode(launcher, wallpaper, WallpaperUtils.sCurrentWallpaperColorMode);
                            WallpaperUtils.this.notifyWallpaperColorChanged();
                            launcher.onWallpaperChanged();
                            if (PreferenceManager.getDefaultSharedPreferences(launcher).getString("pref_key_wallpaper_scroll_type", "byTheme").equals("none")) {
                                WallpaperUtils.this.refreshDesktopTitleBgMode(launcher, wallpaper);
                                WallpaperUtils.refreshHotseatsTitleBgMode(launcher, wallpaper);
                                return;
                            }
                            WallpaperUtils.clearTitleBgMode();
                        }
                    }
                });
            } else {
                sCurrentWallpaperColorMode = 0;
                clearTitleBgMode();
            }
            wpm.forgetLoadedWallpaper();
        }
    }

    private static void clearTitleBgMode() {
        sIconTitleBg = (int[][]) null;
        sHotseatsIconTitleBg = null;
    }

    public static int[][] getIconTitleBgMode(Launcher launcher, Bitmap wallpaper) {
        if (launcher == null || wallpaper == null) {
            return (int[][]) null;
        }
        int cellCountX = DeviceConfig.getPortraitCellCountX();
        int cellCountY = DeviceConfig.getPortraitCellCountY();
        int[][] titleBgMode = (int[][]) Array.newInstance(Integer.TYPE, new int[]{cellCountX, cellCountY});
        int cellWidth = DeviceConfig.getCellWidth();
        int cellHeight = DeviceConfig.getCellHeight();
        int widthGap = cellCountX <= 1 ? 0 : (DeviceConfig.getCellWorkingWidth() - (cellWidth * cellCountX)) / (cellCountX - 1);
        int heightGap = cellCountY <= 1 ? 0 : (DeviceConfig.getCellWorkingHeight() - (cellHeight * cellCountY)) / (cellCountY - 1);
        float[] coord = new float[2];
        Utilities.getDescendantCoordRelativeToAncestor(launcher.getWorkspace().getCurrentCellLayout(), launcher.getDragLayer(), coord, false, true);
        for (int i = 0; i < titleBgMode.length; i++) {
            for (int j = 0; j < titleBgMode[0].length; j++) {
                LayoutParams lp = new LayoutParams();
                lp.setup(i, j, 1, 1, cellWidth, cellHeight, widthGap, heightGap, DeviceConfig.getWorkspaceCellPaddingSide(), DeviceConfig.getWorkspaceCellPaddingSide());
                int left = (int) (((float) lp.x) + coord[0]);
                int bottom = (int) (((float) (lp.y + lp.height)) + coord[1]);
                titleBgMode[i][j] = getTitleBgMode(new Rect(left, (bottom - launcher.getResources().getDimensionPixelSize(R.dimen.icon_title_padding_bottom)) - launcher.getResources().getDimensionPixelSize(R.dimen.workspace_icon_text_size), lp.width + left, bottom), wallpaper);
            }
        }
        return titleBgMode;
    }

    public static int getTitleBgMode(Rect titleLayout, Bitmap wallpaper) {
        if (wallpaper == null) {
            return sCurrentWallpaperColorMode;
        }
        Rect mapResult = mapTitleLayout(titleLayout, wallpaper);
        if (mapResult.width() <= 0 || mapResult.height() <= 0) {
            return sCurrentWallpaperColorMode;
        }
        return getBitmapColorMode(Utilities.createBitmapSafely(wallpaper, mapResult.left, mapResult.top, mapResult.width(), mapResult.height()), 1);
    }

    public static int getBitmapColorMode(Bitmap bmp, int sampleRatio) {
        if (bmp == null) {
            return 0;
        }
        int ret = 2;
        int scaledHeight = bmp.getHeight() / sampleRatio;
        int scaledWidth = bmp.getWidth() / sampleRatio;
        int darkPixelCountThreshold = (scaledWidth * scaledHeight) / 3;
        Bitmap tmpBmp = BitmapFactory.scaleBitmap(bmp, scaledWidth, scaledHeight);
        int darkPixelCount = 0;
        int[] pixels = new int[(scaledWidth * scaledHeight)];
        tmpBmp.getPixels(pixels, 0, scaledWidth, 0, 0, scaledWidth, scaledHeight);
        for (int i = 0; i < scaledWidth; i++) {
            for (int j = 0; j < scaledHeight; j++) {
                int pixel = pixels[(scaledWidth * j) + i];
                if (((int) (((((double) ((float) ((16711680 & pixel) >> 16))) * 0.3d) + (((double) ((float) ((65280 & pixel) >> 8))) * 0.59d)) + (((double) ((float) (pixel & 255))) * 0.11d))) < 180) {
                    darkPixelCount++;
                    if (darkPixelCount > darkPixelCountThreshold) {
                        ret = 1;
                    }
                    if (darkPixelCount > darkPixelCountThreshold * 2) {
                        ret = 0;
                        break;
                    }
                }
            }
        }
        if (tmpBmp == bmp) {
            return ret;
        }
        tmpBmp.recycle();
        return ret;
    }

    public static int getTitleShadowColor(int bgMode) {
        if (sCurrentWallpaperColorMode == 2) {
            return 0;
        }
        if (bgMode == 0) {
            if (((double) sWallpaperScreenSpan) > 1.0d) {
                return ((int) (((double) 255) * 0.3d)) << 24;
            }
            return 0;
        } else if (bgMode == 1) {
            return ((int) (((double) 255) * 0.33d)) << 24;
        } else {
            if (bgMode == 2) {
                return ((int) (((double) 255) * 0.33d)) << 24;
            }
            return -16777216;
        }
    }

    public static void refreshHotseatsTitleBgMode(Launcher launcher, Bitmap wallpaper) {
        if (launcher == null) {
            sHotseatsIconTitleBg = null;
        } else {
            sHotseatsIconTitleBg = launcher.getHotSeats().getHotseatsTitleBgMode(launcher, wallpaper);
        }
    }

    private void refreshDesktopTitleBgMode(Launcher launcher, Bitmap wallpaper) {
        if (launcher == null || wallpaper == null) {
            sIconTitleBg = (int[][]) null;
        } else {
            sIconTitleBg = getIconTitleBgMode(launcher, wallpaper);
        }
    }

    private static Rect mapTitleLayout(Rect layout, Bitmap wallpaper) {
        int left = (int) ((((float) layout.left) / ((float) DeviceConfig.getScreenWidth())) * ((float) wallpaper.getWidth()));
        int top = (int) ((((float) layout.top) / ((float) DeviceConfig.getScreenHeight())) * ((float) wallpaper.getHeight()));
        return new Rect(Math.max(0, left), Math.max(0, top), Math.min(left + ((int) ((((float) layout.width()) / ((float) DeviceConfig.getScreenWidth())) * ((float) wallpaper.getWidth()))), wallpaper.getWidth()), Math.min(top + ((int) ((((float) layout.height()) / ((float) DeviceConfig.getScreenHeight())) * ((float) wallpaper.getHeight()))), wallpaper.getHeight()));
    }

    private void correctStatusBarAreaColorMode(Launcher launcher, Bitmap wallpaper, int defaultMode) {
        if (wallpaper != null) {
            int statusBarBgHeight = (int) (((float) wallpaper.getHeight()) * (((float) DeviceConfig.getStatusBarHeight()) / ((float) DeviceConfig.getScreenHeight())));
            int colorMode = defaultMode;
            if (statusBarBgHeight > 0) {
                Bitmap statusBarBg = Utilities.createBitmapSafely(wallpaper, 0, 0, wallpaper.getWidth(), statusBarBgHeight);
                if (statusBarBg == null) {
                    colorMode = 0;
                } else {
                    colorMode = BitmapFactory.getBitmapColorMode(statusBarBg, getSampleRatio(statusBarBg));
                }
            }
            if (colorMode != sCurrentStatusBarAreaColorMode) {
                sCurrentStatusBarAreaColorMode = colorMode;
                launcher.changeStatusBarMode();
                return;
            }
            return;
        }
        sCurrentStatusBarAreaColorMode = 0;
    }

    public static Drawable getLiveWallpaperPreview(Context context) {
        if (mLauncherRef == null || mLauncherRef.get() == null) {
            return null;
        }
        Launcher launcher = (Launcher) mLauncherRef.get();
        WallpaperInfo paperInfo = ((WallpaperManager) launcher.getSystemService("wallpaper")).getWallpaperInfo();
        if (paperInfo == null) {
            return null;
        }
        if ("com.miui.miwallpaper.MiWallpaper".equals(paperInfo.getServiceName()) && isMiwallpaperPreviewExist()) {
            try {
                Bitmap b = BitmapFactory.decodeBitmap("/data/system/theme/miwallpaper_preview", false);
                if (b != null) {
                    return new BitmapDrawable(context.getResources(), b);
                }
            } catch (IOException e) {
                return null;
            }
        }
        return paperInfo.loadThumbnail(launcher.getPackageManager());
    }

    private static boolean isMiwallpaperPreviewExist() {
        if (new File("/data/system/theme/miwallpaper_preview").exists()) {
            return true;
        }
        return false;
    }

    public static void correctHomeScreenPreview(int wallpaperColorMode, Bitmap result) {
        if (mLauncherRef != null && mLauncherRef.get() != null) {
            Launcher launcher = (Launcher) mLauncherRef.get();
            if (sCurrentWallpaperColorMode != wallpaperColorMode) {
                int lastMode = sCurrentWallpaperColorMode;
                sCurrentWallpaperColorMode = wallpaperColorMode;
                changeDefaultScreenColor(launcher);
                getDefaultHomeScreenTopLayer(result);
                sCurrentWallpaperColorMode = lastMode;
                changeDefaultScreenColor(launcher);
                return;
            }
            getDefaultHomeScreenTopLayer(result);
        }
    }

    private static void changeDefaultScreenColor(Launcher launcher) {
        launcher.getWorkspace().getCellScreen(launcher.getWorkspace().getDefaultScreenIndex()).onWallpaperColorChanged();
        launcher.getHotSeats().onWallpaperColorChanged();
    }

    private void notifyWallpaperColorChanged() {
        ThumbnailView.adaptIconDrawableIndex();
        Iterator i$ = this.mWallpaperColorChangedListeners.iterator();
        while (i$.hasNext()) {
            ((WallpaperColorChangedListener) i$.next()).onWallpaperColorChanged();
        }
    }

    public void onReceive(Context context, Intent data) {
        if (mLauncherRef != null && data.getAction().equals("android.intent.action.WALLPAPER_CHANGED")) {
            onWallpaperChanged();
        }
    }

    public static Options getDecodeDestSize(Options srcOpt, int suggestWidth, int suggestHeight) {
        Options dstOpt = new Options();
        float scaleX = ((float) suggestWidth) / ((float) srcOpt.outWidth);
        float scaleY = ((float) suggestHeight) / ((float) srcOpt.outHeight);
        if (scaleX > scaleY) {
            dstOpt.outWidth = suggestWidth;
            dstOpt.outHeight = (int) (((float) srcOpt.outHeight) * scaleX);
        } else {
            dstOpt.outHeight = suggestHeight;
            dstOpt.outWidth = (int) (((float) srcOpt.outWidth) * scaleY);
        }
        return dstOpt;
    }

    public static Drawable getWallpaperThumbnail(String imagePath, Context context, int suggestWidth, int suggestHeight) {
        if (imagePath == null) {
            return null;
        }
        File imageFile = new File(imagePath);
        if (!imageFile.canRead()) {
            return null;
        }
        try {
            Options dstOpt = getDecodeDestSize(BitmapFactory.getBitmapSize(imagePath), suggestWidth, suggestHeight);
            return new BitmapDrawable(context.getResources(), BitmapFactory.decodeBitmap(imageFile.getAbsolutePath(), dstOpt.outWidth, dstOpt.outHeight, false));
        } catch (IOException e) {
            return null;
        }
    }

    public static Drawable getWallpaperThumbnail(Drawable drawable, Context context, int suggestWidth, int suggestHeight) {
        Options srcOpt = new Options();
        srcOpt.outHeight = drawable.getIntrinsicHeight();
        srcOpt.outWidth = drawable.getIntrinsicWidth();
        Options dstOpt = getDecodeDestSize(srcOpt, suggestWidth, suggestHeight);
        Bitmap bitmap = Utilities.createBitmapSafely(dstOpt.outWidth, dstOpt.outHeight, Config.ARGB_8888);
        if (bitmap != null) {
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            canvas.scale(((float) dstOpt.outWidth) / ((float) drawable.getIntrinsicWidth()), ((float) dstOpt.outHeight) / ((float) drawable.getIntrinsicHeight()));
            drawable.draw(canvas);
        }
        return new BitmapDrawable(context.getResources(), bitmap);
    }

    public static void showConfirmAlert(Context context, boolean cancelable, OnClickListener positiveListener, OnClickListener negativeListener, String title, CharSequence message, int positiveButtonId, int negativeButtonId) {
        try {
            new Builder(context).setCancelable(cancelable).setIconAttribute(16843605).setTitle(title).setMessage(message).setNegativeButton(negativeButtonId, negativeListener).setPositiveButton(positiveButtonId, positiveListener).create().show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setLockScreenShowLiveWallpaper(boolean show) {
        if (mLauncherRef != null && mLauncherRef.get() != null) {
            Launcher launcher = (Launcher) mLauncherRef.get();
            launcher.getWorldReadableSharedPreference().edit().putBoolean("keyguard_show_livewallpaper", show).commit();
            if (show) {
                MiuiSettings.System.putString(launcher.getContentResolver(), "lock_wallpaper_provider_authority", "com.miui.home.none_provider");
            }
        }
    }

    public static void setEnableWallpaperScroll(boolean enable) {
        if (mLauncherRef != null && mLauncherRef.get() != null) {
            PreferenceManager.getDefaultSharedPreferences((Launcher) mLauncherRef.get()).edit().putBoolean("pref_key_enable_wallpaper_scroll", enable).commit();
        }
    }

    public static void setWallpaperFromCustom(Context context, Intent data) {
        if (mLauncherRef != null && mLauncherRef.get() != null) {
            WallpaperManager wpm = (WallpaperManager) ((Launcher) mLauncherRef.get()).getSystemService("wallpaper");
            Uri wallpaperUri = null;
            if (data != null) {
                wallpaperUri = data.getData();
            } else if (wpm.getWallpaperInfo() == null) {
                return;
            }
            startWallpaperPreviewActivity(context, wallpaperUri);
        }
    }

    public static void getDefaultScreenWallpaperOffset(int[] offsets, int wallpaperWidth, int screenWidth) {
        int i = 2;
        if (mLauncherRef != null && mLauncherRef.get() != null) {
            Launcher launcher = (Launcher) mLauncherRef.get();
            if (offsets != null && offsets.length == 2) {
                int leftCount;
                int currentIndex = launcher.getWorkspace().getDefaultScreenIndex();
                if (launcher.isInNormalEditing()) {
                    currentIndex--;
                }
                int screenCount = launcher.getWorkspace().getScreenCount();
                if (!launcher.isInNormalEditing()) {
                    i = 0;
                }
                int screenCount2 = screenCount - i;
                if (DeviceConfig.isLayoutRtl()) {
                    leftCount = (screenCount2 - currentIndex) - 1;
                } else {
                    leftCount = currentIndex;
                }
                int rightCount = DeviceConfig.isLayoutRtl() ? currentIndex : (screenCount2 - currentIndex) - 1;
                offsets[0] = (int) (((float) ((wallpaperWidth - screenWidth) * leftCount)) / ((float) (leftCount + rightCount)));
                offsets[1] = (int) (((float) ((wallpaperWidth - screenWidth) * rightCount)) / ((float) (leftCount + rightCount)));
            }
        }
    }

    public static void startWallpaperPreviewActivity(Context context, Uri wallpaperUri) {
        if (Launcher.class.getName().equals(context.getClass().getName())) {
            AnalyticalDataCollector.setWallpaperEntryType(context, "editing_mode");
        } else {
            AnalyticalDataCollector.setWallpaperEntryType(context, "Settings");
        }
        Intent wallpaperPreviewIntent = new Intent();
        wallpaperPreviewIntent.setClassName(context, "com.miui.home.launcher.setting.WallpaperPreviewActivity");
        wallpaperPreviewIntent.setAction("com.miui.home.set_wallpaper");
        if (wallpaperUri != null) {
            wallpaperPreviewIntent.setData(wallpaperUri);
        }
        wallpaperPreviewIntent.putExtras(new Bundle());
        wallpaperPreviewIntent.setFlags(1);
        context.startActivity(wallpaperPreviewIntent);
    }

    public static void getDefaultHomeScreenTopLayer(Bitmap b) {
        if (mLauncherRef != null && mLauncherRef.get() != null && ((Launcher) mLauncherRef.get()).getWorkspace() != null && ((Launcher) mLauncherRef.get()).getWorkspace().getDefaultScreen() != null && b != null) {
            Launcher launcher = (Launcher) mLauncherRef.get();
            b.eraseColor(0);
            Canvas canvas = new Canvas(b);
            canvas.scale(((float) b.getWidth()) / ((float) DeviceConfig.getDeviceWidth()), ((float) b.getHeight()) / ((float) DeviceConfig.getDeviceHeight()));
            canvas.save();
            CellLayout defaultScreen = ((CellScreen) launcher.getWorkspace().getDefaultScreen()).getCellLayout();
            if (launcher.isInNormalEditing()) {
                setGadgetMode(defaultScreen, false);
            }
            canvas.translate(0.0f, (float) DeviceConfig.getStatusBarHeight());
            defaultScreen.draw(canvas);
            canvas.restore();
            canvas.save();
            View indicator = launcher.getWorkspace().getScreenIndicator();
            canvas.translate((float) ((DeviceConfig.getDeviceWidth() - indicator.getWidth()) / 2), (((float) DeviceConfig.getDeviceHeight()) - (((float) DeviceConfig.getWorkspaceIndicatorMarginBottom()) * (defaultScreen.getScreenType() == 2 ? 0.100000024f : 1.0f))) - ((float) indicator.getHeight()));
            indicator.draw(canvas);
            canvas.restore();
            if (defaultScreen.getScreenType() != 2) {
                HotSeats hotseat = launcher.getHotSeats();
                Paint paint = new Paint();
                paint.setAlpha(100);
                canvas.saveLayer(0.0f, (float) (DeviceConfig.getDeviceHeight() - hotseat.getHeight()), (float) DeviceConfig.getDeviceWidth(), (float) DeviceConfig.getDeviceHeight(), paint, 31);
                canvas.translate(0.0f, (float) (DeviceConfig.getDeviceHeight() - hotseat.getHeight()));
                hotseat.draw(canvas);
                canvas.restore();
            }
            if (launcher.isInNormalEditing()) {
                setGadgetMode(defaultScreen, true);
            }
        }
    }

    private static void setGadgetMode(CellLayout cl, boolean inEditMode) {
        if (mLauncherRef != null && mLauncherRef.get() != null) {
            Iterator i$ = ((Launcher) mLauncherRef.get()).mGadgets.iterator();
            while (i$.hasNext()) {
                Gadget gadget = (Gadget) i$.next();
                if (((ItemInfo) gadget.getTag()).screenId == cl.getScreenId()) {
                    if (inEditMode) {
                        gadget.onEditNormal();
                    } else {
                        gadget.onEditDisable();
                    }
                }
            }
        }
    }

    public static void cropBitmap(Bitmap destB, Bitmap srcB, Matrix m) {
        if (destB != null && srcB != null) {
            Canvas canvas = new Canvas();
            canvas.setBitmap(destB);
            canvas.drawBitmap(srcB, m, new Paint(2));
        }
    }

    public static Bitmap createBitmap(int width, int height, Bitmap b, Matrix m) {
        if (b == null || width <= 0 || height <= 0) {
            return null;
        }
        Bitmap newBitmap = Utilities.createBitmapSafely(width, height, Config.ARGB_8888);
        cropBitmap(newBitmap, b, m);
        return newBitmap;
    }

    private static Bitmap integrateBitmap(Bitmap dst, Bitmap top) {
        if (dst == null || top == null) {
            return null;
        }
        Canvas canvas = new Canvas();
        canvas.setBitmap(dst);
        canvas.scale(((float) dst.getWidth()) / ((float) top.getWidth()), ((float) dst.getHeight()) / ((float) top.getHeight()));
        canvas.drawBitmap(top, 0.0f, 0.0f, new Paint());
        return dst;
    }

    private static Drawable getLiveWallpaperPreview(Launcher launcher, WallpaperManager wpm, int width, int height) {
        WallpaperInfo paperInfo = wpm.getWallpaperInfo();
        Drawable preview = null;
        if (paperInfo.getServiceName().equals("com.miui.miwallpaper.MiWallpaper")) {
            preview = getWallpaperThumbnail("/data/system/theme/miwallpaper_preview", (Context) launcher, width, height);
        }
        if (preview == null) {
            return getWallpaperThumbnail(paperInfo.loadThumbnail(launcher.getPackageManager()), (Context) launcher, width, height);
        }
        return preview;
    }

    public static Drawable getLockScreenPreview(int width, int height) {
        if (mLauncherRef == null || mLauncherRef.get() == null) {
            return null;
        }
        Drawable preview;
        Context launcher = (Launcher) mLauncherRef.get();
        boolean lockShowWallpaper = launcher.getWorldReadableSharedPreference().getBoolean("keyguard_show_livewallpaper", false);
        WallpaperManager wpm = (WallpaperManager) launcher.getSystemService("wallpaper");
        Bitmap lockWallpaper = null;
        if (wpm.getWallpaperInfo() == null || !lockShowWallpaper) {
            preview = getWallpaperThumbnail("/data/system/theme/lock_wallpaper", launcher, width, height);
            if (preview == null) {
                preview = getWallpaperThumbnail("/system/media/theme/default/lock_wallpaper", launcher, width, height);
            }
        } else {
            preview = getWallpaperThumbnail(getLiveWallpaperPreview(launcher, wpm, width, height), launcher, width, height);
        }
        if (preview != null) {
            lockWallpaper = createBitmap(width, height, ((BitmapDrawable) preview).getBitmap(), new Matrix());
        }
        int lockScreenColorMode = 0;
        if (lockWallpaper != null) {
            lockScreenColorMode = BitmapFactory.getBitmapColorMode(lockWallpaper, 5);
        } else {
            lockWallpaper = Utilities.createBitmapSafely(width, height, Config.ARGB_8888);
        }
        Bitmap top = MamlTools.snapshootLockscreen(launcher, lockScreenColorMode);
        if (top == null || lockWallpaper == null) {
            return new BitmapDrawable(launcher.getResources(), top);
        }
        return new BitmapDrawable(launcher.getResources(), integrateBitmap(lockWallpaper, top));
    }

    public static Drawable getHomeScreenPreview(int width, int height) {
        if (mLauncherRef == null || mLauncherRef.get() == null) {
            return null;
        }
        Drawable preview;
        Bitmap wallpaper;
        Context launcher = (Launcher) mLauncherRef.get();
        Bitmap top = Utilities.createBitmapSafely(DeviceConfig.getDeviceWidth(), DeviceConfig.getDeviceHeight(), Config.ARGB_8888);
        getDefaultHomeScreenTopLayer(top);
        WallpaperManager wpm = (WallpaperManager) launcher.getSystemService("wallpaper");
        if (wpm.getWallpaperInfo() != null) {
            WallpaperInfo paperInfo = wpm.getWallpaperInfo();
            preview = getLiveWallpaperPreview(launcher, wpm, width, height);
        } else {
            preview = getWallpaperThumbnail(new BitmapDrawable(launcher.getResources(), wpm.getBitmap()), launcher, width, height);
            wpm.forgetLoadedWallpaper();
        }
        if (preview == null) {
            wallpaper = Utilities.createBitmapSafely(width, height, Config.ARGB_8888);
        } else if (preview instanceof BitmapDrawable) {
            wallpaper = ((BitmapDrawable) preview).getBitmap();
        } else {
            wallpaper = null;
        }
        if (wallpaper == null || top == null) {
            return new BitmapDrawable(launcher.getResources(), top);
        }
        Matrix m = new Matrix();
        String scrollCfg = PreferenceManager.getDefaultSharedPreferences(launcher).getString("pref_key_wallpaper_scroll_type", "byTheme");
        if (scrollCfg.equals("byTheme")) {
            scrollCfg = launcher.getResources().getString(R.string.wallpaper_scrolling);
        }
        if (!scrollCfg.equals("none")) {
            int[] offsets = new int[2];
            getDefaultScreenWallpaperOffset(offsets, wallpaper.getWidth(), width);
            m.postTranslate((float) (-offsets[0]), 0.0f);
        }
        return new BitmapDrawable(launcher.getResources(), integrateBitmap(createBitmap(width, height, wallpaper, m), top));
    }

    private boolean wallpaperChangedByLauncher(Context context) {
        ComponentName cn = ((RunningTaskInfo) ((ActivityManager) context.getSystemService("activity")).getRunningTasks(1).get(0)).topActivity;
        String launcherName = context.getPackageName();
        if (cn == null || !launcherName.equals(cn.getPackageName())) {
            return false;
        }
        return true;
    }

    public static boolean setLiveWallpaper(ComponentName mWallpaper, IBinder windowToken) {
        if (mLauncherRef == null || mLauncherRef.get() == null) {
            return false;
        }
        Launcher launcher = (Launcher) mLauncherRef.get();
        WallpaperManager mWallpaperManager = (WallpaperManager) launcher.getSystemService("wallpaper");
        try {
            mWallpaperManager.getIWallpaperManager().setWallpaperComponent(mWallpaper);
            mWallpaperManager.setWallpaperOffsetSteps(0.5f, 0.0f);
            mWallpaperManager.setWallpaperOffsets(windowToken, 0.5f, 0.0f);
            if (mWallpaper != null) {
                MiuiSettings.System.putString(launcher.getContentResolver(), "current_live_wallpaper_packagename", mWallpaper.getPackageName());
            }
            return true;
        } catch (RemoteException e) {
            return false;
        } catch (RuntimeException e2) {
            Log.w("Launcher.WallpaperUtils", "Failure setting live wallpaper", e2);
            return false;
        }
    }

    public static boolean setWallpaper(Context context, String imagePath) {
        WallpaperManager wpm = (WallpaperManager) context.getSystemService("wallpaper");
        InputStreamLoader streamLoader = new InputStreamLoader(context, Uri.fromFile(new File(imagePath)));
        if (streamLoader.get() == null) {
            return false;
        }
        try {
            wpm.setStream(streamLoader.get());
            streamLoader.close();
            if (!(mLauncherRef == null || mLauncherRef.get() == null)) {
                MiuiSettings.System.putString(((Launcher) mLauncherRef.get()).getContentResolver(), "current_live_wallpaper_packagename", "");
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean setWallpaper(final String imagePath, String srcUri, boolean needBackup) {
        if (mLauncherRef == null || mLauncherRef.get() == null) {
            return false;
        }
        final Context launcher = (Launcher) mLauncherRef.get();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(launcher);
        updateCurrentWallpaperSize(imagePath);
        suggestWallpaperDimension(launcher, true);
        try {
            final Options options = BitmapFactory.getBitmapSize(imagePath);
            final Point screenSize = getScreenSize(launcher);
            if (options.outHeight > screenSize.y) {
                final String str = imagePath;
                final String str2 = srcUri;
                new AsyncTask<Void, Void, Boolean>() {
                    protected void onPreExecute() {
                        super.onPreExecute();
                        WallpaperUtils.mProgress = new ProgressDialog(launcher);
                        WallpaperUtils.mProgress.setProgressStyle(0);
                        WallpaperUtils.mProgress.setCancelable(false);
                        WallpaperUtils.mProgress.setMessage(launcher.getResources().getString(R.string.wallpaper_scaling_message));
                        WallpaperUtils.mProgress.show();
                    }

                    protected Boolean doInBackground(Void... params) {
                        if (WallpaperUtils.setWallpaper(WallpaperUtils.decodeRegion(launcher, Uri.fromFile(new File(str)), new Rect(0, 0, options.outWidth, options.outHeight), (int) (((float) screenSize.x) * WallpaperUtils.sWallpaperScreenSpan), screenSize.y, 0), str2)) {
                            return Boolean.valueOf(true);
                        }
                        return Boolean.valueOf(false);
                    }

                    protected void onPostExecute(Boolean result) {
                        if (!(WallpaperUtils.mProgress == null || launcher.isFinishing())) {
                            WallpaperUtils.mProgress.dismiss();
                        }
                        WallpaperUtils.mProgress = null;
                        super.onPostExecute(result);
                    }
                }.execute(new Void[0]);
            } else if (!setWallpaper(launcher, imagePath)) {
                return false;
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        setWallpaperSourceUri("pref_key_current_wallpaper_path", srcUri);
        if (needBackup) {
            final String backPath = launcher.getFileStreamPath("current_wallpaper").getPath();
            new AsyncTask<Void, Void, Void>() {
                protected Void doInBackground(Void... params) {
                    android.os.FileUtils.copyFile(new File(imagePath), new File(backPath));
                    return null;
                }
            }.execute(new Void[0]);
        }
        return true;
    }

    private static int computeSampleSizeLarger(float scale) {
        int initialSize = (int) Math.floor((double) (1.0f / scale));
        if (initialSize <= 1) {
            return 1;
        }
        return initialSize <= 8 ? Integer.highestOneBit(initialSize) : (initialSize / 8) * 8;
    }

    private static void calcTileRect(Rect tileRect, Rect cropRect, int rotation, int cellX, int cellY, int tileSize) {
        if (rotation == 90) {
            tileRect.left = cropRect.left + (cellY * tileSize);
            tileRect.top = cropRect.bottom - ((cellX + 1) * tileSize);
        } else if (rotation == 180) {
            tileRect.left = cropRect.right - ((cellX + 1) * tileSize);
            tileRect.top = cropRect.bottom - ((cellY + 1) * tileSize);
        } else if (rotation == 270) {
            tileRect.left = cropRect.right - ((cellY + 1) * tileSize);
            tileRect.top = cropRect.top + (cellX * tileSize);
        } else {
            tileRect.left = cropRect.left + (cellX * tileSize);
            tileRect.top = cropRect.top + (cellY * tileSize);
        }
        tileRect.right = tileRect.left + tileSize;
        tileRect.bottom = tileRect.top + tileSize;
    }

    private static void drawInTiles(Canvas canvas, int rotation, BitmapRegionDecoder decoder, Rect cropRect, int destWidth, int destHeight, int sample) {
        int tileSize = sample * 512;
        Rect tileRect = new Rect();
        Options options = new Options();
        options.inPreferredConfig = Config.ARGB_8888;
        options.inSampleSize = sample;
        if (rotation == 90 || rotation == 270) {
            canvas.scale((((float) sample) * ((float) destWidth)) / ((float) cropRect.height()), (((float) sample) * ((float) destHeight)) / ((float) cropRect.width()));
        } else {
            canvas.scale((((float) sample) * ((float) destWidth)) / ((float) cropRect.width()), (((float) sample) * ((float) destHeight)) / ((float) cropRect.height()));
        }
        Paint paint = new Paint(2);
        int targetRectWidth = (rotation == 90 || rotation == 270) ? cropRect.height() : cropRect.width();
        int targetRectHeight = (rotation == 90 || rotation == 270) ? cropRect.width() : cropRect.height();
        int maxCellX = targetRectWidth / tileSize;
        int maxCellY = targetRectHeight / tileSize;
        for (int x = 0; x <= maxCellX; x++) {
            for (int y = 0; y <= maxCellY; y++) {
                calcTileRect(tileRect, cropRect, rotation, x, y, tileSize);
                if (tileRect.intersect(cropRect)) {
                    Bitmap bitmap;
                    synchronized (decoder) {
                        bitmap = decoder.decodeRegion(tileRect, options);
                    }
                    if (!(bitmap == null || tileRect.isEmpty())) {
                        if (rotation != 0) {
                            Matrix m = new Matrix();
                            m.setRotate((float) rotation, (float) (bitmap.getWidth() / 2), (float) (bitmap.getHeight() / 2));
                            Bitmap tmp = bitmap;
                            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, false);
                            tmp.recycle();
                        }
                        canvas.drawBitmap(bitmap, (float) (x * 512), (float) (y * 512), paint);
                        bitmap.recycle();
                    }
                }
            }
        }
    }

    public static boolean saveToBmp(Bitmap orgBitmap, String filePath) throws IOException {
        if (orgBitmap == null) {
            return false;
        }
        int i;
        int width = orgBitmap.getWidth();
        int height = orgBitmap.getHeight();
        byte[] dummyBytesPerRow = null;
        boolean hasDummy = false;
        int rowWidthInBytes = width * 4;
        if (rowWidthInBytes % 4 > 0) {
            hasDummy = true;
            dummyBytesPerRow = new byte[(4 - (rowWidthInBytes % 4))];
            for (i = 0; i < dummyBytesPerRow.length; i++) {
                dummyBytesPerRow[i] = (byte) -1;
            }
        }
        int imageSize = ((hasDummy ? dummyBytesPerRow.length : 0) + rowWidthInBytes) * height;
        int fileSize = imageSize + 54;
        ByteBuffer buffer = ByteBuffer.allocate(fileSize);
        buffer.put((byte) 66);
        buffer.put((byte) 77);
        buffer.put(convertIntToBytes(fileSize));
        buffer.put(convertShortToBytes((short) 0));
        buffer.put(convertShortToBytes((short) 0));
        buffer.put(convertIntToBytes(54));
        buffer.put(convertIntToBytes(40));
        int i2 = hasDummy ? dummyBytesPerRow.length == 3 ? 1 : 0 : 0;
        buffer.put(convertIntToBytes(i2 + width));
        buffer.put(convertIntToBytes(height));
        buffer.put(convertShortToBytes((short) 1));
        buffer.put(convertShortToBytes((short) 32));
        buffer.put(convertIntToBytes(0));
        buffer.put(convertIntToBytes(imageSize));
        buffer.put(convertIntToBytes(0));
        buffer.put(convertIntToBytes(0));
        buffer.put(convertIntToBytes(0));
        buffer.put(convertIntToBytes(0));
        byte[] content = orgBitmap.mBuffer;
        int row = height;
        int col = width;
        int startPosition = (row - 1) * col;
        int endPosition = row * col;
        while (row > 0) {
            for (i = startPosition; i < endPosition; i++) {
                buffer.put(content[(i * 4) + 2]);
                buffer.put(content[(i * 4) + 1]);
                buffer.put(content[i * 4]);
                buffer.put(content[(i * 4) + 3]);
            }
            if (hasDummy) {
                buffer.put(dummyBytesPerRow);
            }
            row--;
            endPosition = startPosition;
            startPosition -= col;
        }
        FileOutputStream fos = new FileOutputStream(filePath);
        fos.write(buffer.array());
        fos.close();
        return true;
    }

    private static byte[] convertIntToBytes(int value) throws IOException {
        sBytesForInt[0] = (byte) (value & 255);
        sBytesForInt[1] = (byte) ((65280 & value) >> 8);
        sBytesForInt[2] = (byte) ((16711680 & value) >> 16);
        sBytesForInt[3] = (byte) ((-16777216 & value) >> 24);
        return sBytesForInt;
    }

    private static byte[] convertShortToBytes(short value) throws IOException {
        sBytesForShort[0] = (byte) (value & 255);
        sBytesForShort[1] = (byte) ((65280 & value) >> 8);
        return sBytesForShort;
    }

    public static boolean saveToJPG(Bitmap b, String path) {
        Exception e;
        Throwable th;
        FileOutputStream out = null;
        boolean success = false;
        try {
            FileOutputStream out2 = new FileOutputStream(path);
            try {
                b.compress(CompressFormat.JPEG, 100, out2);
                success = true;
                if (out2 != null) {
                    try {
                        out2.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                        out = out2;
                    }
                }
                out = out2;
            } catch (Exception e3) {
                e = e3;
                out = out2;
                try {
                    e.printStackTrace();
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                    return success;
                } catch (Throwable th2) {
                    th = th2;
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e222) {
                            e222.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                out = out2;
                if (out != null) {
                    out.close();
                }
                throw th;
            }
        } catch (Exception e4) {
            e = e4;
            e.printStackTrace();
            if (out != null) {
                out.close();
            }
            return success;
        }
        return success;
    }

    public static boolean setWallpaper(Bitmap b, String srcUri) {
        boolean success = false;
        if (!(mLauncherRef == null || mLauncherRef.get() == null || b == null)) {
            Launcher launcher = (Launcher) mLauncherRef.get();
            final File wallpaperTmp = launcher.getFileStreamPath("wallpaper_tmp.png");
            try {
                BitmapFactory.saveToFile(b, wallpaperTmp.getPath(), true);
                final File backupFile = launcher.getFileStreamPath("current_wallpaper");
                new AsyncTask<Void, Void, Void>() {
                    protected Void doInBackground(Void... params) {
                        try {
                            android.os.FileUtils.copyFile(wallpaperTmp, backupFile);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                }.execute(new Void[0]);
                success = setWallpaper(wallpaperTmp.getPath(), srcUri, false);
                if (wallpaperTmp.exists()) {
                    wallpaperTmp.delete();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    private static Point getScreenSize(Launcher launcher) {
        Point p = new Point();
        Display display = launcher.getWindowManager().getDefaultDisplay();
        int rotation = display.getRotation();
        boolean isPortrait = rotation == 0 || rotation == 2;
        CustomizeUtil.getRealSize(display, mTmpPoint);
        p.x = isPortrait ? mTmpPoint.x : mTmpPoint.y;
        p.y = isPortrait ? mTmpPoint.y : mTmpPoint.x;
        return p;
    }

    public static boolean setLockWallpaperWithoutCrop(String src, String srcUri, boolean autoChange) {
        setWallpaperSourceUri("pref_key_lock_wallpaper_path", srcUri);
        return setLockWallpaperWithoutCrop(src, autoChange);
    }

    public static boolean setLockWallpaperWithoutCrop(final String src, final boolean autoChange) {
        new Runnable() {
            public void run() {
                if (WallpaperUtils.mLauncherRef == null || WallpaperUtils.mLauncherRef.get() == null) {
                    LauncherModel.runOnWorkerThread(this, 100);
                    return;
                }
                new File("/data/system/theme/").mkdirs();
                new File("/data/system/theme/lock_wallpaper").delete();
                Shell.move(src, "/data/system/theme/lock_wallpaper");
                ThemeNativeUtils.updateFilePermissionWithThemeContext("/data/system/theme/lock_wallpaper");
                WallpaperUtils.onLockWallpaperChanged(autoChange);
            }
        }.run();
        return true;
    }

    public static boolean setLockWallpaperWithoutCrop(Uri wallpaperUri, boolean autoChange) {
        boolean z = false;
        if (!(mLauncherRef == null || mLauncherRef.get() == null || wallpaperUri == null || !isUriFileExists(wallpaperUri))) {
            Launcher launcher = (Launcher) mLauncherRef.get();
            try {
                InputStream is = launcher.getContentResolver().openInputStream(wallpaperUri);
                File tmpLockScreen = launcher.getFileStreamPath("lockWallpaperBack");
                if (!tmpLockScreen.exists()) {
                    tmpLockScreen.createNewFile();
                }
                OutputStream os = new FileOutputStream(tmpLockScreen);
                byte[] bytes = new byte[1024];
                while (true) {
                    int read = is.read(bytes);
                    if (read == -1) {
                        break;
                    }
                    os.write(bytes, 0, read);
                }
                is.close();
                os.close();
                z = setLockWallpaperWithoutCrop(tmpLockScreen.getPath(), wallpaperUri.toString(), autoChange);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return z;
    }

    public static void resetLockWallpaperToDefault() {
        Shell.remove("/data/system/theme/lock_wallpaper");
    }

    public static boolean setLockWallpaper(Context context, Bitmap b, boolean autoChange, String srcUri) {
        File tmpLockScreen = context.getFileStreamPath(new File("/data/system/theme/lock_wallpaper").getName());
        if (b != null) {
            if (!saveToJPG(b, tmpLockScreen.getAbsolutePath())) {
                return false;
            }
            setLockWallpaperWithoutCrop(tmpLockScreen.getAbsolutePath(), srcUri, autoChange);
            tmpLockScreen.delete();
        }
        if (new File("/data/system/theme/lock_wallpaper").exists()) {
            return onLockWallpaperChanged(autoChange);
        }
        return false;
    }

    private static boolean onLockWallpaperChanged(boolean autoChange) {
        if (mLauncherRef == null || mLauncherRef.get() == null) {
            return false;
        }
        Launcher launcher = (Launcher) mLauncherRef.get();
        if (autoChange) {
            setLockScreenShowLiveWallpaper(false);
        } else {
            PreferenceManager.getDefaultSharedPreferences(launcher).edit().remove("currentWallpaperInfo").commit();
            MiuiSettings.System.putString(launcher.getContentResolver(), "lock_wallpaper_provider_authority", "com.miui.home.none_provider");
        }
        launcher.sendBroadcast(new Intent("com.miui.keyguard.setwallpaper"));
        return true;
    }

    public static boolean setLockWallpaper(Uri wallpaperUri, boolean autoChange) {
        if (mLauncherRef == null || mLauncherRef.get() == null || !isUriFileExists(wallpaperUri)) {
            return false;
        }
        Launcher launcher = (Launcher) mLauncherRef.get();
        Point p = getScreenSize(launcher);
        Bitmap wallpaper = getRotatedBitmap(wallpaperUri);
        if (((float) wallpaper.getWidth()) / ((float) wallpaper.getHeight()) == ((float) p.x) / ((float) p.y)) {
            return setLockWallpaperWithoutCrop(wallpaperUri, autoChange);
        }
        return setLockWallpaper(launcher, autoCropWallpaper(launcher, wallpaper, p), autoChange, wallpaperUri.toString());
    }

    public static Bitmap getRotatedBitmap(Uri wallpaperUri) {
        if (mLauncherRef == null || mLauncherRef.get() == null || !isUriFileExists(wallpaperUri)) {
            return null;
        }
        Launcher launcher = (Launcher) mLauncherRef.get();
        try {
            Options op = BitmapFactory.getBitmapSize(launcher, wallpaperUri);
            Rect cropRect = new Rect(0, 0, op.outWidth, op.outHeight);
            InputStreamLoader is = new InputStreamLoader(launcher, wallpaperUri);
            int rotation = Utilities.getImageRotation(is.get());
            is.close();
            int destWidth = (rotation == 90 || rotation == 270) ? op.outHeight : op.outWidth;
            int destHeight = (rotation == 90 || rotation == 270) ? op.outWidth : op.outHeight;
            return decodeRegion(launcher, wallpaperUri, cropRect, destWidth, destHeight, rotation);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isUriFileExists(Uri uri) {
        if (uri == null) {
            return false;
        }
        try {
            Utilities.closeFileSafely(Application.getInstance().getContentResolver().openInputStream(uri));
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e2) {
            e2.printStackTrace();
            return false;
        }
    }

    public static boolean isKeyguardShowLiveWallpaper() {
        if (mLauncherRef == null || mLauncherRef.get() == null) {
            return false;
        }
        return ((Launcher) mLauncherRef.get()).getWorldReadableSharedPreference().getBoolean("keyguard_show_livewallpaper", false);
    }

    public static String getLockWallpaperProvider(Context context) {
        return MiuiSettings.System.getString(context.getContentResolver(), "lock_wallpaper_provider_authority");
    }

    public static void resetLockWallpaperProviderIfNeeded(Context context) {
        boolean provisioned;
        boolean notSet = true;
        if (Global.getInt(context.getContentResolver(), "device_provisioned", 0) != 0) {
            provisioned = true;
        } else {
            provisioned = false;
        }
        if (provisioned && !isProvisioned(context)) {
            if (!DeviceConfig.needHideLockProvider(context)) {
                MiuiSettings.System.putString(context.getContentResolver(), "lock_wallpaper_provider_authority", sDefaultLockWallpaperProvider);
                setProviderSetter(context, "default");
                Log.d("Launcher.WallpaperUtils", "set Default Lock Wallpaper Provider");
            }
            setProvisioned(context, true);
        }
        String providerInCharge = getLockWallpaperProvider(context);
        if (hasSetProviderSetter(context)) {
            notSet = false;
        }
        boolean applyByTheme = "com.android.thememanager.theme_lockwallpaper".equals(providerInCharge);
        boolean closeByUser = isProviderClosedByUser(context);
        if ((applyByTheme || notSet) && !closeByUser && ((!new File("/data/system/theme/lock_wallpaper").exists() || TextUtils.isEmpty(providerInCharge)) && !DeviceConfig.needHideLockProvider(context) && !hasValidProvider(context))) {
            loadCloudLockWallpaperProvider(context);
            MiuiSettings.System.putString(context.getContentResolver(), "lock_wallpaper_provider_authority", sDefaultLockWallpaperProvider);
            setProviderSetter(context, "default");
            Log.d("Launcher.WallpaperUtils", "reset Default Lock Wallpaper Provider");
        } else if (notSet) {
            setProviderSetter(context, "other");
        }
        if (applyByTheme) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().remove("currentWallpaperInfo").commit();
        }
    }

    public static boolean setLockWallpaperProviderByCloud(Context context) {
        loadCloudLockWallpaperProvider(context);
        if (!isDefaultProviderSetter(context) || TextUtils.equals(sDefaultLockWallpaperProvider, getLockWallpaperProvider(context))) {
            return false;
        }
        Log.d("Launcher.WallpaperUtils", "setLockWallpaperProviderByCloud:" + sDefaultLockWallpaperProvider);
        MiuiSettings.System.putString(context.getContentResolver(), "lock_wallpaper_provider_authority", sDefaultLockWallpaperProvider);
        return true;
    }

    private static void loadCloudLockWallpaperProvider(Context context) {
        String cloudAuthority = SettingsCloudData.getCloudDataString(context.getContentResolver(), "LockWallpaper", "authority", "com.xiaomi.tv.gallerylockscreen.lockscreen_magazine_provider");
        if ("empty".equals(cloudAuthority)) {
            cloudAuthority = null;
        }
        sDefaultLockWallpaperProvider = cloudAuthority;
    }

    public static boolean hasValidProvider(Context context) {
        String providerInCharge = getLockWallpaperProvider(context);
        if (TextUtils.isEmpty(providerInCharge) || "com.miui.home.none_provider".equals(providerInCharge)) {
            return false;
        }
        IContentProvider provider = context.getContentResolver().acquireUnstableProvider(Uri.parse("content://" + providerInCharge));
        if (provider == null) {
            return false;
        }
        context.getContentResolver().releaseProvider(provider);
        return true;
    }

    public static boolean isProviderClosedByUser(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_key_provider_closed", false);
    }

    public static void setProviderClosedByUser(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("pref_key_provider_closed", value).commit();
    }

    public static boolean isProvisioned(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_key_provisioned", true);
    }

    public static void setProvisioned(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("pref_key_provisioned", value).commit();
    }

    public static void setProviderSetter(Context context, String setter) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("pref_key_provider_setter_v1", setter).commit();
    }

    public static boolean hasSetProviderSetter(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).contains("pref_key_provider_setter_v1");
    }

    public static boolean isDefaultProviderSetter(Context context) {
        return "default".equals(PreferenceManager.getDefaultSharedPreferences(context).getString("pref_key_provider_setter_v1", null));
    }

    public static boolean isDefaultLockStyle() {
        if (new File("/data/system/theme//lockscreen").exists() || isKeyguardShowLiveWallpaper()) {
            return false;
        }
        return true;
    }

    public static boolean isLauncherExist() {
        return (mLauncherRef == null || mLauncherRef.get() == null) ? false : true;
    }
}
