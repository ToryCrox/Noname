package com.tory.dmzj.home.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import com.shizhuang.duapp.common.component.module.AbsModuleView
import com.tory.dmzj.home.R
import com.tory.dmzj.home.model.ComicChapterItem
import com.tory.dmzj.home.model.ComicChapterItemModel
import com.tory.dmzj.home.model.ComicChapterTitleModel
import com.tory.library.log.LogUtils
import kotlinx.android.synthetic.main.view_commic_chapter_item.view.*
import kotlinx.android.synthetic.main.view_commic_chapter_title.view.*

/**
 * @author tory
 * @create 2020/9/1
 * @Describe
 */
class ComicChapterTitleView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AbsModuleView<ComicChapterTitleModel>(context, attrs, defStyleAttr) {


    override fun getLayoutId(): Int {
        return R.layout.view_commic_chapter_title
    }

    override fun onChanged(model: ComicChapterTitleModel) {
        super.onChanged(model)
        titleChapter.text = "- ${model.title} -"
    }
}