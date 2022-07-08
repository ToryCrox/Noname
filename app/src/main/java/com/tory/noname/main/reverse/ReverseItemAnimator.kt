package com.tory.noname.main.reverse

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tory.library.log.LogUtils

/**
 * - Author: tory
 * - Date: 2022/6/23
 * - Email: xutao@shizhuang-inc.com
 * - Description:
 */
class ReverseItemAnimator(val cameraDistance: Float) : NormalItemAnimator() {

    override fun animateChange(oldHolder: RecyclerView.ViewHolder,
        newHolder: RecyclerView.ViewHolder?,
        fromX: Int, fromY: Int, toX: Int, toY: Int): Boolean {
        LogUtils.d("ReverseItem ${oldHolder?.itemView?.javaClass}, ${newHolder?.itemView?.javaClass}," +
                ", fromX:$fromX, fromY:$fromY, toX:$toX, toY: $toY")

        if (oldHolder === newHolder) {
            // Don't know how to run change animations when the same view holder is re-used.
            // run a move animation to handle position changes.
            return animateMove(oldHolder, fromX, fromY, toX, toY)
        }
        val prevTranslationX = oldHolder.itemView.translationX
        val prevTranslationY = oldHolder.itemView.translationY
        val prevAlpha = oldHolder.itemView.alpha
        resetAnimation(oldHolder)
        val deltaX = (toX - fromX - prevTranslationX).toInt()
        val deltaY = (toY - fromY - prevTranslationY).toInt()
        // recover prev translation state after ending animation
        // recover prev translation state after ending animation
        oldHolder.itemView.translationX = prevTranslationX
        oldHolder.itemView.translationY = prevTranslationY
        oldHolder.itemView.alpha = prevAlpha
        oldHolder.itemView.cameraDistance = cameraDistance
        oldHolder.itemView.rotationY = 0f
        if (newHolder != null) {
            // carry over translation values
            resetAnimation(newHolder)
            newHolder.itemView.translationX = -deltaX.toFloat()
            newHolder.itemView.translationY = -deltaY.toFloat()
            newHolder.itemView.alpha = 0f
            newHolder.itemView.rotationY = -180f
            newHolder.itemView.cameraDistance = cameraDistance
        }
        mPendingChanges.add(ChangeInfo(oldHolder, newHolder, fromX, fromY, toX, toY))
        return true

    }

    override fun animateChangeImpl(changeInfo: ChangeInfo) {
        val holder = changeInfo.oldHolder
        val view = holder?.itemView
        val newHolder = changeInfo.newHolder
        val newView = newHolder?.itemView
        val duration = 500L
        LogUtils.d("animateChangeImpl view: $view, newView: $newView")
        if (view != null) {
            val oldViewAnim = view.animate().setDuration(duration)
            mChangeAnimations.add(changeInfo.oldHolder)
            oldViewAnim.translationX((changeInfo.toX - changeInfo.fromX).toFloat())
            oldViewAnim.translationY((changeInfo.toY - changeInfo.fromY).toFloat())
            oldViewAnim.rotationY(180f)
            oldViewAnim.alpha(0f).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animator: Animator) {
                    dispatchChangeStarting(changeInfo.oldHolder, true)
                }

                override fun onAnimationEnd(animator: Animator) {
                    oldViewAnim.setListener(null)
                    view.cameraDistance = 0f
                    view.alpha = 1f
                    view.translationX = 0f
                    view.translationY = 0f
                    oldViewAnim.rotationY(0f)
                    dispatchChangeFinished(changeInfo.oldHolder, true)
                    mChangeAnimations.remove(changeInfo.oldHolder)
                    dispatchFinishedWhenDone()
                }
            }).start()
        }
        if (newView != null) {
            val newViewAnimation = newView.animate().setDuration(duration)
            mChangeAnimations.add(changeInfo.newHolder)
            newViewAnimation.rotationY(0f)
            newViewAnimation.translationX(0f).translationY(0f)
                .setDuration(duration)
                .alpha(1f).setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animator: Animator) {
                        dispatchChangeStarting(changeInfo.newHolder, false)
                    }

                    override fun onAnimationEnd(animator: Animator) {
                        newViewAnimation.setListener(null)
                        newView.alpha = 1f
                        newView.translationX = 0f
                        newView.translationY = 0f
                        dispatchChangeFinished(changeInfo.newHolder, false)
                        mChangeAnimations.remove(changeInfo.newHolder)
                        dispatchFinishedWhenDone()
                    }
                }).start()
        }
    }
}