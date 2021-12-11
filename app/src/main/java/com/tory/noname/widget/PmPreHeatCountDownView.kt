package com.tory.noname.widget

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.tory.library.extension.dp
import com.tory.noname.R

/**
 * modify: Tory
 * Description:
 */
class PmPreHeatCountDownView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val itemH = 16.dp() // 固定高度
    private val minItemW = itemH
    private val nounW = 8.dp() // 冒号的宽度

    // 测量后的时分秒宽度
    private var hourW: Float = 0f
    private var minW: Float = 0f
    private var secW: Float = 0f

    private val radius = 1.dp().toFloat()

    // padding
    private val itemPaddingStart = 2.dp()
    private val itemPaddingEnd = 2.dp()

    // 文字大小
    private val txtSize = 11.dp().toFloat()
    private val txtColor: Int = Color.WHITE

    // 事件背景
    private val itemBgColor: Int = Color.parseColor("#434457")

    private val nounSize = 14.dp().toFloat()
    private val nounColor: Int = Color.parseColor("#4A4A5F")

    // 当前显示的时分秒
    private var hour: String = "00"
    private var min: String = "00"
    private var sec: String = "00"

    private val tmpRect: RectF = RectF()
    private var start: Float = 0f

    private val paint = Paint()

    private val xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

    private val itemBg = checkNotNull(ContextCompat.getDrawable(context, R.drawable.ic_count_down_item_bg))

    var useDrawable = false

    init {
        paint.apply {
            color = txtColor
            textSize = txtSize
            style = Paint.Style.FILL
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        setLayerType(LAYER_TYPE_SOFTWARE, paint)
    }

    fun update(countDownTime: Long) {
        val hour = countDownTime / HOUR_MILLIS
        val min = (countDownTime % HOUR_MILLIS) / MINUTES_MILLIS
        val sec = (countDownTime % MINUTES_MILLIS) / SECOND_MILLIS
        update(coverTo(hour), coverTo(min), coverTo(sec))
    }

    private fun coverTo(time: Long): String {
        return if (time < 10) "0$time" else time.toString()
    }

    fun update(hour: String, min: String, sec: String) {
        val preHourSize = this.hour.length
        this.hour = hour
        this.min = min
        this.sec = sec
        if (preHourSize != hour.length) { // 分秒最多60 而时可能是三位数
            requestLayout()
        } else {
            invalidate()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        hourW = minItemW.toFloat().coerceAtLeast(measureItemWithPadding(hour, itemPaddingStart, itemPaddingEnd))
        minW = minItemW.toFloat().coerceAtLeast(measureItemWithPadding(min, itemPaddingStart, itemPaddingEnd))
        secW = minItemW.toFloat().coerceAtLeast(measureItemWithPadding(sec, itemPaddingStart, itemPaddingEnd))

        setMeasuredDimension((hourW + minW + secW + 2 * nounW).toInt(), itemH)
    }

    /**
     * 测量时分秒item宽度
     */
    private fun measureItemWithPadding(item: String, paddingStart: Int, paddingEnd: Int): Float {
        return paint.measureText(item) + paddingStart + paddingEnd
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        // 从左边开始向右绘制
        start = 0f

        // 绘制小时
        tmpRect.set(start, 0f, start + hourW, measuredHeight.toFloat())
        drawText(canvas, hour, tmpRect)
        start += tmpRect.width()

        // 绘制冒号
        tmpRect.set(start, 0f, start + nounW, measuredHeight.toFloat())
        drawNoun(canvas, tmpRect)
        start += tmpRect.width()

        // 绘制分钟
        tmpRect.set(start, 0f, start + minW, measuredHeight.toFloat())
        drawText(canvas, min, tmpRect)
        start += tmpRect.width()

        // 绘制冒号
        tmpRect.set(start, 0f, start + nounW, measuredHeight.toFloat())
        drawNoun(canvas, tmpRect)
        start += tmpRect.width()

        // 绘制秒钟
        tmpRect.set(start, 0f, start + secW, measuredHeight.toFloat())
        drawText(canvas, sec, tmpRect)
        start += tmpRect.width()
    }

    /**
     * 在给定的区域绘制文字 上下居中 包含padding bg mask等等
     */
    private fun drawText(canvas: Canvas, text: String, rectF: RectF) {
        // 绘制背景
        if (useDrawable) {
            itemBg.setBounds(rectF.left.toInt(), rectF.top.toInt(), rectF.right.toInt(), rectF.bottom.toInt())
            itemBg.draw(canvas)
        } else {
            paint.color = itemBgColor
            canvas.drawRoundRect(tmpRect, radius, radius, paint)
            paint.xfermode = xfermode
            canvas.drawCircle(tmpRect.left, tmpRect.centerY(), 1.dp().toFloat(), paint)
            canvas.drawCircle(tmpRect.right, tmpRect.centerY(), 1.dp().toFloat(), paint)
            paint.xfermode = null
        }

        // 绘制文字 上下居中
        paint.color = txtColor
        paint.textSize = txtSize
        paint.textAlign = Paint.Align.CENTER
        val fontMetrics = paint.fontMetrics
        val distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
        val baseline = rectF.centerY() + distance
        canvas.drawText(text, rectF.centerX(), baseline, paint)
    }

    /**
     * 在给定的区域居中绘制冒号
     */
    private fun drawNoun(canvas: Canvas, rectF: RectF) {
        paint.color = nounColor
        paint.textSize = nounSize
        paint.textAlign = Paint.Align.CENTER
        val fontMetrics = paint.fontMetrics
        val distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
        val baseline = rectF.centerY() + distance - 1.dp() // -1dp 是为了冒号向上移一点
        canvas.drawText(":", rectF.centerX(), baseline, paint)
    }

    companion object {
        public const val SECOND_MILLIS = 1000L
        public const val MINUTES_MILLIS = 60 * SECOND_MILLIS
        public const val HOUR_MILLIS = 60 * MINUTES_MILLIS
        public const val DAY_MILLIS = 24 * HOUR_MILLIS
    }
}
