package com.tory.noname.main.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.tory.library.base.BaseActivity

class ComposeTestActivity : BaseActivity() {


    override fun onCreateView(savedInstanceState: Bundle?) {
        setContent {
            MainTest()
        }
    }

    override fun initView(savedInstanceState: Bundle?) {


    }
}

@Composable
fun MainTest() {
    Text("ComposeTest Android!!")
}