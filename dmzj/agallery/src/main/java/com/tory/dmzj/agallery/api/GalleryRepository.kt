package com.tory.dmzj.agallery.api

import com.tory.dmzj.agallery.ui.model.GalleryPageModel
import com.tory.dmzj.dbase.BaseRepository
import com.tory.dmzj.dbase.NetHelper
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
object GalleryRepository : BaseRepository() {
    val service: GalleryService = NetHelper.konachanRetrofit.create(GalleryService::class.java)

    suspend fun getPost(
        limit: Int = 30,
        page: Int = 1,
        tags: String? = null
    ) = safeApiCall {
        val list = service.getPost(limit, page, tags)
        GalleryPageModel(list)
    }
}
