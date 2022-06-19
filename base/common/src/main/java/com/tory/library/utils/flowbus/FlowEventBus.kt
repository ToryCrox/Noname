package com.tory.library.utils.flowbus

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow

/**
 * - Author: tory
 * - Date: 2022/6/15
 * - Description: 全局的事件消息
 */
object FlowEventBus: ViewModel(), IFlowBus {

    private val bus = FlowBusCore(this)

    override fun <T : FlowEvent> of(clazz: Class<T>): Flow<FlowEvent> = bus.of(clazz)

    override fun <T : FlowEvent> post(event: T) = bus.post(event)
}