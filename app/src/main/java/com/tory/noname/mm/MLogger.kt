package com.tory.noname.mm

import android.util.Log

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/7/31
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/7/31 xutao 1.0
 * Why & What is modified:
 */
object MLogger {
    const val TAG = "MLogger"

    fun d(msg: String){
        Log.d(TAG, msg)
    }

    fun e(msg: String, e: Throwable ?= null){
        Log.d(TAG, msg, e)
    }
}
