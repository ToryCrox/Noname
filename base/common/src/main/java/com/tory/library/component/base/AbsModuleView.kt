package com.tory.library.component.base

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.core.os.TraceCompat
import androidx.core.view.forEach
import com.tory.library.extension.inflate

/**
 * Author: xutao
 * Version V1.0
 * Date: 2019-12-19
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2019-12-19 xutao 1.0
 * Why & What is modified:
 */
abstract class AbsModuleView<T> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), IModuleView<T> {

    protected var data: T? = null
    private var subModuleViewhHelper: ISubModuleViewHelper<T>? = null

    protected val simpleViewName: String
        get() = javaClass.simpleName

    init {
        val layoutId = this.getLayoutId()
        if (layoutId != 0) {
            TraceCompat.beginSection("inflate#${javaClass.simpleName}")
            inflateLayout(layoutId)
            TraceCompat.endSection()
        }
    }

    protected fun inflateLayout(layoutId: Int) {
        inflate(layoutId, true)
    }

    @LayoutRes
    open fun getLayoutId(): Int = 0

    override fun update(model: T) {
        val isChanged = data != model
        data = model
        if (isChanged) {
            onChanged(model)
        }
        subModuleViewhHelper?.update(model)
    }

    /**
     * 请搞清楚update和onChanged的区别！！如果发现onChange没有回调，请使用update
     * * update: 相当于onBind，adapter notify的时候就会调用
     * * onChange: 数据改变时调用，请注意，数据变化是指新的**新对象，且内容不同**
     *
     * - 例如:
     * ```
     *   adapter.getItems().forEach {
     *      it.isSelected = true
     *   }
     *   adapter.notifyDataSetChange
     * ```
     * 此时使用onChange方法是错误的，因为修改的是同一个对象, 所以新旧数据对比是相同的，所以onChanged是不会回调的
     */
    @Deprecated("标记过时并不是说不要使用改方法，这是一个警告，" +
            "请搞清楚update和onChanged的区别！onChange是对update的优化，具体见上方注释")
    open fun onChanged(model: T) {}

    fun addSubModuleViews(moduleView: ISubModuleView<T>) {
        if (subModuleViewhHelper == null) {
            subModuleViewhHelper = ISubModuleViewHelper()
        }
        subModuleViewhHelper?.addSubModuleViews(moduleView)
    }

    fun removeSubModuleViews(moduleView: ISubModuleView<T>) {
        subModuleViewhHelper?.removeSubModuleViews(moduleView)
    }

    fun setEnableCompat(view: View, enable: Boolean) {
        if (view is ViewGroup) {
            view.forEach {
                setEnableCompat(it, enable)
            }
        }
        view.isEnabled = enable
    }
}
