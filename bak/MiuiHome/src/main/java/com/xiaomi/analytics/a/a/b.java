package com.xiaomi.analytics.a.a;

import android.content.Context;
import android.content.pm.Signature;

public class b {
    public static Signature[] a(Context context, String str) {
        try {
            return context.getPackageManager().getPackageInfo(str, 64).signatures;
        } catch (Exception e) {
            return null;
        }
    }
}
