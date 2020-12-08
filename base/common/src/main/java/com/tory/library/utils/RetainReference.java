package com.tory.library.utils;

/**
 * Author: tory
 * Date: 2020/11/30
 * Email: xutao@theduapp.com
 * Description:
 */
public class RetainReference<T> implements SafeReference<T> {

    protected T mReferent;

    public RetainReference(T referent) {
        this.mReferent = referent;
    }

    @Override
    public T get() {
        return mReferent;
    }

    @Override
    public void clear() {
        mReferent = null;
    }
}
