package com.tory.dmzj.comic.module.commic_detail

import androidx.lifecycle.MutableLiveData
import com.shizhuang.duapp.common.component.module.ModuleDividerModel
import com.shizhuang.duapp.common.component.module.ModuleEmptyModel
import com.tory.dmzj.comic.api.ComicRepository
import com.tory.dmzj.comic.api.CommentRepository
import com.tory.dmzj.comic.model.*
import com.tory.dmzj.dbase.BaseViewModel
import com.tory.dmzj.dbase.LoadStatus
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

    val limit = 10
    private var detailModel: ComicDetailModel? = null
    //公告
    private var topCommentItemModel: CommentItemModel? = null
    //最近评论
    private var commentCollectModels: MutableList<CommentCollectModel> = mutableListOf()

    fun fetchData(id: Int, isRefresh : Boolean) {
        launchOnUI {
            if (isRefresh) {
                val model = ComicRepository.getComicDetail(id)
                if (model == null) {
                    LogUtils.w("getComicDetail error")
                    loadStatus.value = LoadStatus(isRefresh, false)
                    return@launchOnUI
                }
                detailModel = model
                detailTitle.value = model.title
                handleResult()

                 topCommentItemModel = ComicRepository.getTopComment(id)
                topCommentItemModel?.let {
                    handleResult()
                }
            }
            if (isRefresh) {
                commentCollectModels.clear()
            }
            val pageIndex = commentCollectModels.size + 1
            val model = CommentRepository.getLatestComment(id, pageIndex = pageIndex, limit = limit)
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
        topCommentItemModel?.let {
            result.add(ModuleDividerModel())
            result.add(CommentMainModel(it))
        }
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
            if (!mainItem.uploadImages.isNullOrEmpty()) {
                comments.add(CommentImageModel(mainItem.uploadImages, mainItem.objId))
            }

            val subIds = idList.subList(1, idList.size)
            val subItems = subIds.mapNotNull { commentItems[it] }
            if (subItems.size > 4) {
                val moreItem = CommentSubModel(mainItem, isFirst = false,
                    isLast =  false, allSubItems = subItems)
                comments.addAll(subItems.take(3).mapIndexed { index, item ->
                    CommentSubModel(item, isFirst = index == 0,
                        isLast = false)
                })
                comments.add(moreItem)
                comments.add(CommentSubModel(subItems.last()))

            } else {
                comments.addAll(subItems.mapIndexed { index, item ->
                    CommentSubModel(item, isFirst = index == 0,
                        isLast = index == subItems.size - 1)
                })
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
    }

}
