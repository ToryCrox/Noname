package com.tory.noname.main

import android.graphics.Paint
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.LineHeightSpan
import android.util.Log
import androidx.annotation.Px
import com.tory.library.base.BaseActivity
import com.tory.library.extension.dpToPx
import com.tory.noname.R
import kotlinx.android.synthetic.main.activity_text_test.*

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/8/4
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/8/4 xutao 1.0
 * Why & What is modified:
 */
class TextTestActivity: BaseActivity() {
    override fun getLayoutId(): Int = R.layout.activity_text_test

    override fun initView(savedInstanceState: Bundle?) {
        val textArray = arrayOf(
            "¥596.40  x3期（含手续费13.4/期）起, ¥596.40  x3期（含手续费13.4/期）起, ¥596.40  x3期（含手续费13.4/期）起",
            "¥596.40  x6期（含手续费13.11/期）起",
            "596.40  x12期（含手续费10.93/期）起"
        )
        val space = dpToPx(6)
        val text = textArray.joinToString("\n")
        val spannable = SpannableString(text)
        spannable.setSpan(ParagraphSpacingSpan(space), 0, text.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE )
        textView.text = spannable


    }


    class ParagraphSpacingSpan(@param:Px private val spacing: Int) : LineHeightSpan {

        override fun chooseHeight(text: CharSequence, start: Int, end: Int, spanstartv: Int,
            v: Int, fm: Paint.FontMetricsInt) {
            if (isParagraphEnd(text, start, end)) {
                // let's just add what we want
                fm.descent += spacing
                fm.bottom += spacing
            }
        }

        private fun isParagraphEnd(text: CharSequence, start: Int, end: Int): Boolean{
            return text.getOrNull(end - 1) == '\n'
        }
    }
}
