package com.tory.library.component.base

import android.content.Context
import android.util.AttributeSet
import android.view.View

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/4/7
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/4/7 xutao 1.0
 * Why & What is modified:
 */
open class AbsModuleMView<T> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), IModuleView<T> {

    protected var data: T? = null

    override fun update(model: T) {
        if (data != model) {
            data = model
            onChanged(model)
        }
    }

    open fun onChanged(model: T) {}
}
