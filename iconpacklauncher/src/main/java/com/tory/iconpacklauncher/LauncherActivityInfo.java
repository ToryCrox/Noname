package com.tory.iconpacklauncher;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import com.tory.library.utils.AppUtils;

import butterknife.internal.Utils;

public class LauncherActivityInfo {

    private final ResolveInfo mResolveInfo;
    private final ActivityInfo mActivityInfo;
    private final ComponentName mComponentName;
    private final PackageManager mPm;
    private final boolean isSystemApp;


    private CharSequence mLable;
    private Drawable mIcon;


    LauncherActivityInfo(Context context, PackageManager pm, ResolveInfo info) {
        super();
        mResolveInfo = info;
        mActivityInfo = info.activityInfo;
        mComponentName = new ComponentName(mActivityInfo.packageName, mActivityInfo.name);
        mPm = pm;
        isSystemApp = AppUtils.isSystemApp(context, mComponentName.getPackageName());

        loadIcon();
        loadLable();
    }


    public boolean isSystemApp(){
        return  isSystemApp;
    }

    public CharSequence loadLable(){
        if(mLable == null){
            mLable = mResolveInfo.loadLabel(mPm);
        }
        return mLable;
    }


    public Drawable loadIcon(){
        if(mIcon == null){
            mIcon = mResolveInfo.loadIcon(mPm);
        }
        return mIcon;
    }

    public String loadCompnent(){
        return mComponentName.flattenToShortString();
    }
}
