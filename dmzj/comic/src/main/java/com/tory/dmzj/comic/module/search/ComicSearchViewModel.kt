package com.tory.dmzj.comic.module.search

import androidx.lifecycle.MutableLiveData
import com.shizhuang.duapp.common.component.module.ModuleDividerModel
import com.shizhuang.duapp.common.component.module.joinTo
import com.tory.dmzj.comic.api.ComicRepo
import com.tory.dmzj.comic.model.ComicSearchItemModel
import com.tory.dmzj.dbase.BaseViewModel
import com.tory.dmzj.dbase.LoadStatus

/**
 * @author tory
 * @create 2020/9/4
 * @Describe
 */
class ComicSearchViewModel: BaseViewModel() {

    val result = MutableLiveData<List<Any>>()

    val resList = mutableListOf<List<ComicSearchItemModel>>()


    fun fetchData(key: String, isRefresh: Boolean = false){
        launchOnUI {
            if (isRefresh) {
                resList.clear()
            }
            val pageIndex = resList.size
            val list = ComicRepo.searchComic(key, pageIndex)
            if (list == null){
                loadStatus.value = LoadStatus(isRefresh, false)
                return@launchOnUI
            }
            resList.add(list)

            result.value = ModuleDividerModel().joinTo(resList.flatten())
            loadStatus.value = LoadStatus(isRefresh, list.isNotEmpty())

        }
    }
}