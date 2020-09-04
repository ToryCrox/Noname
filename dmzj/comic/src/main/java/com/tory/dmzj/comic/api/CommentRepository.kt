package com.tory.dmzj.comic.api

import com.tory.dmzj.dbase.BaseRepository
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
object CommentRepository : BaseRepository(){

    private val service = NetHelper.commentRetrofit.create(CommentService::class.java)

    suspend fun getLatestComment(id: Int,  pageIndex: Int = 1, limit: Int = 10) = safeApiCall {
        service.getLatestComment(id, pageIndex, limit).body()
    }
}
