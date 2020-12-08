package com.tory.library.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import java.lang.ref.WeakReference;

import static androidx.lifecycle.Lifecycle.State.DESTROYED;

/**
 * Author: tory
 * Date: 2020/11/30
 * Email: xutao@theduapp.com
 * Description:
 */
public class LifecycleReference<T> extends RetainReference implements LifecycleEventObserver {

    @Nullable
    private LifecycleOwner mOwner;

    private Runnable mClearCallback;


    public LifecycleReference(@NonNull LifecycleOwner owner, @NonNull T referent) {
        super(referent);
        mOwner = owner;
        owner.getLifecycle().addObserver(this);
    }

    /**
     * 设置清除时的回调
     * @param clearCallback
     */
    public void setClearCallback(Runnable clearCallback) {
        this.mClearCallback = clearCallback;
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        if (mOwner != null && mOwner.getLifecycle().getCurrentState() == DESTROYED) {
            clear();
        }
    }


    @Override
    public void clear() {
        if (mClearCallback != null) {
            mClearCallback.run();
        }
        super.clear();
        if (mOwner != null) {
            mOwner.getLifecycle().removeObserver(this);
            mOwner = null;
        }
    }
}
