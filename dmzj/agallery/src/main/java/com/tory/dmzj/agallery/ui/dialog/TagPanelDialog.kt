package com.tory.dmzj.agallery.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.alibaba.android.arouter.launcher.ARouter
import com.tory.dmzj.agallery.R
import com.tory.dmzj.dbase.gallery.GalleryTagListModel
import com.tory.dmzj.dbase.RouterTable

import com.tory.library.base.BaseBottomSheetDialogFragment
import com.tory.library.extension.click
import com.tory.library.extension.inflate
import kotlinx.android.synthetic.main.dialog_gallery_tag_panel.*
import kotlinx.android.synthetic.main.item_gallery_tag.view.*

/**
 * @author tory
 * @create 2020/9/9
 * @Describe
 */
class TagPanelDialog : BaseBottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_gallery_tag_panel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val model: GalleryTagListModel = arguments?.getParcelable<GalleryTagListModel>(KEY_MODEL)
                ?: return

        model.list.forEach { item ->
            val tagView = tagPanelLayout.inflate(R.layout.item_gallery_tag, false)
            tagView.itemTagBtn.text = item.text
            tagPanelLayout.addView(tagView)

            tagView.click {
                ARouter.getInstance().build(RouterTable.AGALLERY_LIST)
                        .withString("galleryTag", item.text)
                        .navigation(requireActivity())
            }
        }
    }

    companion object {

        const val KEY_MODEL = "key_model"

        fun newInstance(model: GalleryTagListModel): TagPanelDialog {
            return TagPanelDialog().also {
                it.arguments = bundleOf(KEY_MODEL to model)
            }
        }
    }
}