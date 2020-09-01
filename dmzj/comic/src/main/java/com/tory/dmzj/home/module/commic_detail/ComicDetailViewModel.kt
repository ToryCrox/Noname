package com.tory.dmzj.home.module.commic_detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shizhuang.duapp.common.component.module.ModuleDividerModel
import com.tory.dmzj.home.api.ComicRepo
import com.tory.dmzj.home.model.ComicChapterItem
import com.tory.dmzj.home.model.ComicChapterItemModel
import com.tory.dmzj.home.model.ComicChapterTitleModel
import com.tory.dmzj.home.model.ComicDetailModel
import kotlinx.coroutines.launch

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
class ComicDetailViewModel: ViewModel() {

    val result: MutableLiveData<List<Any>> = MutableLiveData()

    fun fetchData(id: Int) {
        viewModelScope.launch {
            val model = ComicRepo.getComicDetail(id)
            result.value = handleData(model)
        }
    }

    private fun handleData(model: ComicDetailModel): List<Any>{
        val result = mutableListOf<Any>()
        result.add(model.toHeader())
        result.add(ModuleDividerModel())
        result.add(model.toDesc())
        result.add(ModuleDividerModel())
        result.addAll(model.chapters?.flatMap { chapter->
            val list = chapter.list.orEmpty().map { ComicChapterItemModel(it) }
            val title = ComicChapterTitleModel(chapter.title.orEmpty())
            val ll = if (list.size <= 12){
                list
            } else {
                list.take(11).plus(ComicChapterItemModel(ComicChapterItem(), true, model.chapters))
            }
            val ls = ArrayList<Any>(ll.size + 1)
            ls.add(title)
            ls.addAll(ll)
            ls
        }.orEmpty())

        return result
    }

}
