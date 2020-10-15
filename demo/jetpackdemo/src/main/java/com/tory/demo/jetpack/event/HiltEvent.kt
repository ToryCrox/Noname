package com.tory.demo.jetpack.event

import com.tory.library.utils.livebus.LiveBusEvent

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/10/14
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/10/14 xutao 1.0
 * Why & What is modified:
 */
data class HiltEvent(val context: String): LiveBusEvent
