package com.tory.dmzj.comic.module.commic_detail

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.tory.library.component.base.GroupMargin
import com.tory.library.extension.dp
import com.tory.dmzj.comic.views.*
import com.tory.dmzj.dbase.RouterTable
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

    private val viewModel: ComicDetailViewModel by viewModels()

    override fun registerViews() {
        listAdapter.register { ComicDetailHeaderView(it.context) }
        listAdapter.register { ComicDetailDescView(it.context) }
        listAdapter.register { ComicChapterTitleView(it.context) }
        listAdapter.register(gridSize = 4, groupType = "ComicChapter",
                groupMargin = GroupMargin(4.dp())) { ComicChapterItemView(it.context) }

        listAdapter.register { CommentMainView(it.context) }
        listAdapter.register { CommentImageView(it.context) }
        listAdapter.register { CommentSubView(it.context) }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        ARouter.getInstance().inject(this)
        title = detailTitle

        viewModel.detailTitle.observe(this, Observer {
            title = it
        })
        viewModel.resultList.observe(this, Observer {
            listAdapter.setItems(it.orEmpty())

        })
        viewModel.loadStatus.observe(this, Observer {
            it ?: return@Observer
            setRefreshAndLoadMoreState(it.refresh, it.canLoadMore)
        })
    }

    override fun enablePreloadMore(): Boolean = true

    override fun doRefresh() {
        super.doRefresh()
        viewModel.fetchData(id, true)
    }

    override fun doLoadMore() {
        super.doLoadMore()
        viewModel.fetchData(id, false)
    }
}
