package com.tory.dmzj.comic.api

import com.tory.dmzj.comic.model.ComicDetailModel
import com.tory.dmzj.comic.model.ComicSearchItemModel
import com.tory.dmzj.comic.model.RecommendModel
import com.tory.dmzj.dbase.model.BaseResponse
import retrofit2.Response
import retrofit2.http.GET
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
    suspend fun getRecommendList(): Response<List<RecommendModel>>

    @Headers("dmzj: true", "user: true")
    @GET("recommend/batchUpdate")
    suspend fun getRecommendUpdate(@Query("category_id") cateId: Int)
        : Response<BaseResponse<RecommendModel>>

    @Headers("dmzj: true")
    @GET("comic/comic_{id}.json")
    suspend fun getComicDetail(@Path("id") id: Int)
        : Response<ComicDetailModel>

    @Headers("dmzj: true")
    @GET("search/show/0/{key}/{pageIndex}.json")
    suspend fun searchComic(@Path("key") key: String,@Path("pageIndex") pageIndex: Int)
        : Response<List<ComicSearchItemModel>>
}
