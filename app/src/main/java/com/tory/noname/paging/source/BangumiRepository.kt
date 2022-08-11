package com.tory.noname.paging.source

import com.tory.library.net.NetHelper
import com.tory.noname.paging.model.BangumiItemModel
import kotlinx.coroutines.flow.flow
import java.util.concurrent.Flow

/**
 * - Author: tory
 * - Date: 2022/7/8
 * - Email: xutao@shizhuang-inc.com
 * - Description:
 */
object BangumiRepository {

    private val service: BangumiApiService by lazy {
        BangumiNetHelper.retrofit.create(BangumiApiService::class.java)
    }

    fun <T> apiCall(call: suspend () -> T) = flow {
        val result = call()
        emit(result)
    }


    fun fetchOnAir()
        = apiCall { service.getOnAir() }
}