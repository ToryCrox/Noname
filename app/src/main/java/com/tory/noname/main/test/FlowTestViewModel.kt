package com.tory.noname.main.test

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import com.tory.library.base.BaseViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

class FlowTestViewModel(
    applicationContext: Application,
    savedStateHandle: SavedStateHandle
) : BaseViewModel(applicationContext, savedStateHandle) {


    val stateFlow = MutableStateFlow<String>("state init")

    val sharedFlow = MutableSharedFlow<String>(replay = 0)


}