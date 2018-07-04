package com.miui.home.launcher;

import android.app.IntentService;
import android.content.Context;
import android.miui.Shell;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import com.miui.home.launcher.common.Utilities;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import miui.os.FileUtils;

public class CloudAppBackupIntentService extends IntentService {
    private static final Uri BASE_URI = Uri.parse("content://com.miui.cloudbackup.dbcache");

    public CloudAppBackupIntentService() {
        super("CloudAppBackupIntentService");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void onHandleIntent(android.content.Intent r10) {
        /*
        r9 = this;
        r6 = com.miui.home.launcher.Application.getLauncherApplication(r9);
        r3 = r6.getLauncherProvider();
        if (r3 == 0) goto L_0x008b;
    L_0x000a:
        r7 = r3.getLock();
        monitor-enter(r7);
        r6 = "HomeBackUp";
        r8 = r10.getAction();	 Catch:{ all -> 0x0088 }
        r6 = r6.equals(r8);	 Catch:{ all -> 0x0088 }
        if (r6 == 0) goto L_0x004a;
    L_0x001b:
        r6 = "CloudAppBackupIntentService";
        r8 = "HandleIntent ACTION_HOME_BACK_UP";
        android.util.Log.d(r6, r8);	 Catch:{ all -> 0x0088 }
        r6 = "CloudAppBackupIntentServiceReceiver";
        r4 = r10.getParcelableExtra(r6);	 Catch:{ all -> 0x0088 }
        r4 = (android.os.ResultReceiver) r4;	 Catch:{ all -> 0x0088 }
        if (r4 == 0) goto L_0x0048;
    L_0x002c:
        r0 = new java.util.ArrayList;	 Catch:{ all -> 0x0088 }
        r0.<init>();	 Catch:{ all -> 0x0088 }
        r5 = r9.backup(r0);	 Catch:{ all -> 0x0088 }
        r1 = new android.os.Bundle;	 Catch:{ all -> 0x0088 }
        r1.<init>();	 Catch:{ all -> 0x0088 }
        r6 = "result";
        r1.putBoolean(r6, r5);	 Catch:{ all -> 0x0088 }
        r6 = "backupFileInfos";
        r1.putParcelableArrayList(r6, r0);	 Catch:{ all -> 0x0088 }
        r6 = 0;
        r4.send(r6, r1);	 Catch:{ all -> 0x0088 }
    L_0x0048:
        monitor-exit(r7);	 Catch:{ all -> 0x0088 }
    L_0x0049:
        return;
    L_0x004a:
        r6 = "HomeRestore";
        r8 = r10.getAction();	 Catch:{ all -> 0x0088 }
        r6 = r6.equals(r8);	 Catch:{ all -> 0x0088 }
        if (r6 == 0) goto L_0x0086;
    L_0x0056:
        r6 = "CloudAppBackupIntentService";
        r8 = "HandleIntent ACTION_HOME_RESTORE";
        android.util.Log.d(r6, r8);	 Catch:{ all -> 0x0088 }
        r6 = "CloudAppBackupIntentServiceReceiver";
        r4 = r10.getParcelableExtra(r6);	 Catch:{ all -> 0x0088 }
        r4 = (android.os.ResultReceiver) r4;	 Catch:{ all -> 0x0088 }
        r6 = "RestoreFileNames";
        r2 = r10.getStringArrayListExtra(r6);	 Catch:{ all -> 0x0088 }
        if (r4 == 0) goto L_0x0086;
    L_0x006d:
        r5 = r9.recover(r2);	 Catch:{ all -> 0x0088 }
        r1 = new android.os.Bundle;	 Catch:{ all -> 0x0088 }
        r1.<init>();	 Catch:{ all -> 0x0088 }
        r6 = "result";
        r1.putBoolean(r6, r5);	 Catch:{ all -> 0x0088 }
        r6 = 0;
        r4.send(r6, r1);	 Catch:{ all -> 0x0088 }
        r6 = android.os.Process.myPid();	 Catch:{ all -> 0x0088 }
        android.os.Process.killProcess(r6);	 Catch:{ all -> 0x0088 }
    L_0x0086:
        monitor-exit(r7);	 Catch:{ all -> 0x0088 }
        goto L_0x0049;
    L_0x0088:
        r6 = move-exception;
        monitor-exit(r7);	 Catch:{ all -> 0x0088 }
        throw r6;
    L_0x008b:
        r6 = "CloudAppBackupIntentService";
        r7 = "LauncherProvider=null, return";
        android.util.Log.d(r6, r7);
        goto L_0x0049;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.miui.home.launcher.CloudAppBackupIntentService.onHandleIntent(android.content.Intent):void");
    }

    private boolean backup(ArrayList<Bundle> backupfileInfos) {
        try {
            addBackupFileInfo(backupfileInfos, new File(new File(getDatabasePath("foo").getParentFile().getCanonicalPath()), DeviceConfig.getDatabaseName()));
            try {
                for (File spFile : new File(getSharedPrefsFile("foo").getParentFile().getCanonicalPath()).listFiles()) {
                    addBackupFileInfo(backupfileInfos, spFile);
                }
                return true;
            } catch (IOException e) {
                Log.d("CloudAppBackupIntentService", "IOException in backupSp", e);
                return false;
            }
        } catch (IOException e2) {
            Log.d("CloudAppBackupIntentService", "IOException in backupDatabse", e2);
            return false;
        }
    }

    private void addBackupFileInfo(ArrayList<Bundle> backupfileInfos, File databaseFile) throws FileNotFoundException {
        Bundle fileInfo = new Bundle();
        String fileName = FileUtils.getFileName(databaseFile.getAbsolutePath());
        ParcelFileDescriptor pFD = ParcelFileDescriptor.open(databaseFile, 268435456);
        if (pFD == null) {
            Log.e("CloudAppBackupIntentService", "parcelFileDescriptor of " + fileName + " is null");
            return;
        }
        fileInfo.putString("fileName", fileName);
        fileInfo.putParcelable("parcelFileDescriptor", pFD);
        backupfileInfos.add(fileInfo);
    }

    private boolean recover(ArrayList<String> fileNames) {
        try {
            File databaseFolder = new File(getDatabasePath("foo").getParentFile().getCanonicalPath());
            if (!databaseFolder.exists()) {
                databaseFolder.mkdirs();
            }
            deleteContents(databaseFolder);
            File spFolder = new File(getSharedPrefsFile("foo").getParentFile().getCanonicalPath());
            Log.d("CloudAppBackupIntentService", "spFolder exist?" + spFolder.exists());
            if (!spFolder.exists()) {
                Log.d("CloudAppBackupIntentService", "spFolder mkdirs?" + spFolder.mkdirs());
            }
            Iterator i$ = fileNames.iterator();
            while (i$.hasNext()) {
                String fileName = (String) i$.next();
                Log.d("CloudAppBackupIntentService", "restore file name: " + fileName);
                if ("db".equalsIgnoreCase(FileUtils.getExtension(fileName))) {
                    copyFile(getApplicationContext(), fileName, new File(databaseFolder, fileName));
                }
                if ("xml".equalsIgnoreCase(FileUtils.getExtension(fileName))) {
                    copyFile(getApplicationContext(), fileName, new File(spFolder, fileName));
                }
            }
            Shell.chmod(spFolder.getCanonicalPath(), 493);
            return true;
        } catch (IOException e) {
            Log.d("CloudAppBackupIntentService", "IOException in recover", e);
            return false;
        }
    }

    private static boolean copyFile(Context context, String srcFileName, File dst) {
        IOException e;
        Throwable th;
        FileInputStream srcFile = null;
        FileChannel srcChannel = null;
        FileOutputStream dstFile = null;
        ParcelFileDescriptor srcPFD = null;
        try {
            srcPFD = context.getContentResolver().openFileDescriptor(BASE_URI.buildUpon().appendPath(srcFileName).build(), "r");
            if (srcPFD == null) {
                Log.e("CloudAppBackupIntentService", "src pfd is null");
                Utilities.closeFileSafely(null);
                Utilities.closeFileSafely(null);
                Utilities.closeFileSafely(null);
                Utilities.closeFileSafely(null);
                Utilities.closeFileSafely(srcPFD);
                return false;
            }
            FileInputStream srcFile2 = new FileInputStream(srcPFD.getFileDescriptor());
            try {
                srcChannel = srcFile2.getChannel();
                FileOutputStream dstFile2 = new FileOutputStream(dst);
                try {
                    FileChannel dstChannel = dstFile2.getChannel();
                    dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
                    if (dst.getName().contains("_world_readable_preferences")) {
                        Shell.chmod(dst.getCanonicalPath(), 766);
                    }
                    Utilities.closeFileSafely(srcChannel);
                    Utilities.closeFileSafely(dstChannel);
                    Utilities.closeFileSafely(srcFile2);
                    Utilities.closeFileSafely(dstFile2);
                    Utilities.closeFileSafely(srcPFD);
                    dstFile = dstFile2;
                    srcFile = srcFile2;
                    return true;
                } catch (IOException e2) {
                    e = e2;
                    dstFile = dstFile2;
                    srcFile = srcFile2;
                    try {
                        Log.d("CloudAppBackupIntentService", "IOException in recover", e);
                        Utilities.closeFileSafely(srcChannel);
                        Utilities.closeFileSafely(null);
                        Utilities.closeFileSafely(srcFile);
                        Utilities.closeFileSafely(dstFile);
                        Utilities.closeFileSafely(srcPFD);
                        return false;
                    } catch (Throwable th2) {
                        th = th2;
                        Utilities.closeFileSafely(srcChannel);
                        Utilities.closeFileSafely(null);
                        Utilities.closeFileSafely(srcFile);
                        Utilities.closeFileSafely(dstFile);
                        Utilities.closeFileSafely(srcPFD);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    dstFile = dstFile2;
                    srcFile = srcFile2;
                    Utilities.closeFileSafely(srcChannel);
                    Utilities.closeFileSafely(null);
                    Utilities.closeFileSafely(srcFile);
                    Utilities.closeFileSafely(dstFile);
                    Utilities.closeFileSafely(srcPFD);
                    throw th;
                }
            } catch (IOException e3) {
                e = e3;
                srcFile = srcFile2;
                Log.d("CloudAppBackupIntentService", "IOException in recover", e);
                Utilities.closeFileSafely(srcChannel);
                Utilities.closeFileSafely(null);
                Utilities.closeFileSafely(srcFile);
                Utilities.closeFileSafely(dstFile);
                Utilities.closeFileSafely(srcPFD);
                return false;
            } catch (Throwable th4) {
                th = th4;
                srcFile = srcFile2;
                Utilities.closeFileSafely(srcChannel);
                Utilities.closeFileSafely(null);
                Utilities.closeFileSafely(srcFile);
                Utilities.closeFileSafely(dstFile);
                Utilities.closeFileSafely(srcPFD);
                throw th;
            }
        } catch (IOException e4) {
            e = e4;
            Log.d("CloudAppBackupIntentService", "IOException in recover", e);
            Utilities.closeFileSafely(srcChannel);
            Utilities.closeFileSafely(null);
            Utilities.closeFileSafely(srcFile);
            Utilities.closeFileSafely(dstFile);
            Utilities.closeFileSafely(srcPFD);
            return false;
        }
    }

    public static void deleteContents(File dirname) {
        File[] contents = dirname.listFiles();
        if (contents != null) {
            for (File file : contents) {
                if (file.isDirectory()) {
                    deleteContents(file);
                }
                file.delete();
            }
        }
    }
}
