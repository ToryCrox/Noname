package com.tory.library.utils.livebus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.tory.library.log.LogUtils;

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

    @SuppressLint("WrongThread")
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
    public void postLatest(@NonNull T value) {
        liveData.postValue(value);
    }

    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {
        liveData.observe(owner, observer);
    }

    @Override
    public void observeSticky(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {
        liveData.observeSticky(owner, observer);
    }

    private void observeWithView(@NonNull View view, @NonNull Observer<T> observer,
                                 boolean isSticky) {
        final Runnable observeTask = () -> {
            LogUtils.d("observeWithView observe add " + observer);
            if (isSticky) {
                observeStickyForever(observer);
            } else {
                observeForever(observer);
            }
        };

        observeTask.run();
        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            private boolean hasDetached = false;

            @Override
            public void onViewAttachedToWindow(View v) {
                if (hasDetached) {
                    observeTask.run();
                }
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                hasDetached = true;
                removeObserver(observer);
            }
        });
    }

    @Override
    public void observe(@NonNull View view, @NonNull Observer<T> observer) {
        observeWithView(view, observer, false);
    }

    @Override
    public void observeSticky(@NonNull View view, @NonNull Observer<T> observer) {
        observeWithView(view, observer, true);
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
