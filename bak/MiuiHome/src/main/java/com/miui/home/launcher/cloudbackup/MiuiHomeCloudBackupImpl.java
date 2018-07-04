package com.miui.home.launcher.cloudbackup;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import com.miui.home.launcher.WallpaperUtils;
import com.xiaomi.settingsdk.backup.ICloudBackup;
import com.xiaomi.settingsdk.backup.data.DataPackage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map.Entry;
import miui.util.IOUtils;

public class MiuiHomeCloudBackupImpl implements ICloudBackup {
    public void onBackupSettings(Context context, DataPackage dataPackage) {
        Log.d("MiuiHomeCloudBackupImpl", "start to backup wallpaper");
        Bitmap wallpaper = WallpaperManager.getInstance(context).getBitmap();
        if (wallpaper == null) {
            Log.d("MiuiHomeCloudBackupImpl", "failed to get wallpaper bitmap");
            return;
        }
        File cachedDir = context.getCacheDir();
        if (!cachedDir.exists()) {
            cachedDir.mkdirs();
        }
        File tmpWallpaper = new File(cachedDir, "tmpwallpaper");
        if (tmpWallpaper.exists()) {
            tmpWallpaper.delete();
        }
        try {
            tmpWallpaper.createNewFile();
            if (WallpaperUtils.saveToBmp(wallpaper, tmpWallpaper.getAbsolutePath())) {
                dataPackage.addKeyFile(WallpaperUtils.SYSTEM_WALLPAPER_RUNTIME_PATH, tmpWallpaper);
            } else {
                Log.e("MiuiHomeCloudBackupImpl", "failed to compress wallpaper bitmap");
            }
        } catch (FileNotFoundException e) {
            Log.e("MiuiHomeCloudBackupImpl", "FileNotFoundException", e);
        } catch (IOException e2) {
            Log.e("MiuiHomeCloudBackupImpl", "IOException when create temp wallpaper", e2);
        }
    }

    public void onRestoreSettings(Context context, DataPackage dataPackage, int packageVersion) {
        Log.d("MiuiHomeCloudBackupImpl", "start to restore wallpaper");
        restoreFiles(context, dataPackage);
    }

    public int getCurrentVersion(Context context) {
        return 1;
    }

    private static void restoreOneFile(Context context, String path, ParcelFileDescriptor data) {
        FileNotFoundException e;
        Throwable th;
        IOException e2;
        InputStream fileInputStream = null;
        OutputStream fileOutputStream = null;
        try {
            InputStream fileInputStream2 = new FileInputStream(data.getFileDescriptor());
            try {
                String fileName = path.substring(path.lastIndexOf(File.separator) + 1, path.length());
                File tmpTargetFile = new File(context.getCacheDir(), fileName);
                OutputStream fileOutputStream2 = new FileOutputStream(tmpTargetFile);
                try {
                    byte[] buffer = new byte[1024];
                    while (true) {
                        int length = fileInputStream2.read(buffer);
                        if (length <= 0) {
                            break;
                        }
                        fileOutputStream2.write(buffer, 0, length);
                    }
                    fileOutputStream2.flush();
                    if (fileName.equals("lock_wallpaper")) {
                        WallpaperUtils.setLockWallpaperWithoutCrop(tmpTargetFile.getAbsolutePath(), false);
                    } else if (fileName.equals("wallpaper")) {
                        WallpaperUtils.setWallpaper(context, tmpTargetFile.getAbsolutePath());
                    }
                    tmpTargetFile.delete();
                    IOUtils.closeQuietly(fileInputStream2);
                    IOUtils.closeQuietly(fileOutputStream2);
                    fileOutputStream = fileOutputStream2;
                    fileInputStream = fileInputStream2;
                } catch (FileNotFoundException e3) {
                    e = e3;
                    fileOutputStream = fileOutputStream2;
                    fileInputStream = fileInputStream2;
                    try {
                        Log.e("MiuiHomeCloudBackupImpl", "FileNotFoundException in restoreFiles: " + path, e);
                        IOUtils.closeQuietly(fileInputStream);
                        IOUtils.closeQuietly(fileOutputStream);
                    } catch (Throwable th2) {
                        th = th2;
                        IOUtils.closeQuietly(fileInputStream);
                        IOUtils.closeQuietly(fileOutputStream);
                        throw th;
                    }
                } catch (IOException e4) {
                    e2 = e4;
                    fileOutputStream = fileOutputStream2;
                    fileInputStream = fileInputStream2;
                    Log.e("MiuiHomeCloudBackupImpl", "IOException in restoreFiles: " + path, e2);
                    IOUtils.closeQuietly(fileInputStream);
                    IOUtils.closeQuietly(fileOutputStream);
                } catch (Throwable th3) {
                    th = th3;
                    fileOutputStream = fileOutputStream2;
                    fileInputStream = fileInputStream2;
                    IOUtils.closeQuietly(fileInputStream);
                    IOUtils.closeQuietly(fileOutputStream);
                    throw th;
                }
            } catch (FileNotFoundException e5) {
                e = e5;
                fileInputStream = fileInputStream2;
                Log.e("MiuiHomeCloudBackupImpl", "FileNotFoundException in restoreFiles: " + path, e);
                IOUtils.closeQuietly(fileInputStream);
                IOUtils.closeQuietly(fileOutputStream);
            } catch (IOException e6) {
                e2 = e6;
                fileInputStream = fileInputStream2;
                Log.e("MiuiHomeCloudBackupImpl", "IOException in restoreFiles: " + path, e2);
                IOUtils.closeQuietly(fileInputStream);
                IOUtils.closeQuietly(fileOutputStream);
            } catch (Throwable th4) {
                th = th4;
                fileInputStream = fileInputStream2;
                IOUtils.closeQuietly(fileInputStream);
                IOUtils.closeQuietly(fileOutputStream);
                throw th;
            }
        } catch (FileNotFoundException e7) {
            e = e7;
            Log.e("MiuiHomeCloudBackupImpl", "FileNotFoundException in restoreFiles: " + path, e);
            IOUtils.closeQuietly(fileInputStream);
            IOUtils.closeQuietly(fileOutputStream);
        } catch (IOException e8) {
            e2 = e8;
            Log.e("MiuiHomeCloudBackupImpl", "IOException in restoreFiles: " + path, e2);
            IOUtils.closeQuietly(fileInputStream);
            IOUtils.closeQuietly(fileOutputStream);
        }
    }

    private static void restoreFiles(Context context, DataPackage dataPackage) {
        for (Entry<String, ParcelFileDescriptor> entry : dataPackage.getFileItems().entrySet()) {
            restoreOneFile(context, (String) entry.getKey(), (ParcelFileDescriptor) entry.getValue());
        }
    }
}
