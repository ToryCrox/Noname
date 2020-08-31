package com.tory.library.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/8/31
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/8/31 xutao 1.0
 * Why & What is modified:
 */
object DateUtils {
    val dateFormat = SimpleDateFormat("", Locale.getDefault())

    fun format(pattern: String, timeStamp: Long): String {
        dateFormat.applyPattern(pattern)
        return dateFormat.format(Date(timeStamp))
    }
}
