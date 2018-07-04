package com.market.sdk.utils;

import android.os.Build.VERSION;

public class VersionUtils {
    public static boolean isDevVersionLaterThan(String targetVersion) {
        String reg = "\\d{1,2}\\.\\d{1,2}\\.\\d{1,2}(-internal)?";
        if (targetVersion.matches(reg)) {
            String currVersion = VERSION.INCREMENTAL;
            if (!currVersion.matches(reg)) {
                return false;
            }
            currVersion = currVersion.replace("-internal", "");
            targetVersion = targetVersion.replace("-internal", "");
            String[] currArr = currVersion.split("\\.");
            String[] targetArr = targetVersion.split("\\.");
            return ((Long.parseLong(currArr[0]) * 10000) + (Long.parseLong(currArr[1]) * 100)) + Long.parseLong(currArr[2]) >= ((Long.parseLong(targetArr[0]) * 10000) + (Long.parseLong(targetArr[1]) * 100)) + Long.parseLong(targetArr[2]);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static boolean isStableVersionLaterThan(String targetVersion) {
        if (targetVersion.matches("V\\d{1,2}\\.\\d{1,2}\\.\\d{1,2}\\.\\d{1,2}")) {
            String currVersion = VERSION.INCREMENTAL;
            if (!currVersion.matches("V\\d{1,2}\\.\\d{1,2}\\.\\d{1,2}\\.\\d{1,2}\\..*")) {
                return false;
            }
            String[] currArr = currVersion.split("\\.");
            String[] targetArr = targetVersion.split("\\.");
            return (((Long.parseLong(currArr[0].substring(1)) * 1000000) + (Long.parseLong(currArr[1]) * 10000)) + (Long.parseLong(currArr[2]) * 100)) + Long.parseLong(currArr[3]) >= (((Long.parseLong(targetArr[0].substring(1)) * 1000000) + (Long.parseLong(targetArr[1]) * 10000)) + (Long.parseLong(targetArr[2]) * 100)) + Long.parseLong(targetArr[3]);
        } else {
            throw new IllegalArgumentException();
        }
    }
}
