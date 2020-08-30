@file:Suppress("unused")

package com.shizhuang.duapp.common.extension

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat

/**
 * Created by joe on 2019-09-18.
 * Email: lovejjfg@gmail.com
 */
fun Context.color(@ColorRes id: Int): Int {
    return ContextCompat.getColor(this, id)
}

fun Activity.color(@ColorRes id: Int): Int {
    return ContextCompat.getColor(this, id)
}

fun Context.dpToPx(dp: Float) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

fun Context.dpToPx(dp: Int): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics)
        .toInt()

fun Context.pxToDp(px: Int): Int {
    val scale = resources.displayMetrics.density
    return (px / scale + 0.5f).toInt()
}

fun Context.spToPx(sp: Float): Int {
    val scale = resources.displayMetrics.scaledDensity
    return (sp * scale + 0.5f).toInt()
}

fun Context.pxToSp(px: Float): Int {
    val scale = resources.displayMetrics.scaledDensity
    return (px / scale + 0.5f).toInt()
}

fun Context.formatString(@StringRes messageRes: Int, vararg args: Any?): String {
    return java.lang.String.format(getString(messageRes), *args)
}
