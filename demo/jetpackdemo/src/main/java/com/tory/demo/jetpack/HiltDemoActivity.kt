package com.tory.demo.jetpack

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.tory.library.base.BaseListActivity
import com.tory.library.log.LogUtils
import com.tory.demo.jetpack.analytics.AnalyticsService
import com.tory.demo.jetpack.views.GankItemView
import dagger.hilt.android.AndroidEntryPoint
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
class HiltDemoActivity: BaseListActivity() {

    val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var analyticsService: AnalyticsService


    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setDisplayHomeAsUpEnabled(false)
        refreshLayout.isEnabled = false
        viewModel.gankData.observe(this, Observer {
            val list = it?.results.orEmpty()
            LogUtils.d("list: " + list)
            listAdapter.appendItems(list)
        })

        viewModel.getGankData("Android")

        analyticsService.analyticsMethods()

    }

    override fun registerViews() {
        listAdapter.register { GankItemView(it.context) }
    }
}
