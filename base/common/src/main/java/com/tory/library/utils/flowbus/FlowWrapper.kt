package com.tory.library.utils.flowbus

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * - Author: tory
 * - Date: 2022/6/13
 * - Description:
 */
internal class FlowWrapper<T : FlowEvent>(
    val sharedFlow: MutableSharedFlow<T>,
    val eventType: Class<T>
) {
    val flow = sharedFlow.asSharedFlow()
}