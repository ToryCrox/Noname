package com.miui.home.launcher.common;

import android.app.Activity;
import android.os.Build.VERSION;
import android.support.v4.app.ActivityCompat;
import java.util.ArrayList;

public class PermissionUtils {
    private static final String[] CALL_PONHE_PERMISSIONS = new String[]{"android.permission.CALL_PHONE"};
    private static final String[] RUNTIME_PERMISSIONS = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE", "android.permission.READ_PHONE_STATE", "android.permission.READ_FRAME_BUFFER", "android.permission.READ_CALL_LOG", "android.permission.CALL_PHONE", "android.permission.READ_CONTACTS", "android.permission.READ_SMS", "android.permission.CAMERA", "android.permission.RECORD_AUDIO"};
    private static final String[] STORAGE_PERMISSIONS = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"};

    public static void requestAccessStoragePermissions(Activity activity) {
        requestMultiplePermissions(activity, STORAGE_PERMISSIONS, 1);
    }

    public static void requestCallPhonePermissions(Activity activity, int requestCode) {
        requestMultiplePermissions(activity, CALL_PONHE_PERMISSIONS, requestCode);
    }

    public static boolean checkCallPhonePermission(Activity activity) {
        return ActivityCompat.checkSelfPermission(activity, CALL_PONHE_PERMISSIONS[0]) == 0;
    }

    public static void requestMultiplePermissions(Activity activity, String[] permissions, int requestCode) {
        if (VERSION.SDK_INT >= 23) {
            ArrayList<String> neededPermissions = new ArrayList();
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(activity, permission) != 0) {
                    neededPermissions.add(permission);
                }
            }
            if (!neededPermissions.isEmpty()) {
                String[] permissionArr = new String[neededPermissions.size()];
                neededPermissions.toArray(permissionArr);
                ActivityCompat.requestPermissions(activity, permissionArr, requestCode);
            }
        }
    }
}
