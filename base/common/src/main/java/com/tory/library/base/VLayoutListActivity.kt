package com.tory.library.base

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.alibaba.android.vlayout.VirtualLayoutManager
import com.shizhuang.duapp.common.component.module.NormalModuleAdapter
import com.shizhuang.duapp.common.component.module.VLayoutModuleAdapter
import com.tory.library.R
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
abstract class VLayoutListActivity: BaseActivity() {
    protected lateinit var recyclerView: RecyclerView
    protected lateinit var refreshLayout: SwipeRefreshLayout
    protected val listAdapter = VLayoutModuleAdapter()

    override fun getLayoutId(): Int = R.layout.common_activity_list

    override fun initView(savedInstanceState: Bundle?) {
        recyclerView = findViewById(R.id.recyclerView)
        refreshLayout = findViewById(R.id.refreshLayout)
        registerViews()

        val layoutManager = VirtualLayoutManager(this)
        val adapter = VLayoutDelegateAdapter(layoutManager)
        adapter.addAdapter(listAdapter)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
    }

    abstract fun registerViews()
}
