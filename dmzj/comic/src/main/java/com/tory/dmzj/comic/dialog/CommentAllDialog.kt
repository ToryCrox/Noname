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
import com.shizhuang.duapp.common.component.module.joinTo
import com.shizhuang.duapp.common.extension.dp
import com.tory.dmzj.comic.model.ComicChapter
import com.tory.dmzj.comic.model.ComicChapterItemModel
import com.tory.dmzj.comic.model.ComicChapterTitleModel
import com.tory.dmzj.comic.model.CommentItemModel
import com.tory.dmzj.comic.model.CommentMainModel
import com.tory.dmzj.comic.views.ComicChapterItemView
import com.tory.dmzj.comic.views.ComicChapterTitleView
import com.tory.dmzj.comic.views.CommentMainView
import com.tory.dmzj.home.R
import com.tory.library.component.vlayout.VLayoutDelegateAdapter

/**
 * @author tory
 * @create 2020/9/1
 * @Describe
 */
class CommentAllDialog: BottomSheetDialogFragment() {

    val listAdapter = VLayoutModuleAdapter()
    protected lateinit var recyclerView: RecyclerView
    protected lateinit var refreshLayout: SwipeRefreshLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.include_base_refresh_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(com.tory.library.R.id.recyclerView)
        refreshLayout = view.findViewById(com.tory.library.R.id.refreshLayout)
        registerViews()
        val layoutManager = VirtualLayoutManager(view.context)
        val adapter = VLayoutDelegateAdapter(layoutManager)
        adapter.addAdapter(listAdapter)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        val comments = arguments?.getParcelableArrayList<CommentItemModel>(KEY_COMMENT) ?: return

        val result = mutableListOf<Any>()
        val allComments = comments.map { CommentMainModel(it) }.reversed()
        result.addAll(ModuleDividerModel().joinTo(allComments))
        listAdapter.setItems(result)
    }

    private fun registerViews() {
        listAdapter.register { CommentMainView(it.context) }
    }


    companion object {
        const val KEY_COMMENT = "KEY_COMMENT"

        fun newInstance(allComments: List<CommentItemModel>): CommentAllDialog{
            val args = Bundle()
            args.putParcelableArrayList(KEY_COMMENT, ArrayList(allComments))
            val fragment = CommentAllDialog()
            fragment.arguments = args
            return fragment
        }
    }
}
