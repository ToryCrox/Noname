package com.tory.demo.diskcache

import android.app.Application

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/7/12
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/7/12 xutao 1.0
 * Why & What is modified:
 */
class MApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        DiskCache.getInstance().initialize(DiskCacheConfig
                .newBuilder(this)
                .setIsNio(true)
                .setIsExternal(true)
                .build())
    }
}
