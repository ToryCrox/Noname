package com.tory.dmzj.agallery.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.shizhuang.duapp.common.component.module.AbsModuleView
import com.tory.dmzj.agallery.R
import com.tory.dmzj.agallery.ui.dialog.TagPanelDialog
import com.tory.dmzj.agallery.ui.model.GalleryImageModel
import com.tory.dmzj.dbase.gallery.GalleryTagItemModel
import com.tory.dmzj.dbase.gallery.GalleryTagListModel
import com.tory.library.model.PicItemModel
import com.tory.library.ui.pics.PicsHelper
import kotlinx.android.synthetic.main.view_gallery_image_item.view.*

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
class GalleryImageView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AbsModuleView<GalleryImageModel>(context, attrs, defStyleAttr) {


    override fun getLayoutId(): Int {
        return R.layout.view_gallery_image_item
    }

    override fun onChanged(model: GalleryImageModel) {
        super.onChanged(model)

        itemImage.setImageRatio(if (model.previewWidth > 0)
            model.previewHeight.toFloat() / model.previewWidth else 1f)
        val thumbnailRequest = if (!model.previewUrl.isNullOrEmpty()) {
            Glide.with(this)
                    .load(model.previewUrl)
        } else null
        Glide.with(this)
                .load(model.sampleUrl)
                .thumbnail(thumbnailRequest)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(itemImage)

        setOnClickListener {
            val items = model.allImages.orEmpty().map {
                PicItemModel(it.sampleUrl, previewUrl = it.previewUrl)
            }
            PicsHelper.launch(context, items, model.index)
        }

        setOnLongClickListener {
            val tags = model.tags.split(" ", ignoreCase = true).map {
                GalleryTagItemModel(it)
            }
            val model = GalleryTagListModel(title = "", list = tags)
            TagPanelDialog.newInstance(model).show((context as FragmentActivity).supportFragmentManager,
                    "TagPanelDialog")

            return@setOnLongClickListener true
        }
    }
}
