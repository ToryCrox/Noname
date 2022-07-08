package com.tory.noname.main.test

import android.app.Application
import androidx.lifecycle.*
import com.tory.library.base.BaseViewModel
import com.tory.library.log.LogUtils
import com.tory.library.utils.flowbus.FlowBusCore
import com.tory.library.utils.flowbus.FlowEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class FlowTestViewModel(
    applicationContext: Application,
    savedStateHandle: SavedStateHandle
) : BaseViewModel(applicationContext, savedStateHandle) {

    private val _testState = MutableStateFlow("init")
    val testState = _testState.asStateFlow()

    private val _test2State = MutableStateFlow(0)
    val test2State = _test2State.asStateFlow()

    val test3State = test2State.map { it.toString() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val combineState = combine(testState, test2State){s1, s2->
        "$s1#$s2"
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "")


    private val _testEvent = MutableSharedFlow<String>()
    val testEvent = _testEvent.asSharedFlow()


    val bus = FlowBusCore(this)

    companion object {

    }

    init {

        viewModelScope.launch {
            FlowTestViewModel
            repeat(1000) { index ->
                delay(1000)
                LogUtils.d("FlowTestFlow emit: $index")
                _testState.value = "$index"

                //delay(1000)
                _test2State.value = index
            }
        }

        viewModelScope.launch {
            repeat(10) { index ->
                delay(1000)
                //stateFlow.value = "state $index"
                LogUtils.d("FlowTestEvent emit: $index")
                _testEvent.emit("$index")
            }
        }
        viewModelScope.launch {
            repeat(1000) { index ->
                delay(1000)
                LogUtils.d("FlowTestBus post index: $index")
                bus.post(CosEvent(index))
            }
        }

    }

    fun postEvent(msg: String) {
        viewModelScope.launch {
            LogUtils.d("FlowTestEvent emit msg: $msg")
            _testEvent.emit(msg)
        }

    }

}

data class CosEvent(val index: Int): FlowEvent