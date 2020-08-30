package com.tory.module.hilt

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.tory.library.base.BaseActivity
import com.tory.module.hilt.analytics.AnalyticsService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_hilt_demo.*
import javax.inject.Inject

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/8/28
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/8/28 xutao 1.0
 * Why & What is modified:
 */
@AndroidEntryPoint
class HiltDemoActivity: BaseActivity() {

    val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var analyticsService: AnalyticsService

    override fun getLayoutId(): Int = R.layout.activity_hilt_demo

    override fun initView(savedInstanceState: Bundle?) {

        viewModel.gankData.observe(this, Observer {
            textView.text = it?.toString()
        })

        viewModel.getGankData("Android")

        analyticsService.analyticsMethods()

    }
}
