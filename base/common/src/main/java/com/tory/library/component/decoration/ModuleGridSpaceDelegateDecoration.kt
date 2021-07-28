package com.tory.library.component.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.tory.library.component.base.*

/**
 * - Author: xutao
 * - Date: 7/27/21
 * - Email: xutao@shizhuang-inc.com
 * - Description:
 */
class ModuleGridSpaceDelegateDecoration(val moduleAdapter: IModuleAdapter) : RecyclerView.ItemDecoration() {

    private val spaceMap = mutableMapOf<String, MItemSpace>()

    fun registerSpace(gridType: String, gridSize: Int, itemSpace: ItemSpace) {
        spaceMap[gridType] = MItemSpace(gridSize, itemSpace)
    }

    override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        if (view !is IModuleView<*>) {
            return
        }
        val layoutPosition = view.layoutPosition
        val groupType = moduleAdapter.getGroupTypeByPosition(layoutPosition)
        val itemSpace = spaceMap.get(groupType) ?: return

        getOffsets(outRect, groupType, layoutPosition, itemSpace)
    }


    private fun getOffsets(outRect: Rect, groupType: String, layoutPosition: Int,
                           mItemSpace: MItemSpace) {
        val groupPosition = moduleAdapter.getGroupPosition(groupType, layoutPosition)
        if (groupPosition < 0) return

        val gridSize = mItemSpace.gridSize
        val spaceH = mItemSpace.itemSpace.spaceH
        val spaceV = mItemSpace.itemSpace.spaceV
        val edgeH = mItemSpace.itemSpace.edgeH

        val perSpan = moduleAdapter.getSpanCount() / gridSize

        val row: Int = groupPosition / gridSize
        val column: Int = moduleAdapter.getSpanIndex(layoutPosition) / perSpan
        if (row != 0) {
            outRect.top = spaceV
        }
        val  p = (2 * edgeH + (gridSize - 1) * spaceH) * 1f / gridSize
        val left = edgeH + column * (spaceH - p)
        val right = p - left

        outRect.left = Math.round(left)
        outRect.right = Math.round(right)
    }

    class MItemSpace(
            val gridSize: Int,
            val itemSpace: ItemSpace
    )
}