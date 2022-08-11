package com.tory.noname.main.test

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.SystemClock
import android.view.Choreographer
import android.view.MotionEvent
import androidx.activity.viewModels
import androidx.annotation.IntDef
import androidx.core.location.LocationManagerCompat.requestLocationUpdates
import androidx.lifecycle.*
import androidx.lifecycle.repeatOnLifecycle
import com.tory.library.base.BaseActivity
import com.tory.library.extension.*
import com.tory.library.log.LogUtils
import com.tory.noname.R
import dalvik.system.*
import kotlinx.android.synthetic.main.activity_flow_test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import retrofit2.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.random.Random

class FlowTestActivity : BaseActivity() {

    val viewModel: FlowTestViewModel by viewModels()

    override fun getLayoutId(): Int = R.layout.activity_flow_test

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return super.dispatchTouchEvent(ev)
    }

    override fun initView(savedInstanceState: Bundle?) {

        lifecycleScope.launchWhenStarted {
            viewModel.testState.collect {
                LogUtils.d("FlowTestFlow collect stateFlow collect... $it")
            }
        }

        viewModel.combineState.launchCollect(this) {
            LogUtils.d("FlowTestFlow collect combineState collect... $it")
        }

        viewModel.testState
            .filter { it.isNotBlank() }
            .map { "do $it" }
            .launchCollect(this) {
                LogUtils.d("FlowTestFlow launchCollect stateFlow collect... $it")
            }

        lifecycleScope.launch {
            LogUtils.d("FlowTestFlow firstOrNull wait....")
            val result = viewModel.test2State.firstOrNull { it >= 10 }
            LogUtils.d("FlowTestFlow firstOrNull wait end result:$result")
        }

        lifecycleScope.launchWhenStarted {
            viewModel.testState.collect {
                LogUtils.d("FlowTestFlow launchCollect stateFlow collect... $it")
            }
        }

//        viewModel.sharedFlow.launchCollect(this) {
//            LogUtils.d("FlowTestFlow launchCollect sharedFlow collect...$it")
//            textView2.text = it
//        }
        viewModel.bus.of(CosEvent::class.java)
            .filter { it.index > 10 }
            .onEach {
                LogUtils.d("FlowTestFlow launchCollect bus event...$it")
            }.launchIn(lifecycleScope)

        btn1.clickThrottle {
            val value = "stateFlow ${SystemClock.elapsedRealtime()}"
            LogUtils.d("$value")
        }

        viewModel.testEvent.onEach {
            LogUtils.d("FlowTestEvent launchWhenStarted event: $it")
        }.launchIn(lifecycleScope)

        fun testCallbackFlow() = callbackFlow<Int> {
            var isStop = false
            lifecycleScope.launch {
                var index = 0
                while (!isStop) {
                    LogUtils.d("testCallbackFlow send: $index")
                    delay(1000)
                    send(index++)
                }
            }
            awaitClose {
                LogUtils.d("testCallbackFlow closed")
                isStop = true
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                testCallbackFlow()
                    .collect {
                        LogUtils.d("testCallbackFlow collect: $it")
                    }
            }
        }

        btn2.clickThrottle {
            viewModel.postEvent("click event")
        }
        var lastTime = 0L
        observeFrameCallback(this) {
            if (lastTime > 0) {
                LogUtils.d("doFrame, timeSpent: ${(it - lastTime) * 0.000001f}")
            }
            lastTime = it
        }

    }

    private fun observeFrameCallback(lifecycleOwner: LifecycleOwner, callback: Choreographer.FrameCallback) {
        val frameCallback =object :Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                callback.doFrame(frameTimeNanos)
                Choreographer.getInstance().postFrameCallback(this)
            }
        }
        lifecycleOwner.doOnLifecycle(onCreate = {
            Choreographer.getInstance().postFrameCallback(frameCallback)
        }, onDestroy = {
            Choreographer.getInstance().removeFrameCallback(frameCallback)
        })
    }
}

public fun <T> Flow<T>.launchCollect(
    lifecycleOwner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (T) -> Unit
): Job {
    return lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.lifecycle.repeatOnLifecycle(minActiveState) {
            collect(action)
        }
    }
}

public fun <T> SharedFlow<T>.launchCollect(
    lifecycleOwner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (T) -> Unit
): Job {
    return lifecycleOwner.lifecycleScope.launch {
        collect {
            lifecycleOwner.lifecycle.whenStateAtLeast(minActiveState) {
                action(it)
            }
        }
    }
}

/**
 * 周期性的回调
 */
@ExperimentalCoroutinesApi
fun <T> LifecycleOwner.tickFlow(
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    initPeriod: Long = 0L,
    period: Long, action: suspend (index: Int) -> T
) = channelFlow<T> {
    check(initPeriod >= 0) { "initPeriod must large than 0" }
    check(period > 0) { "period must large than 0" }
    var index = 0
    delay(initPeriod)
    lifecycle.whenStateAtLeast(minActiveState) {
        while (isActive) {
            send(action(index++))
            delay(period)
        }
    }
}

