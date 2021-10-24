package com.tory.demo.jetpack.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import com.tory.demo.jetpack.databinding.DataBindingActivity
import com.tory.demo.jetpack.hilt.HiltDemoActivity
import com.tory.library.base.BaseListActivity
import com.tory.library.component.base.AbsModuleView

import com.tory.library.extension.clickThrottle

class MainActivity: BaseListActivity() {


    override fun registerViews() {
        listAdapter.register { JumpView(this) }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        val list = listOf(
                JumpModel("Hilt", HiltDemoActivity::class.java),
                JumpModel("DataBiding", DataBindingActivity::class.java)
        )
        listAdapter.setItems(list)
    }



    data class JumpModel(val text: String,
            val clazz: Class<*>, val bundle: Bundle? = null)

    class JumpView @JvmOverloads constructor(
            context: Context, attrs: AttributeSet? = null
    ) : AbsModuleView<JumpModel>(context, attrs) {


        val button = AppCompatButton(context)

        init {
            addView(button, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }

        override fun onChanged(model: JumpModel) {
            super.onChanged(model)
            button.text = model.text
            button.clickThrottle {
                context.startActivity(Intent(context, model.clazz).apply {
                    val b = model.bundle ?: return@apply
                    putExtras(b)
                })
            }
        }

    }
}