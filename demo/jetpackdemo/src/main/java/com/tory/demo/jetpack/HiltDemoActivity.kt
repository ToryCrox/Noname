package com.tory.demo.jetpack

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.tory.library.base.BaseListActivity
import com.tory.library.log.LogUtils
import com.tory.demo.jetpack.analytics.AnalyticsService
import com.tory.demo.jetpack.event.HitEvent
import com.tory.demo.jetpack.views.GankItemView
import com.tory.library.utils.livebus.LiveEventBus
import com.tory.library.utils.livebus.PageEventBus
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
class HiltDemoActivity : BaseListActivity() {

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


        PageEventBus.get(this)
            .ofEmpty("testPageEvent")
            .observe(this, Observer {
                LogUtils.d("PageEventBus  testPageEvent receive: $it")
        })
        PageEventBus.get(this).postEmpty("testPageEvent")
        PageEventBus.get(this)
            .ofEmpty("testPageEvent1")
            .observeSticky(this, Observer {
                LogUtils.d("PageEventBus testPageEvent1 receive: $it")
            })

        PageEventBus.get(this)
            .of(HitEvent::class.java)
            .observeSticky(this, Observer {
                LogUtils.d("PageEventBus HiltEvent receive: $it")
            })

        LiveEventBus.get()
            .of(HitEvent::class.java)
            .observe(this, Observer {
                LogUtils.d("LiveEventBus HiltEvent receive: $it $this")
            })
    }

    override fun registerViews() {
        listAdapter.register { GankItemView(it.context) }
    }
}
