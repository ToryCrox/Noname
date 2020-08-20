package com.tory.noname.mm

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.core.os.TraceCompat
import com.tory.library.extension.inflate

abstract class AbsView<T> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), IView<T> {

    protected var data: T? = null

    init {
        val layoutId = this.getLayoutId()
        if (layoutId != 0) {
            TraceCompat.beginSection("inflate:${javaClass.simpleName}")
            inflate(layoutId, true)
            TraceCompat.endSection()
        }
    }

    @LayoutRes
    open fun getLayoutId(): Int = 0

    override fun update(model: T) {
        val isChanged = data != model
        data = model
        if (isChanged) {
            onChanged(model)
        }
    }

    open fun onChanged(model: T) {}
}
