package com.tory.noname.paging.source

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * - Author: tory
 * - Date: 2022/7/8
 * - Email: xutao@shizhuang-inc.com
 * - Description:
 */
object BangumiNetHelper {

    private const val DEFAULT_CONNECT_TIMEOUT = 5L
    private const val DEFAULT_READ_TIMEOUT = 10L
    private const val DEFAULT_WRITE_TIMEOUT = 20L

    private const val BASE_URL = "https://bgmlist.com/"

    val okHttpClient: OkHttpClient by lazy {
        val logInterceptor = HttpLoggingInterceptor()
        logInterceptor.level = HttpLoggingInterceptor.Level.BODY
        OkHttpClient.Builder()
            .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(DEFAULT_WRITE_TIMEOUT, TimeUnit.SECONDS)

            .addNetworkInterceptor(logInterceptor)

            .build()
    }
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}