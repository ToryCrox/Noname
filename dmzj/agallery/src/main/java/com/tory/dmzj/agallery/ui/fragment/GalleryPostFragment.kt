package com.tory.dmzj.agallery.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.alibaba.android.vlayout.LayoutHelper
import com.alibaba.android.vlayout.layout.StaggeredGridLayoutHelper
import com.shizhuang.duapp.common.component.module.VLayoutModuleAdapter
import com.tory.dmzj.agallery.ui.views.GalleryImageView
import com.tory.library.base.VLayoutListFragment
import com.tory.library.log.LogUtils

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/9/5
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/9/5 xutao 1.0
 * Why & What is modified:
 */
class GalleryPostFragment: VLayoutListFragment() {

    val viewModel: GalleryPostViewModel by viewModels()

    override fun createModuleAdapter(): VLayoutModuleAdapter {
        return VLayoutModuleAdapter(calDiff = true,
            layoutHelper = StaggeredGridLayoutHelper(2))
    }

    override fun registerViews() {
        listAdapter.register { GalleryImageView(it.context) }
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)
        viewModel.loadStatus.observe(this, Observer {
            it ?: return@Observer
            LogUtils.d("GalleryPostFragment", "loadStatus $it")
            setRefreshAndLoadMoreState(it.refresh, it.canLoadMore)
        })
        viewModel.resultList.observe(this, Observer {
            listAdapter.setItems(it.orEmpty())
        })
    }


    override fun doRefresh() {
        super.doRefresh()
        viewModel.fetchData(true)
    }

    override fun doLoadMore() {
        super.doLoadMore()
        viewModel.fetchData(false)

    }

    override fun enablePreloadMore(): Boolean {
        return true
    }

    companion object {
        const val TAG = "GalleryPostFragment"

        fun newInstance(): GalleryPostFragment {
            return GalleryPostFragment()
        }
    }
}
