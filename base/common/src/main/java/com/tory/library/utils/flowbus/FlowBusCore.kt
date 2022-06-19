package com.tory.library.utils.flowbus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

/**
 * - Author: tory
 * - Date: 2022/6/13
 * - Email: xutao@shizhuang-inc.com
 * - Description: SharedFlow的事件发送收集器，不实现粘性方法
 *
 * 为强调作用域，需要强制放到ViewModel中使用，在ViewModel中创建
 * ```
 * val bus = FlowBusCore(this)
 * ```
 */
class FlowBusCore(private val vm: ViewModel): IFlowBus {

    private val bus = mutableMapOf<String, FlowWrapper<out FlowEvent>>()

    private fun <T: FlowEvent> ofWrapper(eventName: String, clazz: Class<T>): FlowWrapper<T>{
        val wrapper = bus[eventName]
        if (wrapper == null) {
            val flow = MutableSharedFlow<T>()
            val wp = FlowWrapper(flow, clazz)
            bus[eventName] = wp
            return wp
        } else if (wrapper.eventType != clazz){
            throw IllegalArgumentException(
                "FlowBusCore eventType not match, " +
                        "event:" + eventName +
                        "target is " + clazz + ", has been: " + wrapper.eventType
            )
        }
        return wrapper as FlowWrapper<T>
    }

    private fun <T: FlowEvent> ofWrapper(clazz: Class<T>): FlowWrapper<T> {
        return ofWrapper(clazz.name, clazz)
    }

    /**
     * 获取flow事件
     */
    private fun <T: FlowEvent> of(eventName: String, clazz: Class<T>): Flow<T>{
        return ofWrapper(eventName, clazz).flow
    }

    override fun <T: FlowEvent> of(clazz: Class<T>): Flow<T> {
        return of(clazz.name, clazz)
    }

    override fun <T: FlowEvent> post(event: T) {
        vm.viewModelScope.launch {
            ofWrapper(event.javaClass).sharedFlow.emit(event)
        }
    }
}