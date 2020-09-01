package com.tory.library.base

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.shizhuang.duapp.common.component.module.NormalModuleAdapter
import com.tory.library.R

/**
 * @Author: Tory
 * Create: 2016/9/25
 * Update: 2016/9/25
 */
abstract class BaseListFragment : BaseFragment() {

    protected lateinit var recyclerView: RecyclerView
    protected lateinit var refreshLayout: SwipeRefreshLayout
    protected val listAdapter = NormalModuleAdapter()

    override fun getLayoutId(): Int {
        return R.layout.include_base_refresh_list
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerView)
        refreshLayout = view.findViewById(R.id.refreshLayout)
        registerViews()
        recyclerView.adapter = listAdapter
        recyclerView.layoutManager = listAdapter.getGridLayoutManager(view.context)
    }

    abstract fun registerViews()
}
