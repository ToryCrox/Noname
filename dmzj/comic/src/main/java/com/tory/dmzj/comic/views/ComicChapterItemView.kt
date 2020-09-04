package com.tory.dmzj.comic.views

import android.content.Context
import android.util.AttributeSet
import androidx.fragment.app.FragmentActivity
import com.shizhuang.duapp.common.component.module.AbsModuleView
import com.tory.dmzj.comic.R
import com.tory.dmzj.comic.dialog.ComicChapterAllDialog
import com.tory.dmzj.comic.model.ComicChapterItemModel
import kotlinx.android.synthetic.main.view_comic_chapter_item.view.*

/**
 * @author tory
 * @create 2020/9/1
 * @Describe
 */
class ComicChapterItemView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AbsModuleView<ComicChapterItemModel>(context, attrs, defStyleAttr) {


    override fun getLayoutId(): Int {
        return R.layout.view_comic_chapter_item
    }

    override fun onChanged(model: ComicChapterItemModel) {
        super.onChanged(model)
        if (model.isMore) {
            btnChapter.text = "..."
            btnChapter.setOnClickListener {
                val chapters = model.allChapter ?: return@setOnClickListener
                ComicChapterAllDialog.newInstance(chapters)
                        .show((context as FragmentActivity).supportFragmentManager, "ComicChapterAllDialog.")
            }
        } else {
            btnChapter.text = model.item.chapterTitle
            btnChapter.setOnClickListener {

            }
        }
    }
}
