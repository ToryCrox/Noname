package com.tory.demo.jetpack.analytics

import com.tory.library.log.LogUtils
import com.tory.demo.jetpack.api.GankRepo
import javax.inject.Inject

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
class AnalyticsServiceImpl @Inject constructor(private val repo: GankRepo): AnalyticsService {
    override fun analyticsMethods() {
        LogUtils.d("analyticsMethods....")
    }
}
