package com.tory.demo.webview

import android.app.Application
import com.tencent.smtt.sdk.QbSdk
import com.tory.library.log.LogUtils



/**
 * Author: xutao
 * Version V1.0
 * Date: 2020-01-12
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020-01-12 xutao 1.0
 * Why & What is modified:
 */
class MApplication: Application() {

    companion object{
        const val TAG = "MApplication"
    }


    override fun onCreate() {
        super.onCreate()
        val cb = object : QbSdk.PreInitCallback {

            override fun onViewInitFinished(arg0: Boolean) {
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                LogUtils.d(TAG, " onViewInitFinished is $arg0")
            }

            override fun onCoreInitFinished() {
                LogUtils.d(TAG, " onCoreInitFinished")
            }
        }
        //x5内核初始化接口
        QbSdk.initX5Environment(this, cb)
    }
}