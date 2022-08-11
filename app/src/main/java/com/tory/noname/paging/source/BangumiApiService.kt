package com.tory.noname.paging.source

import com.tory.noname.gank.bean.GankApiResult
import com.tory.noname.paging.model.BangumiItemModel
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * @author tory
 * @date 2018/3/21
 */
interface BangumiApiService {

    @GET("api/v1/bangumi/onair")
    fun getOnAir(): List<BangumiItemModel>
}