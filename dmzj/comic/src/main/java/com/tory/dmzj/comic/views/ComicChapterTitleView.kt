package com.tory.dmzj.comic.views

import android.content.Context
import android.util.AttributeSet
import com.tory.library.component.base.AbsModuleView
import com.tory.dmzj.comic.R
import com.tory.dmzj.comic.model.ComicChapterTitleModel
import kotlinx.android.synthetic.main.view_comic_chapter_title.view.*

/**
 * @author tory
 * @create 2020/9/1
 * @Describe
 */
class ComicChapterTitleView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AbsModuleView<ComicChapterTitleModel>(context, attrs, defStyleAttr) {


    override fun getLayoutId(): Int {
        return R.layout.view_comic_chapter_title
    }

    override fun onChanged(model: ComicChapterTitleModel) {
        super.onChanged(model)
        titleChapter.text = "- ${model.title} -"
    }
}
