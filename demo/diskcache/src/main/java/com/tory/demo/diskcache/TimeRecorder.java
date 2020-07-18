package com.tory.demo.diskcache;

import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.ConcurrentHashMap;

public class TimeRecorder {

    private static final String TAG = "TimeRecorder";
    /**
     * release版下通过以下命令打印log，但是需要重启应用
     * $: adb shell setprop log.tag.TimeRecorder V
     * 关闭命令:
     * $: adb shell setprop log.tag.TimeRecorder D
     */
    public static final String TIME_RECORDER_LOG = "TimeRecorder";
    /**
     * debug模式下打开log，但是如果放在library里面会失效，因为library编译的一直是release版
     */
    private static boolean ENABLED = true;

    private static long t1;
    private static ConcurrentHashMap<String, Long> sTimeMap;
    private static ConcurrentHashMap<String, CountValue> sNanoCountTimeMap;

    private static Printer sPrinter;

    public static void setDebug(boolean debug) {
        ENABLED = debug;
    }

    public static void setPrinter(Printer printer){
        sPrinter = printer;
    }

    /**
     * @param msg
     */
    private static void logd(@NonNull String msg) {
        //这里的TAG最好整个模块一个统一的
        //LogUtils.d(TAG, msg);
        if (sPrinter != null){
            sPrinter.print(msg + "\n");
        }
        Log.d(TAG,  msg);
    }

    /**
     * 开始记录时间较短的耗时情况
     * 调用方式为：
     * {@link #beginNanoCount(String)}开始计时 -> {@link #pauseNanoCount(String)} 暂停计时
     * 以上重复调用
     * ->  {@link #endNanoCount(String, String)} 输出总耗时情况
     *
     * @param tag
     */
    public static void beginNanoCount(@NonNull String tag) {
        if (!ENABLED) {
            return;
        }
        ensureNanoCountTimeMap();
        CountValue countValue = sNanoCountTimeMap.get(tag);
        if (countValue == null) {
            countValue = new CountValue();
            sNanoCountTimeMap.put(tag, countValue);
            countValue.eclipseTime = 0;
            countValue.count = 0;
        }
        countValue.nanoTime = System.nanoTime();
    }

    /**
     * 暂停计时
     * 调用方式为：
     * {@link #beginNanoCount(String)}开始计时 -> {@link #pauseNanoCount(String)} 暂停计时
     * 以上重复调用
     * ->  {@link #endNanoCount(String, String)} 输出总耗时情况
     *
     * @param tag
     */
    public static void pauseNanoCount(@NonNull String tag) {
        if (!ENABLED) {
            return;
        }
        ensureNanoCountTimeMap();
        CountValue countValue = sNanoCountTimeMap.get(tag);
        if (countValue == null || countValue.nanoTime == 0) {
            return;
        }
        countValue.eclipseTime += System.nanoTime() - countValue.nanoTime;
        countValue.nanoTime = 0;
        countValue.count++;
    }

    /**
     * 输出耗时 {@link #endNanoCount(String, String)}
     *
     * @param tag
     */
    public static void endNanoCount(@NonNull String tag) {
        endNanoCount(tag, null);
    }

    /**
     * 输出耗时
     * 调用方式为：
     * {@link #beginNanoCount(String)}开始计时 -> {@link #pauseNanoCount(String)} 暂停计时
     * 以上重复调用
     * ->  {@link #endNanoCount(String, String)} 输出总耗时情况
     *
     * @param tag
     * @param call
     */
    public static void endNanoCount(@NonNull String tag, @Nullable String call) {
        if (!ENABLED) {
            return;
        }
        ensureNanoCountTimeMap();
        CountValue countValue = sNanoCountTimeMap.get(tag);
        if (countValue == null || countValue.count <= 0) {
            return;
        }
        logd(tag + " " + (call == null ? "" : call) +
                " time spent=" + nanoToMillis(countValue.eclipseTime) +
                ", count=" + countValue.count + ", per time spent="
                + nanoToMillis(countValue.eclipseTime / countValue.count) + "ms");
        sNanoCountTimeMap.remove(tag);
    }

    public static long nanoToMillis(long nanoTime) {
        return nanoTime / 1000000L;
    }

    /**
     * 开始记录时间，供临时开发调用
     * 对应调用无参数的end或者一个参数的end
     * {@link #end()}
     */
    public static void begin() {
        if (ENABLED) {
            t1 = currentTimeMillis();
        }
    }

    public static long end() {
        if (ENABLED) {
            return currentTimeMillis() - t1;
        }
        return 0;
    }


    /**
     * 开始记录时间
     * 对应调用两个参数的end
     * {@link #end(String, String)}
     *
     * @param tag : 记录时间的惟一标识
     */
    public static void begin(@NonNull String tag) {
        if (ENABLED) {
            ensureTimeMap();
            sTimeMap.put(tag, currentTimeMillis());
        }
    }

    /**
     * 输出耗时
     * {@link #begin(String)}
     *
     * @param tag
     */
    public static void end(@NonNull String tag) {
        end(tag, null);
    }

    /**
     * 输出耗时
     * {@link #begin(String)}
     *
     * @param tag  记录时间的惟一标识
     * @param call :log输出内容,可以为空
     */
    public static void end(@NonNull String tag, @Nullable String call) {
        if (ENABLED) {
            ensureTimeMap();
            Long timeStamp = sTimeMap.get(tag);
            if (timeStamp == null) {
                return;
            }
            logd(tag + " " + (call == null ? "" : call)
                    + " time spent=" + (currentTimeMillis() - timeStamp) + "ms");
            sTimeMap.remove(tag);
        }
    }

    /**
     * 记录时间,可考虑以后使用其它单位
     *
     * @return
     */
    private static long currentTimeMillis() {
        return SystemClock.uptimeMillis();
    }

    private static void ensureTimeMap() {
        if (ENABLED) {
            if (sTimeMap == null) {
                sTimeMap = new ConcurrentHashMap<>();
            }
        }
    }

    private static void ensureNanoCountTimeMap() {
        if (ENABLED) {
            if (sNanoCountTimeMap == null) {
                sNanoCountTimeMap = new ConcurrentHashMap<>();
            }
        }
    }

    private static class CountValue {
        int count;
        long nanoTime;
        long eclipseTime;
    }

    interface Printer {
        void print(@NonNull String msg);
    }
}
