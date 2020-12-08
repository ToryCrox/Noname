package com.tory.library.utils;

import androidx.annotation.Nullable;

/**
 * Author: tory
 * Date: 2020/11/30
 * Email: xutao@theduapp.com
 * Description:
 */
public interface SafeReference<T> {

    public T get();

    public void clear();
}