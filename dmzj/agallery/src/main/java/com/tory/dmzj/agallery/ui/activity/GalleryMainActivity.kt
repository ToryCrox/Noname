package com.tory.dmzj.agallery.ui.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.tory.dmzj.agallery.R
import com.tory.dmzj.agallery.ui.fragment.GalleryPostFragment
import com.tory.dmzj.dbase.RouterTable
import com.tory.library.base.BaseActivity

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
@Route(path = RouterTable.AGALLERY_MAIN)
class GalleryMainActivity: BaseActivity() {

    override fun getLayoutId(): Int = R.layout.activity_gallery_main

    override fun initView(savedInstanceState: Bundle?) {
        setDisplayHomeAsUpEnabled(false)
        showFragment(GalleryPostFragment.TAG)
    }

    override fun getFragmentContainer(tag: String): Int {
        return R.id.fragmentContainer
    }

    override fun createNewFragmentForTag(tag: String): Fragment {
        return GalleryPostFragment.newInstance()
    }

}
