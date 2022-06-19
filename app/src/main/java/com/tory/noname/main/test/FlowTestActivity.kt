package com.tory.noname.main.test

import android.os.Bundle
import android.os.SystemClock
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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random

class FlowTestActivity : BaseActivity() {

    val viewModel: FlowTestViewModel by viewModels()

    override fun getLayoutId(): Int = R.layout.activity_flow_test

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return super.dispatchTouchEvent(ev)
    }

    override fun initView(savedInstanceState: Bundle?) {

//        viewModel.stateFlow
//            .onEach {
//                LogUtils.d("FlowTestFlow launchWhenStarted stateFlow collect... $it")
//                textView1.text = it
//            }
//            .launchIn(this)

        lifecycleScope.launchWhenStarted {
            viewModel.testState.collect {
                LogUtils.d("FlowTestFlow collect stateFlow collect... $it")
            }
        }

        val map = HashMap<String, String>()
        val r = Random.nextInt(100)
        val ss = ((r / 2) == (r shr 2))
        val i= 9
        Executors.newCachedThreadPool()

        viewModel.testState
            .filter { it.isNotBlank() }
            .map { "do $it" }
            .launchCollect(this) {
                LogUtils.d("FlowTestFlow launchCollect stateFlow collect... $it")
            }
//        lifecycleScope.launchWhenStarted {
//            viewModel.stateFlow.collect {
//                LogUtils.d("FlowTestFlow launchCollect stateFlow collect... $it")
//            }
//        }

//        viewModel.sharedFlow.launchCollect(this) {
//            LogUtils.d("FlowTestFlow launchCollect sharedFlow collect...$it")
//            textView2.text = it
//        }
        viewModel.bus.of(CosEvent::class.java)
            .filter { it.index > 10 }
            .onEach {
                LogUtils.d("FlowTestFlow launchCollect bus event...$it")
            }.launchIn(lifecycleScope)

//        lifecycleScope.launch {
//            delay(5000L)
//            viewModel.testEvent
//                .onEach {
//                    LogUtils.d("FlowTestEvent onEach event...$it")
//                }.launchIn(lifecycleScope)
//        }

        btn1.clickThrottle {
            val value = "stateFlow ${SystemClock.elapsedRealtime()}"
            LogUtils.d("$value")
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.testEvent
                    .collect {
                        LogUtils.d("FlowTestEvent launchWhenStarted event: $it")
                    }
            }

        }

        fun testCallbackFlow() = callbackFlow<Int> {
            var isStop = false
            lifecycleScope.launch {
                var index = 0
                while (!isStop) {
                    LogUtils.d("testCallbackFlow send: $index")
                    delay(1000)
                    trySend(index++)
                }
            }
            awaitClose {
                LogUtils.d("testCallbackFlow closed")
                isStop = true
            }
        }

//        lifecycleScope.launchWhenStarted {
//            testCallbackFlow()
//                .collect {
//                    LogUtils.d("testCallbackFlow collect: $it")
//                }
//        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                testCallbackFlow()
                    .collect {
                        LogUtils.d("testCallbackFlow collect: $it")
                    }
            }
        }
        Dispatchers.IO

//        viewModel.testEvent.launchCollect(this) {
//            LogUtils.d("FlowTestEvent launchCollect event: $it")
//        }
//        viewModel.testEvent
//            .onEach {
//                LogUtils.d("FlowTestEvent onEach event: $it")
//            }.launchIn(lifecycleScope)
/*        doOnStop {
            postDelayed(1000) {
                viewModel.postEvent("stop event")
            }
        }*/

        btn2.clickThrottle {
            viewModel.postEvent("click event")
        }

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