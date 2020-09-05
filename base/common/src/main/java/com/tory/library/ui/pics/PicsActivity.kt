package com.tory.library.ui.pics

import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.ImageViewState
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.tory.library.R
import com.tory.library.base.BaseActivity
import com.tory.library.component.CommonViewHolder
import com.tory.library.extension.inflate
import com.tory.library.model.PicItemModel
import com.tory.library.model.PicsModel
import com.tory.library.utils.SystemBarUtils
import kotlinx.android.synthetic.main.activity_pics.*
import kotlinx.android.synthetic.main.item_subsampling_scale_image_view.*
import kotlinx.android.synthetic.main.item_subsampling_scale_image_view.view.*
import java.io.File

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/9/5
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/9/5 xutao 1.0
 * Why & What is modified:
 */
class PicsActivity: BaseActivity() {

    private val picItems = mutableListOf<PicItemModel>()
    private var startIndex = 0

    override fun getLayoutId(): Int = R.layout.activity_pics

    override fun setThemeColor() {
        super.setThemeColor()
        SystemBarUtils.setRealFullUi(window)
    }

    override fun initView(savedInstanceState: Bundle?) {
        val picsModel = intent?.getParcelableExtra<PicsModel>(PicsHelper.KEY_ARGS_PICS)
        if (picsModel == null || picsModel.items.isEmpty()) {
            finish()
            return
        }

        val (items, index) = picsModel

        viewPager.adapter = PicsViewPageAdapter(items)
//        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
//            override fun onPageSelected(position: Int) {
//                super.onPageSelected(position)
//                pageIndexText.text = "${position + 1}/${items.size}"
//            }
//        })
        viewPager.setCurrentItem(index, false)
        pageIndexText.text = "${index + 1}/${items.size}"
        //viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
    }

    class PicsViewPageAdapter(val picItems: List<PicItemModel>): PagerAdapter() {

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun getCount(): Int = picItems.size

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val picItem = picItems.get(position)
            val view = container.inflate(R.layout.item_subsampling_scale_image_view)
            val photoView = view.photoView
            photoView.isEnabled = true
            photoView.isZoomEnabled = true
            Glide.with(photoView).asFile()
                .load(picItem.url)
                .into(object : CustomViewTarget<SubsamplingScaleImageView, File>(photoView) {
                    override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                        photoView.setImage(ImageSource.uri(Uri.fromFile(resource)))
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                    }

                    override fun onResourceCleared(placeholder: Drawable?) {
                    }
                })
            container.addView(view)
            return view

        }
    }



    class PicsPageAdapter(val picItems: List<PicItemModel>): RecyclerView.Adapter<CommonViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommonViewHolder {
            return CommonViewHolder(parent.inflate(R.layout.item_subsampling_scale_image_view))
        }

        override fun getItemCount(): Int = picItems.size

        override fun onBindViewHolder(holder: CommonViewHolder, position: Int) {
            val picItem = picItems.getOrNull(position) ?: return
            val photoView = holder.photoView
            photoView.isEnabled = true
            Glide.with(photoView).asFile()
                .load(picItem.url)
                .into(object : CustomViewTarget<SubsamplingScaleImageView, File>(photoView) {
                    override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                        photoView.setImage(ImageSource.uri(Uri.fromFile(resource)))
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                    }

                    override fun onResourceCleared(placeholder: Drawable?) {
                    }
                })

        }
    }
}
