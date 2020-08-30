package com.shizhuang.duapp.common.component.module

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.shizhuang.duapp.common.extension.dp
import com.tory.library.R

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
