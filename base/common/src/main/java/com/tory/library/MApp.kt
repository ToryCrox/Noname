package com.tory.library

import android.app.Application
import android.content.Context
import com.tory.library.applife.AppLifeApplication
import com.tory.library.utils.AppUtils

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

    companion object {
        lateinit var instance: Application
    }
}
