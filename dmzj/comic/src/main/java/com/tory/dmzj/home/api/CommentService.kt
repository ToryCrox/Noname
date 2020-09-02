package com.tory.dmzj.home.api

import com.tory.dmzj.home.model.CommentCollectModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * @author tory
 * @create 2020/9/2
 * @Describe
 */
interface CommentService {

    @Headers("dmzj: true")
    @GET("v1/4/latest/{id}")
    suspend fun getLatestComment(@Path("id") id: Int,
                                 @Query("page_index") pageIndex: Int,
                                 @Query("limit") limit: Int): Response<CommentCollectModel>
}