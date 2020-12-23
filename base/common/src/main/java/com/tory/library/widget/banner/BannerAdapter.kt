package com.tory.library.widget.banner


import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

/**
 * Author:    shaw
 * Version    V1.0
 * Date:      2017/3/10
 * Description:
 * Modification  History:
 * Date          Author        		Version        	Description
 * -----------------------------------------------------------------------------------
 * 2017/3/10       shaw             1.0
 * Why & What is modified:
 */
class BannerAdapter(var size: Int, private val bannerListener: BannerListener?) : androidx.viewpager.widget.PagerAdapter() {

    override fun getCount(): Int {
        return size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun destroyItem(view: ViewGroup, position: Int, `object`: Any) {
        view.removeView(`object` as View)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageView = bannerListener?.createImageView(container.context, position)
                ?: ImageView(container.context)

        imageView.setOnClickListener { view ->
            bannerListener?.onItemClick(view, position)
        }

        container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        return imageView
    }


    interface BannerListener {
        fun onItemClick(view: View, position: Int)
        fun createImageView(context: Context, position: Int): ImageView
    }


}
