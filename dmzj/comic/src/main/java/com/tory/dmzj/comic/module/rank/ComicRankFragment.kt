package com.tory.dmzj.comic.module.rank

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.tory.dmzj.comic.views.ComicRankItemView
import com.tory.library.base.VLayoutListFragment
import com.tory.library.log.LogUtils

/**
 * @author tory
 * @create 2020/9/4
 * @Describe
 */
class ComicRankFragment: VLayoutListFragment() {

    val viewModel: ComicRankViewModel by viewModels()

    override fun registerViews() {
        listAdapter.register { ComicRankItemView(it.context) }
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)
        viewModel.loadStatus.observe(this, Observer {
            it ?: return@Observer
            LogUtils.d("ComicRankFragment", "loadStatus $it")
            setRefreshAndLoadMoreState(it.refresh, it.canLoadMore)
        })
        viewModel.resultList.observe(this, Observer {
            listAdapter.setItems(it.orEmpty())
        })
    }

    override fun enablePreloadMore(): Boolean {
        return true
    }

    override fun doLoadMore() {
        super.doLoadMore()
        viewModel.fetchData(false)
    }

    override fun doRefresh() {
        super.doRefresh()
        viewModel.fetchData(true)
    }


    companion object {

        fun newFragment(): ComicRankFragment {
            return ComicRankFragment()
        }
    }
}
