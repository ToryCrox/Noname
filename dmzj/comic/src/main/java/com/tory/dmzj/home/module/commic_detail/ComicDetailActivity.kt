package com.tory.dmzj.home.module.commic_detail

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.tory.dmzj.home.RouterTable
import com.tory.dmzj.home.views.ComicDetailHeaderView
import com.tory.library.base.VLayoutListActivity

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/8/31
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/8/31 xutao 1.0
 * Why & What is modified:
 */
@Route(path = RouterTable.COMIC_DETAIL_PAGE)
class ComicDetailActivity: VLayoutListActivity() {

    @JvmField
    @Autowired
    var id: Int = 0

    @JvmField
    @Autowired
    var detailTitle: String? = null

    val viewModel: ComicDetailViewModel by viewModels()

    override fun registerViews() {
        listAdapter.register { ComicDetailHeaderView(it.context) }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        ARouter.getInstance().inject(this)
        title = detailTitle

        viewModel.result.observe(this, Observer {
            listAdapter.setItems(it.orEmpty())
        })

        viewModel.fetchData(id)
    }
}
