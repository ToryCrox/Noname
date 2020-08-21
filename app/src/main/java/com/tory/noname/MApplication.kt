package com.tory.noname

import android.content.Context
import com.facebook.stetho.Stetho
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import com.tory.library.applife.AppLifeApplication
import com.tory.library.utils.SettingHelper
import com.tory.library.utils.Utilities
import com.tory.noname.main.utils.L
import org.koin.core.context.startKoin

/**
 * @Author: Tory
 * Create: 2016/9/15
 * Update: 2016/9/15
 */
class MApplication : AppLifeApplication() {
    private var refWatcher: RefWatcher? = null

    override fun attachBaseContext(base: Context?) {
        instance = this
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        //chrome://inspect
        Stetho.initializeWithDefaults(this)
        //if(BuildConfig.DEBUG){
        //    ReflectDebugUtil.reflectInitStetho(this);
        //}
        Utilities.setNightMode(this,
            SettingHelper.getInstance(this).isNightMode)
        refWatcher = setupLeakCanary()
        L.d("StethoReflection sourceDir=" + applicationInfo.sourceDir)
        startKoin {

        }
    }

    private fun setupLeakCanary(): RefWatcher {
        return if (LeakCanary.isInAnalyzerProcess(this)) {
            RefWatcher.DISABLED
        } else LeakCanary.install(this)
    }

    companion object {
        @JvmStatic
        var instance: MApplication? = null
            private set
    }
}
