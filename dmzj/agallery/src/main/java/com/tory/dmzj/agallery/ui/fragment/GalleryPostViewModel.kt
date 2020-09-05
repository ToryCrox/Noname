package com.tory.dmzj.agallery.ui.fragment

import com.tencent.smtt.utils.l
import com.tory.dmzj.agallery.api.GalleryRepository
import com.tory.dmzj.agallery.ui.model.GalleryImageModel
import com.tory.dmzj.dbase.BaseRepository
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
class GalleryPostViewModel : BaseViewModel() {

    private val responseData: MutableList<List<GalleryImageModel>> = mutableListOf()

    fun fetchData(isRefresh: Boolean) {
        launchOnUI {

            val pageIndex = if (isRefresh) 1 else responseData.size + 1
           val list = GalleryRepository.getPost(page = pageIndex)
            if (!list.isNullOrEmpty()){
                responseData.add(list)
                val allList = responseData.flatten()
                allList.forEachIndexed { index, item ->
                    item.index = index
                    item.allImages = allList
                }
                resultList.value = allList
            }

            loadStatus.value = LoadStatus(isRefresh, !list.isNullOrEmpty())
        }
    }
}
