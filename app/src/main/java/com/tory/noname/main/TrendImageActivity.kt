package com.tory.noname.main

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.tory.library.base.BaseActivity
import com.tory.library.component.base.AbsModuleView
import com.tory.library.component.base.NormalModuleAdapter
import com.tory.noname.R
import com.tory.noname.views.TrendLayoutManager
import kotlinx.android.synthetic.main.activity_trend_image.*

class TrendImageActivity: BaseActivity() {

    override fun getLayoutId(): Int = R.layout.activity_trend_image

    val listAdapter = NormalModuleAdapter()

    override fun initView(savedInstanceState: Bundle?) {
        listAdapter.register { CustomImageView(this) }

        recyclerView.layoutManager = TrendLayoutManager(this)
        recyclerView.adapter = listAdapter

        val list = (0 until 3).map { ImageModel(url = "https://i0.hdslb.com/bfs/album/113578f21fe6850abafa1fbdb63c2de6ce234011.jpg@1036w.jpg") }
        listAdapter.setItems(list)

    }

    data class ImageModel(val url: String)

    class CustomImageView @JvmOverloads constructor(
            context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
    ) : AbsModuleView<ImageModel>(context, attrs, defStyleAttr) {

        private val imageView = AppCompatImageView(context)
        init {
            addView(imageView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            imageView.setBackgroundColor(Color.BLUE)
        }

        override fun onChanged(model: ImageModel) {
            super.onChanged(model)
            Glide.with(this).load(model.url).centerCrop().into(imageView)
        }
    }
}