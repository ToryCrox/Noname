package com.tory.module.hilt

import com.tory.library.applife.AppLife
import com.tory.module.hilt.di.networkModule
import com.tory.module.hilt.di.repoModule
import com.tory.module.hilt.di.viewmodelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

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
class KoinAppLife: AppLife() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(level = Level.DEBUG)
            androidContext(mApplication)
            modules(listOf(networkModule, repoModule, viewmodelModule))
        }
    }
}
