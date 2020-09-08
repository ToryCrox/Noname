package com.tory.library.base

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.NonNull
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tory.library.log.LogUtils
import com.tory.library.utils.DensityUtils

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/9/5
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/9/5 xutao 1.0
 * Why & What is modified:
 */
open class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {


    private var mBottomSheetBehavior: BottomSheetBehavior<FrameLayout>? = null
    private val mBottomSheetBehaviorCallback: BottomSheetCallback = object : BottomSheetCallback() {
        override fun onStateChanged(@NonNull bottomSheet: View, newState: Int) {
            //禁止拖拽，
            if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                //设置为收缩状态
                mBottomSheetBehavior?.setState(BottomSheetBehavior.STATE_COLLAPSED)
            }
        }

        override fun onSlide(@NonNull bottomSheet: View, slideOffset: Float) {}
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val baseDialog = dialog
        if (baseDialog is BottomSheetDialog) {
            val behavior: BottomSheetBehavior<FrameLayout> = baseDialog.behavior
            mBottomSheetBehavior = behavior
            //behavior.addBottomSheetCallback(mBottomSheetBehaviorCallback)
            val peekHeight = DensityUtils.getScreenH(context) * 3 / 4
            LogUtils.d("BaseBottomSheetDialogFragment peekHeight:$peekHeight")
            mBottomSheetBehavior?.setPeekHeight(peekHeight)
        }
    }

    override fun onStart() {
        super.onStart()
        val peekHeight = DensityUtils.getScreenH(context) * 3 / 4
        dialog?.window?.let {
            val lp = it.attributes
            lp.height = peekHeight
            lp.gravity = Gravity.BOTTOM
            it.attributes = lp
        }
    }

}
