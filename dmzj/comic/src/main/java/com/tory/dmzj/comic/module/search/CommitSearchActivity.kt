package com.tory.dmzj.comic.module.search


import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.tory.dmzj.comic.R
import com.tory.dmzj.comic.views.ComicSearchItemView
import com.tory.dmzj.dbase.RouterTable
import com.tory.library.base.VLayoutListActivity
import com.tory.library.log.LogUtils


/**
 * @author tory
 * @create 2020/9/4
 * @Describe
 */
@Route(path = RouterTable.COMIC_SEARCH_PAGE)
class CommitSearchActivity: VLayoutListActivity() {

    val viewModel : ComicSearchViewModel by viewModels()

    var queryKey: String = ""

    override fun registerViews() {
        listAdapter.register { ComicSearchItemView(this) }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        viewModel.loadStatus.observe(this, Observer {
            it ?: return@Observer
            LogUtils.d(TAG, "loadStatus $it")
            setRefreshAndLoadMoreState(it.refresh, it.canLoadMore)
        })
        viewModel.result.observe(this, Observer {
            listAdapter.setItems(it.orEmpty())
        })
    }

    override fun autoRefresh(): Boolean = false

    override fun enableRefresh(): Boolean = false

    override fun enablePreloadMore(): Boolean = true

    override fun doLoadMore() {
        super.doLoadMore()
        LogUtils.d(TAG, "doLoadMore $queryKey")
        viewModel.fetchData(queryKey)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu ?: return false
        menuInflater.inflate(R.menu.menu_comic_search, menu)
        val searchItem: MenuItem = menu.findItem(R.id.menu_search)
        //通过MenuItem得到SearchView
        //通过MenuItem得到SearchView
        val searchView = searchItem.actionView as SearchView
        searchView.setOnSearchClickListener {
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query ?: return false
                queryKey = query
                viewModel.fetchData(query, true)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })

        return super.onCreateOptionsMenu(menu)
    }
}