package com.tory.dmzj.dbase

import com.tory.library.log.LogUtils
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


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
object NetHelper {
    private const val DEFAULT_CONNECT_TIMEOUT = 5L
    private const val DEFAULT_READ_TIMEOUT = 10L
    private const val DEFAULT_WRITE_TIMEOUT = 20L
    private const val BASE_URL = "http://v3api.dmzj.com/"
    private const val BASE_COMMENT_URL = "http://v3comment.dmzj.com"
    private val API_IMAGE_BASE = "http://images.dmzj.com/"
    private val API_IMAGE_BASE_HTTPS = "https://images.dmzj.com/"
    private val API_IMAGE_BASE_AVATAR = "https://avatar.dmzj.com/"
     val COMMENT_IMAGE_BASE_URL = "http://images.dmzj.com/commentImg/"
    private val DMZJ_IMAGES = arrayOf(API_IMAGE_BASE,
            API_IMAGE_BASE_HTTPS, API_IMAGE_BASE_AVATAR)

    private val UID = 100013896

    val okHttpClient: OkHttpClient by lazy {
        val logInterceptor = HttpLoggingInterceptor()
        logInterceptor.level = HttpLoggingInterceptor.Level.BODY
        OkHttpClient.Builder()
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_WRITE_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(DMZJInterceptor())
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

    val commentRetrofit: Retrofit by lazy {
        Retrofit.Builder()
                .baseUrl(BASE_COMMENT_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    private class DMZJInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val headers: Headers = request.headers()
            val builder = request.newBuilder()
            val origUrl = request.url().toString()

            LogUtils.w("DMZJInterceptor intercept origUrl:$origUrl")
            for (dmzjImage in DMZJ_IMAGES) {
                if (!origUrl.isNullOrEmpty() && origUrl.startsWith(dmzjImage)) {
                    builder.header("Referer", API_IMAGE_BASE)
                }
            }
            val isDmzj = headers.get("dmzj")
            if (isDmzj?.toLowerCase()?.trim() == "true") {
                val dmzjBuilder = request.url().newBuilder()
                        .addQueryParameter("terminal_model", "MI 9")
                        .addQueryParameter("channel", "Android")
                        .addQueryParameter("_debug", "0")
                        .addQueryParameter("version", "2.7.031")
                        .addQueryParameter("timestamp", (System.currentTimeMillis() / 1000).toString())
                if (headers.get("user")?.toLowerCase()?.trim() == "true") {
                    dmzjBuilder.addQueryParameter("uid", UID.toString())
                }
                builder.url(dmzjBuilder.build())
                builder.removeHeader("dmzj")
                builder.removeHeader("user")
            }

            return chain.proceed(builder.build())
        }
    }
}
