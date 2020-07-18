package com.tory.demo.demo

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.LabelVisibility

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/7/18
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/7/18 xutao 1.0
 * Why & What is modified:
 */
class MSlidingTabLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr) {

    private var tabTextSize: Float = 0f
    private var tabTextActiveSize: Float = 0f
    private var tabTextColor: Int = 0
    private var tabTextActiveColor: Int = 0

    private var tabPaddingStart: Int = 0
    private var tabPaddingTop: Int = 0
    private var tabPaddingEnd: Int = 0
    private var tabPaddingBottom: Int = 0

    private var tabIndicatorFitWidth: Boolean = true
    private var tabIndicatorColor: Int = Color.BLACK
    private var tabIndicatorWidth: Int = 0
    private var tabIndicatorHeight: Int = 0

    private var slidingTabIndicator: SlidingTabIndicator = SlidingTabIndicator(context)

    init {
        // Disable the Scroll Bar
        isHorizontalScrollBarEnabled = false

        var a = context.obtainStyledAttributes(attrs, R.styleable.MSlidingTabLayout,
            0, R.style.DefaultSlidTabLayout)
        tabTextSize = a.getDimension(R.styleable.MSlidingTabLayout_tab_textSize, 0f)
        tabTextActiveSize = a.getDimension(R.styleable.MSlidingTabLayout_tab_textActiveSize, 0f)
        tabTextColor = a.getColor(R.styleable.MSlidingTabLayout_tab_textColor, Color.BLACK)
        tabTextActiveColor = a.getColor(R.styleable.MSlidingTabLayout_tab_textActiveSize, Color.BLACK)

        tabPaddingStart = a.getDimensionPixelOffset(R.styleable.MSlidingTabLayout_tab_paddingStart, 0)
        tabPaddingEnd = a.getDimensionPixelOffset(R.styleable.MSlidingTabLayout_tab_paddingEnd, 0)
        tabPaddingTop = a.getDimensionPixelOffset(R.styleable.MSlidingTabLayout_tab_paddingTop, 0)
        tabPaddingBottom = a.getDimensionPixelOffset(R.styleable.MSlidingTabLayout_tab_paddingBottom, 0)

        tabIndicatorFitWidth = a.getBoolean(R.styleable.MSlidingTabLayout_tab_indicatorFitWidth, true)
        tabIndicatorColor = a.getColor(R.styleable.MSlidingTabLayout_tab_indicatorColor, Color.BLACK)
        tabIndicatorWidth = a.getDimensionPixelOffset(R.styleable.MSlidingTabLayout_tab_indicatorWidth, 0)
        tabIndicatorHeight = a.getDimensionPixelOffset(R.styleable.MSlidingTabLayout_tab_indicatorHeight, 0)
        a.recycle()

        super.addView(slidingTabIndicator, LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
    }




    inner class SlidingTabIndicator(context: Context) : LinearLayout(context) {

        init {
            setWillNotDraw(false)
            orientation = HORIZONTAL
        }

    }
}
