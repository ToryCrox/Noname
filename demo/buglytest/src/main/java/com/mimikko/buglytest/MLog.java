package com.mimikko.buglytest;

import android.util.Log;

/**
 * @author xutao
 * @date 2018.5.14
 * Log打印工具，log.d和log.i会被控制输出，log.w和log.e会一直输出
 */
public class MLog {
    private static final String TAG = "BUGLY_TEST";
    /**
     * release版下通过以下命令打印log
     * $: adb shell setprop log.tag.MimikkoUI V
     * 关闭命令:
     * $: adb shell setprop log.tag.MimikkoUI D
     */
    private static final boolean DEFAULT_LOG_STATUS = BuildConfig.DEBUG || isPropertyEnabled(TAG);
    private static boolean sLogStatus = DEFAULT_LOG_STATUS;

    public static boolean isPropertyEnabled(String propertyName) {
        return Log.isLoggable(propertyName, Log.VERBOSE);
    }

    public static void setDebug(boolean isDebug){
            sLogStatus =  isDebug;
    }

    public static boolean getDebugStatus(){
        return sLogStatus;
    }

    /**
     *
     */
    public static void initLogStatus() {
        sLogStatus = DEFAULT_LOG_STATUS;
    }


    /**
     * adb shell dumpsys activity com.mimikko.mimikkoui open/close
     *
     * @param command
     */

    public static void dumpLog(String[] command) {
        if (DEFAULT_LOG_STATUS || command == null || command.length == 0) {
            return;
        }
        if (command[0].contains("open")) {
            sLogStatus = true;
        } else if (command[0].contains("close")) {
            initLogStatus();
        }
    }


    public static void v(String msg) {
        if (sLogStatus) {
            Log.v(TAG, msg);
        }
    }

    public static void i(String msg) {
        if (sLogStatus) {
            Log.i(TAG, msg);
        }
    }

    public static void i(String tag, String msg) {
        Log.i(TAG, tag + " " + msg);
    }

    public static void d(String msg) {
        if (sLogStatus) {
            Log.d(TAG, msg);
        }
    }

    public static void d(String tag, String msg){
        if (sLogStatus) {
            Log.d(TAG, tag + " " +msg);
        }
    }

    public static void w(String msg) {
        Log.w(TAG, msg);
    }

    public static void w(String tag, String msg) {
        w(tag + " "+msg);
    }


    public static void e(String msg) {
        Log.e(TAG, msg);
    }

    public static void e(String tag, String msg) {
        e(TAG + " " + msg);
    }

    public static void e(String msg, Throwable e) {
        Log.e(TAG, msg, e);
    }

    public static Throwable getStackTrace() {
        final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        Throwable e = new Throwable();
        e.setStackTrace(stackTrace);
        return e;
    }
}
