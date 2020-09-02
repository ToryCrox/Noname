package com.tory.dmzj.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tory.library.utils.diskcache.DiskCacheManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author tory
 * @create 2020/9/2
 * @Describe
 */
open class BaseViewModel: ViewModel() {

    fun launchOnUI(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch { block() }
    }

    suspend fun <T> launchOnIO(block: suspend CoroutineScope.() -> T) {
        withContext(Dispatchers.IO) {
            block
        }
    }

    open fun getCacheKey(): String? = null

    suspend inline fun <reified T> readCache(): T? {
        return withContext(Dispatchers.IO) {
            val cacheKey = getCacheKey() ?: return@withContext null
            val clazz = T::class.java
            return@withContext DiskCacheManager.getInstance().read(cacheKey, clazz)
        }
    }

    suspend fun writeCache(data: Any) {
        withContext(Dispatchers.IO) {
            val cacheKey = getCacheKey() ?: return@withContext
            DiskCacheManager.getInstance().write(cacheKey, data)
        }
    }
}