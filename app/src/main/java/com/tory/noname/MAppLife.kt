package com.tory.noname

import com.facebook.stetho.Stetho
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import com.tory.library.applife.AppLife
import com.tory.library.utils.SettingHelper
import com.tory.library.utils.Utilities
import com.tory.noname.main.utils.L
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.context.startKoin
import retrofit2.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
class MAppLife: AppLife() {
    private var refWatcher: RefWatcher? = null


    override fun onCreate() {
        super.onCreate()
        //chrome://inspect
        Stetho.initializeWithDefaults(mApplication)
        //if(BuildConfig.DEBUG){
        //    ReflectDebugUtil.reflectInitStetho(this);
        //}
        Utilities.setNightMode(mApplication,
            SettingHelper.getInstance(mApplication).isNightMode)
        refWatcher = setupLeakCanary()
    }

    private fun setupLeakCanary(): RefWatcher {
        return if (LeakCanary.isInAnalyzerProcess(mApplication)) {
            RefWatcher.DISABLED
        } else LeakCanary.install(mApplication)
    }
}
