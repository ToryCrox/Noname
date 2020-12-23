package com.tory.library.component.base

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import com.tory.library.R
import com.tory.library.extension.dp

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/4/27
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/4/27 xutao 1.0
 * Why & What is modified:
 */
class MallNoMoreTipView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr), IModuleView<ModuleNoMoreTipModel> {

    init {
        val padding = 20.dp()
        setPadding(padding, padding, padding, padding)
        gravity = Gravity.CENTER
        text = "没有更多了"
        //setTextColor(ContextCompat.getColor(context, R.color.color_text_tertiary))
    }

    override fun update(model: ModuleNoMoreTipModel) {
    }
}
