package com.tory.dmzj.home.views

import android.content.Context
import android.util.AttributeSet
import com.bumptech.glide.Glide
import com.shizhuang.duapp.common.component.module.AbsModuleView
import com.tory.dmzj.home.R
import com.tory.dmzj.home.model.CommentMainModel
import com.tory.library.utils.DateUtils
import kotlinx.android.synthetic.main.view_comment_main.view.*

/**
 * @author tory
 * @create 2020/9/2
 * @Describe
 */
class CommentMainView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AbsModuleView<CommentMainModel>(context, attrs, defStyleAttr) {

    override fun getLayoutId(): Int {
        return R.layout.view_comment_main
    }

    override fun onChanged(model: CommentMainModel) {
        super.onChanged(model)
        val item = model.data
        Glide.with(this).load(item.avatarUrl).into(itemAvatar)
        itemNickName.text = item.nickname
        itemUpdateTime.text = DateUtils.format("yyyy-MM-dd hh:mm",
                item.createTime * 1000L)
        itemContent.text = item.content
    }
}