package com.tory.demo.webview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tory.library.utils.Utilities
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        openWeb1.setOnClickListener {
            Utilities.openInBrowser(this,
                    "https://m.poizon.com/nvwa/#/detail/5e0dc866078669512366e1e0?debug=1")
        }

    }
}
