package com.tory.dmzj.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.launcher.ARouter
import com.tory.dmzj.dbase.RouterTable
import com.tory.library.base.BaseActivity
import com.tory.library.log.LogUtils

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/8/30
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/8/30 xutao 1.0
 * Why & What is modified:
 */
class HomeActivity : BaseActivity() {

    override fun getLayoutId(): Int = R.layout.activity_home

    override fun initView(savedInstanceState: Bundle?) {
        LogUtils.d("$TAG iniView")
        showFragment(RouterTable.COMIC_MAIN_PAGE)
    }

    override fun getFragmentContainer(tag: String): Int = R.id.fragmentContainer

    override fun createNewFragmentForTag(tag: String): Fragment {
        val fragment = ARouter.getInstance().build(tag).navigation() as Fragment
        LogUtils.d("$TAG createNewFragmentForTag fragment:$fragment")
        return fragment
    }

}
