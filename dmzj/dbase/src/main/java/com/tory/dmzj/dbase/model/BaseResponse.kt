package com.tory.dmzj.dbase.model

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/8/30
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/8/30 xutao 1.0
 * Why & What is modified:
 */
class BaseResponse<T>(
    val code: Int,
    val msg: String? = null,
    val data: T
)
