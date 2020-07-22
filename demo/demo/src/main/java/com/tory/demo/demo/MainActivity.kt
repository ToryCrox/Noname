package com.tory.demo.demo

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.PersistableBundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.tory.demo.demo.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_item_test.view.*
import java.io.File

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


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
                Log.v("AppBarLayout", "verticalOffset:$verticalOffset")
                val fraction = 1- Math.abs(verticalOffset).toFloat() / 60.dp()
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
    }

    private fun Int.dp() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(), resources.displayMetrics).toInt()

}
