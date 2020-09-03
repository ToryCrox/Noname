package com.tory.dmzj.comic.views

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import androidx.core.view.updatePadding
import com.shizhuang.duapp.common.component.module.AbsModuleView
import com.shizhuang.duapp.common.extension.color
import com.shizhuang.duapp.common.extension.dp
import com.tory.dmzj.comic.R
import com.tory.dmzj.comic.model.CommentSubModel
import kotlinx.android.synthetic.main.view_comment_sub_item.view.*

/**
 * @author tory
 * @create 2020/9/3
 * @Describe
 */
class CommentSubView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AbsModuleView<CommentSubModel>(context, attrs, defStyleAttr) {

    val colorPrimary = context.color(R.color.colorPrimary)

    override fun getLayoutId(): Int {
        return R.layout.view_comment_sub_item
    }

    override fun onChanged(model: CommentSubModel) {
        super.onChanged(model)
        val item = model.data
        val spannable = SpannableStringBuilder()
        spannable.append(item.nickname, ForegroundColorSpan(colorPrimary), Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        spannable.append(": ")
        spannable.append(item.content)
        itemText.text = spannable
        itemText.updatePadding(
                top = if (model.isFirst) 10.dp() else 4.dp(),
                bottom = if (model.isLast) 10.dp() else 4.dp()
        )
    }
}