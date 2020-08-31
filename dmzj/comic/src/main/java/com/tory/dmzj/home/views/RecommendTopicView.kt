package com.tory.dmzj.home.views

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.shizhuang.duapp.common.component.module.AbsModuleView
import com.tory.dmzj.home.R
import com.tory.dmzj.home.glide.GlideTransforms
import com.tory.dmzj.home.model.RecommendComicModel
import com.tory.dmzj.home.model.RecommendTopicModel
import kotlinx.android.synthetic.main.view_recommend_topic.view.*

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
class RecommendTopicView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AbsModuleView<RecommendTopicModel>(context, attrs, defStyleAttr) {


    init {
        itemCover.setImageRatio(0.5f)
    }

    override fun getLayoutId(): Int = R.layout.view_recommend_topic

    override fun onChanged(model: RecommendTopicModel) {
        super.onChanged(model)
        Glide.with(this)
            .load(model.cover)
            .transform(CenterCrop(), GlideTransforms.round)
            .into(itemCover)

        itemTitle.text = model.title
    }
}
