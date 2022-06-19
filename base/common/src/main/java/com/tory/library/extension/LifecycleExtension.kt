package com.tory.library.extension

import android.os.Looper
import android.os.MessageQueue
import android.view.View
import androidx.core.view.doOnAttach
import androidx.lifecycle.*
import kotlinx.coroutines.*

/**
 * - Author: tory
 * - Date: 2022/6/14
 * - Description:
 */

/**
 * 延迟执行，比较安全的写法, 采用协程方法, 方法体可直接使用协程的挂起函数
 *
 * 在Activity中可以直接使用
 *
 *      postDelay(200L){  doSomething   }
 *
 * 在fragment中建议使用viewLifecycleOwner
 *
 *      viewLifecycleOwner.postDelay(200L) {  doSomething   }
 */
fun LifecycleOwner.postDelayed(delayInMillis: Long = 0L, block: () -> Unit): Job {
    return lifecycleScope.launch {
        if (delayInMillis > 0) {
            delay(delayInMillis)
        }
        block()
    }
}

val LifecycleOwner.isCreated: Boolean
    get() = lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)

val LifecycleOwner.isStarted: Boolean
    get() = lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)

val LifecycleOwner.isResumed: Boolean
    get() = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)

val LifecycleOwner.isDestroyed: Boolean
    get() = lifecycle.currentState == Lifecycle.State.DESTROYED


typealias LifecycleEventCallback = (LifecycleOwner) -> Unit
typealias LifecycleAllEventCallback = (LifecycleOwner, Lifecycle.Event) -> Unit

/**
 * Lifecycle生命周期的监听
 */
fun Lifecycle.doOnLifecycle(
    onCreate: LifecycleEventCallback? = null,
    onStart: LifecycleEventCallback? = null,
    onResume: LifecycleEventCallback? = null,
    onPause: LifecycleEventCallback? = null,
    onStop: LifecycleEventCallback? = null,
    onDestroy: LifecycleEventCallback? = null,
    onEvent: LifecycleAllEventCallback? = null
) {
    addObserver(LifecycleEventObserver { source, event ->
        when (event) {
            Lifecycle.Event.ON_CREATE -> onCreate?.invoke(source)
            Lifecycle.Event.ON_START -> onStart?.invoke(source)
            Lifecycle.Event.ON_RESUME -> onResume?.invoke(source)
            Lifecycle.Event.ON_PAUSE -> onPause?.invoke(source)
            Lifecycle.Event.ON_STOP -> onStop?.invoke(source)
            Lifecycle.Event.ON_DESTROY -> onDestroy?.invoke(source)
            else -> Unit
        }
        onEvent?.invoke(source, event)
    })
}

fun LifecycleOwner.doOnLifecycle(
    onCreate: LifecycleEventCallback? = null,
    onStart: LifecycleEventCallback? = null,
    onResume: LifecycleEventCallback? = null,
    onPause: LifecycleEventCallback? = null,
    onStop: LifecycleEventCallback? = null,
    onDestroy: LifecycleEventCallback? = null,
    onEvent: LifecycleAllEventCallback? = null
) {
    lifecycle.doOnLifecycle(onCreate, onStart, onResume, onPause, onStop, onDestroy, onEvent)
}


fun LifecycleOwner.doOnCreate(onCreate: LifecycleEventCallback) {
    doOnLifecycle(onCreate = onCreate)
}

fun LifecycleOwner.doOnStart(onStart: LifecycleEventCallback) {
    doOnLifecycle(onStart = onStart)
}

fun LifecycleOwner.doOnResume(onResume: LifecycleEventCallback) {
    doOnLifecycle(onResume = onResume)
}

fun LifecycleOwner.doOnPause(onPause: LifecycleEventCallback) {
    doOnLifecycle(onPause = onPause)
}

fun LifecycleOwner.doOnStop(onStop: LifecycleEventCallback) {
    doOnLifecycle(onStop = onStop)
}

fun LifecycleOwner.doOnDestroy(onDestroy: LifecycleEventCallback) {
    doOnLifecycle(onDestroy = onDestroy)
}

/**
 * 在空闲时间执行任务
 */
fun LifecycleOwner.addIdleTask(block: () -> Unit) {
    lifecycleScope.launch {
        val idleHandler = MessageQueue.IdleHandler {
            if (!isDestroyed) {
                block()
            }
            false
        }
        Looper.myQueue().addIdleHandler(idleHandler)
        doOnDestroy {
            Looper.myQueue().removeIdleHandler(idleHandler)
        }
    }

}