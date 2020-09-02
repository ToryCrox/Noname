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

    private val service = NetHelper.retrofit.create(ComicService::class.java)

    suspend fun getRecommendList() =
        service.getRecommendList().body()

    suspend fun getRecommendUpdate(cateId: Int) =
        service.getRecommendUpdate(cateId).body()

    suspend fun getComicDetail(id: Int) =
        service.getComicDetail(id).body()
}
