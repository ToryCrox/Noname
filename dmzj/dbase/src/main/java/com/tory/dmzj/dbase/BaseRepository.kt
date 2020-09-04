package com.tory.dmzj.dbase

import com.tory.library.log.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/9/4
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/9/4 xutao 1.0
 * Why & What is modified:
 */
open class BaseRepository {
    suspend fun <T : Any> apiCall(call: suspend () -> T): T? {
        return call.invoke()
    }

    suspend fun <T : Any> safeApiCall(lazyMsg: (() -> String)? = null, call: suspend () -> T?): T? {
        return withContext(Dispatchers.IO) {
            try {
                call()
            } catch (e: Exception) {
                LogUtils.e(lazyMsg?.invoke().orEmpty(), e)
                null
            }
        }
    }
}
