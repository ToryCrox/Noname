package com.tory.noname.main.test

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.tory.library.base.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class FlowTestViewModel(
    applicationContext: Application,
    savedStateHandle: SavedStateHandle
) : BaseViewModel(applicationContext, savedStateHandle) {

    val stateFlow = MutableStateFlow<String>("state init")

    val sharedFlow = MutableSharedFlow<String>(replay = 0)

    init {

        viewModelScope.launch {
            repeat(20) { index ->
                delay(1000)
                stateFlow.value = "state $index"
            }
        }
    }
}