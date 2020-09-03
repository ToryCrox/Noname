package com.tory.dmzj.comic.module.recommend

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.tory.dmzj.comic.views.RecommendBannerView
import com.tory.dmzj.comic.views.RecommendComicView
import com.tory.dmzj.comic.views.RecommendTitleView
import com.tory.dmzj.comic.views.RecommendTopicView
import com.tory.dmzj.dbase.RouterTable
import com.tory.library.base.VLayoutListFragment
import com.tory.library.log.LogUtils

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/8/30
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/8/30 xutao 1.0
 * Why & What is modified:
 */
@Route(path = RouterTable.COMIC_MAIN_PAGE)
class ComicMainFragment : VLayoutListFragment() {
    val viewModel: RecommendViewModel by viewModels()

    override fun registerViews() {
        LogUtils.d("ComicMainFragment registerViews")
        listAdapter.register { RecommendTitleView(it.context) }
        listAdapter.register { RecommendBannerView(it.context) }
        listAdapter.register(gridSize = 3) { RecommendComicView(it.context) }
        listAdapter.register(gridSize = 2) { RecommendTopicView(it.context) }
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)

        LogUtils.d("ComicMainFragment initView")
        viewModel.result.observe(this, Observer {
            listAdapter.setItems(it.orEmpty())
            refreshLayout.isRefreshing = false
        })
        refreshLayout.setOnRefreshListener {
            viewModel.fetchData()
        }
        refreshLayout.isRefreshing = true
        viewModel.fetchData()
    }
}
