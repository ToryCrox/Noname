package com.tory.demo.jetpack.di

import com.tory.demo.jetpack.api.BASE_URL
import com.tory.demo.jetpack.api.GankService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/8/29
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/8/29 xutao 1.0
 * Why & What is modified:
 */
@Module
@InstallIn(ApplicationComponent::class)
class ApiModule {

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }

    @GankRetrofit
    @Singleton
    @Provides
    fun provideGankRetrofit(okHttp: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(okHttp)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    fun provideGankService(@GankRetrofit retrofit: Retrofit): GankService{
        return retrofit.create(GankService::class.java)
    }

//    @Singleton
//    @Provides
//    fun provideGankRepo(retrofit: Retrofit): GankRepo {
//        return GankRepo(retrofit.create(GankService::class.java))
//    }
}
