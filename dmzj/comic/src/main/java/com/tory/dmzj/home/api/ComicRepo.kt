package com.tory.dmzj.home.api

import com.tory.dmzj.home.NetHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/8/30
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/8/30 xutao 1.0
 * Why & What is modified:
 */
object ComicRepo {

    val service = NetHelper.retrofit.create(ComicService::class.java)

    suspend fun getRecommendList() = withContext(Dispatchers.IO) {
        service.getRecommendList()
    }

    suspend fun getRecommendUpdate(cateId: Int) = withContext(Dispatchers.IO) {
        service.getRecommendUpdate(cateId)
    }

    suspend fun getComicDetail(id: Int) = withContext(Dispatchers.IO) {
        service.getComicDetail(id)
    }
}
