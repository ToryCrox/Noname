package com.tory.library.widget.banner

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.tory.library.extension.addLifecycleObserver
import com.tory.library.widget.loopviewpager.LoopViewPager
import com.tory.library.log.LogUtils
import java.util.*

/**
 * Author: xutao
 * Version V1.0
 * Date: 2019-09-27
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2019-09-27 xutao 1.0
 * Why & What is modified:
 */
open class BannerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr),
    BannerAdapter.BannerListener, ViewPager.OnPageChangeListener, LifecycleObserver {
    private var mRatio = 0f
    private val mBanners = ArrayList<BannerModel>()
    private var mBannerListener: BannerListener? = null
    val viewpager = LoopViewPager(context)

    init {
        addView(viewpager, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        addLifecycleObserver()
        setRatio(1 / 3f)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        viewpager.addOnPageChangeListener(this)
        viewpager.setBoundaryCaching(true)//需要设置，否则会在最前面和最后面时会闪烁
    }

    fun setBannerListener(bannerListener: BannerListener) {
        this.mBannerListener = bannerListener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (mRatio > 0) {
            val width = MeasureSpec.getSize(widthMeasureSpec)
            val height = (width * mRatio).toInt()
            super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY))
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        this.requestLayout()
        viewpager.requestLayout()//第一次requestLayout在onAttachedToWindow之前，需要requestLayout，否则不显示
    }

    /**
     * 设置指示器的方向
     *
     * @param indicatorPosition
     */
    fun setIndicatorPosition(indicatorPosition: String) {
    }

    fun setRatio(ratio: Float) {
        this.mRatio = ratio
    }

    /**
     * 设置方向
     * @param models
     */
    fun setBanners(models: List<BannerModel>) {
        if (mBanners == models) {
            LogUtils.d(TAG, "setBanners banners is not changed")
            return
        }
        LogUtils.d(TAG, "setBanners $models")
        mBanners.clear()
        mBanners.addAll(models)
        viewpager.adapter = BannerAdapter(mBanners.size, this)
        viewpager.setCanAutoScroll(models.size > 1)
    }

    override fun onItemClick(view: View, position: Int) {
        if (position < 0 || position >= mBanners.size) {
            return
        }
        val model = mBanners[position]
        mBannerListener?.onSelected(position, model)
    }

    override fun createImageView(context: Context, position: Int): ImageView {
        val imageView = ImageView(context)
        val url = mBanners.getOrNull(position)?.url
        LogUtils.d(TAG, "createImageView position=$position, url:$url")
        Glide.with(this)
            .load(url)
            .centerCrop()
            .into(imageView)
        return imageView
    }

    override fun onPageScrolled(i: Int, v: Float, i1: Int) {
    }

    override fun onPageSelected(position: Int) {
        LogUtils.d(TAG, "onPageSelected position=$position")
        mBannerListener?.onIndexChanged(position)
    }

    override fun onPageScrollStateChanged(i: Int) {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        val canScroll = mBanners.size > 1
        viewpager.setCanAutoScroll(canScroll)
        if (canScroll) {
            viewpager.startScroll()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onHostPause() {
        viewpager.stopScroll()
        viewpager.setCanAutoScroll(false)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
    }

    interface BannerListener {
        fun onIndexChanged(index: Int)
        fun onSelected(index: Int, model: BannerModel)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        parent?.requestDisallowInterceptTouchEvent(true)
        return super.dispatchTouchEvent(ev)
    }
    companion object {
        private const val TAG = "BannerView"
    }
}
