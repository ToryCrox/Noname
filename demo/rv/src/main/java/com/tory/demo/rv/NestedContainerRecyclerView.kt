package com.tory.demo.rv

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.view.NestedScrollingParent2
import androidx.core.view.NestedScrollingParentHelper
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import java.lang.ref.WeakReference

/**
 * @author lenny
 * @version 1.0
 * @date 2019-09-10
 */
class NestedContainerRecyclerView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : RecyclerView(context, attrs, defStyle), NestedScrollingParent2 {
    private var itemTargetY = 0
    private val mParentHelper: NestedScrollingParentHelper = NestedScrollingParentHelper(this)
    private var mTouchType = 0

    /**
     * 当前显示的 recycleView
     */
    private var targetChild: WeakReference<View?>? = null

    /**
     * targetChild对应的itemView
     */
    private var itemChild: WeakReference<View?>? = null
    private var xDistance = 0f
    private var yDistance = 0f
    private var lastX = 0f
    private var lastY = 0f

    /*@Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        final int action = e.getAction();
        switch (action & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_MOVE:
                if ((getNestedScrollAxes() & ViewCompat.SCROLL_AXIS_VERTICAL) != 0){
                    return false;
                }
        }

        return super.onInterceptTouchEvent(e);
    }*/
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                run {
                    this.yDistance = 0.0f
                    this.xDistance = this.yDistance
                }
                lastX = ev.x
                lastY = ev.y
                if (targetChild != null) {
                    super.onInterceptTouchEvent(ev)
                    targetChild = null
                    return false
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val curX = ev.x
                val curY = ev.y
                xDistance += Math.abs(curX - lastX)
                yDistance += Math.abs(curY - lastY)
                if (xDistance > yDistance) {
                    return false
                }
                if (targetChild?.get() != null) {
                    val itemY = itemChildY
                    if (itemY == itemTargetY || itemY < lastY) {
                        return false
                    }
                }
                lastX = curX
                lastY = curY
            }
            else -> {
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        return super.onTouchEvent(e)
    }

    override fun dispatchDraw(canvas: Canvas) {
        try {
            super.dispatchDraw(canvas)
        } catch (var3: IndexOutOfBoundsException) {
            var3.printStackTrace()
        } catch (var4: Exception) {
            var4.printStackTrace()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return try {
            super.dispatchTouchEvent(ev)
        } catch (var3: IndexOutOfBoundsException) {
            var3.printStackTrace()
            false
        } catch (var4: Exception) {
            var4.printStackTrace()
            false
        }
    }

    fun setTargetY(targetY: Int) {
        itemTargetY = targetY
    }

    /**
     * *
     *
     * @param child  包裹target的父布局的直接子view
     * @param target 触发滑动的view
     * @param axes   滚动方向
     * @param type   滑动事件类型 true：表示父控件接受该嵌套滑动事件，后续嵌套滑动事件就会通知到该父控件 当子 view
     * （直接或间接）调用startNestedScroll(View, int)时，会回调父控件该方法。
     */
    override fun onStartNestedScroll(
            child: View, target: View, axes: Int, type: Int): Boolean {
        if (axes == ViewCompat.SCROLL_AXIS_HORIZONTAL) {
            return false
        }
        mTouchType = type
        targetChild = WeakReference(target)
        itemChild = WeakReference(getItemChild(child))
        return true
    }

    /**
     * 滑动前的准备工作
     */
    override fun onNestedScrollAccepted(
            child: View, target: View, axes: Int, type: Int) {
        mParentHelper.onNestedScrollAccepted(child, target, axes, type)
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        mTouchType = ViewCompat.TYPE_NON_TOUCH
        mParentHelper.onStopNestedScroll(target, type)
    }

    private fun isCanScrollToBottom(view: View): Boolean = view.canScrollVertically(1)
    private fun isCanScrollToTop(view: View): Boolean = view.canScrollVertically(-1)


    /**
     * @param dxConsumed   view 消费了x方向的距离
     * @param dyConsumed   view 消费了y方向的距离
     * @param dxUnconsumed 表示 view 剩余未消费 x 方向距离
     * @param dyUnconsumed 表示 view 剩余未消费 y 方向距离 接收子View处理完滑动后的滑动距离信息, 在这里父控件可以选择是否处理剩余的滑动距离
     */
    override fun onNestedScroll(
            target: View,
            dxConsumed: Int,
            dyConsumed: Int,
            dxUnconsumed: Int,
            dyUnconsumed: Int,
            type: Int) {
        if (dyUnconsumed == 0) {
            return
        }
        if (type == ViewCompat.TYPE_TOUCH) {
            if (dyUnconsumed > 0) {
                if ((!isCanScrollToBottom(target)|| itemChildY != itemTargetY)
                        && isCanScrollToBottom(this)) {
                    scrollBy(0, dyUnconsumed)
                }
            } else {
                if ((!target.canScrollVertically(-1) || itemChildY != itemTargetY)
                        && canScrollVertically(-1)) {
                    scrollBy(0, dyUnconsumed)
                }
            }
        } else if (type == ViewCompat.TYPE_NON_TOUCH) {
            if (dyUnconsumed > 0) {
                val canscroll = target.canScrollVertically(1)
                if ((!canscroll || itemChildY != itemTargetY) && canScrollVertically(1)) {
                    scrollBy(0, dyUnconsumed)
                }
            } else {
                val canscroll = target.canScrollVertically(-1)
                if ((!canscroll || itemChildY != itemTargetY) && canScrollVertically(-1)) {
                    scrollBy(0, dyUnconsumed)
                    if (itemChildY > height + dyUnconsumed) {
                        //                        JDLog.i("1myrecycle", "onNestedScroll1--fling");
                        fling(0, dyUnconsumed * VELUE)
                    }
                }
            }
        }
    }

    /**
     * @param dx 表示 view 本次 x 方向的滚动的总距离，单位：像素
     * @param dy 表示 view 本次 y 方向的滚动的总距离，单位：像素 在子View消费滑动事件前，优先响应滑动操作，消费部分或全部滑动距离。
     */
    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if (dy == 0) return
        if (type == ViewCompat.TYPE_TOUCH && itemChild?.get() != null) {
            var y = itemChildY
            if (dy > 0) {
                if (y != itemTargetY) {
                    if (!canScrollVertically(1)) {
                        scrollBy(0, y - itemTargetY)
                        consumed[1] = y - itemTargetY
                    } else {
                        scrollBy(0, dy)
                        y -= itemChildY
                        consumed[1] = dy
                    }
                } else if (!target.canScrollVertically(1) && canScrollVertically(1)) {
                    scrollBy(0, dy)
                    y -= itemChildY
                    consumed[1] = dy
                }
            } else {
                if (target is RecyclerView) {
                    if ((target.computeVerticalScrollOffset() <= 0
                                    || y - itemTargetY > 1)
                            && canScrollVertically(-1)) {
                        scrollBy(0, dy)
                        y -= itemChildY
                        consumed[1] = y
                    }
                } else {
                    if ((!target.canScrollVertically(-1) || y - itemTargetY > 1)
                            && canScrollVertically(-1)) {
                        scrollBy(0, dy)
                        y -= itemChildY
                        consumed[1] = y
                    }
                }
            }
        }
        if (type == ViewCompat.TYPE_NON_TOUCH && itemChild?.get() != null) {
            var y = itemChildY
            if (dy > 0) {
                if ((!target.canScrollVertically(1) || y != itemTargetY)
                        && canScrollVertically(1)) {
                    scrollBy(0, dy)
                    y -= itemChildY
                    consumed[1] = y
                }
            } else {
                if ((!target.canScrollVertically(-1) || y != itemTargetY)
                        && canScrollVertically(-1)) {
                    scrollBy(0, dy)
                    y -= itemChildY
                    consumed[1] = y
                }
            }
        }
    }

    /**
     * 处于拖动状态时，会调用该方法，回调父控件的onNestedScroll方法，传递当前 view 滑动距离详情给到父控件
     */
    override fun dispatchNestedScroll(
            dxConsumed: Int,
            dyConsumed: Int,
            dxUnconsumed: Int,
            dyUnconsumed: Int,
            offsetInWindow: IntArray?,
            type: Int): Boolean {
        if (mTouchType == type || dyUnconsumed == 0) {
            return super.dispatchNestedScroll(
                    dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type)
        }
        if (dyUnconsumed > 0) {
            if (targetChild != null && targetChild!!.get() != null) {
                val isAttache = targetChild!!.get()!!.isAttachedToWindow
                if (targetChild!!.get()!!.canScrollVertically(1) && isAttache) {
                    if (targetChild!!.get() is RecyclerView) {
                        (targetChild!!.get() as RecyclerView?)!!.fling(0, dyUnconsumed * VELUE)
                    } else {
                        targetChild!!.get()!!.scrollBy(0, dyUnconsumed)
                    }
                    return true
                }
            }
        } else {
            if (canScrollVertically(-1)) {
                fling(0, dyUnconsumed * VELUE)
                return true
            }
        }
        return super.dispatchNestedScroll(
                dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type)
    }

    override fun onNestedFling(target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        // Re-dispatch up the tree by default
//        Log.e("1myrecycle", "onNestedFling");
        if (velocityY != 0f) {
            if (velocityY > 0) {
                if ((!target.canScrollVertically(1) || itemChildY != itemTargetY)
                        && canScrollVertically(1)) {
                    fling(velocityX.toInt(), velocityY.toInt())
                    return true
                }
            } else {
                if ((!target.canScrollVertically(-1) || itemChildY != itemTargetY)
                        && canScrollVertically(-1)) {
                    //                    JDLog.i("1myrecycle", "onNestedFling------");
                    fling(velocityX.toInt(), velocityY.toInt())
                    return true
                }
            }
        }
        return false
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {

        return false
    }

    private fun getItemChild(target: View): View? {
        val parent = target.parent ?: return null
        if (parent === this) {
            return target
        } else if (parent is View) {
            return getItemChild(parent as View)
        }
        return null
    }

    private val itemChildY: Int
        private get() = if (itemChild == null || itemChild!!.get() == null) {
            -10000
        } else (itemChild!!.get()!!.y + 0.5f).toInt()

    companion object {
        private const val VELUE = 40
    }

    init {
        isNestedScrollingEnabled = false
        overScrollMode = View.OVER_SCROLL_NEVER
    }
}
