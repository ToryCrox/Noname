package com.tory.dmzj.home.api

import com.tory.dmzj.home.NetHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
object CommentRepo {

    private val service = NetHelper.commentRetrofit.create(CommentService::class.java)

    suspend fun getLatestComment(id: Int,  pageIndex: Int = 1, limit: Int = 10) =
        service.getLatestComment(id, pageIndex, limit).body()
}
