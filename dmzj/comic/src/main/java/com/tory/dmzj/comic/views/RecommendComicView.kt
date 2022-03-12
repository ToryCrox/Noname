package com.tory.dmzj.comic.views

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.isVisible
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.tory.library.component.base.AbsModuleView
import com.tory.dmzj.comic.R
import com.tory.dmzj.comic.model.RecommendComicModel

import com.tory.dmzj.dbase.RouterTable
import com.tory.dmzj.dbase.glide.GlideTransforms
import kotlinx.android.synthetic.main.view_recommend_commic.view.*

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/8/30
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/8/30 xutao 1.0
 * Why & What is modified:
 */
class RecommendComicView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AbsModuleView<RecommendComicModel>(context, attrs, defStyleAttr) {


    init {
        itemCover.setImageRatio(4 / 3f)
    }

    override fun getLayoutId(): Int = R.layout.view_recommend_commic

    override fun onChanged(model: RecommendComicModel) {
        super.onChanged(model)
        Glide.with(this)
            .load(model.cover)
            .transform(CenterCrop(), GlideTransforms.round)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(itemCover)

        itemTitle.text = model.title
        itemSubTitle.isVisible = !model.subTitle.isNullOrEmpty()
        itemSubTitle.text = model.subTitle

        setOnClickListener {
            ARouter.getInstance()
                .build(RouterTable.COMIC_DETAIL_PAGE)
                .withInt("id", model.id)
                .withString("detailTitle", model.title)
                .navigation(context)

        }
    }
}
