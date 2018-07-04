package com.miui.home.launcher.common;

import android.os.AsyncTask;
import java.util.ArrayList;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class AsyncTaskExecutorHelper {
    private static RejectedExecutionPolicy mRejectedPolicy = new RejectedExecutionPolicy();

    public static class RejectedExecutionPolicy implements RejectedExecutionHandler {
        private static ArrayList<Runnable> sRejectedTask = new ArrayList();

        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            synchronized (sRejectedTask) {
                sRejectedTask.add(r);
            }
        }

        public static void executeRejectedTaskIfNeeded() {
            synchronized (sRejectedTask) {
                if (sRejectedTask.size() > 0) {
                    Runnable task = (Runnable) sRejectedTask.remove(0);
                    if (task != null) {
                        AsyncTask.THREAD_POOL_EXECUTOR.execute(task);
                    }
                }
            }
        }
    }

    public static void initDefaultExecutor() {
        ((ThreadPoolExecutor) AsyncTask.THREAD_POOL_EXECUTOR).setRejectedExecutionHandler(mRejectedPolicy);
        AsyncTask.setDefaultExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void clearExcutorQueue() {
        ((ThreadPoolExecutor) AsyncTask.THREAD_POOL_EXECUTOR).getQueue().clear();
    }
}
