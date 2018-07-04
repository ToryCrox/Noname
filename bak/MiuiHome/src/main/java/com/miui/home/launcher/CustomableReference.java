package com.miui.home.launcher;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

public class CustomableReference<T> {
    private Reference<T> mRef;
    private T mStrongRef;
    private int mType;

    public CustomableReference(T obj, int type) {
        this.mType = type;
        switch (type) {
            case 1:
                this.mStrongRef = obj;
                return;
            case 2:
                this.mRef = new WeakReference(obj);
                return;
            case 3:
                this.mRef = new SoftReference(obj);
                return;
            default:
                throw new RuntimeException("unknown reference type:" + this.mType);
        }
    }

    public T get() {
        switch (this.mType) {
            case 1:
                return this.mStrongRef;
            case 2:
            case 3:
                return this.mRef.get();
            default:
                throw new RuntimeException("unknown reference type:" + this.mType);
        }
    }
}
