package com.tory.noname.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.tory.library.extension.dp
import com.tory.library.log.LogUtils
import kotlin.math.ceil
import kotlin.math.min

class TrendLayoutManager(val context: Context, val spaceSize: Int = 6.dp())
    : RecyclerView.LayoutManager() {

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return LayoutManager(context)
    }

    override fun onMeasure(recycler: RecyclerView.Recycler,
                           state: RecyclerView.State,
                           widthSpec: Int, heightSpec: Int) {
        val itemCount = this.itemCount
        if (itemCount <= 0) {
            return
        }
        if (state.isPreLayout) {
            return
        }
        val widthSize = View.MeasureSpec.getSize(widthSpec)
        val itemWidth = (widthSize - getPaddingH() - 2 * spaceSize) / 3

        val normalRow = (itemCount - 1) / 3 + 1
        val realRow = when {
            itemCount < 3 -> normalRow
            itemCount <= 15 -> normalRow + 1
            else -> normalRow + 2
        }
        val heightSize = (realRow - 1) * spaceSize + realRow * itemWidth + getPaddingV()
        LogUtils.d("TrendLayoutManager  widthSize:$widthSize heightSize:$heightSize")
        setMeasuredDimension(widthSize, heightSize)
    }


    private fun getPaddingH(): Int = paddingLeft + paddingRight

    private fun getPaddingV(): Int = paddingTop + paddingBottom


    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (state.itemCount <= 0) {
            removeAndRecycleAllViews(recycler)
            return
        }
        if (state.isPreLayout) {
            return
        }
        detachAndScrapAttachedViews(recycler)


        val itemCount = min(state.itemCount, 18)
        LogUtils.d("TrendLayoutManager itemCount:$itemCount")

        val hasFirstBigLayout = itemCount >= 3
        val hasLastBigLayout = itemCount >= 18
        val itemWidth = (width - getPaddingH() - 2 * spaceSize) / 3
        for (index: Int in 0 until itemCount) {
            val child = recycler.getViewForPosition(index)
            addView(child)

            val isBigLayout = (index == 0 && hasFirstBigLayout) || (index == itemCount - 1 && hasLastBigLayout)
            val childWidth = if (isBigLayout) itemWidth * 2 + spaceSize else itemWidth
            val childSizeSpec = View.MeasureSpec.makeMeasureSpec(childWidth, View.MeasureSpec.EXACTLY)
            child.measure(childSizeSpec, childSizeSpec)

            var top: Int
            var left: Int

            when (index) {
                0 -> {
                    top = 0
                    left = 0
                }
                1 -> {
                    top = 0
                    left = if (hasFirstBigLayout) itemWidth * 2 + spaceSize * 2 else itemWidth + spaceSize
                }
                2 -> {
                    left = itemWidth * 2 + spaceSize * 2
                    top = if (hasFirstBigLayout) itemWidth + spaceSize else 0
                }
                in 3..16 -> {
                    val row = index/ 3 + 2
                    val column = index % 3
                    left = column * (itemWidth + spaceSize)
                    top = (row - 1) * (itemWidth + spaceSize)
                }
                17 -> {
                    left = if (hasLastBigLayout) 0 else itemWidth + spaceSize
                    top = (if (hasLastBigLayout) 7 else 6) * (itemWidth + spaceSize)
                }
                else -> {
                    left = itemWidth + spaceSize
                    top = 6 * (itemWidth + spaceSize)
                }
            }
            top += paddingTop
            left += paddingLeft
            LogUtils.d("TrendLayoutManager layout  left:$left, top:$top, width:$childWidth")
            child.layout(left, top, left + childWidth, top + childWidth)
        }

    }


    class LayoutManager(c: Context, attrs: AttributeSet? = null) : RecyclerView.LayoutParams(c, attrs)

}