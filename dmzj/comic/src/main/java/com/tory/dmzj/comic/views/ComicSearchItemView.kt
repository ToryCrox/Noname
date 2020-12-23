package com.tory.dmzj.comic.views

import android.content.Context
import android.util.AttributeSet
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.tory.library.component.base.AbsModuleView
import com.tory.dmzj.comic.R
import com.tory.dmzj.comic.model.ComicSearchItemModel
import com.tory.dmzj.dbase.RouterTable
import com.tory.dmzj.dbase.glide.GlideTransforms
import com.tory.library.log.LogUtils
import kotlinx.android.synthetic.main.view_comic_search_item.view.*

/**
 * @author tory
 * @create 2020/9/4
 * @Describe
 */
class ComicSearchItemView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AbsModuleView<ComicSearchItemModel>(context, attrs, defStyleAttr) {

    override fun getLayoutId(): Int {
        return R.layout.view_comic_search_item
    }

    override fun onChanged(model: ComicSearchItemModel) {
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
        itemLastName.text = model.lastName

        setOnClickListener {
            ARouter.getInstance()
                    .build(RouterTable.COMIC_DETAIL_PAGE)
                    .withInt("id", model.id)
                    .withString("detailTitle", model.title)
                    .navigation(context)
        }
    }
}
