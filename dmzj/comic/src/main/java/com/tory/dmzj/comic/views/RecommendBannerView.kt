package com.tory.dmzj.comic.views

import android.content.Context
import android.util.AttributeSet
import com.shizhuang.duapp.common.component.module.IModuleView
import com.shizhuang.duapp.modules.rn.views.banner.BannerModel
import com.tory.dmzj.comic.model.RecommendBannerModel
import com.tory.library.widget.banner.BannerView

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
class RecommendBannerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BannerView(context, attrs, defStyleAttr), IModuleView<RecommendBannerModel> {

    init {
        setRatio(400f/750)
    }

    override fun update(model: RecommendBannerModel) {
        val banners = model.list?.map { BannerModel(
            url = it.cover.orEmpty(),
            title = it.title
        ) }.orEmpty()
        setBanners(banners)
    }
}
