package com.tory.demo.jetpack.views

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.shizhuang.duapp.common.component.module.AbsModuleView
import com.tory.demo.jetpack.R
import com.tory.demo.jetpack.model.GankItem
import kotlinx.android.synthetic.main.item_gank.view.*
import java.util.regex.Pattern

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
class GankItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AbsModuleView<GankItem>(context, attrs, defStyleAttr) {

    override fun getLayoutId(): Int = R.layout.item_gank

    override fun onChanged(model: GankItem) {
        super.onChanged(model)
        val hasImage = model.url?.endsWith(".jpg") == true
        iv_img.isVisible = hasImage
        tv_desc.isVisible = !hasImage
        if (hasImage) {
            Glide.with(this)
                .load(model.url)
                .into(iv_img)
        } else {
            tv_desc.text = model.desc
        }

        tv_source.text = model.source
        tv_people.text = model.who + matchGithub(model.url)
        tv_time.text = model.publishedAt?.substring(0, 10)
        tv_tag.text = model.type

        setOnClickListener {

        }
    }

    companion object {
        var sGitHubPattern = Pattern.compile("github\\.com")
        private fun matchGithub(url: String?): String? {
            return if (sGitHubPattern.matcher(url.orEmpty()).find()) " GitHub" else ""
        }
    }
}
