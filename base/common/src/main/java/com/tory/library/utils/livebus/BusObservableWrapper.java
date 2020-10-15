package com.tory.library.utils.livebus;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/10/14
 * Description:
 */
class BusObservableWrapper<T extends LiveBusEvent> implements BusObservable<T> {

    private final Class<T> eventType;
    private final BusLiveData<T> liveData = new BusLiveData<>();
    private Handler mainHandler = null;

    public BusObservableWrapper(Class<T> eventType) {
        this.eventType = eventType;
    }

    @Override
    @NonNull
    public Class<T> getEventType(){
        return eventType;
    }

    private Handler getMainHandler() {
        if (mainHandler == null) {
            mainHandler = new Handler(Looper.getMainLooper());
        }
        return mainHandler;
    }

    @Override
    public void post(@Nullable T value) {
        if (isMainThread()) {
            liveData.setValue(value);
        } else {
            getMainHandler().post(new Runnable() {
                @Override
                public void run() {
                    liveData.setValue(value);
                }
            });
        }
    }

    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {
        liveData.observe(owner, observer);
    }

    @Override
    public void observeSticky(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {
        liveData.observeSticky(owner, observer);
    }

    @Override
    public void observeForever(@NonNull Observer<T> observer) {
        liveData.observeForever(observer);
    }

    @Override
    public void observeStickyForever(@NonNull Observer<T> observer) {
        liveData.observeStickyForever(observer);
    }

    @Override
    public void removeObserver(@NonNull Observer<T> observer) {
        liveData.removeObserver(observer);
    }

    private boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }
}
