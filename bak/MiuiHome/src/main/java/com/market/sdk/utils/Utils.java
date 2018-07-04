package com.market.sdk.utils;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.util.Log;
import com.market.sdk.MarketManager;
import java.io.Closeable;
import java.io.IOException;
import miui.os.Build;

public class Utils {
    public static boolean isConnected(Context context) {
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    public static String getStringResources(String resName) {
        try {
            Resources res = MarketManager.getContext().getPackageManager().getResourcesForApplication(MarketManager.MARKET_PACKAGE_NAME);
            return res.getString(res.getIdentifier(resName, "string", MarketManager.MARKET_PACKAGE_NAME));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String[] getStringArray(String pkgName, String resName) {
        try {
            Context context = MarketManager.getContext().createPackageContext(pkgName, 0);
            return context.getResources().getStringArray(context.getResources().getIdentifier(resName, "array", pkgName));
        } catch (Exception e) {
            Log.e("MarketManager", e.getMessage(), e);
            return null;
        }
    }

    public static boolean isScreenOff() {
        return !((PowerManager) MarketManager.getContext().getSystemService("power")).isScreenOn();
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                Log.e("MarketManager", e.getMessage(), e);
            }
        }
    }

    public static boolean isXSpace() {
        if (UserHandle.myUserId() == Secure.getIntForUser(MarketManager.getContext().getContentResolver(), "second_user_id", -2, 0)) {
            return true;
        }
        return false;
    }

    public static boolean isInternationalBuild() {
        return Build.IS_INTERNATIONAL_BUILD;
    }
}
