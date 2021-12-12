package com.tory.library.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle

open class BaseViewModel(
    val applicationContext: Application,
    val savedStateHandle: SavedStateHandle
) : AndroidViewModel(applicationContext)