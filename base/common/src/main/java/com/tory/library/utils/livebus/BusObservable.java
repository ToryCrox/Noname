package com.tory.library.utils.livebus;

import android.view.View;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/10/14
 * Description:
 */
public interface BusObservable<T> {

    /**
     * 数据类型
     * @return
     */
    @NonNull
    public Class<T> getEventType();

    /**
     * 进程内发送消息，都往主线程发送消息
     *
     * @param value 发送的消息
     */
    @AnyThread
    void post(@NonNull T value);

    /**
     * 进程内发送消息，都往主线程发送消息，如果连续postLatest，只会保留最后一个
     *
     * @param value 发送的消息
     */
    @AnyThread
    void postLatest(@NonNull T value);

    /**
     * 注册一个Observer，生命周期感知，自动取消订阅
     *
     * @param owner    LifecycleOwner
     * @param observer 观察者
     */
    @MainThread
    void observe(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer);

    /**
     * 注册一个Observer，生命周期感知，自动取消订阅
     * 如果之前有消息发送，可以在注册时收到消息（消息同步）
     *
     * @param owner    LifecycleOwner
     * @param observer 观察者
     */
    @MainThread
    void observeSticky(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer);

    /**
     * 注册一个Observer，view移除时自动取消订阅
     *
     * @param owner    LifecycleOwner
     * @param observer 观察者
     */
    @MainThread
    void observe(@NonNull View view, @NonNull Observer<T> observer);

    /**
     * 注册一个Observer，view移除时自动取消订阅、
     * 如果之前有消息发送，可以在注册时收到消息（消息同步）
     *
     * @param owner    LifecycleOwner
     * @param observer 观察者
     */
    @MainThread
    void observeSticky(@NonNull View view, @NonNull Observer<T> observer);


    /**
     * 注册一个Observer，需手动解除绑定
     *
     * @param observer 观察者
     */
    @MainThread
    void observeForever(@NonNull Observer<T> observer);

    /**
     * 注册一个Observer，需手动解除绑定
     * 如果之前有消息发送，可以在注册时收到消息（消息同步）
     *
     * @param observer 观察者
     */
    @MainThread
    void observeStickyForever(@NonNull Observer<T> observer);

    /**
     * 通过observeForever或observeStickyForever注册的，需要调用该方法取消订阅
     *
     * @param observer 观察者
     */
    @MainThread
    void removeObserver(@NonNull Observer<T> observer);
}
