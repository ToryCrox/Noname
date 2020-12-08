package com.tory.dmzj.comic.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.math.MathUtils
import com.shizhuang.duapp.common.component.module.IModuleView
import com.shizhuang.duapp.common.extension.dp
import com.shizhuang.duapp.modules.rn.views.banner.BannerModel
import com.tory.dmzj.comic.model.RecommendBannerModel
import com.tory.library.log.LogUtils
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

    val maxScale = 0.9f
    init {
        setRatio(400f/750)

        setPadding(20.dp(), 0, 20.dp(), 0)
        clipToPadding = false
        clipChildren = false
        viewpager.clipChildren = false
        viewpager.clipToPadding = false
        viewpager.offscreenPageLimit = 2

        viewpager.setPageTransformer(false, { page: View, position: Float ->
            val maxPosition = if (viewpager.width > 0) (viewpager.width + viewpager.pageMargin).toFloat() / viewpager.width else 1.0f
            val absFloat = MathUtils.clamp(Math.abs(position), 0f, maxPosition)
            val scale = 1 - (1 - maxScale) * absFloat / maxPosition
            LogUtils.d("RecommendBannerView position:$position, " +
                    "maxPosition:${maxPosition}, scale: $scale, ")
            page.scaleX = scale
            page.scaleY = scale
        })
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = viewpager.measuredWidth
        //viewpager.pageMargin = 8.dp() - (width * (1 - maxScale) / 2).toInt()
        LogUtils.d("RecommendBannerView pageMargin:${viewpager.pageMargin}")
    }

    override fun update(model: RecommendBannerModel) {
        val banners = model.list?.map { BannerModel(
            url = it.cover.orEmpty(),
            title = it.title
        ) }.orEmpty()
        setBanners(banners)
    }
}
