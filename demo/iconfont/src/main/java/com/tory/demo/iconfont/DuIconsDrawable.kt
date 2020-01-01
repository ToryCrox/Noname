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
                      var tintColor: ColorStateList?): Drawable() {
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        val iconFont = sIconFont ?: Typeface.createFromAsset(context.assets, "iconfont.ttf")
        iconPaint.typeface = iconFont
        iconPaint.textAlign = Paint.Align.LEFT

        iconPaint.textSize = iconSize
        updateColorTint()
    }

    override fun getIntrinsicWidth(): Int {
        return iconPaint.measureText(iconText).roundToInt()
    }

    override fun getIntrinsicHeight(): Int {
        val fm = iconPaint.fontMetrics
        return (fm.bottom - fm.top).roundToInt()
    }

    private fun updateColorTint(){
        tintColor?.let {
            iconPaint.color = it.getColorForState(state, it.defaultColor)
        } ?: run{
            iconPaint.color = Color.BLACK
        }
    }

    override fun setTintList(tint: ColorStateList?) {
        super.setTintList(tint)
        tintColor = tint
        updateColorTint()
    }

    override fun onStateChange(state: IntArray?): Boolean {
        updateColorTint()
        return super.onStateChange(state)
    }

    override fun draw(canvas: Canvas) {
        val iconX = bounds.left.toFloat()
        val iconY = bounds.bottom - iconPaint.fontMetrics.bottom
        canvas.drawText(iconText, iconX, iconY, iconPaint)
    }

    override fun setAlpha(alpha: Int) {
        iconPaint.alpha = alpha
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setColorFilter(colorFilter: ColorFilter?) = Unit

    companion object{
        var sIconFont: Typeface?= null
    }
}