package com.miui.home.launcher.common;

import android.app.Activity;
import android.os.Handler;
import java.util.ArrayList;
import java.util.List;

public class ForegroundTaskQueue {
    private List<Runnable> mMessageList = new ArrayList();

    public void handleRemainingTasksOnResume(Activity activity, Handler handler) {
        if (activity.isResumed()) {
            for (Runnable r : this.mMessageList) {
                handler.post(r);
            }
            this.mMessageList.clear();
        }
    }

    private void handleTask(Activity activity, Handler handler, Runnable runable) {
        if (activity.isResumed()) {
            handler.post(runable);
        } else {
            this.mMessageList.add(runable);
        }
    }

    public void addTask(Activity activity, Handler handler, Runnable runable) {
        if (this.mMessageList.contains(runable)) {
            this.mMessageList.remove(runable);
        }
        handleTask(activity, handler, runable);
    }

    public void onDestroy() {
        this.mMessageList.clear();
    }
}
