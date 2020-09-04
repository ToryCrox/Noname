package com.tory.dmzj.comic.api

import com.tory.dmzj.dbase.BaseRepository
import com.tory.dmzj.dbase.NetHelper
import com.tory.library.log.LogUtils

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
object ComicRepository : BaseRepository() {
    private val service = NetHelper.retrofit.create(ComicService::class.java)

    suspend fun getRecommendList() = safeApiCall {
        service.getRecommendList().body()
    }

    suspend fun getRecommendUpdate(cateId: Int) = safeApiCall {
        service.getRecommendUpdate(cateId).body()
    }

    /**
     * 获取漫画详情
     */
    suspend fun getComicDetail(id: Int) = safeApiCall {
        service.getComicDetail(id).body()
    }

    /**
     * 获取公告
     */
    suspend fun getTopComment(id: Int) = safeApiCall {
        service.getTopComment(id).body()
    }

    suspend fun getHotComments(id: Int) = safeApiCall {
        service.getHotComments(id).body()
    }

    /**
     * 搜索
     */
    suspend fun searchComic(key: String, pageIndex: Int) =
        safeApiCall { service.searchComic(key, pageIndex).body() }

    /**
     * 排行
     */
    suspend fun getRankList(pageIndex: Int) = safeApiCall {
        service.getRankList(pageIndex = pageIndex).body()
    }
}
