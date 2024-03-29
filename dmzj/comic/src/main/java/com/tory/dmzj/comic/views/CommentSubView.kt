package com.tory.dmzj.comic.views

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.Gravity
import androidx.core.view.updatePadding
import androidx.fragment.app.FragmentActivity
import com.tory.library.component.base.AbsModuleView
import com.tory.dmzj.comic.R
import com.tory.dmzj.comic.dialog.CommentAllDialog
import com.tory.dmzj.comic.model.CommentSubModel
import com.tory.library.extension.color
import com.tory.library.extension.dp
import kotlinx.android.synthetic.main.view_comment_sub_item.view.*

/**
 * @author tory
 * @create 2020/9/3
 * @Describe
 */
class CommentSubView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AbsModuleView<CommentSubModel>(context, attrs, defStyleAttr) {

    private val colorPrimary = context.color(R.color.colorPrimary)

    override fun getLayoutId(): Int {
        return R.layout.view_comment_sub_item
    }

    override fun onChanged(model: CommentSubModel) {
        super.onChanged(model)
        if (model.allSubItems != null) {
            itemText.gravity = Gravity.CENTER
            val spannable = SpannableStringBuilder()
            spannable.append("展开所有", ForegroundColorSpan(colorPrimary), Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            itemText.text = spannable
            setOnClickListener {
                CommentAllDialog.newInstance(listOf(model.data) + model.allSubItems)
                    .show((context as FragmentActivity).supportFragmentManager, "CommentAllDialog")
            }

        } else {
            itemText.gravity = Gravity.START

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
            setOnClickListener(null)
        }
    }
}
