package com.tory.library.base

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.tory.library.component.base.NormalModuleAdapter
import com.tory.library.R

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
abstract class BaseListActivity: BaseActivity() {

    protected lateinit var recyclerView: RecyclerView
    protected lateinit var refreshLayout: SwipeRefreshLayout
    protected val listAdapter = NormalModuleAdapter()

    override fun getLayoutId(): Int = R.layout.common_activity_list

    override fun initView(savedInstanceState: Bundle?) {
        recyclerView = findViewById(R.id.recyclerView)
        refreshLayout = findViewById(R.id.refreshLayout)
        registerViews()
        recyclerView.itemAnimator = null
        recyclerView.adapter = listAdapter
        recyclerView.layoutManager = listAdapter.getGridLayoutManager(this)
    }



    abstract fun registerViews()
}
