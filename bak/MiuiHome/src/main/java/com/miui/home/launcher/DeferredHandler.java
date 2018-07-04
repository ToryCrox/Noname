package com.miui.home.launcher;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.MessageQueue.IdleHandler;
import java.util.LinkedList;

public class DeferredHandler {
    private Impl mHandler = new Impl();
    private MessageQueue mMessageQueue = Looper.myQueue();
    private LinkedList<Runnable> mQueue = new LinkedList();

    private class IdleRunnable implements Runnable {
        Runnable mRunnable;

        public void run() {
            this.mRunnable.run();
        }
    }

    private class Impl extends Handler implements IdleHandler {
        private Impl() {
        }

        public void handleMessage(Message msg) {
            synchronized (DeferredHandler.this.mQueue) {
                if (DeferredHandler.this.mQueue.size() == 0) {
                    return;
                }
                Runnable r = (Runnable) DeferredHandler.this.mQueue.removeFirst();
                r.run();
                synchronized (DeferredHandler.this.mQueue) {
                    DeferredHandler.this.scheduleNextLocked();
                }
            }
        }

        public boolean queueIdle() {
            handleMessage(null);
            return false;
        }
    }

    public void post(Runnable runnable) {
        synchronized (this.mQueue) {
            this.mQueue.add(runnable);
            if (this.mQueue.size() == 1) {
                scheduleNextLocked();
            }
        }
    }

    public void cancel() {
        synchronized (this.mQueue) {
            this.mQueue.clear();
        }
    }

    void scheduleNextLocked() {
        if (this.mQueue.size() <= 0) {
            return;
        }
        if (((Runnable) this.mQueue.getFirst()) instanceof IdleRunnable) {
            this.mMessageQueue.addIdleHandler(this.mHandler);
        } else {
            this.mHandler.sendEmptyMessage(1);
        }
    }
}
