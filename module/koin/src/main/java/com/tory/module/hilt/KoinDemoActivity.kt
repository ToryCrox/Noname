package com.tory.module.hilt

import android.os.Bundle
import androidx.lifecycle.Observer
import com.tory.library.base.BaseActivity
import kotlinx.android.synthetic.main.activity_koin_demo.*
import org.koin.android.viewmodel.ext.android.viewModel

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/8/28
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/8/28 xutao 1.0
 * Why & What is modified:
 */
class KoinDemoActivity: BaseActivity() {

    private val mainViewModel by viewModel<MainViewModel>()

    override fun getLayoutId(): Int = R.layout.activity_koin_demo

    override fun initView(savedInstanceState: Bundle?) {

        mainViewModel.gankData.observe(this, Observer {
            textView.text = it?.toString()
        })

        mainViewModel.getGankData("Android")
    }
}
