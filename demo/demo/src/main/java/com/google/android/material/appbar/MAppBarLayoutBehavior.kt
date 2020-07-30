package com.google.android.material.appbar

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout

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
class MAppBarLayoutBehavior @kotlin.jvm.JvmOverloads constructor(context: Context, attrs: AttributeSet? = null)
: AppBarLayout.Behavior(context, attrs)  {
    /**
     * 是否可以展开
     */
    var isExpandable: Boolean = true

    override fun onLayoutChild(parent: CoordinatorLayout,
        abl: AppBarLayout, layoutDirection: Int): Boolean {
        return super.onLayoutChild(parent, abl, layoutDirection)
    }

    public fun setAppbarOffset( parent:CoordinatorLayout,  header : AppBarLayout,  newOffset: Int){
        setHeaderTopBottomOffset(parent, header, newOffset, Int.MIN_VALUE, Int.MAX_VALUE)
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: AppBarLayout, target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if (dy != 0) {
            val min: Int
            val max: Int
            if (dy < 0) {
                // We're scrolling down
                min = -child.totalScrollRange
                max = min + child.downNestedPreScrollRange
            } else {//向上滑动
                // We're scrolling up
                min = -child.upNestedPreScrollRange
                max = 0
            }
            Log.v("MAppBarLayoutBehavior", "onNestedPreScroll " +
                "dy:$dy min:$min, max:$max, isClose:$isExpandable")
            if (min != max && isExpandable) {
                consumed[1] = scroll(coordinatorLayout, child, dy, min, max)
            }
        }
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: AppBarLayout, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int, consumed: IntArray) {
        log("onNestedScroll " +
            "dyUnconsumed:$dyUnconsumed :" + topAndBottomOffset)

        if (isExpandable){
            super.onNestedScroll(coordinatorLayout,
                child,
                target,
                dxConsumed,
                dyConsumed,
                dxUnconsumed,
                dyUnconsumed,
                type,
                consumed)
        }
    }

    private fun log(msg: String){
        Log.v(TAG, msg)
    }

    companion object{
        const val TAG = "MAppBarLayoutBehavior"
    }
}
