package com.xiaomi.settingsdk.backup;

import android.app.IntentService;
import android.os.Bundle;
import android.util.Log;
import com.xiaomi.settingsdk.backup.data.DataPackage;

public abstract class CloudBackupServiceBase extends IntentService {
    protected abstract ICloudBackup getBackupImpl();

    public CloudBackupServiceBase() {
        super("SettingsBackup");
    }

    private String prependPackageName(String msg) {
        return getPackageName() + ": " + msg;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void onHandleIntent(android.content.Intent r14) {
        /*
        r13 = this;
        r12 = 0;
        if (r14 != 0) goto L_0x0004;
    L_0x0003:
        return;
    L_0x0004:
        r9 = "SettingsBackup";
        r10 = new java.lang.StringBuilder;
        r10.<init>();
        r11 = "myPid: ";
        r10 = r10.append(r11);
        r11 = android.os.Process.myPid();
        r10 = r10.append(r11);
        r10 = r10.toString();
        r10 = r13.prependPackageName(r10);
        android.util.Log.d(r9, r10);
        r9 = "SettingsBackup";
        r10 = new java.lang.StringBuilder;
        r10.<init>();
        r11 = "intent: ";
        r10 = r10.append(r11);
        r10 = r10.append(r14);
        r10 = r10.toString();
        r10 = r13.prependPackageName(r10);
        android.util.Log.d(r9, r10);
        r9 = "SettingsBackup";
        r10 = new java.lang.StringBuilder;
        r10.<init>();
        r11 = "extras: ";
        r10 = r10.append(r11);
        r11 = r14.getExtras();
        r10 = r10.append(r11);
        r10 = r10.toString();
        r10 = r13.prependPackageName(r10);
        android.util.Log.d(r9, r10);
        r0 = r14.getAction();
        r9 = "result_receiver";
        r6 = r14.getParcelableExtra(r9);
        r6 = (android.os.ResultReceiver) r6;
        r9 = "miui.action.CLOUD_BACKUP_SETTINGS";
        r9 = r9.equals(r0);
        if (r9 == 0) goto L_0x008c;
    L_0x0074:
        if (r6 == 0) goto L_0x0003;
    L_0x0076:
        r1 = r13.backupSettings();
        if (r1 != 0) goto L_0x0087;
    L_0x007c:
        r9 = "SettingsBackup";
        r10 = "bundle result is null after backupSettings";
        r10 = r13.prependPackageName(r10);
        android.util.Log.e(r9, r10);
    L_0x0087:
        r6.send(r12, r1);
        goto L_0x0003;
    L_0x008c:
        r9 = "miui.action.CLOUD_RESTORE_SETTINGS";
        r9 = r9.equals(r0);
        if (r9 == 0) goto L_0x0003;
    L_0x0094:
        if (r6 == 0) goto L_0x0003;
    L_0x0096:
        r9 = r14.getExtras();
        r10 = "data_package";
        r3 = r9.getBinder(r10);
        r2 = android.os.Parcel.obtain();
        r7 = android.os.Parcel.obtain();
        r9 = 2;
        r10 = 0;
        r3.transact(r9, r2, r7, r10);	 Catch:{ RemoteException -> 0x00ff, BadParcelableException -> 0x010f }
        r9 = "version";
        r10 = -1;
        r5 = r14.getIntExtra(r9, r10);	 Catch:{ RemoteException -> 0x00ff, BadParcelableException -> 0x010f }
        r9 = r13.getClass();	 Catch:{ RemoteException -> 0x00ff, BadParcelableException -> 0x010f }
        r9 = r9.getClassLoader();	 Catch:{ RemoteException -> 0x00ff, BadParcelableException -> 0x010f }
        r9 = r7.readParcelable(r9);	 Catch:{ RemoteException -> 0x00ff, BadParcelableException -> 0x010f }
        r9 = (com.xiaomi.settingsdk.backup.data.DataPackage) r9;	 Catch:{ RemoteException -> 0x00ff, BadParcelableException -> 0x010f }
        r8 = r13.restoreSettings(r9, r5);	 Catch:{ RemoteException -> 0x00ff, BadParcelableException -> 0x010f }
        r9 = "SettingsBackup";
        r10 = new java.lang.StringBuilder;	 Catch:{ RemoteException -> 0x00ff, BadParcelableException -> 0x010f }
        r10.<init>();	 Catch:{ RemoteException -> 0x00ff, BadParcelableException -> 0x010f }
        r11 = "r.send()";
        r10 = r10.append(r11);	 Catch:{ RemoteException -> 0x00ff, BadParcelableException -> 0x010f }
        r11 = java.lang.Thread.currentThread();	 Catch:{ RemoteException -> 0x00ff, BadParcelableException -> 0x010f }
        r10 = r10.append(r11);	 Catch:{ RemoteException -> 0x00ff, BadParcelableException -> 0x010f }
        r10 = r10.toString();	 Catch:{ RemoteException -> 0x00ff, BadParcelableException -> 0x010f }
        r10 = r13.prependPackageName(r10);	 Catch:{ RemoteException -> 0x00ff, BadParcelableException -> 0x010f }
        android.util.Log.d(r9, r10);	 Catch:{ RemoteException -> 0x00ff, BadParcelableException -> 0x010f }
        if (r8 == 0) goto L_0x00f9;
    L_0x00e8:
        r9 = 0;
        r10 = new android.os.Bundle;	 Catch:{ RemoteException -> 0x00ff, BadParcelableException -> 0x010f }
        r10.<init>();	 Catch:{ RemoteException -> 0x00ff, BadParcelableException -> 0x010f }
        r6.send(r9, r10);	 Catch:{ RemoteException -> 0x00ff, BadParcelableException -> 0x010f }
    L_0x00f1:
        r2.recycle();
        r7.recycle();
        goto L_0x0003;
    L_0x00f9:
        r9 = 0;
        r10 = 0;
        r6.send(r9, r10);	 Catch:{ RemoteException -> 0x00ff, BadParcelableException -> 0x010f }
        goto L_0x00f1;
    L_0x00ff:
        r4 = move-exception;
        r9 = "SettingsBackup";
        r10 = "RemoteException in onHandleIntent()";
        android.util.Log.e(r9, r10, r4);	 Catch:{ all -> 0x011f }
        r2.recycle();
        r7.recycle();
        goto L_0x0003;
    L_0x010f:
        r4 = move-exception;
        r9 = "SettingsBackup";
        r10 = "BadParcelableException when read readParcelable";
        android.util.Log.e(r9, r10, r4);	 Catch:{ all -> 0x011f }
        r2.recycle();
        r7.recycle();
        goto L_0x0003;
    L_0x011f:
        r9 = move-exception;
        r2.recycle();
        r7.recycle();
        throw r9;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xiaomi.settingsdk.backup.CloudBackupServiceBase.onHandleIntent(android.content.Intent):void");
    }

    private boolean restoreSettings(DataPackage dataPackage, int version) {
        Log.d("SettingsBackup", prependPackageName("SettingsBackupServiceBase:restoreSettings"));
        ICloudBackup backuper = checkAndGetBackuper();
        int currentVersion = backuper.getCurrentVersion(this);
        if (version > currentVersion) {
            Log.w("SettingsBackup", "drop restore data because dataVersion is higher than currentAppVersion, dataVersion: " + version + ", currentAppVersion: " + currentVersion);
            return false;
        }
        backuper.onRestoreSettings(this, dataPackage, version);
        return true;
    }

    private Bundle backupSettings() {
        Log.d("SettingsBackup", prependPackageName("SettingsBackupServiceBase:backupSettings"));
        ICloudBackup backuper = checkAndGetBackuper();
        DataPackage dataPackage = new DataPackage();
        backuper.onBackupSettings(this, dataPackage);
        Bundle bundle = new Bundle();
        dataPackage.appendToWrappedBundle(bundle);
        bundle.putInt("version", backuper.getCurrentVersion(this));
        return bundle;
    }

    private ICloudBackup checkAndGetBackuper() {
        ICloudBackup backuper = getBackupImpl();
        if (backuper != null) {
            return backuper;
        }
        throw new IllegalArgumentException("backuper must not be null");
    }
}
