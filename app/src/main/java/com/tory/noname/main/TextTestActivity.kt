package com.tory.noname.main

import android.graphics.*
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.tory.library.base.BaseActivity
import com.tory.library.extension.dp
import com.tory.library.utils.BitmapUtils
import com.tory.library.utils.blur.BlurUtil
import com.tory.library.widget.span.AlignImageSpan
import com.tory.library.widget.span.ParagraphSpacingSpan
import com.tory.noname.R
import kotlinx.android.synthetic.main.activity_text_test.*
import kotlinx.coroutines.*
import java.lang.Runnable

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
        val text = ParagraphSpacingSpan.getSpacingSpannable(textParagraph, 10.dp())
        textView.text = text

        val builder = SpannableStringBuilder()
        val spanLeft = AlignImageSpan(this, R.drawable.ic_product_tag_paren_left,
            AlignImageSpan.ALIGN_ASCENT, transY = 1.dp())
        val spanRight = AlignImageSpan(this, R.drawable.ic_product_tag_paren_right,
            AlignImageSpan.ALIGN_BASELINE, transX = -1.dp())
        builder.append("#", spanLeft, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        builder.append("MOSHCHINO热度榜 No.1")
        builder.append("#", spanRight, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        textView.text = builder

        val tags = arrayOf("得物APP专供", "限量款", "联名款")
        val tagSpan = SpannableStringBuilder()
        tags.forEachIndexed { index, tag ->
            if (index != 0) {
                tagSpan.append("", AlignImageSpan(this, R.drawable.ic_product_tag_divider,
                    AlignImageSpan.ALIGN_CENTER), Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            }
            tagSpan.append(tag)
        }
        textView.text = tagSpan

        //marqueeTextView.startScroll()
        marqueeTextView.setMarqueeEnable(true)
        marqueeTextView.setOnClickListener {
            marqueeTextView.isVisible = false
            //marqueeTextView.setText("修改文字字dddddddddddddddddddddddddddddddddddddddddd")
        }
        marqueeTextView.postDelayed(Runnable {
            marqueeTextView.setText("我是文字, 我是文字, 我是文字sssssssssssssssssssssssssssssss")
        }, 1000L)


        doTestXFormode()
    }

    private fun doTestXFormode() {
        lifecycleScope.launch {
            val bitmap1 = withContext(Dispatchers.IO) {
                doXformode(R.drawable.b1)
            } ?: return@launch
            imageTest1.setImageBitmap(bitmap1)

            val bitmap2 = withContext(Dispatchers.IO) {
                doXformode(R.drawable.b2)
            } ?: return@launch
            imageTest2.setImageBitmap(bitmap2)

            val bitmap3 = withContext(Dispatchers.IO) {
                doXformode(R.drawable.b3)
            } ?: return@launch
            imageTest3.setImageBitmap(bitmap3)


            val bitmap4 = withContext(Dispatchers.IO) {
                doXformode(R.drawable.b4)
            } ?: return@launch
            imageTest4.setImageBitmap(bitmap4)
        }
    }


    private fun doXformode(srcId: Int): Bitmap? {
        val w = 200.dp()
        val h = 125.dp()
        var tt = BitmapFactory.decodeResource(resources, R.drawable.t2)
        tt  = BlurUtil.blur(this, tt)
        //var dst = BitmapUtils.scaleCenterCrop(tt, w, h) ?: return null
        val dst = BitmapFactory.decodeResource(resources, R.drawable.glass)
        val dstDrawable = dst.toDrawable(resources)
        //DrawableCompat.setTint(dstDrawable, Color.RED)

        val source = BitmapUtils.scaleCenterCrop(BitmapFactory.decodeResource(resources, srcId), 72.dp(), 46.dp()) ?: return null

        val bitmap = Bitmap.createBitmap(375.dp(), 85.dp(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.isAntiAlias = true
        dstDrawable.setBounds(0, 0, 375.dp(), 85.dp())
        dstDrawable.draw(canvas)
        //canvas.drawBitmap(dst, 0.dp().toFloat(), 0.dp().toFloat(), paint)
        paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.MULTIPLY))
        canvas.drawBitmap(source, 168.dp().toFloat(), 30.dp().toFloat(), paint)

        return bitmap
    }
}
