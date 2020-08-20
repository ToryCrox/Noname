package com.tory.noname.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shizhuang.duapp.modules.du_mall_common.views.MallRVAdapter
import com.tory.library.base.BaseActivity
import com.tory.noname.R
import com.tory.noname.main.ui.NavMainActivity
import com.tory.noname.model.RedirectModel
import com.tory.noname.views.RedirectView
import kotlinx.android.extensions.LayoutContainer
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

    val listAdapter = MallRVAdapter()

    override fun getLayoutId(): Int = R.layout.activity_main

    override fun initView(savedInstanceState: Bundle?) {
        listAdapter.register { RedirectView(it.context) }
        recyclerView.adapter = listAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val list = listOf(
            RedirectModel("NavMainActivity", NavMainActivity::class.java),
            RedirectModel("TextTestActivity", TextTestActivity::class.java)
        )
        listAdapter.appendItems(list)
    }

}
