package com.tory.library.widget.span

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.text.style.ImageSpan
import com.tory.library.log.LogUtils
import java.lang.ref.WeakReference

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/9/1
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/9/1 xutao 1.0
 * Why & What is modified:
 */
class AlignImageSpan
    : ImageSpan {

    private var mDrawableRef: WeakReference<Drawable>? = null
    private var transX = 0
    private var transY = 0

    constructor(context: Context, resourceId: Int, alignment: Int = ALIGN_CENTER,
        transX :Int = 0, transY :Int = 0) : super(context, resourceId, alignment) {
        this.transX = transX
        this.transY = transY
    }

    constructor(drawable: Drawable, alignment: Int = ALIGN_CENTER,
        transX :Int = 0, transY :Int = 0) : super(drawable, alignment){
        this.transX = transX
        this.transY = transY
    }

    override fun getSize(paint: Paint, text: CharSequence, start: Int, end: Int,
        fm: Paint.FontMetricsInt?): Int {
        val drawable = drawable
        val rect = drawable.bounds
        if (fm != null && verticalAlignment == ALIGN_CENTER) {
            val fmPaint = paint.fontMetricsInt
            val fontHeight = fmPaint.descent - fmPaint.ascent
            val drHeight = rect.bottom - rect.top
            val centerY = fmPaint.ascent + fontHeight / 2
            fm.ascent = centerY - drHeight / 2
            fm.top = fm.ascent
            fm.bottom = centerY + drHeight / 2
            fm.descent = fm.bottom
        }
        return rect.right
    }

    override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float,
        top: Int, y: Int, bottom: Int, paint: Paint) {
        val drawable = getCachedDrawable()
        canvas.save()
        val fm = paint.fontMetricsInt
        var transY = when(verticalAlignment) {
            ALIGN_BOTTOM -> {
                bottom - drawable.getBounds().bottom
            }
            ALIGN_BASELINE -> {
                bottom - fm.descent - drawable.getBounds().bottom
            }
            ALIGN_CENTER -> {
                val fontHeight = fm.descent - fm.ascent
                val centerY = y + fm.descent - fontHeight / 2
                centerY - (drawable.bounds.bottom - drawable.bounds.top) / 2
            }
            ALIGN_ASCENT -> {
                bottom - (fm.descent - fm.ascent)
            }
            else -> {
                bottom - (fm.descent - fm.top)
            }
        }
        transY += this.transY
        canvas.translate(x + this.transX, transY.toFloat())
        drawable.draw(canvas)
        canvas.restore()
    }

    private fun getCachedDrawable(): Drawable {
        val wr = mDrawableRef
        var d: Drawable? = null
        if (wr != null) {
            d = wr.get()
        }
        return if (d != null) {
            d
        } else {
            d = drawable
            mDrawableRef = WeakReference(d)
            drawable
        }
    }


    companion object {

        const val ALIGN_BOTTOM = 0
        const val ALIGN_BASELINE = 1
        const val ALIGN_CENTER = 2
        const val ALIGN_ASCENT = 3
        const val ALIGN_TOP = 4
    }
}
