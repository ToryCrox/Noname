package com.tory.noname.main

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.tory.library.base.BaseActivity
import com.tory.library.component.base.ModuleDividerModel
import com.tory.library.component.base.NormalModuleAdapter
import com.tory.library.component.base.joinTo
import com.tory.module.hilt.KoinDemoActivity
import com.tory.noname.R
import com.tory.noname.main.ui.NavMainActivity
import com.tory.noname.model.RedirectModel
import com.tory.noname.views.RedirectView
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/7/30
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/7/30 xutao 1.0
 * Why & What is modified:
 */
class MainActivity: BaseActivity() {

    val listAdapter = NormalModuleAdapter()

    override fun getLayoutId(): Int = R.layout.activity_main

    override fun initToolbar() {
        super.initToolbar()
        setDisplayHomeAsUpEnabled(false)
    }

    override fun initView(savedInstanceState: Bundle?) {
        listAdapter.register { RedirectView(it.context) }
        recyclerView.adapter = listAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val list = listOf(
            RedirectModel("NavMainActivity", NavMainActivity::class.java),
            RedirectModel("TextTestActivity", TextTestActivity::class.java),
            RedirectModel("Koin", KoinDemoActivity::class.java),
            RedirectModel("TrendImageActivity", TrendImageActivity::class.java),
            RedirectModel("VLayoutTestActivity", VLayoutTestActivity::class.java)
        )

        listAdapter.appendItems(ModuleDividerModel().joinTo(list))
    }

}
