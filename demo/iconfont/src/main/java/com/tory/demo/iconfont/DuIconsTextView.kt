package com.tory.demo.iconfont

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class DuIconsTextView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private val iconPaint = Paint()

    private val iconText: String
    private val iconSize = PointF()
    private val iconDirection: Int
    private var iconPadding = 0

    private val hasIcon: Boolean
        get()  = iconText.isNotEmpty()

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.DuIconsTextView)
        iconPadding = array.getDimensionPixelSize(R.styleable.DuIconsTextView_itv_iconPadding, 0)
        iconPaint.color = array.getColor(R.styleable.DuIconsTextView_itv_iconColor, textColors.defaultColor)
        iconPaint.textSize = array.getDimension(R.styleable.DuIconsTextView_itv_iconSize, textSize)
        iconDirection = array.getInt(R.styleable.DuIconsTextView_itv_iconDirection, DIRECTION_LEFT)
        iconText = array.getString(R.styleable.DuIconsTextView_itv_icon) ?: ""
        array.recycle()

        val iconFont = sIconFont ?: Typeface.createFromAsset(context.assets, "iconfont.ttf")
        iconPaint.typeface = iconFont
        iconPaint.textAlign = Paint.Align.LEFT

        iconSize.x = iconPaint.measureText(iconText)
        val fm = iconPaint.fontMetrics
        iconSize.y = fm.bottom - fm.top
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!hasIcon) return
        val iconX = when(iconDirection){
            DIRECTION_LEFT -> paddingLeft.toFloat()
            DIRECTION_RIGHT -> width - iconSize.x
            else -> (width - iconSize.x) / 2
        }
        val fm = iconPaint.fontMetrics
        val iconY = when(iconDirection){
            DIRECTION_TOP -> paddingTop + iconSize.y - fm.bottom
            DIRECTION_BOTTOM -> height - (paddingBottom + fm.bottom)
            else -> height / 2 - (iconSize.y/2 - fm.bottom)
        }
        canvas.drawText(iconText, iconX, iconY, iconPaint)
    }


    override fun getCompoundPaddingTop(): Int {
        return if (hasIcon && iconDirection == DIRECTION_TOP){
            super.getCompoundPaddingTop() + iconSize.y.toInt()
        } else {
            super.getCompoundPaddingTop()
        }
    }


    override fun getCompoundPaddingBottom(): Int {
        return if (hasIcon && iconDirection == DIRECTION_BOTTOM){
            super.getCompoundPaddingBottom() + iconSize.y.toInt()
        } else {
            super.getCompoundPaddingBottom()
        }
    }


    override fun getCompoundPaddingLeft(): Int {
        return if (hasIcon&& iconDirection == DIRECTION_LEFT){
            super.getCompoundPaddingLeft() + iconSize.x.toInt()
        } else {
            super.getCompoundPaddingLeft()
        }
    }

    override fun getCompoundPaddingRight(): Int {
        return if (hasIcon && iconDirection == DIRECTION_RIGHT){
            super.getCompoundPaddingRight() + iconSize.x.toInt()
        } else {
            super.getCompoundPaddingRight()
        }
    }



    companion object{
        const val DIRECTION_LEFT = 0
        const val DIRECTION_TOP = 1
        const val DIRECTION_RIGHT = 2
        const val DIRECTION_BOTTOM = 3

        var sIconFont: Typeface?= null


    }
}