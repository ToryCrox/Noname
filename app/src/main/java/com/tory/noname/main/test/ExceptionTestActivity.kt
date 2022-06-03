package com.tory.noname.main.test

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.tory.library.base.BaseActivity
import com.tory.noname.R
import kotlinx.android.synthetic.main.activity_exception_test.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.coroutines.cancellation.CancellationException
import kotlin.random.Random

/**
 * - Author: tory
 * - Date: 2022/5/30
 * - Email: xutao@shizhuang-inc.com
 * - Description:
 */
class ExceptionTestActivity: BaseActivity() {

    override fun getLayoutId(): Int {
        return R.layout.activity_exception_test
    }

    override fun initView(savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            try {
                Log.i("ExceptionTest", "fetchData start")
                val result = fetchData()
                Log.i("ExceptionTest", result)
                textView.text = result
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("ExceptionTest", "error isDestroyed:$isDestroyed", e)
            }
            Log.i("ExceptionTest", "do something after suspend")
        }


    }

    suspend fun fetchData(): String {
        delay(3000L)
        return "fetchData success"
    }
}