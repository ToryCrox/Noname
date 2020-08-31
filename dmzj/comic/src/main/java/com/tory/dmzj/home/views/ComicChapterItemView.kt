package com.tory.dmzj.home.views

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.button.MaterialButton
import com.shizhuang.duapp.common.component.module.AbsModuleView
import com.shizhuang.duapp.common.extension.dp
import com.tory.dmzj.home.R
import com.tory.dmzj.home.model.ComicChapterItem
import kotlinx.android.synthetic.main.view_commic_chapter_item.view.*

/**
 * @author tory
 * @create 2020/9/1
 * @Describe
 */
class ComicChapterItemView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AbsModuleView<ComicChapterItem>(context, attrs, defStyleAttr) {

    init {
        //setPadding(2.dp(), 2.dp(), 2.dp(), 0.dp())
    }

    override fun getLayoutId(): Int {
        return R.layout.view_commic_chapter_item
    }

    override fun onChanged(model: ComicChapterItem) {
        super.onChanged(model)
        btnChapter.text = model.chapterTitle
    }
}