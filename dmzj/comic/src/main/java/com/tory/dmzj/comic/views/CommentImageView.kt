package com.tory.dmzj.comic.views

import android.content.Context
import android.util.AttributeSet
import com.bumptech.glide.Glide
import com.shizhuang.duapp.common.component.module.AbsModuleView
import com.tory.dmzj.comic.R
import com.tory.dmzj.comic.model.CommentImageModel
import com.tory.dmzj.comic.model.CommentImagesModel
import com.tory.library.log.LogUtils
import kotlinx.android.synthetic.main.view_comment_image.view.*

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
class CommentImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AbsModuleView<CommentImageModel>(context, attrs, defStyleAttr) {
    override fun getLayoutId(): Int {
        return R.layout.view_comment_image
    }

    override fun onChanged(model: CommentImageModel) {
        super.onChanged(model)
        LogUtils.d("CommentImageView image:${model.getImageUrl(true)}")
        itemImage.setImageRatio(1f)
        Glide.with(this)
            .load(model.getImageUrl(true))
            .into(itemImage)
    }


}
