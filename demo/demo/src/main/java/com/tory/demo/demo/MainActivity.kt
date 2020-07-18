package com.tory.demo.demo

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.PersistableBundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.viewpager.widget.PagerAdapter
import com.tory.demo.demo.R
import kotlinx.android.synthetic.main.activity_main.*
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

        val pages = arrayOf("推荐", "球鞋", "数码", "手表", "服装")

        viewPager.adapter = object : PagerAdapter(){

            override fun getCount(): Int = pages.size

            override fun getPageTitle(position: Int): CharSequence? {
                return pages[position]
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val textView = AppCompatTextView(container.context)
                textView.setText(getPageTitle(position))
                textView.gravity = Gravity.CENTER
                container.addView(textView, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
                return textView
            }

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as View)
            }

            override fun isViewFromObject(view: View, obj : Any): Boolean {
                return obj  == view
            }
        }
    }

}
