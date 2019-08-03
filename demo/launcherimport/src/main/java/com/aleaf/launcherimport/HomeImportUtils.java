package com.aleaf.launcherimport;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.os.Process;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author tory
 * @date 2018/10/22
 * @des:
 */
public class HomeImportUtils {
    private static final String TAG = "HomeImportUtils";

    public static final Set<String> MIMIKKO_PKGS = new HashSet<>();
    static {
        MIMIKKO_PKGS.add("com.mimikko.mimikkoui");
        MIMIKKO_PKGS.add("com.mimikko.mimikkoui.mimikkoui2_app");
        MIMIKKO_PKGS.add("com.mimikko.mimikkoui.launcher3_app");
    }

    /**
     * 获取所有支持导入的桌面信息
     * @param context
     * @return
     */
    public static List<HomeAppSupportInfo> loadHomeAppSupportInfos(@NonNull Context context) {
        List<HomeAppSupportInfo> list = new ArrayList<>();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        final PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        String json = null;
        try {
            json = FileUtils.readAssets(context, R.raw.home_import_support);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Type type = new TypeToken<ArrayList<HomeAppSupportInfo>>() {
        }.getType();
        List<HomeAppSupportInfo> allSupportInfoList = new Gson().fromJson(json, type);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkg = resolveInfo.activityInfo.packageName;
            for (HomeAppSupportInfo info : allSupportInfoList) {
                if (TextUtils.equals(info.packageName, pkg)) {
                    info.title = resolveInfo.loadLabel(pm).toString();
                    list.add(info);
                    break;
                }
            }
        }
        return list;
    }

    /**
     * 检查是否有权限
     * @param context
     * @param sourceInfo
     * @return
     */
    public static boolean checkPermission(@NonNull Context context, HomeAppSupportInfo sourceInfo){
        for (ProviderInfo info : context.getPackageManager().queryContentProviders(
                null, context.getApplicationInfo().uid, 0)) {

            if (sourceInfo.packageName.equals(info.packageName)) {
                // Wait until we found a provider with matching authority.
                if (sourceInfo.authority.equals(info.authority)) {
                    if (TextUtils.isEmpty(info.readPermission) ||
                            context.checkPermission(info.readPermission, Process.myPid(),
                                    Process.myUid()) == PackageManager.PERMISSION_GRANTED) {
                        // All checks passed, run the import task.
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static List<HomeAppSupportInfo> loadAllHomeProviders(@NonNull Context context){
        List<HomeAppSupportInfo> list = new ArrayList<>();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        final PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        Map<String, String> pkgTitleMap = new HashMap<>();
        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkg = resolveInfo.activityInfo.packageName;
            if (MIMIKKO_PKGS.contains(pkg)){
                continue;
            }
            pkgTitleMap.put(pkg, resolveInfo.loadLabel(pm).toString());
        }

        for (ProviderInfo info : context.getPackageManager().queryContentProviders(
                null, context.getApplicationInfo().uid, 0)) {
            if (pkgTitleMap.containsKey(info.packageName)){
                String title = pkgTitleMap.get(info.packageName);
                String log = "loadAllHomeProviders title="+title + ", name="+info.name
                        +", pkg=" +info.packageName
                        + " authority=" + info.authority + ", readPermission="+info.readPermission;
                if (!TextUtils.isEmpty(info.name) && info.name.endsWith("LauncherProvider")){
                    //高亮显示
                    Log.e(TAG, log);
                    HomeAppSupportInfo homeInfo = new HomeAppSupportInfo();
                    homeInfo.title = title;
                    homeInfo.packageName = info.packageName;
                    homeInfo.authority = info.authority;
                    homeInfo.readPermission = info.readPermission;
                    homeInfo.name = info.name;
                    homeInfo.providerInfo = info;
                    homeInfo.isGranted = info.readPermission == null || context.checkPermission(info.readPermission,
                            Process.myPid(), Process.myUid()) == PackageManager.PERMISSION_GRANTED;
                    list.add(homeInfo);
                } else {
                    Log.i(TAG, log);
                }

            }
        }
        return list;
    }


}
