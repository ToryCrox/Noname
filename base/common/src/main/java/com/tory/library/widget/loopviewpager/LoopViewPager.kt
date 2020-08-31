/*
 * Copyright (C) 2013 Leszek Mzyk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shizhuang.duapp.modules.rn.views.loopviewpager

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2

open class LoopViewPager @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ViewPager(context, attrs) {
    private var mAdapter: LoopPagerAdapterWrapper? = null
    private var mBoundaryCaching = DEFAULT_BOUNDARY_CASHING
    private var mBoundaryLooping = DEFAULT_BOUNDARY_LOOPING
    private val mOnPageChangeListeners: MutableList<OnPageChangeListener> = mutableListOf()
    private var mPosition = 0
    private var isAttached = true
    private var mManualControl = false
    private var isCanAutoScroll = true
    var scrollInterval: Long = DEFAULT_SCROLL_INTERVAL
        set(value) {
            if (value > 500L){
                field = value
            }
        }
    var isScrollEnabled = true

    /**
     * 是否正在自动滚动
     *
     * @return
     */
    private var isScrolling = false

    private val onPageChangeListener = MOnPageChangeListener()

    init {
        super.addOnPageChangeListener(onPageChangeListener)

    }

    /**
     * If set to true, the boundary views (i.e. first and last) will never be
     * destroyed This may help to prevent "blinking" of some views
     */
    fun setBoundaryCaching(flag: Boolean) {
        mBoundaryCaching = flag
        mAdapter?.setBoundaryCaching(flag)
    }

    fun setBoundaryLooping(flag: Boolean) {
        mBoundaryLooping = flag
        mAdapter?.setBoundaryLooping(flag)
    }

    override fun setAdapter(adapter: PagerAdapter?) {
        adapter ?: return
        mAdapter = LoopPagerAdapterWrapper(adapter)
        mAdapter?.setBoundaryCaching(mBoundaryCaching)
        mAdapter?.setBoundaryLooping(mBoundaryLooping)
        super.setAdapter(mAdapter)
        setCurrentItem(0, false)
    }

    override fun getAdapter(): PagerAdapter? {
        return mAdapter?.realAdapter ?: mAdapter
    }

    override fun getCurrentItem(): Int {
        return mAdapter?.toRealPosition(super.getCurrentItem()) ?: 0
    }

    override fun setCurrentItem(item: Int, smoothScroll: Boolean) {
        val realItem = mAdapter?.toInnerPosition(item) ?: 0
        super.setCurrentItem(realItem, smoothScroll)
    }

    override fun setCurrentItem(item: Int) {
        if (currentItem != item) {
            setCurrentItem(item, true)
        }
    }

    override fun setOnPageChangeListener(listener: OnPageChangeListener) {
        addOnPageChangeListener(listener)
    }

    override fun addOnPageChangeListener(listener: OnPageChangeListener) {
        mOnPageChangeListeners.add(listener)
    }

    override fun removeOnPageChangeListener(listener: OnPageChangeListener) {
        mOnPageChangeListeners.remove(listener)
    }

    override fun clearOnPageChangeListeners() {
        mOnPageChangeListeners.clear()
    }



    inner class MOnPageChangeListener: OnPageChangeListener {
        private var mPreviousOffset = -1f
        private var mPreviousPosition = -1f
        override fun onPageSelected(position: Int) {
            val realPosition = mAdapter?.toRealPosition(position) ?: 0
            mPosition = realPosition
            if (mPreviousPosition != realPosition.toFloat()) {
                mPreviousPosition = realPosition.toFloat()
                mOnPageChangeListeners.forEach {
                    it.onPageSelected(realPosition)
                }
            }
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            var realPosition = position
            val adapter = mAdapter ?: return
            realPosition = adapter.toRealPosition(position)
            if (positionOffset == 0f && mPreviousOffset == 0f && (position == 0
                            || position == adapter.count - 1)) {
                setCurrentItem(realPosition, false)
            }
            mPreviousOffset = positionOffset
            for (listener in mOnPageChangeListeners) {
                if (realPosition != adapter.realCount - 1) {
                    listener.onPageScrolled(realPosition, positionOffset,
                            positionOffsetPixels)
                } else {
                    if (positionOffset > 0.5) {
                        listener.onPageScrolled(0, 0f, 0)
                    } else {
                        listener.onPageScrolled(realPosition, 0f, 0)
                    }
                }
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            val adapter = mAdapter ?: return
            val position = super@LoopViewPager.getCurrentItem()
            val realPosition = adapter.toRealPosition(position)
            if (state == SCROLL_STATE_IDLE && (position == 0
                            || position == adapter.count - 1)) {
                setCurrentItem(realPosition, false)
            }

            mOnPageChangeListeners.forEach {
                it.onPageScrollStateChanged(state)
            }
        }
    }

    /**
     * 设置其是否能滑动换页
     *
     * @param isCanScroll false 不能换页， true 可以滑动换页
     */
    fun setCanAutoScroll(isCanScroll: Boolean) {
        this.isCanAutoScroll = isCanScroll
    }

    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == MESSAGE_SCROLL) {
                mPosition++
                setCurrentItem(mPosition, true)
                this.sendEmptyMessageDelayed(MESSAGE_SCROLL, scrollInterval)
            }
        }
    }

    fun stopScroll() {
//        if (!isCanAutoScroll) {
//            return
//        }
        mHandler.removeMessages(MESSAGE_SCROLL)
        isScrolling = false
    }

    fun startScroll() {
        if (!isCanAutoScroll || !isAttached || mManualControl) {
            return
        }
        stopScroll()
        val pageSize = adapter?.count ?: 0
        if (pageSize > 1) {
            mHandler.sendEmptyMessageDelayed(MESSAGE_SCROLL, 3 * 1000.toLong())
            isScrolling = true
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return isScrollEnabled && super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return isScrollEnabled && super.onTouchEvent(ev)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> stopScroll()
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> startScroll()
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isAttached = true
        startScroll()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isAttached = false
        stopScroll()
    }

    /**
     * 手动控制的话，必须在生命周期内开始动画
     *
     * @param manualControl
     */
    fun setManualControl(manualControl: Boolean) {
        mManualControl = manualControl
    }

    companion object {
        private const val TAG = "LoopViewPager"
        private const val DEFAULT_BOUNDARY_CASHING = false
        private const val DEFAULT_BOUNDARY_LOOPING = true
        private const val DEFAULT_SCROLL_INTERVAL = 3 * 1000L

        private val MESSAGE_SCROLL = 1000
    }
}
