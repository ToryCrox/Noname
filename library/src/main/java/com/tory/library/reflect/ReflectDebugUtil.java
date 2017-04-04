package com.tory.library.reflect;

import android.content.Context;

import java.io.File;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

/**
 * @Author: tory
 * Create: 2017/4/4
 */
public class ReflectDebugUtil {



    public static final String DEBUG_PACKGE = "com.tory.debug";
    public static final String DEBUG_STETHO_CLASS_NAME = "com.tory.debug.StethoReflection";

    /**
     * //chrome://inspect
     * @param context
     */
    public static void reflectInitStetho(Context context){
        try {
            Context stethoContext = context.createPackageContext(
                    DEBUG_PACKGE, Context.CONTEXT_INCLUDE_CODE
                            | Context.CONTEXT_IGNORE_SECURITY);

            String outDir = context.getFilesDir() + File.separator + "debug";
            if(!new File(outDir).exists()){
                new File(outDir).mkdirs();
            }
            DexClassLoader dexLoader = new DexClassLoader(
                    stethoContext.getApplicationInfo().sourceDir,//dst apk surce path
                    outDir,//
                    context.getApplicationInfo().nativeLibraryDir,//.so
                    context.getClassLoader());
            Class<?> clazz = dexLoader.loadClass(DEBUG_STETHO_CLASS_NAME);
            Object  ste = clazz.newInstance();
            Method m = clazz.getMethod("initStetho",Context.class);
            m.invoke(ste,context);
            //XLog.d("initStetho .. finish");
        } catch (Exception e) {
            e.printStackTrace();
            //XLog.e("initStetho .. error", e);
        }
    }
}
