package com.tory.dmzj.home.api

import com.tory.dmzj.home.model.BaseResponse
import com.tory.dmzj.home.model.ComicDetailHeaderModel
import com.tory.dmzj.home.model.ComicDetailModel
import com.tory.dmzj.home.model.RecommendModel
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

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
interface ComicService {

    @Headers("dmzj: true")
    @GET("recommend_index_androids.json")
    suspend fun getRecommendList(): List<RecommendModel>

    @Headers("dmzj: true", "user: true")
    @GET("recommend/batchUpdate")
    suspend fun getRecommendUpdate(@Query("category_id") cateId: Int)
        : BaseResponse<RecommendModel>

    @GET("comic/comic_{id}.json")
    suspend fun getComicDetail(@Path("id") id: Int)
        : ComicDetailModel
}
