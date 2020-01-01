package com.tory.demo.iconfont

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.Drawable
import kotlin.math.roundToInt

/**
 * @author tory
 * @create 2020/1/1
 * @Describe
 */
class DuIconsDrawable(val context: Context,
                      val iconText: String,
                      var iconSize: Float,
                      val iconSelectedText: String? = null,
                      var tintColor: ColorStateList? = null): Drawable() {
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var isSelected = false

    init {
        iconPaint.typeface = getIconFont(context)
        iconPaint.textAlign = Paint.Align.LEFT

        iconPaint.textSize = iconSize

        updateColorTint()
        updateSelectedState()
    }

    private fun updateSelectedState(): Boolean {
        val newSelectedState = android.R.attr.state_selected in state
        return (isSelected != newSelectedState && iconSelectedText != null).also {
            isSelected = newSelectedState
        }
    }

    override fun getIntrinsicWidth(): Int {
        return iconPaint.measureText(iconText).roundToInt()
    }

    override fun getIntrinsicHeight(): Int {
        val fm = iconPaint.fontMetrics
        return (fm.bottom - fm.top).roundToInt()
    }

    private fun updateColorTint(): Boolean{
        val usedColor = tintColor?.let {
             it.getColorForState(state, it.defaultColor)
        } ?: run{
            Color.BLACK
        }
        return (iconPaint.color != usedColor).also {
            iconPaint.color = usedColor
        }
    }

    override fun setTintList(tint: ColorStateList?) {
        super.setTintList(tint)
        tintColor = tint
        updateColorTint()
    }

    /**
     * 标记为true才能有状态变化
     */
    override fun isStateful(): Boolean = true

    override fun onStateChange(state: IntArray?): Boolean {
        val b1 = updateColorTint()
        val b2 = updateSelectedState()
        return b1 || b2
    }

    override fun draw(canvas: Canvas) {
        val drawText = if (isSelected && iconSelectedText != null) iconSelectedText else iconText
        val iconX = bounds.left.toFloat()
        val iconY = bounds.bottom - iconPaint.fontMetrics.bottom
        canvas.drawText(drawText, iconX, iconY, iconPaint)
    }

    override fun setAlpha(alpha: Int) {
        iconPaint.alpha = alpha
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setColorFilter(colorFilter: ColorFilter?) = Unit

    companion object{
        var sIconFont: Typeface?= null

        fun getIconFont(context: Context): Typeface{
            return sIconFont ?: Typeface.createFromAsset(context.assets, "iconfont.ttf")
        }
    }
}