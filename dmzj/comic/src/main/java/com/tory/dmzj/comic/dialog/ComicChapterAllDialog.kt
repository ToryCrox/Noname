package com.tory.dmzj.comic.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.alibaba.android.vlayout.VirtualLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.shizhuang.duapp.common.component.module.GroupMargin
import com.shizhuang.duapp.common.component.module.ModuleDividerModel
import com.shizhuang.duapp.common.component.module.VLayoutModuleAdapter
import com.shizhuang.duapp.common.extension.dp
import com.tory.dmzj.comic.model.ComicChapter
import com.tory.dmzj.comic.model.ComicChapterItemModel
import com.tory.dmzj.comic.model.ComicChapterTitleModel
import com.tory.dmzj.comic.views.ComicChapterItemView
import com.tory.dmzj.comic.views.ComicChapterTitleView
import com.tory.dmzj.home.R
import com.tory.library.base.BaseBottomSheetDialogFragment
import com.tory.library.component.vlayout.VLayoutDelegateAdapter

/**
 * @author tory
 * @create 2020/9/1
 * @Describe
 */
class ComicChapterAllDialog: BaseBottomSheetDialogFragment() {

    val listAdapter = VLayoutModuleAdapter()
    protected lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.include_base_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(com.tory.library.R.id.recyclerView)
        registerViews()
        val layoutManager = VirtualLayoutManager(view.context)
        val adapter = VLayoutDelegateAdapter(layoutManager)
        adapter.addAdapter(listAdapter)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        val chapters = arguments?.getParcelableArrayList<ComicChapter>(KEY_CHAPTER) ?: return

        val result = mutableListOf<Any>()
        result.addAll(chapters.flatMap { chapter->
            val list = chapter.list.orEmpty().map { ComicChapterItemModel(it) }
            val title = ComicChapterTitleModel(chapter.title.orEmpty())
            val ls = ArrayList<Any>(list.size + 1)
            ls.add(title)
            ls.addAll(list)
            ls
        })
        listAdapter.setItems(result)
    }

    private fun registerViews() {
        listAdapter.register { ComicChapterTitleView(it.context) }
        listAdapter.register(gridSize = 4, groupType = "ComicChapter",
                groupMargin = GroupMargin(4.dp())) { ComicChapterItemView(it.context) }
    }


    companion object {
        const val KEY_CHAPTER = "KEY_CHAPTER"

        fun newInstance(chapters: List<ComicChapter>): ComicChapterAllDialog{
            val args = Bundle()
            args.putParcelableArrayList(KEY_CHAPTER, ArrayList(chapters))
            val fragment = ComicChapterAllDialog()
            fragment.arguments = args
            return fragment
        }
    }
}
