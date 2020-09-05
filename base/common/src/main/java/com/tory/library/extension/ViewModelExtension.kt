package com.tory.library.extension

import android.view.View
import androidx.activity.viewModels
import androidx.annotation.MainThread
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Created by joe on 2020/8/24.
 * Email: lovejjfg@gmail.com
 */
@MainThread
inline fun <reified VM : ViewModel> View.activityViewModels(
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<VM> = appCompatActivity().viewModels(factoryProducer)

@MainThread
inline fun <reified VM : ViewModel> View.fragmentViewModels(
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<VM> = checkNotNull(this.findFragment(appCompatActivity())).viewModels(
    factoryProducer = factoryProducer)
