package com.tory.noname.main.test

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.viewModels
import androidx.lifecycle.*
import com.tory.library.base.BaseActivity
import com.tory.library.extension.clickThrottle
import com.tory.library.log.LogUtils
import com.tory.noname.R
import kotlinx.android.synthetic.main.activity_flow_test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.launchIn

class FlowTestActivity : BaseActivity() {

    val viewModel: FlowTestViewModel by viewModels()

    override fun getLayoutId(): Int = R.layout.activity_flow_test

    override fun initView(savedInstanceState: Bundle?) {

        viewModel.stateFlow
            .onEach {
                LogUtils.d("FlowTestFlow launchWhenStarted stateFlow collect... $it")
                textView1.text = it
            }
            .launchIn(this)

        viewModel.stateFlow.launchCollect(this) {
            LogUtils.d("FlowTestFlow launchCollect stateFlow collect... $it")
        }

        lifecycleScope.launchWhenStarted {
            viewModel.sharedFlow.collect {
                LogUtils.d("FlowTestFlow launchWhenStarted sharedFlow collect...$it")
                textView2.text = it
            }
        }

        lifecycleScope.launch {

            var index = 0
            var lastTime = 0L
            channelFlow<Int> {
                whenResumed {
                    invokeOnClose {
                        LogUtils.d("FlowTestFlowList doClose")
                        close()
                    }
                    while (isActive) {
                        LogUtils.d("FlowTestFlowList send:${index}")
                        send(index++)
                        LogUtils.d("FlowTestFlowList delay:${index}")
                        delay(5000)
                        LogUtils.d("FlowTestFlowList end:${index}")
                    }
                    LogUtils.d("FlowTestFlowList while end")
                }
            }.onCompletion {
                LogUtils.d("FlowTestFlowList onCompletion")
            }.collect {
                LogUtils.d("FlowTestFlowList collect...$it")
                if (lastTime > 0) {
                    val timeSpent = SystemClock.elapsedRealtime() - lastTime
                    LogUtils.d("FlowTestFlowList timeSpent...$timeSpent")
                }
                lastTime = SystemClock.elapsedRealtime()
            }
        }

        lifecycleScope.launch {
            var index = 0
            var lastTime = 0L
            tickFlow(period = 5000) {
                LogUtils.d("FlowTestFlowTick send...$index")
                index++
            }.collect {
                LogUtils.d("FlowTestFlowTick collect...$it")
                if (lastTime > 0) {
                    val timeSpent = SystemClock.elapsedRealtime() - lastTime
                    LogUtils.d("FlowTestFlowTick timeSpent...$timeSpent")
                }
                lastTime = SystemClock.elapsedRealtime()
            }
        }


        btn1.clickThrottle {
            val value = "stateFlow ${SystemClock.elapsedRealtime()}"
            LogUtils.d("$value")
            viewModel.stateFlow.value = value
        }

        btn2.clickThrottle {
            val value = "sharedFlow ${SystemClock.elapsedRealtime()}"
            lifecycleScope.launch {
                viewModel.sharedFlow.emit(value)
            }
        }
    }
}

public fun <T> Flow<T>.launchCollect(
    lifecycleOwner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (T) -> Unit
): Job {
    return lifecycleOwner.lifecycleScope.launch {
        flowWithLifecycle(lifecycleOwner.lifecycle, minActiveState)
            .collect(action)
    }
}

public fun <T> Flow<T>.launchIn(
    lifecycleOwner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED
): Job {
    return lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.lifecycle.whenStateAtLeast(minActiveState) {
            collect()
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
    check(initPeriod > 0) { "initPeriod must large than 0" }
    check(period > 0) { "period must large than 0" }
    var index = 0
    delay(initPeriod)
    lifecycle.whenStateAtLeast(minActiveState) {
        invokeOnClose {
            close()
        }
        while (isActive) {
            send(action(index++))
            delay(period)
        }
    }
}