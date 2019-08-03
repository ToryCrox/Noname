package com.tory.iconpacklauncher;

import android.content.ComponentName;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

public class AppInfo implements Comparable {
    String name;
    String pkg;
    ComponentName cn;
    Drawable icon;
    int versionCode;

    public AppInfo(String name, String pkg, int versionCode, Drawable icon) {
        this.name = name;
        this.pkg = pkg;
        this.icon = icon;
        this.versionCode = versionCode;
    }

    public AppInfo(String name, ComponentName cn, Drawable icon) {
        this.name = name;
        this.cn = cn;
        this.icon = icon;
    }


    @Override
    public String toString() {
        return "AppInfo{" +
                "pkg='" + pkg + '\'' +
                ", icon=" + icon +
                '}';
    }

    @Override
    public int compareTo(@NonNull Object o) {
        AppInfo other = (AppInfo) o;
        int a = this.pkg.compareTo(other.pkg);
        if (a != 0) {
            return a;
        }
        return this.name.compareTo(other.name);
    }
}