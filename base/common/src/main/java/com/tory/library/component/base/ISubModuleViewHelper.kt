package com.tory.library.component.base

class ISubModuleViewHelper<T> {

    private val attachView: MutableList<ISubModuleView<T>> = mutableListOf()

    fun addSubModuleViews(moduleView: ISubModuleView<T>) {
        if (!attachView.contains(moduleView)) {
            attachView.add(moduleView)
        }
    }

    fun removeSubModuleViews(moduleView: ISubModuleView<T>) {
        attachView.remove(moduleView)
    }

    fun update(model: T) {
        for (attachView in attachView) {
            attachView.update(model)
        }
    }
}
