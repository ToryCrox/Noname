package com.tory.demo.demo

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.appbar.AppBarLayout

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/7/22
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/7/22 xutao 1.0
 * Why & What is modified:
 */
class MAppBarScrollBehavior @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null)
    : AppBarLayout.ScrollingViewBehavior(context, attrs) {

    init {
        isVerticalOffsetEnabled = false
    }


    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        val behavior = (dependency.layoutParams as CoordinatorLayout.LayoutParams).behavior
        Log.d("MAppBarBehavior", "onDependentViewChanged child $child, behavior:$behavior")
        //return super.onDependentViewChanged(parent, child, dependency)
        //return false

        return super.onDependentViewChanged(parent, child, dependency)
    }


    override fun onMeasureChild(parent: CoordinatorLayout, child: View, parentWidthMeasureSpec: Int, widthUsed: Int, parentHeightMeasureSpec: Int, heightUsed: Int): Boolean {
        Log.d("MAppBarBehavior", "onMeasureChild child $child")
        return super.onMeasureChild(parent,
            child,
            parentWidthMeasureSpec,
            widthUsed,
            parentHeightMeasureSpec,
            heightUsed)
    }

    override fun layoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int) {
        super.layoutChild(parent, child, layoutDirection)
        // Let the parent lay it out by default
        //parent.onLayoutChild(child, layoutDirection)
        val dependencies = parent.getDependencies(child)
        Log.d("MAppBarBehavior", "layoutChild child $child")
    }

    override fun setTopAndBottomOffset(offset: Int): Boolean {
        Log.d("MAppBarBehavior", "setTopAndBottomOffset offset $offset")
        //return super.setTopAndBottomOffset(offset)
        return false
    }

}
