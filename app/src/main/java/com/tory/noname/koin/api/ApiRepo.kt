package com.tory.noname.koin.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

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

interface Api {
    @GET("")
    suspend fun getCountryData(): BaseModel<List<String>>
}

object ApiRepo {
    private val okHttpClient = OkHttpClient.Builder()
        .build()

    private val retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl("your baseurl")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api : Api = retrofit.create(Api::class.java)
}

class MainRepo(private val api:Api) {
    suspend fun getCountryData() : BaseModel<List<String>> = api.getCountryData()
}
