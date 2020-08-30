package com.tory.library.applife;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tory
 * @date 2018-7-9
 * 仿Glide
 */
public final class ManifestParser {
    private static final String GLIDE_MODULE_VALUE = "AppLifeModule";

    private final Context context;

    public ManifestParser(Context context) {
        this.context = context;
    }

    public List<IAppLife> parse() {
        List<IAppLife> modules = new ArrayList<>();
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo.metaData != null) {
                for (String key : appInfo.metaData.keySet()) {
                    if (GLIDE_MODULE_VALUE.equals(appInfo.metaData.get(key))) {
                        modules.add(parseModule(key));
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Unable to find metadata to parse GlideModules", e);
        }

        return modules;
    }

    private static IAppLife parseModule(String className) {
        Class<?> clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Unable to find AppLifeModule implementation," +
                    " className:" + className, e);
        }

        Object module;
        try {
            module = clazz.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("Unable to instantiate AppLifeModule implementation for " + clazz, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to instantiate AppLifeModule implementation for " + clazz, e);
        }

        if (!(module instanceof IAppLife)) {
            throw new RuntimeException("Expected instanceof AppLifeModule, but found: " + module);
        }
        return (IAppLife) module;
    }
}
