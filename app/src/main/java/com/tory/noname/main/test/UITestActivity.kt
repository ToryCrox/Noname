package com.tory.noname.main.test

import android.os.Bundle
import com.tory.library.base.BaseActivity
import com.tory.noname.R
import kotlinx.android.synthetic.main.activity_ui_test.*

/**
 * - Author: xutao
 * - Date: 2021/12/7
 * - Email: xutao@shizhuang-inc.com
 * - Description:
 */
class UITestActivity: BaseActivity() {

    override fun getLayoutId(): Int = R.layout.activity_ui_test

    override fun initView(savedInstanceState: Bundle?) {
        countDownView2.useDrawable = true
    }
}