package com.tory.dmzj.agallery.ui.activity

import android.os.Bundle
import androidx.fragment.app.commit
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.tory.dmzj.agallery.R
import com.tory.dmzj.agallery.ui.fragment.GalleryPostFragment
import com.tory.dmzj.dbase.RouterTable
import com.tory.library.base.BaseActivity

/**
 * @author tory
 * @create 2020/9/10
 * @Describe
 */
@Route(path = RouterTable.AGALLERY_LIST)
class GalleryListActivity: BaseActivity() {

    @JvmField
    @Autowired
    var galleryTag: String? = null

    override fun getLayoutId(): Int = R.layout.common_activity_fragment_container

    override fun initView(savedInstanceState: Bundle?) {
        ARouter.getInstance().inject(this)
        title = galleryTag
        if (savedInstanceState == null && galleryTag != null) {
            supportFragmentManager.commit {
                add(R.id.fragmentContainer, GalleryPostFragment.newInstance(tag = galleryTag.orEmpty()),
                        GalleryPostFragment.TAG)
            }
        }
    }
}