package com.tory.library

import android.app.Application
import android.content.Context
import android.text.TextUtils
import com.tory.library.applife.AppLifeApplication
import com.tory.library.log.LogUtils
import com.tory.library.utils.AppUtils
import com.tory.library.utils.diskcache.DiskCacheConfig
import com.tory.library.utils.diskcache.DiskCacheManager

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/8/28
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/8/28 xutao 1.0
 * Why & What is modified:
 */
open class MApp: AppLifeApplication() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        instance = this
        AppUtils.init(this)
    }

    override fun onCreate() {
        super.onCreate()
        initDiskCache(packageName)
    }

    private fun initDiskCache(processName: String?) {
        var suffix = ""
        if (processName != null) {
            val index = processName.indexOf(":")
            suffix = if (index >= 0) "_" + processName.substring(index + 1) else ""
        }
        val isMainProcess = TextUtils.equals(processName, packageName)
        val config = DiskCacheConfig.newBuilder(this)
                .setDirSuffix(suffix)
                .build()
        DiskCacheManager.getInstance().initialize(config, isMainProcess)
    }

    companion object {
        lateinit var instance: Application
    }
}
