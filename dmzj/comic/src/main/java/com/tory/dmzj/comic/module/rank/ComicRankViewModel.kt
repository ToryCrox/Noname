package com.tory.dmzj.comic.module.rank

import com.shizhuang.duapp.common.component.module.ModuleDividerModel
import com.shizhuang.duapp.common.component.module.joinTo
import com.tory.dmzj.comic.api.ComicRepository
import com.tory.dmzj.comic.model.ComicRankItemModel
import com.tory.dmzj.dbase.BaseViewModel
import com.tory.dmzj.dbase.LoadStatus

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
class ComicRankViewModel: BaseViewModel() {

    private val rankListData: MutableList<List<ComicRankItemModel>> = mutableListOf()


   fun fetchData(isRefresh: Boolean) {
       launchOnUI {
           val pageIndex = if (isRefresh) 0 else rankListData.size

           val rankData = ComicRepository.getRankList(pageIndex = pageIndex)
           if (rankData.isNullOrEmpty()) {
               loadStatus.value = LoadStatus(isRefresh, false)
               return@launchOnUI
           }
           if (isRefresh){
               rankListData.clear()
           }
           rankListData.add(rankData)
           var index = 1
           val rankModels = rankListData.flatMap { it.map {
               it.rankIndex = index ++
               it
           } }
           resultList.value = ModuleDividerModel().joinTo(rankModels)
           loadStatus.value = LoadStatus(isRefresh, rankData.isNotEmpty())
       }

   }
}
