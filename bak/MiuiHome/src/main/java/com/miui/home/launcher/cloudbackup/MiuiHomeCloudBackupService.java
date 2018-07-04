package com.miui.home.launcher.cloudbackup;

import com.xiaomi.settingsdk.backup.CloudBackupServiceBase;
import com.xiaomi.settingsdk.backup.ICloudBackup;

public class MiuiHomeCloudBackupService extends CloudBackupServiceBase {
    protected ICloudBackup getBackupImpl() {
        return new MiuiHomeCloudBackupImpl();
    }
}
