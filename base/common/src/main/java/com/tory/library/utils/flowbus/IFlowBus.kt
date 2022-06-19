package com.tory.library.utils.flowbus

import kotlinx.coroutines.flow.Flow

/**
 * - Author: tory
 * - Date: 2022/6/15
 * - Email: xutao@shizhuang-inc.com
 * - Description:
 */
interface IFlowBus {

    /**
     * 获取Flow实例，接收消息使用Flow的一系列方法
     * ```
     * viewModel.bus.of(MFlowEvent::class::java)
     *  .onEach{
     *
     *  }.launchIn(lifecycleScope)
     *
     *```
     */
    fun <T: FlowEvent> of(clazz: Class<T>): Flow<FlowEvent>

    /**
     * 发送消息，发送
     * ```
     * viewModel.bus.post(MFlowEvent())
     * ```
     */
    fun <T: FlowEvent> post(event: T)
}