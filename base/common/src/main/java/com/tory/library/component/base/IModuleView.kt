package com.shizhuang.duapp.common.component.module

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
