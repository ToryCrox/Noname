package com.miui.home.launcher;

import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import miui.util.HardwareInfo;

public class SpecificDeviceConfig {
    private static int mFlags = 0;

    static {
        if ("maguro".equals(Build.DEVICE) && "samsung".equals(Build.MANUFACTURER)) {
            mFlags |= 1;
        } else if ("crespo".equals(Build.DEVICE) && "samsung".equals(Build.MANUFACTURER)) {
            mFlags |= 2;
        }
        long totalMemory = HardwareInfo.getTotalPhysicalMemory();
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        if (((1.0f * ((float) dm.widthPixels)) * ((float) dm.heightPixels)) / ((float) totalMemory) >= 0.703125f) {
            mFlags |= 4;
        }
    }

    public static boolean isBigScreenLowMemory() {
        return (mFlags & 4) != 0;
    }
}
