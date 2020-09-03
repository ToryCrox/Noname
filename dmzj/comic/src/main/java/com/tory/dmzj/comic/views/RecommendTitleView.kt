package com.tory.dmzj.comic.views

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView
import com.shizhuang.duapp.common.component.module.IModuleView
import com.shizhuang.duapp.common.extension.color
import com.shizhuang.duapp.common.extension.dp
import com.tory.dmzj.comic.R
import com.tory.dmzj.comic.model.RecommendTitleModel

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/8/30
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/8/30 xutao 1.0
 * Why & What is modified:
 */
class RecommendTitleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr), IModuleView<RecommendTitleModel> {

    init {
        setPadding(4.dp(), 4.dp(), 16.dp(), 4.dp())
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
        setTextColor(context.color(R.color.color_text_primary))
    }


    override fun update(model: RecommendTitleModel) {
        setText(model.title)
    }
}
