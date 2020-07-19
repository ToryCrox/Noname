package com.tory.demo.demo

import android.content.Context
import android.database.DataSetObserver
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnAdapterChangeListener
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
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
    private var tabBackground: Drawable = ColorDrawable(Color.TRANSPARENT)

    private var tabPaddingStart: Int = 0
    private var tabPaddingTop: Int = 0
    private var tabPaddingEnd: Int = 0
    private var tabPaddingBottom: Int = 0

    private var tabIndicatorFitWidth: Boolean = true
    private var tabIndicatorColor: Int = Color.BLACK
    private var tabIndicatorWidth: Int = 0
    private var tabIndicatorHeight: Int = 0

    private var slidingTabIndicator: SlidingTabIndicator = SlidingTabIndicator(context)

    private var viewPager: ViewPager? = null
    private var pagerAdapter :PagerAdapter? = null

    private val pageChangeListener: TabLayoutOnPageChangeListener by lazy { TabLayoutOnPageChangeListener(this)}
    private val adapterDataSetObserver: AdapterDataSetObserver by lazy { AdapterDataSetObserver() }
    private val adapterChangeListener: AdapterChangeListener by lazy { AdapterChangeListener()}

    private val tabSelectListeners : MutableList<TabSelectListener> = mutableListOf()
    private var vpSelectListener: TabSelectListener? = null
    private val tabViews: MutableList<TabView> = mutableListOf()
    private var selectedTabView: TabView? = null

    init {
        // Disable the Scroll Bar
        isHorizontalScrollBarEnabled = false

        val a = context.obtainStyledAttributes(attrs, R.styleable.MSlidingTabLayout,
            0, R.style.DefaultSlidTabLayout)
        tabTextSize = a.getDimension(R.styleable.MSlidingTabLayout_tab_textSize, 0f)
        tabTextActiveSize = a.getDimension(R.styleable.MSlidingTabLayout_tab_textActiveSize, 0f)
        tabTextColor = a.getColor(R.styleable.MSlidingTabLayout_tab_textColor, Color.BLACK)
        tabTextActiveColor = a.getColor(R.styleable.MSlidingTabLayout_tab_textActiveColor, Color.BLACK)
        tabBackground = a.getDrawable(R.styleable.MSlidingTabLayout_tab_background) ?: tabBackground

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

    fun addTabSelectListener(listener: TabSelectListener){
        if (!tabSelectListeners.contains(listener)){
            tabSelectListeners.add(listener)
        }
    }

    fun removeTabSelectListener(listener: TabSelectListener){
        tabSelectListeners.remove(listener)
    }

    fun setupWithViewPager(viewPager: ViewPager){
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
    private fun populateFromPagerAdapter(){
        removeAllTabs()
        tabViews.clear()
        val adapter = pagerAdapter ?: return
        val count = adapter.count
        for(index : Int in 0 until count){
            val tabView = createTabView(index)
            tabView.text = adapter.getPageTitle(index)
            slidingTabIndicator.addView(tabView)
            tabViews.add(tabView)
        }
    }

    private fun removeAllTabs() {
        slidingTabIndicator.removeAllViews()
    }


    private fun createTabView(index: Int): TabView{
        return TabView(context, index)
    }

    private fun getTabAt(index: Int): TabView?{
        return tabViews.getOrNull(index)
    }

    fun setScrollPosition(
            position: Int,
            positionOffset: Float,
            updateSelectedText: Boolean,
            updateIndicatorPosition: Boolean) {
        val roundedPosition = Math.round(position + positionOffset)
        if (roundedPosition < 0 || roundedPosition >= slidingTabIndicator.childCount) {
            return
        }



        //scrollTo(calculateScrollXForTab(position, positionOffset), 0)

        if (updateSelectedText){
            val tabView = getTabAt(position) ?: return
            val nextTabView = getTabAt(position + 1)
            tabView.setFraction(1 - positionOffset)
            nextTabView?.setFraction(positionOffset)
        }
    }

    private fun dispatchTabSelected(tabView: TabView){
        tabSelectListeners.forEach { it.onTabSelected(tabView) }
    }

    private fun dispatchTabUnSelected(tabView: TabView){
        tabSelectListeners.forEach { it.onTabUnSelected(tabView) }
    }

    private fun selectTab(position: Int, updateIndicator: Boolean = true){
        val tabView = tabViews.getOrNull(position) ?: return
        val currentTabView = selectedTabView
        if (currentTabView == tabView) return
        if (currentTabView != null){
            currentTabView.isSelected = false
            currentTabView.setFraction(0f)
            dispatchTabUnSelected(currentTabView)
        }
        selectedTabView = tabView
        tabView.isSelected = true
        tabView.setFraction(1f)
        dispatchTabSelected(tabView)
    }


    inner class SlidingTabIndicator(context: Context) : LinearLayout(context) {

        init {
            setWillNotDraw(false)
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

    }

    inner class TabView(context: Context, val position: Int) : FrameLayout(context){

        val textView = AppCompatTextView(context)
        private var fraction: Float = -1f
        private val maxTextScale = tabTextActiveSize / tabTextSize

        var text: CharSequence?
            get() = textView.text
            set(value){
                textView.text = value
            }

        init {
            isClickable = true
            background = tabBackground.constantState?.newDrawable()?.mutate() ?: tabBackground
            setPadding(tabPaddingStart, tabPaddingTop, tabPaddingEnd, tabPaddingBottom)
            val lp = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            lp.gravity = Gravity.CENTER
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize)
            addView(textView, lp)
            setOnClickListener {
                Log.d(TAG, "on Click position:$position, text:$text")
                selectTab(position)
            }
            setFraction(0f)
        }

        override fun setSelected(selected: Boolean) {
            val changed = isSelected != selected
            super.setSelected(selected)
            textView.isSelected = selected
        }

        fun setFraction(fraction: Float){
            if (this.fraction == fraction) return
            this.fraction = fraction
            val textSize = MathUtils.lerp(tabTextSize, tabTextActiveSize, fraction)
            if (textView.textSize != textSize){
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            }
            val textColor = if (tabTextColor != tabTextActiveColor)
                ArgbEvaluatorCompat.getInstance().evaluate(fraction, tabTextColor, tabTextActiveColor)
            else tabTextColor
            if (textView.currentTextColor != textColor){
                textView.setTextColor(textColor)
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
                position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            val tabLayout = tabLayoutRef.get() ?: return

            // Only update the text selection if we're not settling, or we are settling after
            // being dragged
            val updateText = scrollState != ViewPager.SCROLL_STATE_SETTLING
                    || previousScrollState == ViewPager.SCROLL_STATE_DRAGGING
            //不是点击状态
            val updateIndicator = !(scrollState == ViewPager.SCROLL_STATE_SETTLING
                    && previousScrollState == ViewPager.SCROLL_STATE_IDLE)
            Log.v(TAG, "onPageScrolled position:$position, positionOffset:$positionOffset scrollState:$scrollState, " +
                    "update:$updateText, updateIndicator:$updateIndicator")
            tabLayout.setScrollPosition(position, positionOffset, updateText, updateIndicator)
        }

        override fun onPageSelected(position: Int) {
            val tabLayout = tabLayoutRef.get()
            // Update the indicator if we're not settling after being idle. This is caused
            // from a setCurrentItem() call and will be handled by an animation from
            // onPageSelected() instead.
            Log.v(TAG, "onPageScrolled position:$position")
        }

    }

    private inner class AdapterChangeListener internal constructor() : OnAdapterChangeListener {
        override fun onAdapterChanged(
                viewPager: ViewPager,
                oldAdapter: PagerAdapter?,
                newAdapter: PagerAdapter?) {
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

    private class ViewPagerTabSelectListener(val viewPager: ViewPager): TabSelectListener{
        override fun onTabSelected(tabView: TabView) {
            viewPager.currentItem = tabView.position
        }

        override fun onTabUnSelected(tabView: TabView) = Unit
    }


    interface TabSelectListener{
        fun onTabSelected(tabView: TabView)
        fun onTabUnSelected(tabView: TabView)
    }


    companion object{
        const val TAG = "MSlidingTabLayout"
    }

}
