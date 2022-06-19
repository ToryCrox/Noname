package com.tory.noname.main.test

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.tory.library.base.BaseViewModel
import com.tory.library.log.LogUtils
import com.tory.library.utils.flowbus.FlowBusCore
import com.tory.library.utils.flowbus.FlowEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FlowTestViewModel(
    applicationContext: Application,
    savedStateHandle: SavedStateHandle
) : BaseViewModel(applicationContext, savedStateHandle) {

    private val _testState = MutableStateFlow("init")
    val testState = _testState.asStateFlow()

    private val _testEvent = MutableSharedFlow<String>()
    val testEvent = _testEvent.asSharedFlow()


    val bus = FlowBusCore(this)


    init {

        viewModelScope.launch {
            repeat(1000) { index ->
                delay(1000)
                //stateFlow.value = "state $index"
                LogUtils.d("FlowTestFlow emit: $index")
                _testState.value = "$index"
                _testState.emit("$index")
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