package com.shizhuang.duapp.common.extension

import com.tory.library.MApp
import com.tory.library.utils.AppUtils
import com.tory.library.utils.DensityUtils

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020-02-10
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020-02-10 xutao 1.0
 * Why & What is modified:
 */

inline fun Int.dp(): Int {
    return DensityUtils.dp2px(AppUtils.getContext(), this.toFloat())
}

inline fun Float.dp(): Int {
    return DensityUtils.dp2px(AppUtils.getContext(), this)
}
