@file:Suppress("NOTHING_TO_INLINE", "unused")

package com.tory.library.extension

import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Rect
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.viewpager.widget.ViewPager

/**
 * Created by joe on 2019-09-18.
 * Email: lovejjfg@gmail.com
 */
typealias AfterTextChange = (s: Editable?) -> Unit

typealias BeforeTextChanged = (s: CharSequence?, start: Int, count: Int, after: Int) -> Unit
typealias OnTextChanged = (s: CharSequence?, start: Int, before: Int, count: Int) -> Unit

inline fun View.getString(@StringRes messageRes: Int): String {
    return context.getString(messageRes)
}

inline fun View.formatString(@StringRes messageRes: Int, vararg args: Any?): String {
    return context.formatString(messageRes, *args)
}

inline fun EditText.getContent(): String {
    return this.text.toString()
}

inline fun EditText.getContentWithTrim(): String {
    return getContent().trim()
}

inline fun EditText.addListener(
    crossinline afterTextChanged: AfterTextChange = {},
    crossinline beforeTextChanged: BeforeTextChanged = { _, _, _, _ -> },
    crossinline onTextChanged: OnTextChanged = { _, _, _, _ -> }
): TextWatcher {
    val listener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            afterTextChanged.invoke(s)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            beforeTextChanged(s, start, count, after)
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            onTextChanged(s, start, before, count)
        }
    }
    addTextChangedListener(listener)
    return listener
}

inline fun EditText.doOnTextChanged(
    crossinline onTextChanged: OnTextChanged
) {
    addListener(onTextChanged = onTextChanged)
}

inline fun EditText.doBeforeTextChanged(
    crossinline beforeTextChanged: BeforeTextChanged
) {
    addListener(beforeTextChanged = beforeTextChanged)
}

inline fun EditText.doAfterTextChanged(
    crossinline afterTextChanged: AfterTextChange
) {
    addListener(afterTextChanged = afterTextChanged)
}

inline fun ViewGroup.find(action: (View) -> Boolean): View? {
    val childCount = this.childCount
    if (childCount == 0) {
        return null
    }
    for (index in 0 until childCount) {
        val child = this.getChildAt(index)
        if (action(child)) {
            return child
        }
    }
    return null
}

inline fun ViewGroup.find(action: (View, index: Int) -> Boolean): View? {
    val childCount = this.childCount
    if (childCount == 0) {
        return null
    }
    for (index in 0 until childCount) {
        val child = this.getChildAt(index)
        if (action(child, index)) {
            return child
        }
    }
    return null
}

inline fun View.disableOutlineProvider() {
    outlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View?, outline: Outline?) {
            outline?.setOval(0, 0, 0, 0)
        }
    }
}

inline fun Toolbar.tintNavigationIcon(@ColorRes colorResource: Int) {
    navigationIcon?.mutate()?.setTint(context.color(colorResource))
}

inline fun ViewGroup.inflate(@LayoutRes res: Int, attachRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(res, this, attachRoot)
}

typealias PageScrollStateChanged = (state: Int) -> Unit
typealias PageScrolled = (position: Int, positionOffset: Float, positionOffsetPixels: Int) -> Unit
typealias PageSelected = (position: Int) -> Unit

inline fun ViewPager.addOnPageChangeListener(
    crossinline pageScrollStateChanged: PageScrollStateChanged = {},
    crossinline pageScrolled: PageScrolled = { _, _, _ -> },
    crossinline pageSelected: PageSelected = {}
) {
    this.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {
            pageScrollStateChanged.invoke(state)
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            pageScrolled.invoke(position, positionOffset, positionOffsetPixels)
        }

        override fun onPageSelected(position: Int) {
            pageSelected.invoke(position)
        }
    })
}

inline fun ViewPager.doOnPageScrollStateChanged(crossinline pageScrollStateChanged: PageScrollStateChanged) {
    this.addOnPageChangeListener(pageScrollStateChanged = pageScrollStateChanged)
}

inline fun ViewPager.doOnPageScrolled(crossinline pageScrolled: PageScrolled) {
    this.addOnPageChangeListener(pageScrolled = pageScrolled)
}

inline fun ViewPager.doOnPageSelected(crossinline pageSelected: PageSelected) {
    this.addOnPageChangeListener(pageSelected = pageSelected)
}

inline fun TextView.strike() {
    paint.flags = Paint.STRIKE_THRU_TEXT_FLAG
}

inline fun View.click(crossinline block: (View) -> Unit) = setOnClickListener {
    block(it)
}

inline fun View.isVisibleToParent(p: ViewGroup?): Boolean {
    if (p == null || this.parent == null) {
        return false
    }
    val parent: ViewGroup = p
    if (!this.isVisible) {
        return false
    }
    val visibleRect = Rect()
    parent.getHitRect(visibleRect)
    return isChildFrom(parent) && this.getLocalVisibleRect(visibleRect)
}

inline fun View.isChildFrom(parent: ViewGroup): Boolean {
    var p: ViewParent? = this.parent
    while (p != null) {
        if (parent === p) {
            return true
        }
        p = p.parent
    }
    return false
}
