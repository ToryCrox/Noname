package com.tory.dmzj.home.module.commic_detail

import androidx.lifecycle.MutableLiveData
import com.shizhuang.duapp.common.component.module.ModuleDividerModel
import com.shizhuang.duapp.common.component.module.ModuleEmptyContentModel
import com.shizhuang.duapp.common.component.module.ModuleEmptyModel
import com.shizhuang.duapp.common.component.module.joinTo
import com.tory.dmzj.home.BaseViewModel
import com.tory.dmzj.home.api.ComicRepo
import com.tory.dmzj.home.api.CommentRepo
import com.tory.dmzj.home.model.*
import com.tory.library.log.LogUtils
import java.lang.Exception

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
    private var detailModel: ComicDetailModel? = null
    private var commentCollectModel: CommentCollectModel? = null

    fun fetchData(id: Int) {
        launchOnUI {
            val model = ComicRepo.getComicDetail(id)
            if (model == null) {
                LogUtils.w("getComicDetail error")
                return@launchOnUI
            }
            detailModel = model
            handleResult()

            commentCollectModel = CommentRepo.getLatestComment(id)
            handleResult()
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

        commentCollectModel?.let {
            result.addAll(flatComment(it))
        }



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
            comments.add(ModuleEmptyModel())
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
