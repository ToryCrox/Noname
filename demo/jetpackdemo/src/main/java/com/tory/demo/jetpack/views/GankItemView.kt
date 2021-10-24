package com.tory.demo.jetpack.views

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.tory.demo.jetpack.hilt.HiltDemoActivity
import com.tory.demo.jetpack.R
import com.tory.demo.jetpack.event.HitEvent
import com.tory.demo.jetpack.model.GankItem
import com.tory.library.component.base.AbsModuleView
import com.tory.library.component.base.groupPosition
import com.tory.library.log.LogUtils
import com.tory.library.utils.livebus.LiveEventBus
import com.tory.library.utils.livebus.PageEventBus
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

    init {
        PageEventBus.get(context)
                .of(HitEvent::class.java)
                .observe(this) {
                    LogUtils.d("PageEventBus ${groupPosition} ${it?.content}")
                }
    }

    override fun getLayoutId(): Int = R.layout.item_gank


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        LogUtils.d("GankItemViewTest onDetachedFromWindow ${groupPosition} ")
    }

    override fun update(model: GankItem) {
        LogUtils.d("GankItemViewTest update ${groupPosition} ")
        super.update(model)
    }

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
            when (groupPosition) {
                0 -> {
                    PageEventBus.get(context).postEmpty("testPageEvent")
                }
                1 -> context.startActivity(Intent(context, HiltDemoActivity::class.java))
                2 -> {
                    LiveEventBus.get().postLatest(HitEvent("HiltEvent 111"))
                    LiveEventBus.get().postLatest(HitEvent("HiltEvent 222"))
                    LiveEventBus.get().postLatest(HitEvent("HiltEvent 333"))
                }
                3 -> PageEventBus.get(context).postEmpty("testPageEvent1")
                else -> PageEventBus.get(context).post(HitEvent(model.desc.orEmpty()))

            }
        }
    }

    companion object {
        var sGitHubPattern = Pattern.compile("github\\.com")
        private fun matchGithub(url: String?): String? {
            return if (sGitHubPattern.matcher(url.orEmpty()).find()) " GitHub" else ""
        }
    }
}
