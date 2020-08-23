package com.tory.library.widget;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadUtils {

    /**
     * copy from launcher3
     */
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE = 1;
    /**
     * An {@link Executor} to be used with async task with no limit on the queue size.
     */
    public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
            CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    private static Handler mMainHandler;

    public static void execute(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        THREAD_POOL_EXECUTOR.execute(runnable);
    }

    public static void executeDelayed(@NonNull Runnable runnable, int delay){
        postMain(() -> execute(runnable),(long) delay);
    }

    /**
     * execute to ui thread
     *
     * @param runnable
     */
    public static void postMain(Runnable runnable) {
        postMain(runnable, 0L);
    }

    /**
     * execute to ui thread with delay time
     *
     * @param runnable
     */
    public static void postMain(Runnable runnable, long delayMillis) {
        if (runnable == null) {
            return;
        }
        getMainHandler().postDelayed(runnable, delayMillis);
    }

    /**
     * remove from ui thread queue
     *
     * @param runnable
     */
    public static void removeMain(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        getMainHandler().removeCallbacks(runnable);
    }

    public static Handler getMainHandler() {
        if (mMainHandler == null) {
            mMainHandler = new Handler(Looper.getMainLooper());
        }
        return mMainHandler;
    }

}