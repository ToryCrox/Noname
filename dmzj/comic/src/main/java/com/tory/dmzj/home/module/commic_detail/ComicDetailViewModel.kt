package com.tory.dmzj.home.module.commic_detail

import androidx.lifecycle.MutableLiveData
import com.shizhuang.duapp.common.component.module.ModuleDividerModel
import com.shizhuang.duapp.common.component.module.ModuleEmptyModel
import com.tory.dmzj.home.BaseViewModel
import com.tory.dmzj.home.LoadStatus
import com.tory.dmzj.home.api.ComicRepo
import com.tory.dmzj.home.api.CommentRepo
import com.tory.dmzj.home.model.*
import com.tory.library.log.LogUtils

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
class ComicDetailViewModel: BaseViewModel() {

    val resultList: MutableLiveData<List<Any>> = MutableLiveData()
    val detailTitle: MutableLiveData<String> = MutableLiveData()
    val loadStatus: MutableLiveData<LoadStatus> = MutableLiveData()

    val limit = 10
    private var detailModel: ComicDetailModel? = null
    private var commentCollectModels: MutableList<CommentCollectModel> = mutableListOf()

    fun fetchData(id: Int, isRefresh : Boolean) {
        launchOnUI {
            if (isRefresh) {
                val model = ComicRepo.getComicDetail(id)
                if (model == null) {
                    LogUtils.w("getComicDetail error")
                    loadStatus.value = LoadStatus(isRefresh, false)
                    return@launchOnUI
                }
                detailModel = model
                detailTitle.value = model.title
                handleResult()
            }
            if (isRefresh) {
                commentCollectModels.clear()
            }
            val pageIndex = commentCollectModels.size + 1
            val model = CommentRepo.getLatestComment(id, pageIndex = pageIndex, limit = limit)
            if (model != null){
                commentCollectModels.add(model)
                handleResult()
                loadStatus.value = LoadStatus(isRefresh, model.commentIds.orEmpty().size >= limit)
            } else {
                LogUtils.w("can not find comments")
                loadStatus.value = LoadStatus(isRefresh, false)
            }
        }
    }

    private fun handleResult(){
        val model: ComicDetailModel = detailModel ?: return
        val result = mutableListOf<Any>()
        result.add(model.toHeader())
        result.add(ModuleDividerModel())
        result.add(model.toDesc())
        result.add(ModuleDividerModel())
        result.addAll(flatChapters(model))

        result.addAll(commentCollectModels.flatMap { flatComment(it) })

        resultList.value = result
    }

    private fun flatChapters(model: ComicDetailModel): List<Any> {
        return model.chapters?.flatMap { chapter->
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
        }.orEmpty()
    }

    private fun flatComment(model: CommentCollectModel): List<Any> {
        val comments = mutableListOf<Any>()
        val commentItems = model.comments.orEmpty()
        for (ids in model.commentIds.orEmpty()) {
            val idList = ids.split(",")
            if (idList.isEmpty()){
                continue
            }
            val mId = idList[0]
            val mainItem = commentItems[mId] ?:continue
            comments.add(CommentMainModel(mainItem))
            val subIds = idList.subList(1, idList.size)
            val subItems = subIds.mapNotNull { commentItems[it] }
            subItems.forEachIndexed { index, item ->
                comments.add(CommentSubModel(item, isFirst = index == 0,
                        isLast = index == subItems.size -1))
            }
            if (subItems.isNotEmpty()) {
                comments.add(ModuleEmptyModel())
            }
            comments.add(ModuleDividerModel())
        }
        if (comments.isNotEmpty()){
            comments.add(0, ModuleDividerModel())
        }
        return comments

//        val comments = model.comments?.mapNotNull { it.value }
//                ?.map { CommentMainModel(it) }.orEmpty()
//
//        return ModuleDividerModel().joinTo(comments, true, true)
    }

}
