package com.tory.dmzj.comic.views

import android.content.Context
import android.util.AttributeSet
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.tory.library.component.base.AbsModuleView
import com.tory.dmzj.comic.R
import com.tory.dmzj.comic.model.ComicDetailHeaderModel
import com.tory.dmzj.dbase.glide.GlideTransforms
import com.tory.library.log.LogUtils
import com.tory.library.utils.DateUtils
import kotlinx.android.synthetic.main.view_comic_detail_header.view.*

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/8/31
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/8/31 xutao 1.0
 * Why & What is modified:
 */
class ComicDetailHeaderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AbsModuleView<ComicDetailHeaderModel>(context, attrs, defStyleAttr) {

    override fun getLayoutId(): Int {
        return R.layout.view_comic_detail_header
    }

    override fun onChanged(model: ComicDetailHeaderModel) {
        super.onChanged(model)
        LogUtils.d("ComicDetailHeaderView cover:${model.cover}")
        Glide.with(this)
            .load(model.cover)
            .transform(CenterCrop(), GlideTransforms.round)
            .into(itemCover)

        itemId.text = model.id.toString()
        itemAuthors.text = model.authors.joinToString(" ") { it.tagName.orEmpty() }
        itemTypes.text = model.types.joinToString(" ") { it.tagName.orEmpty() }
        itemHot.text = "人气: ${model.hotNum}"
        itemSubscribe.text= "订阅: ${model.subscribeNum}"

        val updateStr = DateUtils.format("yyyy-MM-dd",
            model.lastUpDatetime * 1000L)
        itemLastUpDate.text = "$updateStr ${model.status.joinToString (" "){ it.tagName.orEmpty() }}"
    }

}
