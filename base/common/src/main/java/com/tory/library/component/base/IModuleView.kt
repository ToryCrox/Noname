package com.tory.library.component.base

interface IModuleView<T> {
    /**
     * 数据更新
     *
     * @param model
     */
    fun update(model: T)
}

interface ISelectableView {
    fun isSelectable(): Boolean
    fun setSelected(selected: Boolean)
}
