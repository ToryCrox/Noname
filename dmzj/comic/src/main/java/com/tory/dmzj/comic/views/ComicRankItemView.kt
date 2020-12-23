package com.tory.dmzj.comic.views

import android.content.Context
import android.util.AttributeSet
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.tory.library.component.base.AbsModuleView
import com.tory.dmzj.comic.R
import com.tory.dmzj.comic.model.ComicRankItemModel
import com.tory.dmzj.dbase.RouterTable
import com.tory.dmzj.dbase.glide.GlideTransforms
import com.tory.library.log.LogUtils
import com.tory.library.utils.DateUtils
import kotlinx.android.synthetic.main.view_comic_rank_item.view.*

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
class ComicRankItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AbsModuleView<ComicRankItemModel>(context, attrs, defStyleAttr) {
    override fun getLayoutId(): Int {
        return R.layout.view_comic_rank_item
    }

    override fun onChanged(model: ComicRankItemModel) {
        super.onChanged(model)

        LogUtils.d("ComicDetailHeaderView cover:${model.cover}")
        Glide.with(this)
            .load(model.cover)
            .transform(CenterCrop(), GlideTransforms.round)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(itemCover)
        itemTitle.text = model.title
        itemAuthors.text = model.authors
        itemTypes.text = model.types
        val updateStr = DateUtils.format("yyyy-MM-dd",
            model.lastUpdatetime * 1000L)
        itemLastTime.text = "$updateStr ${model.status}"
        itemRankIndex.text = model.rankIndex.toString()

        setOnClickListener {
            ARouter.getInstance()
                .build(RouterTable.COMIC_DETAIL_PAGE)
                .withInt("id", model.comicId)
                .withString("detailTitle", model.title)
                .navigation(context)
        }
    }
}
