package com.tory.library.extension

import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import com.tory.library.log.LogUtils

/**
 * Author: xutao
 * Version V1.0
 * Date: 2019-12-12
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2019-12-12 xutao 1.0
 * Why & What is modified:
 */

fun View.addLifecycleObserver() {
    if (this is LifecycleObserver) {
        this.doOnPreDraw {
            findLifecycleOwner()?.lifecycle?.addObserver(this)
        }
    } else {
        LogUtils.w("View is not implement LifecycleObserver")
    }
}

fun View.removeLifecycleObserver() {
    if (this is LifecycleObserver) {
        post {
            findLifecycleOwner()?.lifecycle?.removeObserver(this)
        }
    } else {
        LogUtils.w("View is not implement LifecycleObserver")
    }
}

/**
 * 添加Lifecycle，会监听View的Attach状态， attach时添加，detached移除
 * 注: !!!!!detach了可能就监听不到onDestroy了，慎用!!!!!!
 */
fun View.attachLifecycleObserver() {
    if (this !is LifecycleObserver) {
        LogUtils.w("$this is not implement LifecycleObserver")
        return
    }
    val target = this
    addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {

        override fun onViewAttachedToWindow(v: View?) {
            findLifecycleOwner()?.lifecycle?.addObserver(target)
        }

        override fun onViewDetachedFromWindow(v: View?) {
            findLifecycleOwner()?.lifecycle?.removeObserver(target)
        }
    })
}

fun View.findLifecycleOwnerNotNull(): LifecycleOwner {
    return checkNotNull(findLifecycleOwner(), {
        "${this.javaClass.simpleName} is not in LifecycleOwner Activity or fragment"
    })
}

/**
 * 寻找LifecycleOwner，可找到view所在的Fragment或者Activity
 */
fun View.findLifecycleOwner(): LifecycleOwner? {
    if (context is FragmentActivity) {
        val activity: FragmentActivity = context as FragmentActivity
        return findFragment(activity) ?: activity
    }
    return null
}

fun View.findFragment(activity: FragmentActivity): Fragment? {
    val fragments: MutableMap<View, Fragment> = mutableMapOf()
    findAllSupportFragmentsWithViews(
        activity.supportFragmentManager.fragments, fragments)
    if (fragments.isEmpty()) {
        return null
    }
    val activityRoot: View = activity.findViewById(android.R.id.content)
    var current = this
    var result: Fragment? = null
    while (current != activityRoot) {
        result = fragments[current]
        if (result != null) {
            break
        }
        if (current.parent is View) {
            current = current.parent as View
        } else {
            break
        }
    }
    return result
}

private fun findAllSupportFragmentsWithViews(
    topLevelFragments: Collection<Fragment?>?,
    result: MutableMap<View, Fragment>
) {
    if (topLevelFragments == null) {
        return
    }
    for (fragment in topLevelFragments) {
        // getFragment()s in the support FragmentManager may contain null values, see #1991.
        fragment?.view?.let {
            result[it] = fragment
            findAllSupportFragmentsWithViews(fragment.childFragmentManager.fragments, result)
        }
    }
}
