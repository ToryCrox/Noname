package com.tory.library.base

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.alibaba.android.vlayout.LayoutHelper
import com.alibaba.android.vlayout.VirtualLayoutManager
import com.shizhuang.duapp.common.component.module.VLayoutModuleAdapter
import com.tory.library.R
import com.tory.library.component.loadmore.LoadMoreHelper
import com.tory.library.component.vlayout.VLayoutDelegateAdapter

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
abstract class VLayoutListFragment : BaseFragment() {
    protected lateinit var recyclerView: RecyclerView
    protected lateinit var refreshLayout: SwipeRefreshLayout
    private var loadMoreHelper: LoadMoreHelper? = null
    protected val listAdapter = createModuleAdapter()

    override fun getLayoutId(): Int = R.layout.include_base_refresh_list

    override fun initView(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerView)
        refreshLayout = view.findViewById(R.id.refreshLayout)
        registerViews()
        val layoutManager = VirtualLayoutManager(view.context)
        val adapter = VLayoutDelegateAdapter(layoutManager)
        adapter.addAdapter(listAdapter)
        recyclerView.itemAnimator = null
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        initLoadMoreHelper()

        refreshLayout.isEnabled = enableRefresh()
        refreshLayout.setOnRefreshListener {
            startRefresh()
        }
    }

    abstract fun registerViews()


    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
        if (autoRefresh()) {
            startRefresh()
        }
    }

    open fun createModuleAdapter() = VLayoutModuleAdapter(calDiff = true)


    fun startRefresh() {
        refreshLayout.isRefreshing = true
        doRefresh()
    }

    open fun autoRefresh(): Boolean = true

    open fun enableRefresh(): Boolean = true

    open fun enablePreloadMore(): Boolean = false

    open fun getPreloadMoreThreshold(): Int = 1

    open fun doRefresh() = Unit

    open fun doLoadMore() = Unit

    open fun setRefreshAndLoadMoreState(isRefresh: Boolean, canLoadMore: Boolean) {
        if (isRefresh) {
            refreshLayout.isRefreshing = false
        }
        if (canLoadMore) {
            loadMoreHelper?.hasMoreDataToLoad("more")
        } else {
            loadMoreHelper?.stopLoadMore()
        }
    }

    private fun initLoadMoreHelper() {
        val preloadMore = enablePreloadMore()
        if (preloadMore) {
            LoadMoreHelper.newInstance({
                doLoadMore()
            }, getPreloadMoreThreshold()).apply {
                initLoadMoreView(recyclerView)
                loadMoreHelper = this
            }
        }
    }
}
