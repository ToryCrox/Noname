package com.tory.library.widget.span

import android.graphics.Paint
import android.text.Spannable
import android.text.SpannableString
import android.text.style.LineHeightSpan
import androidx.annotation.Px

/**
 * @author tory
 * 设置段落高度
 */
class ParagraphSpacingSpan(@param:Px private val spacing: Int) : LineHeightSpan {
    override fun chooseHeight(
        text: CharSequence, start: Int, end: Int, spanstartv: Int,
        v: Int, fm: Paint.FontMetricsInt
    ) {
        if (isParagraphEnd(text, end)) {
            // let's just add what we want
            fm.descent += spacing
            fm.bottom += spacing
        }
    }

    private fun isParagraphEnd(text: CharSequence, end: Int): Boolean {
        return text.getOrNull(end - 1) == '\n'
    }

    companion object {

        fun getSpacingSpannable(text: String, spacing: Int): CharSequence {
            return if (text.contains('\n')) {
                val spannable = SpannableString(text)
                spannable.setSpan(ParagraphSpacingSpan(spacing),
                    0, text.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                return spannable
            } else text
        }
    }
}
