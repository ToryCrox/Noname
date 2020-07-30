package com.tory.demo.demo

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MAppBarLayoutBehavior
import kotlinx.android.synthetic.main.activity_main_1.*
import kotlinx.android.synthetic.main.layout_item_test.view.*

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/7/10
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/7/10 xutao 1.0
 * Why & What is modified:
 */
class MainActivity : AppCompatActivity() {

    var appbarBehavior : MAppBarLayoutBehavior? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_1)

        val pages = arrayOf("推荐", "球鞋", "数码", "手表", "服装", "推荐", "球鞋", "数码", "手表", "服装")

        slidingTabLayout.setupWithViewPager(viewPager)
        viewPager.adapter = object : PagerAdapter(){

            override fun getCount(): Int = pages.size

            override fun getPageTitle(position: Int): CharSequence? {
                return pages[position]
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val view = LayoutInflater.from(container.context).inflate(R.layout.layout_item_test,
                    container, false)
                val textView = view.textView
                textView.setText(getPageTitle(position))
                textView.gravity = Gravity.CENTER_HORIZONTAL
                container.addView(view, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
                return view
            }

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as View)
            }

            override fun isViewFromObject(view: View, obj : Any): Boolean {
                return obj  == view
            }
        }

        appbar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener{
            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                val total = appBarLayout.totalScrollRange
                Log.v("AppBarLayout", "verticalOffset:$verticalOffset  total:$total")

                val fraction = 1- Math.abs(verticalOffset).toFloat() / (if (total > 0) total else 60.dp())
                slidingTabLayout.expandProgress = fraction

            }
        })


        slidingTabLayout.addOnScrollListener(object : MSlidingTabLayout.OnScrollListener{
            override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
            }

            override fun onStateChanged(state: MSlidingTabLayout.ScrollState) {
                Log.v("slidingTabLayout", "ScrollState:" + state)
            }
        })
        val behavior = (appbar.layoutParams as CoordinatorLayout.LayoutParams).behavior
        if (behavior is MAppBarLayoutBehavior){
            appbarBehavior = behavior
        }
        slidingTabLayout.addTabSelectListener(object : MSlidingTabLayout.TabSelectListener{
            override fun onTabSelected(tabView: MSlidingTabLayout.TabView) {
                Log.v("TabSelectListener", "isExpand :" + (tabView.position == 0))
                val isFirstPage = tabView.position == 0
                appbarBehavior?.isExpandable = isFirstPage
                //appbar.setExpanded(isFirstPage)
            }

            override fun onTabUnSelected(tabView: MSlidingTabLayout.TabView) {
            }
        })
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            var scrollState: Int = 0

            override fun onPageScrollStateChanged(state: Int) {
                scrollState = state
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if (position == 0){
                    val start = SystemClock.elapsedRealtime()
                    val offset = -positionOffset * appbar.totalScrollRange
                    Log.v("MMMAAA", "onPageScrolled position:$position," +
                        " positionOffset:$positionOffset, offset:$offset, scrollState:$scrollState")
                    appbarBehavior?.setAppbarOffset(coordinator, appbar, offset.toInt())
                    Log.v("MMMAAA", "onPageScrolled: time:" + (SystemClock.elapsedRealtime() - start))
                }
            }
            override fun onPageSelected(position: Int) {

            }
        })
    }

    private fun Int.dp() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(), resources.displayMetrics).toInt()

}
