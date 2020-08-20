package com.tory.noname.mm

interface IView<T> {
    /**
     * 数据更新
     *
     * @param model
     */
    fun update(model: T)
}
