package com.tory.library.base

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.TraceCompat
import androidx.fragment.app.Fragment
import com.tory.library.R
import com.tory.library.utils.SystemBarUtils

abstract class BaseActivity : AppCompatActivity() {
    @JvmField
    protected val TAG = this.javaClass.simpleName

    var toolbar: Toolbar? = null
        protected set
    protected var mTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onCreateView(savedInstanceState)
        setThemeColor()
        initToolbar()
        initView(savedInstanceState)
        initData(savedInstanceState)
    }

    /**
     * 创建View
     */
    open fun onCreateView(savedInstanceState: Bundle?) {
        val layoutId = getLayoutId()
        if (layoutId > 0) {
            setContentView(layoutId)
        }
    }

    protected open fun setThemeColor() {
        SystemBarUtils.setStatusBarColor(this, Color.WHITE)
        SystemBarUtils.setStatusBarDarkMode(this, true)
        SystemBarUtils.setNavigationBarDarkMode(this, true)
    }
    protected open fun initToolbar() {
        toolbar = findViewById<Toolbar>(R.id.toolbar)
        if (toolbar != null) {
            setSupportActionBar(toolbar)
        }
        setDisplayHomeAsUpEnabled(true)
        setToolbarBackpress()
    }

    //监听toolbar左上角后退按钮
    fun setToolbarBackpress() {
        toolbar?.setNavigationOnClickListener { v: View? -> onBackPressed() }
    }

    //  mToolbar.setTitle(title);
    var toolbarTitle: String?
        get() = mTitle
        set(title) {
            if (toolbar != null) {
                mTitle = title
                //  mToolbar.setTitle(title);
                supportActionBar!!.title = mTitle
            }
        }

    protected fun setDisplayHomeAsUpEnabled(enable: Boolean) {
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(enable)
            actionBar.setDisplayShowHomeEnabled(false)
        }
    }

    fun showFragment(tag: String, show: Boolean = true, executeImmediately: Boolean = false) {
        TraceCompat.beginSection("showFragment - $tag")
        val fm = supportFragmentManager
        var fragment = fm.findFragmentByTag(tag)
        if (!show && fragment == null) {
            // Nothing to show, so bail early.
            return
        }
        val transaction = fm.beginTransaction()
        if (show) {
            if (fragment == null) {
                Log.d(TAG, "showFragment: fragment need create: $tag")
                fragment = createNewFragmentForTag(tag)
                transaction.add(getFragmentContainer(tag), fragment, tag)
            } else {
                Log.d(TAG, "showFragment: fragment is all ready created $tag")
                transaction.show(fragment)
            }
        } else {
            transaction.hide(fragment!!)
        }
        transaction.commitAllowingStateLoss()
        if (executeImmediately) {
            fm.executePendingTransactions()
        }
        TraceCompat.endSection()
    }

    private fun showOsFragment(tag: String, show: Boolean, executeImmediately: Boolean) {}
    fun getFragmentManagerByTag(tag: String?): Any {
        return supportFragmentManager
    }

    open fun getFragmentContainer(tag: String): Int {
        throw IllegalStateException("Unexpected fragmentContainer: $tag")
    }

    open fun createNewFragmentForTag(tag: String): Fragment {
        throw IllegalStateException("Unexpected fragment: $tag")
    }

    /**
     * 绑定渲染视图的布局文件
     *
     * @return 布局文件资源id
     */
    @LayoutRes
    open fun getLayoutId(): Int = 0

    /**
     * 初始化控件
     */
    abstract fun initView(savedInstanceState: Bundle?)

    /**
     * 业务处理操作（onCreate方法中调用）
     *
     */
    open fun initData(savedInstanceState: Bundle?) = Unit
}
