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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tory
 * @date 2018/10/22
 * @des:
 */
public class HomeImportUtils {


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
}
