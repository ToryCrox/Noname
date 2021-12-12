package com.tory.noname.main.test

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.tory.library.base.BaseActivity
import com.tory.library.extension.clickThrottle
import com.tory.library.log.LogUtils
import com.tory.noname.R
import kotlinx.android.synthetic.main.activity_flow_test.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class FlowTestActivity: BaseActivity() {

    val viewModel: FlowTestViewModel by viewModels()

    override fun getLayoutId(): Int = R.layout.activity_flow_test

    override fun initView(savedInstanceState: Bundle?) {

        lifecycleScope.launch {
            viewModel.stateFlow.collect {
                textView1.text = it
            }
        }

        lifecycleScope.launch {
            viewModel.sharedFlow.collect {
                LogUtils.d("lifecycleScope: collect...$it")
                textView2.text = it
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