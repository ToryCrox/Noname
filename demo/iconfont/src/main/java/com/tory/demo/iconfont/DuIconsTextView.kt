package com.tory.demo.iconfont

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView

class DuIconsTextView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private val iconText: String
    private val iconSelectedText: String?
    private val iconDirection: Int
    private var iconTextPadding = 0
    private val iconPadding: Rect = Rect()
    private val iconSize: Float
    private val iconColor: ColorStateList

    private val iconOnly: Boolean

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.DuIconsTextView)
        iconOnly = array.getBoolean(R.styleable.DuIconsTextView_itv_iconOnly, text.isEmpty())
        iconTextPadding = array.getDimensionPixelSize(R.styleable.DuIconsTextView_itv_iconTextPadding, 0)
        iconColor = array.getColorStateList(R.styleable.DuIconsTextView_itv_iconColor) ?: textColors
        iconSize = array.getDimension(R.styleable.DuIconsTextView_itv_iconSize, textSize)
        iconDirection = array.getInt(R.styleable.DuIconsTextView_itv_iconDirection, DIRECTION_LEFT)
        iconText = array.getString(R.styleable.DuIconsTextView_itv_icon) ?: ""
        iconSelectedText  = array.getString(R.styleable.DuIconsTextView_itv_iconSelected)

        val padding = array.getDimensionPixelOffset(R.styleable.DuIconsTextView_itv_iconPadding, 0)
        iconPadding.left = array.getDimensionPixelOffset(R.styleable.DuIconsTextView_itv_iconPaddingLeft, padding)
        iconPadding.top = array.getDimensionPixelOffset(R.styleable.DuIconsTextView_itv_iconPaddingTop, padding)
        iconPadding.right = array.getDimensionPixelOffset(R.styleable.DuIconsTextView_itv_iconPaddingRight, padding)
        iconPadding.bottom = array.getDimensionPixelOffset(R.styleable.DuIconsTextView_itv_iconPaddingBottom, padding)

        array.recycle()

        if (!iconOnly){
            setIconDrawable()
        } else {
            typeface = DuIconsDrawable.getIconFont(context)
            text = if (isSelected && iconSelectedText != null) iconSelectedText else iconText
            setTextSize(TypedValue.COMPLEX_UNIT_PX, iconSize)
            setTextColor(iconColor)
            setPadding(iconPadding.left, iconPadding.top, iconPadding.right, iconPadding.bottom)
        }
    }

    private fun setIconDrawable(){
        val icon = DuIconsDrawable(context, iconText, iconSize,
                iconSelectedText, iconColor, iconPadding)
        icon.setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)
        when(iconDirection){
            DIRECTION_TOP -> setCompoundDrawables(null, icon , null, null)
            DIRECTION_RIGHT -> setCompoundDrawables(null, null , icon, null)
            DIRECTION_BOTTOM -> setCompoundDrawables(null, null , null, icon)
            else -> setCompoundDrawables(icon, null , null, null)
        }
        compoundDrawablePadding = iconTextPadding
    }

    override fun dispatchSetSelected(selected: Boolean) {
        super.dispatchSetSelected(selected)
        if (iconOnly){
            text = if (iconSelectedText != null && selected) iconSelectedText else iconText
        }
    }


    companion object{
        const val DIRECTION_LEFT = 0
        const val DIRECTION_TOP = 1
        const val DIRECTION_RIGHT = 2
        const val DIRECTION_BOTTOM = 3
    }
}