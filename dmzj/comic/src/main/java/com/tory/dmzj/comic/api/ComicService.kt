package com.tory.dmzj.comic.api

import com.tory.dmzj.comic.model.ComicDetailModel
import com.tory.dmzj.comic.model.ComicRankItemModel
import com.tory.dmzj.comic.model.ComicSearchItemModel
import com.tory.dmzj.comic.model.CommentItemModel
import com.tory.dmzj.comic.model.RecommendModel
import com.tory.dmzj.dbase.model.BaseResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.Date

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

    /**
     * 获取公告
     */
    @Headers("dmzj: true")
    @GET("comment2/getTopComment/4/4/{id}.json")
    suspend fun getTopComment(@Path("id") id: Int)
        : Response<CommentItemModel>

    /**
     * 热门评论
     * 从1开始
     */
    @Headers("dmzj: true")
    @GET("comment2/4/1/{id}/3/{pageIndex}.json")
    suspend fun getHotComments(@Path("id") id: Int)
        : Response<List<CommentItemModel>>

    @Headers("dmzj: true")
    @GET("search/show/0/{key}/{pageIndex}.json")
    suspend fun searchComic(@Path("key") key: String, @Path("pageIndex") pageIndex: Int)
        : Response<List<ComicSearchItemModel>>

    /**
     * pageIndex: 从0开始
     * type: 0：人气，1吐槽，2: 订阅
     * dateType: 0: 日排行，1: 周排行，1: 月，2: 总
     * cateType：类别: 0全部，5欢乐向
     */
    @Headers("dmzj: true")
    @GET("rank/{cateType}/{dateType}/{type}/{pageIndex}.json")
    suspend fun getRankList(
        @Path("cateType") cateType: Int = 0,
        @Path("dateType") dateType: Int = 0,
        @Path("type") type: Int = 0,
        @Path("pageIndex") pageIndex: Int = 0
    ): Response<List<ComicRankItemModel>>
}
