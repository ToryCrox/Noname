package com.tory.demo.demo

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.database.DataSetObserver
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Point
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.animation.doOnEnd
import androidx.core.view.ViewCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnAdapterChangeListener
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.animation.AnimationUtils
import com.google.android.material.animation.ArgbEvaluatorCompat
import com.google.android.material.math.MathUtils
import java.lang.ref.WeakReference

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
    private var isToggleBoldText: Boolean = false
    private var tabBackground: Drawable = ColorDrawable(Color.TRANSPARENT)
    private var tabMinHeight: Int = 40.dp()
    private var tabMaxHeight: Int = 40.dp()
    private var tabPaddingStart: Int = 0
    private var tabPaddingTop: Int = 0
    private var tabPaddingEnd: Int = 0
    private var tabPaddingBottom: Int = 0
    private var tabIndicatorFitWidth: Boolean = true
    private var tabIndicatorColor: Int = Color.BLACK
    private var tabIndicatorWidth: Int = 0
    private var tabIndicatorHeight: Int = 0
    private var bottomDividerDrawable: Drawable? = null
    private var maskRightDrawable: Drawable? = null
    private val slidingTabIndicator: SlidingTabIndicator
    private var viewPager: ViewPager? = null
    private var pagerAdapter: PagerAdapter? = null
    private val pageChangeListener: TabLayoutOnPageChangeListener by lazy {
        TabLayoutOnPageChangeListener(this)
    }
    private val adapterDataSetObserver: AdapterDataSetObserver by lazy { AdapterDataSetObserver() }
    private val adapterChangeListener: AdapterChangeListener by lazy { AdapterChangeListener() }
    private val tabSelectListeners: MutableList<TabSelectListener> = mutableListOf()
    private var vpSelectListener: TabSelectListener? = null
    private val tabViews: MutableList<TabView> = mutableListOf()
    private var selectedTabView: TabView? = null
    var tabAnimationDuration = 300L
    private val scrollAnimator: ValueAnimator by lazy { createScrollAnimator() }
    var expandProgress: Float = 1f
        set(value) {
            if (field != value) {
                field = value
                log("expandProgress $value")
                updateExpandProgress()
            }
        }
    private var currentX: Int = Integer.MIN_VALUE
    private var scrollState: ScrollState = ScrollState.IDLE
    private val scrollListeners: MutableList<OnScrollListener> = mutableListOf()
    private val scrollRunnable: Runnable = object : Runnable {
        override fun run() {
            if (scrollX == currentX) {
                dispatchScrollStateChanged(ScrollState.IDLE)
                removeCallbacks(this)
                return
            } else {
                currentX = scrollX
                dispatchScrollStateChanged(ScrollState.FLING)
                postDelayed(this, 50)
            }
        }
    }

    init {
        // Disable the Scroll Bar
        isHorizontalScrollBarEnabled = false
        val a = context.obtainStyledAttributes(attrs, R.styleable.MSlidingTabLayout,
            0, R.style.DefaultSlidTabLayout)
        tabTextSize = a.getDimension(R.styleable.MSlidingTabLayout_tab_textSize, 0f)
        tabTextActiveSize = a.getDimension(R.styleable.MSlidingTabLayout_tab_textActiveSize, 0f)
        tabTextColor = a.getColor(R.styleable.MSlidingTabLayout_tab_textColor, Color.BLACK)
        tabTextActiveColor = a.getColor(R.styleable.MSlidingTabLayout_tab_textActiveColor,
            Color.BLACK)
        isToggleBoldText = a.getBoolean(R.styleable.MSlidingTabLayout_tab_isToggleBoldText,
            isToggleBoldText)
        tabBackground = a.getDrawable(R.styleable.MSlidingTabLayout_tab_background) ?: tabBackground
        tabMinHeight = a.getDimensionPixelOffset(R.styleable.MSlidingTabLayout_tab_minHeight,
            tabMinHeight)
        tabMaxHeight = a.getDimensionPixelOffset(R.styleable.MSlidingTabLayout_tab_maxHeight,
            tabMaxHeight)

        tabPaddingStart = a.getDimensionPixelOffset(R.styleable.MSlidingTabLayout_tab_paddingStart,
            0)
        tabPaddingEnd = a.getDimensionPixelOffset(R.styleable.MSlidingTabLayout_tab_paddingEnd, 0)
        tabPaddingTop = a.getDimensionPixelOffset(R.styleable.MSlidingTabLayout_tab_paddingTop, 0)
        tabPaddingBottom = a.getDimensionPixelOffset(R.styleable.MSlidingTabLayout_tab_paddingBottom,
            0)

        tabIndicatorFitWidth = a.getBoolean(R.styleable.MSlidingTabLayout_tab_indicatorFitWidth,
            true)
        tabIndicatorColor = a.getColor(R.styleable.MSlidingTabLayout_tab_indicatorColor,
            Color.BLACK)
        tabIndicatorWidth = a.getDimensionPixelOffset(R.styleable.MSlidingTabLayout_tab_indicatorWidth,
            0)
        tabIndicatorHeight = a.getDimensionPixelOffset(R.styleable.MSlidingTabLayout_tab_indicatorHeight,
            0)

        bottomDividerDrawable = a.getDrawable(R.styleable.MSlidingTabLayout_tab_bottomDivider)
        maskRightDrawable = a.getDrawable(R.styleable.MSlidingTabLayout_tab_maskRight)
        a.recycle()
        minimumHeight = tabMinHeight


        slidingTabIndicator = SlidingTabIndicator(context)
        super.addView(slidingTabIndicator, LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        log("minimumHeight: " + minimumHeight)
    }

    fun getTotalOffset(): Int = tabMaxHeight - tabMinHeight

    fun addTabSelectListener(listener: TabSelectListener) {
        if (!tabSelectListeners.contains(listener)) {
            tabSelectListeners.add(listener)
        }
    }

    fun removeTabSelectListener(listener: TabSelectListener) {
        tabSelectListeners.remove(listener)
    }

    fun addOnScrollListener(listener: OnScrollListener) {
        if (!scrollListeners.contains(listener)) {
            scrollListeners.add(listener)
        }
    }

    fun removeOnScrollListener(listener: OnScrollListener) {
        scrollListeners.remove(listener)
    }

    fun dispatchScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        scrollListeners.forEach { it.onScrollChanged(l, t, oldl, oldt) }
    }

    fun dispatchScrollStateChanged(state: ScrollState) {
        if (scrollState == state) return
        scrollState = state
        scrollListeners.forEach { it.onStateChanged(state) }
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        dispatchScrollChanged(l, t, oldl, oldt)
    }

    fun setupWithViewPager(viewPager: ViewPager) {
        this.viewPager?.removeOnPageChangeListener(pageChangeListener)
        this.viewPager?.removeOnAdapterChangeListener(adapterChangeListener)
        this.viewPager?.adapter?.unregisterDataSetObserver(adapterDataSetObserver)
        vpSelectListener?.let { removeTabSelectListener(it) }

        this.viewPager = viewPager
        viewPager.addOnPageChangeListener(pageChangeListener)
        viewPager.addOnAdapterChangeListener(adapterChangeListener)
        vpSelectListener = ViewPagerTabSelectListener(viewPager).also {
            addTabSelectListener(it)
        }
        setPagerAdapter(viewPager.adapter)
    }

    private fun setPagerAdapter(newAdapter: PagerAdapter?) {
        this.pagerAdapter?.unregisterDataSetObserver(adapterDataSetObserver)

        this.pagerAdapter = newAdapter
        newAdapter?.registerDataSetObserver(adapterDataSetObserver)
        populateFromPagerAdapter()
    }

    /**
     * 创建tabs
     */
    private fun populateFromPagerAdapter() {
        removeAllTabs()
        tabViews.clear()
        val adapter = pagerAdapter ?: return
        val count = adapter.count
        for (index: Int in 0 until count) {
            val tabView = createTabView(index)
            tabView.text = adapter.getPageTitle(index)
            slidingTabIndicator.addView(tabView,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT)
            tabViews.add(tabView)
        }
        val curItem = viewPager?.currentItem ?: return
        if (adapter.count > 0 && curItem != getSelectedPosition() && curItem < getTabCount()) {
            selectTab(curItem)
        }
    }

    private fun removeAllTabs() {
        slidingTabIndicator.removeAllViews()
    }

    private fun createTabView(index: Int): TabView {
        return TabView(context, index)
    }

    private fun getTabAt(index: Int): TabView? {
        return tabViews.getOrNull(index)
    }

    private fun getSelectedPosition(): Int {
        return selectedTabView?.position ?: -1
    }

    private fun getTabCount() = tabViews.size

    fun setScrollPosition(
        position: Int,
        positionOffset: Float,
        updateSelectedText: Boolean,
        updateIndicatorPosition: Boolean = true
    ) {
        val roundedPosition = Math.round(position + positionOffset)
        if (roundedPosition < 0 || roundedPosition >= slidingTabIndicator.childCount) {
            return
        }
        //scrollTo(calculateScrollXForTab(position, positionOffset), 0)
        if (updateSelectedText) {
            val tabView = getTabAt(position) ?: return
            val nextTabView = getTabAt(position + 1)
            tabView.setStateFraction(1 - positionOffset)
            nextTabView?.setStateFraction(positionOffset)
        }
        // Now update the scroll position, canceling any running animation
        // Now update the scroll position, canceling any running animation
        if (updateIndicatorPosition) {
            slidingTabIndicator.setIndicatorPositionFromTabPosition(position, positionOffset)
        }

        if (scrollAnimator.isRunning) {
            scrollAnimator.cancel()
        }
        scrollTo(calculateScrollXForTab(position, positionOffset), 0)
    }

    private fun dispatchTabSelected(tabView: TabView) {
        tabSelectListeners.forEach { it.onTabSelected(tabView) }
    }

    private fun dispatchTabUnSelected(tabView: TabView) {
        tabSelectListeners.forEach { it.onTabUnSelected(tabView) }
    }

    private fun selectTab(position: Int, updateIndicator: Boolean = true) {
        val tabView = tabViews.getOrNull(position) ?: return
        val currentTabView = selectedTabView
        if (currentTabView == tabView) return
        val newPosition = tabView.position
        if (updateIndicator) {
            if ((currentTabView == null || currentTabView.position == -1)
                && newPosition != -1) {
                // If we don't currently have a tab, just draw the indicator
                setScrollPosition(newPosition, 0f, true)
            } else {
                animateToTab(newPosition)
            }
            currentTabView?.animateSelectedState(false)
            tabView.animateSelectedState(true)
        }

        if (currentTabView != null) {
            dispatchTabUnSelected(currentTabView)
        }
        selectedTabView = tabView
        dispatchTabSelected(tabView)
    }

    private fun calculateScrollXForTab(position: Int, positionOffset: Float): Int {
        val selectedChild = slidingTabIndicator.getChildAt(position) ?: return 0
        val nextChild = if (position + 1 < slidingTabIndicator.childCount) slidingTabIndicator.getChildAt(
            position + 1) else null
        val selectedWidth = selectedChild.width
        val nextWidth = nextChild?.width ?: 0
        // base scroll amount: places center of tab in center of parent
        val scrollBase = selectedChild.left + selectedWidth / 2 - width / 2
        // offset amount: fraction of the distance between centers of tabs
        val scrollOffset = ((selectedWidth + nextWidth) * 0.5f * positionOffset).toInt()
        return if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR) scrollBase + scrollOffset else scrollBase - scrollOffset
    }

    private fun animateToTab(newPosition: Int) {
        if (newPosition == -1) return
        if (windowToken == null || !ViewCompat.isLaidOut(this)) {
            // If we don't have a window token, or we haven't been laid out yet just draw the new
            // position now
            setScrollPosition(newPosition, 0f, true)
            return
        }
        val startScrollX = scrollX
        val targetScrollX = calculateScrollXForTab(newPosition, 0f)

        if (startScrollX != targetScrollX) {
            scrollAnimator.setIntValues(startScrollX, targetScrollX)
            scrollAnimator.start()
        }

        // Now animate the indicator
        slidingTabIndicator.animateIndicatorToPosition(newPosition, tabAnimationDuration)
    }

    private fun createScrollAnimator(): ValueAnimator {
        val animator = ValueAnimator()
        animator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR)
        animator.setDuration(tabAnimationDuration)
        animator.addUpdateListener { _ -> scrollTo(animator.animatedValue as Int, 0) }
        return animator
    }

    private fun updateExpandProgress() {
        //requestLayout()
        //translationY = (tabMaxHeight - tabMinHeight) * (1 - expandProgress)
        //slidingTabIndicator.translationY = translationY / 2
        selectedTabView?.updateFraction()
        requestLayout()

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = MathUtils.lerp(tabMinHeight.toFloat(), tabMaxHeight.toFloat(), expandProgress).toInt()
        super.onMeasure(widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bottomDividerDrawable?.let {
            it.setBounds(0, height - it.intrinsicHeight, width, height)
        }
        maskRightDrawable?.let {
            it.setBounds(width - it.intrinsicWidth, 0, width, height)
        }
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_MOVE -> {
                dispatchScrollStateChanged(ScrollState.TOUCH_SCROLL)
                removeCallbacks(scrollRunnable)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                post(scrollRunnable)
            }
        }
        return super.onTouchEvent(ev)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(scrollRunnable)
    }

    override fun dispatchDraw(canvas: Canvas) {
        bottomDividerDrawable?.let {
            canvas.save()
            canvas.translate(scrollX.toFloat(), 0f)
            it.draw(canvas)
            canvas.restore()
        }
        super.dispatchDraw(canvas)
        maskRightDrawable?.let {
            canvas.save()
            canvas.translate(scrollX.toFloat(), 0f)
            it.draw(canvas)
            canvas.restore()
        }
    }

    inner class SlidingTabIndicator(context: Context) : LinearLayout(context) {
        val indicator: GradientDrawable = GradientDrawable()
        var selectedPosition: Int = -1
        var selectionOffset: Float = 0f
        private var indicatorLeft: Int = -1
        private var indicatorAnimator: ValueAnimator? = null

        init {
            setWillNotDraw(false)
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            indicator.setColor(tabIndicatorColor)
            indicator.setBounds(0, 0, tabIndicatorWidth, tabIndicatorHeight)
        }

        fun setIndicatorPositionFromTabPosition(position: Int, positionOffset: Float) {
            if (indicatorAnimator?.isRunning() == true) {
                indicatorAnimator?.cancel()
            }
            selectedPosition = position
            selectionOffset = positionOffset
            updateIndicatorPosition()
        }

        private fun updateIndicatorPosition() {
            val tabView = getChildAt(selectedPosition) ?: return
            if (tabView.width <= 0) return
            var startLeft = tabView.left + (tabView.width - tabIndicatorWidth) / 2
            if (selectedPosition + 1 < childCount) {
                val nextChild = getChildAt(selectedPosition + 1) as TabView
                val targetLeft = nextChild.left + (nextChild.getMaxWidth() - tabIndicatorWidth) / 2 - getFixDetalX(
                    selectedPosition + 1)
                startLeft = MathUtils.lerp(startLeft.toFloat(),
                    targetLeft.toFloat(),
                    selectionOffset).toInt()
            }
            setIndicatorPosition(startLeft)
        }

        fun setIndicatorPosition(left: Int) {
            if (left != indicatorLeft) {
                // If the indicator's left/right has changed, invalidate
                indicatorLeft = left
                ViewCompat.postInvalidateOnAnimation(this)
            }
        }

        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            super.onLayout(changed, l, t, r, b)

            val animator = indicatorAnimator
            if (animator?.isRunning == true) {
                // If we're currently running an animation, lets cancel it and start a
                // new animation with the remaining duration
                animator.cancel()
                val duration = animator.duration
                animateIndicatorToPosition(
                    selectedPosition,
                    Math.round((1f - animator.animatedFraction) * duration).toLong())
            } else {
                // If we've been layed out, update the indicator position
                updateIndicatorPosition()
            }
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val indicatorTop = height - tabIndicatorHeight - translationY.toInt()
            val indicatorBottom = indicatorTop + tabIndicatorHeight
            val indicatorRight = indicatorLeft + tabIndicatorWidth
            indicator.setBounds(indicatorLeft, indicatorTop, indicatorRight, indicatorBottom)
            indicator.draw(canvas)
        }

        fun getFixDetalX(position: Int): Int {
            var detaWidth = 0
            for (index in 0 until position) {
                val child = getChildAt(index)
                if (child is TabView) {
                    detaWidth += child.width - child.getMinWidth()
                }
            }
            return detaWidth
        }

        fun animateIndicatorToPosition(position: Int, duration: Long) {
            if (indicatorAnimator?.isRunning() == true) {
                indicatorAnimator?.cancel()
            }
            val targetView = getChildAt(position)
            if (targetView !is TabView) {
                // If we don't have a view, just update the position now and return
                updateIndicatorPosition()
                return
            }
            val detaX = getFixDetalX(position + 1)
            val startLeft = indicatorLeft
            val targetLeft = targetView.left + (targetView.getMaxWidth() - tabIndicatorWidth) / 2 - detaX


            log("animateIndicatorToPosition position:$position, " +
                "startLeft:$startLeft, targetLeft:$targetLeft, detaX:$detaX ")
            if (startLeft != targetLeft) {
                val animator = ValueAnimator().also { indicatorAnimator = it }
                animator.interpolator = AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR
                animator.duration = duration.toLong()
                animator.setFloatValues(0f, 1f)
                animator.addUpdateListener { valueAnimator ->
                    val fraction = valueAnimator.animatedFraction
                    setIndicatorPosition(
                        MathUtils.lerp(startLeft.toFloat(), targetLeft.toFloat(), fraction).toInt())
                }
                animator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animator: Animator) {
                        selectedPosition = position
                        selectionOffset = 0f
                    }
                })
                animator.start()
            }
        }
    }

    inner class TabView(context: Context, val position: Int) : FrameLayout(context) {
        private val isUseTextSizeChange = false
        val textView = AppCompatTextView(context)
        private var fraction: Float = -1f
        private val maxTextScale = tabTextActiveSize / tabTextSize
        private var stateAnimator: ValueAnimator? = null
        private val minSize = Point()
        private val maxSize = Point()
        private var isInMeasure = false
        var text: CharSequence?
            get() = textView.text
            set(value) {
                textView.text = value
                updateTabSize()
            }

        init {
            isClickable = true
            clipToPadding = false
            background = tabBackground.constantState?.newDrawable()?.mutate() ?: tabBackground
            setPadding(tabPaddingStart, tabPaddingTop, tabPaddingEnd, tabPaddingBottom)
            val lp = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            lp.gravity = Gravity.CENTER
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize)
            addView(textView, lp)
            setOnClickListener {
                log("on Click position:$position, text:$text")
                selectTab(position)
            }
        }

        private fun updateTabSize() {
            if (isUseTextSizeChange) {
                measureSize(1f, maxSize)
                measureSize(0f, minSize)
            } else {
                measureSize(0f, minSize)
                maxSize.set(minSize.x, minSize.y)
            }
        }

        private fun measureSize(fraction: Float, size: Point) {
            setStateFraction(fraction)
            isInMeasure = true
            measure(0, 0)
            size.set(measuredWidth, measuredHeight)
            isInMeasure = false
        }

        fun getMaxWidth() = maxSize.x

        fun getMinWidth() = minSize.x

        override fun setSelected(selected: Boolean) {
            super.setSelected(selected)
            textView.isSelected = selected
        }

        fun setSelectedState(selected: Boolean) {
            isSelected = selected
            log("TabView $position setSelectedState selected:$selected")
            setStateFraction(if (selected) 1f else 0f)
        }

        fun animateSelectedState(selected: Boolean) {
            val targetFraction = if (selected) 1f else 0f
            if (this.fraction == targetFraction) return
            log("TabView $position animateSelectedState " +
                "selected:$selected targetFraction:$targetFraction")
            if (stateAnimator?.isRunning() == true) stateAnimator?.cancel()
            val animator = ValueAnimator().also { stateAnimator = it }
            animator.interpolator = AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR
            animator.duration = tabAnimationDuration
            animator.setFloatValues(fraction, targetFraction)
            animator.addUpdateListener {
                val animatorFraction = it.animatedValue as Float
                setStateFraction(animatorFraction)
            }
            animator.doOnEnd {
                setSelectedState(selected)
            }
            animator.start()
        }

        fun updateFraction() {
            setStateFraction(fraction, true)
        }

        fun setStateFraction(fraction: Float, force: Boolean = false) {
            if (this.fraction == fraction && !force) return
            this.fraction = fraction
            log("TabView $position setStateFraction fraction:$fraction")
            val textSize = MathUtils.lerp(tabTextSize, tabTextActiveSize, fraction * expandProgress)
            if (isUseTextSizeChange) {
                if (textView.textSize != textSize) {
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
                }
            } else {
                textView.scaleX = textSize / tabTextSize
                textView.scaleY = textSize / tabTextSize
            }
            val textColor = if (tabTextColor != tabTextActiveColor)
                ArgbEvaluatorCompat.getInstance()
                    .evaluate(fraction, tabTextColor, tabTextActiveColor)
            else tabTextColor
            if (textView.currentTextColor != textColor) {
                textView.setTextColor(textColor)
            }
            if (isToggleBoldText) {
                if (fraction >= 0.5f) textView.typeface = Typeface.DEFAULT_BOLD
                if (fraction < 0.5f) textView.typeface = Typeface.DEFAULT
            }
        }
    }

    class TabLayoutOnPageChangeListener(tabLayout: MSlidingTabLayout) : OnPageChangeListener {
        private val tabLayoutRef: WeakReference<MSlidingTabLayout> = WeakReference(tabLayout)
        private var previousScrollState = 0
        private var scrollState = 0
        override fun onPageScrollStateChanged(state: Int) {
            previousScrollState = scrollState
            scrollState = state
        }

        override fun onPageScrolled(
            position: Int, positionOffset: Float, positionOffsetPixels: Int
        ) {
            val tabLayout = tabLayoutRef.get() ?: return
            // Only update the text selection if we're not settling, or we are settling after
            // being dragged
            val updateText = scrollState != ViewPager.SCROLL_STATE_SETTLING
                || previousScrollState == ViewPager.SCROLL_STATE_DRAGGING
            //不是点击状态
            val updateIndicator = !(scrollState == ViewPager.SCROLL_STATE_SETTLING
                && previousScrollState == ViewPager.SCROLL_STATE_IDLE)
            log("onPageScrolled position:$position, positionOffset:$positionOffset" +
                " scrollState:$scrollState, update:$updateText, updateIndicator:$updateIndicator")
            tabLayout.setScrollPosition(position, positionOffset, updateText, updateIndicator)
        }

        override fun onPageSelected(position: Int) {
            val tabLayout = tabLayoutRef.get() ?: return
            if ((tabLayout.getSelectedPosition() == position || position >= tabLayout.getTabCount())) {
                return
            }
            // Select the tab, only updating the indicator if we're not being dragged/settled
            // (since onPageScrolled will handle that).
            val updateIndicator = (scrollState == ViewPager.SCROLL_STATE_IDLE
                || (scrollState == ViewPager.SCROLL_STATE_SETTLING
                && previousScrollState == ViewPager.SCROLL_STATE_IDLE))
            log("onPageSelected position:$position, updateIndicator:$updateIndicator")
            tabLayout.selectTab(position, updateIndicator)
        }
    }

    private inner class AdapterChangeListener internal constructor() : OnAdapterChangeListener {
        override fun onAdapterChanged(
            viewPager: ViewPager,
            oldAdapter: PagerAdapter?,
            newAdapter: PagerAdapter?
        ) {
            if (this@MSlidingTabLayout.viewPager == viewPager) {
                setPagerAdapter(newAdapter)
            }
        }
    }

    private inner class AdapterDataSetObserver : DataSetObserver() {
        override fun onChanged() {
            super.onChanged()
            populateFromPagerAdapter()
        }

        override fun onInvalidated() {
            super.onInvalidated()
            populateFromPagerAdapter()
        }
    }

    private class ViewPagerTabSelectListener(val viewPager: ViewPager) : TabSelectListener {
        override fun onTabSelected(tabView: TabView) {
            viewPager.currentItem = tabView.position
        }

        override fun onTabUnSelected(tabView: TabView) = Unit
    }

    interface TabSelectListener {
        fun onTabSelected(tabView: TabView)
        fun onTabUnSelected(tabView: TabView)
    }

    enum class ScrollState {
        IDLE,
        TOUCH_SCROLL,
        FLING
    }

    interface OnScrollListener {
        fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int)
        fun onStateChanged(state: ScrollState)
    }

    private fun Int.dp() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(), context.resources.displayMetrics).toInt()

    companion object {
        const val TAG = "MSlidingTabLayout"

        fun log(msg: String) {
            Log.d(TAG, "$msg")
        }
    }
}
