package com.tory.dmzj.comic.api

import com.tory.dmzj.dbase.NetHelper

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

    suspend fun searchComic(key: String, pageIndex: Int) =
            service.searchComic(key, pageIndex).body()
}
