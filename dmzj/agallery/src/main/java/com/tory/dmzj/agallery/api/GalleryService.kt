package com.tory.dmzj.agallery.api

import com.tory.dmzj.agallery.ui.model.GalleryImageModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

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
interface GalleryService {
    /**
     * page从1开始
     */
    @GET("post.json")
    suspend fun getPost(
        @Query("limit") limit: Int = 10,
        @Query("page") page: Int,
        @Query("tags") tags: String? = null
    ): Response<List<GalleryImageModel>>
}
