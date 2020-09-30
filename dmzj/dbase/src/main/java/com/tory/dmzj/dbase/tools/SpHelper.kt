package com.tory.dmzj.dbase.tools

import android.os.Parcelable
import com.tencent.mmkv.MMKV
import com.tory.library.utils.AppUtils

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/9/5
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/9/5 xutao 1.0
 * Why & What is modified:
 */
object SpHelper {
    private val mmapId = "dmzj"

    val mmkv: MMKV by lazy {
        MMKV.initialize(AppUtils.getContext())
        MMKV.mmkvWithID(mmapId)
    }


    fun getString(key: String, default: String = ""): String {
        return mmkv.decodeString(key, default) ?: default
    }

    inline fun <reified T : Parcelable> getParcelable(key: String, default: T?): T? {
        return mmkv.decodeParcelable(key, T::class.java, default )
    }

    fun < T : Parcelable> put(key: String, value: T){
        mmkv.encode(key, value)
    }
}
