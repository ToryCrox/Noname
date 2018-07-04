package com.miui.home.launcher.setting;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.LauncherApps.Callback;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.service.wallpaper.IWallpaperConnection;
import android.service.wallpaper.IWallpaperService;
import com.miui.home.launcher.LauncherModel.ComponentAndUser;
import java.util.ArrayList;
import java.util.List;

public class PortableUtils {

    public static abstract class LauncherApps_Callback extends Callback {
    }

    public static void attachWallpaperService(IWallpaperService service, IWallpaperConnection connection, IBinder windowToken, int windowType, boolean isPreview, int reqWidth, int reqHeight) throws RemoteException {
        service.attach(connection, windowToken, windowType, isPreview, reqWidth, reqHeight, new Rect());
    }

    public static void registerLauncherAppsCallback(Context context, LauncherApps_Callback callback) {
        ((LauncherApps) context.getSystemService("launcherapps")).registerCallback(callback);
    }

    public static Drawable getUserBadgedIcon(Context context, Drawable icon, UserHandle user) {
        if ((icon instanceof BitmapDrawable) && !Process.myUserHandle().equals(user)) {
            Bitmap bmp = ((BitmapDrawable) icon).getBitmap();
            icon = new BitmapDrawable(context.getResources(), bmp.copy(bmp.getConfig(), true));
        }
        return context.getPackageManager().getUserBadgedIcon(icon, user);
    }

    public static List<ResolveInfo> resolveActivityAsUser(Context context, Intent intent, int flags, int userId) {
        return context.getPackageManager().queryIntentActivities(intent, flags);
    }

    public static List<ComponentAndUser> launcherApps_getActivityList(Context context, String pkgName, UserHandle user) {
        LauncherApps launcherApps = (LauncherApps) context.getSystemService("launcherapps");
        List<ComponentAndUser> list = new ArrayList();
        if (user != null) {
            addToComponentAndUserList(list, launcherApps.getActivityList(pkgName, user));
        } else {
            List<UserHandle> profiles = ((UserManager) context.getSystemService("user")).getUserProfiles();
            for (int i = 0; i < profiles.size(); i++) {
                addToComponentAndUserList(list, launcherApps.getActivityList(pkgName, (UserHandle) profiles.get(i)));
            }
        }
        return list;
    }

    private static void addToComponentAndUserList(List<ComponentAndUser> list, List<LauncherActivityInfo> infos) {
        for (LauncherActivityInfo info : infos) {
            list.add(new ComponentAndUser(info.getComponentName(), info.getUser()));
        }
    }

    public static void startMainActivity(Context context, ComponentName component, UserHandle user, Rect sourceBounds, Bundle opts) {
        ((LauncherApps) context.getSystemService("launcherapps")).startMainActivity(component, user, sourceBounds, opts);
    }
}
