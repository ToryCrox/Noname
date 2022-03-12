package com.tory.dmzj.comic.views

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView
import com.tory.library.component.base.AbsModuleView
import com.tory.dmzj.comic.R
import com.tory.dmzj.comic.model.ComicDetailDescModel
import com.tory.library.extension.color
import com.tory.library.extension.dp

/**
 * @author tory
 * @create 2020/9/1
 * @Describe
 */
class ComicDetailDescView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AbsModuleView<ComicDetailDescModel>(context, attrs, defStyleAttr) {

    private val textView = AppCompatTextView(context)
    init {
        textView.setTextColor(context.color(R.color.color_text_tertiary))
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
        setPadding(10.dp(), 10.dp(), 10.dp(), 10.dp())
        addView(textView)
    }

    override fun onChanged(model: ComicDetailDescModel) {
        super.onChanged(model)
        textView.text = model.text
    }
}