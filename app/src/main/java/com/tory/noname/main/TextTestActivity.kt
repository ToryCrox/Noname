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
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import com.tory.library.base.BaseActivity
import com.tory.library.extension.dpToPx
import com.tory.library.widget.span.ParagraphSpacingSpan
import com.tory.noname.R
import kotlinx.android.synthetic.main.activity_text_test.*
import kotlinx.coroutines.launch

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

        val textParagraph = """
            我们查看了直接来自与会开发者、我们的客户顾问委员会 (CAB)、Google Developers 专家 (GDE) 的反馈，以及我们通过开发者调研获得的反馈。许多开发者已喜欢上使用 Kotlin，且提供更多 Kotlin 支持的呼声很高。下面介绍了开发者喜欢用 Kotlin 编写代码的原因：
            Kotlin 主要优势的示意图
            * 富有表现力且简洁：您可以使用更少的代码实现更多的功能。表达自己的想法，少编写样板代码。
            * 更安全的代码：提升应用质量。Kotlin 有许多语言功能，可帮助您避免 null 指针异常等常见编程错误。
            * 可互操作：您可以在 Kotlin 代码中调用 Java 代码，或者在 Java 代码中调用 Kotlin 代码。Kotlin 可完全与 Java 编程语言互操作，因此您可以根据需要在项目中添加任意数量的 Kotlin 代码。
           *  结构化并发：Kotlin 协程让异步代码像阻塞代码一样易于使用。协程可大幅简化后台任务管理，例如网络调用、本地数据访问等任务的管理。
        """.trimIndent()
        val text = ParagraphSpacingSpan.getSpacingSpannable(textParagraph, dpToPx(10))
        textView.text = text

        lifecycleScope.launch {
            whenCreated {

            }
        }

    }
}
