package com.miui.home.launcher.backup;

import android.app.backup.BackupDataOutput;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import com.miui.home.launcher.DeviceConfig;
import com.miui.home.launcher.LauncherSettings;
import java.io.IOException;
import miui.app.backup.BackupManager;
import miui.app.backup.BackupMeta;
import miui.app.backup.FullBackupAgent;

public class LauncherBackupAgent extends FullBackupAgent {
    private boolean mHasRemovedDB = false;

    protected int onRestoreEnd(BackupMeta meta) throws IOException {
        BackupManager.getBackupManager(this).setIsNeedBeKilled(true);
        this.mHasRemovedDB = false;
        return 0;
    }

    protected void onOriginalAttachesRestore(BackupMeta meta, ParcelFileDescriptor data, long size, int type, String domain, String path, long mode, long mtime) throws IOException {
        if (path.endsWith(".db") && !this.mHasRemovedDB) {
            DeviceConfig.removeInvalidateDatabase(this, false);
            DeviceConfig.removeDownloadInstallInfo(getApplicationContext());
            this.mHasRemovedDB = true;
        } else if (path.endsWith(LauncherSettings.getDownloadInstallInfoPath(getApplicationContext()))) {
            Log.i("Launcher.restore", "ignore download install info txt");
            return;
        }
        super.onOriginalAttachesRestore(meta, data, size, type, domain, path, mode, mtime);
    }

    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {
        DeviceConfig.removeInvalidateDatabase(this, true);
        super.onBackup(oldState, data, newState);
    }
}
