package com.miui.home.launcher;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.BaseColumns;
import miui.os.Environment;

public class LauncherSettings {
    public static final String OPERATOR_RETAINED_LIST_PATH = (Environment.getMiuiCustomizedDirectory().getAbsolutePath() + "operator.retained.list");
    public static final String PRESET_APPS_PATH = Environment.getMiuiPresetAppDirectory().getAbsolutePath();
    private static String mDownloadInstallInfoFileName = "download_install_info.txt";
    private static String mDownloadInstallInfoPath;
    private static String mRemovedComponentFileName = "removed_component_info.txt";
    private static String mRemovedComponentPath;

    public interface BaseLauncherColumns extends BaseColumns {
    }

    public static final class Favorites implements BaseLauncherColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://com.miui.home.launcher.settings/favorites");

        public static Uri getContentUri(long id) {
            return Uri.parse("content://com.miui.home.launcher.settings/favorites/" + id);
        }

        public static Uri getJoinContentUri(String join) {
            return Uri.parse("content://com.miui.home.launcher.settings/favorites" + join);
        }
    }

    public static final class Packages implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://com.miui.home.launcher.settings/packages");
    }

    public static final class Screens implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://com.miui.home.launcher.settings/screens");
    }

    public static String getRemovedComponentInfoPath(Context context) {
        mRemovedComponentPath = context.getDatabasePath(mRemovedComponentFileName).getAbsolutePath();
        return mRemovedComponentPath;
    }

    public static String getDownloadInstallInfoPath(Context context) {
        mDownloadInstallInfoPath = context.getDatabasePath(mDownloadInstallInfoFileName).getAbsolutePath();
        return mDownloadInstallInfoPath;
    }

    public static void deletePackage(Context context, String packageName, UserHandle user) {
        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put("name", packageName);
        values.put("delete", Boolean.valueOf(true));
        UserManager userManager = (UserManager) context.getSystemService("user");
        if (user == null) {
            user = Process.myUserHandle();
        }
        values.put("profileId", Long.valueOf(userManager.getSerialNumberForUser(user)));
        cr.update(Packages.CONTENT_URI, values, null, null);
    }

    public static boolean isRetainedPackage(String packageName) {
        return "com.android.stk".equals(packageName);
    }
}
