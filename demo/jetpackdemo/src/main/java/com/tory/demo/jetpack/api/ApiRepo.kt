package com.tory.demo.jetpack.api

import com.tory.demo.jetpack.model.GankApiResult
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/8/21
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/8/21 xutao 1.0
 * Why & What is modified:
 */
// ApiRepo.kt
// Api.kt
class BaseModel<T>(
    val code: Int,
    val data: T
)

val TAGS = arrayOf("all",
    "Android",
    "休息视频",
    "福利",
    "iOS",
    "拓展资源",
    "前端",
    "瞎推荐")
var BASE_URL = "https://gank.io/api/data/"

interface GankService {
    @GET("{tag}/{pageCount}/{pageIndex}")
    suspend fun getGankApiResult(
        @Path("tag") tag: String?, @Path("pageCount") pageCount: Int,
        @Path("pageIndex") pageIndex: Int
    ): GankApiResult

}

object ApiRepo {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
    private val retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val api: GankService = retrofit.create(GankService::class.java)
}

@Singleton
class GankRepo @Inject constructor(private val service: GankService) {

    suspend fun getGankData(tag: String, pageCount: Int, pageIndex: Int) =
        service.getGankApiResult(tag, pageCount, pageIndex)
}
