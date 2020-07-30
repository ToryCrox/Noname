package com.google.android.material.appbar

import android.content.Context
import android.util.AttributeSet

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
class MAppBarLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppBarLayout(context, attrs, defStyleAttr) {

    override fun getUpNestedPreScrollRange(): Int {
        return super.getUpNestedPreScrollRange()
    }
}
