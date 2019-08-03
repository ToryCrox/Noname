package com.aleaf.launcherimport;

import android.content.pm.ProviderInfo;

import java.util.Arrays;

/**
 * @author tory
 * @date 2018/10/22
 * @des:
 */
public class HomeAppSupportInfo {

    public String title;
    public String aliasTitle;
    public String packageName;
    public String authority;
    public String readPermission;

    public String name;
    public ProviderInfo providerInfo;
    public boolean isGranted;

    @Override
    public String toString() {
        return "HomeAppSupportInfo{" +
                "title='" + title + '\'' +
                ", aliasTitle='" + aliasTitle + '\'' +
                ", packageName='" + packageName + '\'' +
                ", authority='" + authority + '\'' +
                ", readPermission='" + readPermission + '\'' +
                ", name='" + name + '\'' +
                ", providerInfo.flags=" + (providerInfo != null ? providerInfo.flags : -1)+
                ", providerInfo.pathPermissions=" + (providerInfo != null ?
                        Arrays.toString(providerInfo.pathPermissions) : -1)+
                ", providerInfo.uriPermissionPatterns=" + (providerInfo != null ?
                        Arrays.toString(providerInfo.uriPermissionPatterns) : -1)+
                ", isGranted=" + isGranted +
                '}';
    }
}
