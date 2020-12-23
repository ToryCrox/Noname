package com.tory.library.component.base

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/8/19
 * Description: 埋点接口，自定义实现
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/8/19 xutao 1.0
 * Why & What is modified:
 */
interface IViewTracker<T> {
    fun trackEvent(model: T, view: IModuleView<T>)
}
