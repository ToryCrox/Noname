package com.tory.library.component.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.tory.library.BuildConfig
import com.tory.library.component.base.IModuleAdapter
import com.tory.library.log.LogUtils

/**
 * Author: tory
 * Date: 2020/11/18
 * Email: xutao@theduapp.com
 * Description: space的Decoration
 */
class ModuleGridSpaceDecoration(
        private val rvAdapter: IModuleAdapter,
        private val groupType: String,
        private val gridSize: Int,
        private val spaceH: Int = 0,
        private val spaceV: Int = 0,
        private val edgeH: Int = 0 // edgeH代表第一列距最左边和最后一列距离最右边的距离
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.getChildAdapterPosition(view)
        val adapterPosition = position - rvAdapter.getStartPosition()

        val typePosition = rvAdapter.getGroupPosition(groupType, position)
        if (typePosition < 0) return

        val perSpan = rvAdapter.getSpanCount() / gridSize

        val row: Int = typePosition / gridSize
        val column: Int = rvAdapter.getSpanIndex(adapterPosition) / perSpan
        if (row != 0) {
            outRect.top = spaceV
        }

        // p为每个Item都需要减去的间距
        val  p = (2 * edgeH + (gridSize - 1) * spaceH) * 1f / gridSize
        val left = edgeH + column * (spaceH - p)
        val right = p - left

        outRect.left = Math.round(left)
        outRect.right = Math.round(right)

        if (BuildConfig.DEBUG) {
            LogUtils.d("getItemOffsets position:${typePosition} column: $column, left: ${outRect.left}, right: ${outRect.right}")
        }
    }

}
