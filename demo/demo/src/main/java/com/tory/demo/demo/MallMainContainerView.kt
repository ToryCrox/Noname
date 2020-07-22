package com.tory.demo.demo

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.NestedScrollingParent2
import androidx.core.view.NestedScrollingParentHelper
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tencent.smtt.utils.i

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/7/16
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/7/16 xutao 1.0
 * Why & What is modified:
 */
class MallMainContainerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), NestedScrollingParent2 {

    var scrollingParentHelper = NestedScrollingParentHelper(this);


    private fun log(msg : String){
        Log.v(TAG, msg)
        invalidate()
    }
    /**
     * 当onStartNestedScroll返回为true时，也就是父控件接受嵌套滑动时，该方法才会调用
     *
     * @param child
     * @param target
     * @param axes
     * @param type
     */
    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        log("$TAG onNestedScrollAccepted child: $child target: $target")
        scrollingParentHelper.onNestedScrollAccepted(child, target, axes, type)
    }


    /**
     * 即将开始嵌套滑动，此时嵌套滑动尚未开始，由子控件的 startNestedScroll 方法调用
     *
     * @param child  嵌套滑动对应的父类的子类(因为嵌套滑动对于的父控件不一定是一级就能找到的，可能挑了两级父控件的父控件，child的辈分>=target)
     * @param target 具体嵌套滑动的那个子类
     * @param axes   嵌套滑动支持的滚动方向
     * @param type   嵌套滑动的类型，有两种ViewCompat.TYPE_NON_TOUCH fling效果,ViewCompat.TYPE_TOUCH 手势滑动
     * @return true 表示此父类开始接受嵌套滑动，只有true时候，才会执行下面的 onNestedScrollAccepted 等操作
     */
    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        log("$TAG onStartNestedScroll, child: $child target: $target")
        return axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
    }

    /**
     * 在子控件开始滑动之前，会先调用父控件的此方法，由父控件先消耗一部分滑动距离，并且将消耗的距离存在consumed中，传递给子控件
     * 在嵌套滑动的子View未滑动之前
     * ，判断父view是否优先与子view处理(也就是父view可以先消耗，然后给子view消耗）
     *
     * @param target   具体嵌套滑动的那个子类
     * @param dx       水平方向嵌套滑动的子View想要变化的距离
     * @param dy       垂直方向嵌套滑动的子View想要变化的距离 dy<0向下滑动 dy>0 向上滑动
     * @param consumed 这个参数要我们在实现这个函数的时候指定，回头告诉子View当前父View消耗的距离
     *                 consumed[0] 水平消耗的距离，consumed[1] 垂直消耗的距离 好让子view做出相应的调整
     * @param type     滑动类型，ViewCompat.TYPE_NON_TOUCH fling效果,ViewCompat.TYPE_TOUCH 手势滑动
     */
    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        log("$TAG onNestedPreScroll, target: $target, dy: $dy, consumed: ${consumed[1]}, " +
            "canScrollToBottom:${target.isCanScrollToBottom()}, canScrollToTop:${target.isCanScrollToTop()}")
        //向上是负，向下是正
        if (target.isCanScrollToTop() && dy < 0){
            consumed[1] = dy
        }
    }

    /**
     * 在 onNestedPreScroll 中，父控件消耗一部分距离之后，剩余的再次给子控件，
     * 子控件消耗之后，如果还有剩余，则把剩余的再次还给父控件
     *
     * @param target       具体嵌套滑动的那个子类
     * @param dxConsumed   水平方向嵌套滑动的子控件滑动的距离(消耗的距离)
     * @param dyConsumed   垂直方向嵌套滑动的子控件滑动的距离(消耗的距离)
     * @param dxUnconsumed 水平方向嵌套滑动的子控件未滑动的距离(未消耗的距离)
     * @param dyUnconsumed 垂直方向嵌套滑动的子控件未滑动的距离(未消耗的距离)
     */
    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
        log("$TAG onStartNestedScroll, target: $target, dyConsumed: $dyConsumed, type: ${type}")
    }

    /**
     * 停止滑动
     *
     * @param target
     * @param type
     */
    override fun onStopNestedScroll(target: View, type: Int) {
        log("$TAG target:$target, type:$type")
    }

    fun View.isCanScrollToBottom(): Boolean{
        return canScrollVertically(1)
    }

    fun View.isCanScrollToTop(): Boolean {
        return canScrollVertically(-1)
    }

    companion object{
        const val TAG = "MallMainContainerView"
    }
}
