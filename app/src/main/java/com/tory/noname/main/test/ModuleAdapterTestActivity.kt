package com.tory.noname.main.test

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tory.library.base.BaseActivity
import com.tory.library.component.base.AbsModuleView
import com.tory.library.component.base.NormalModuleAdapter
import com.tory.library.component.decoration.ModuleGridSpaceDecoration
import com.tory.library.extension.dp
import com.tory.noname.R
import kotlinx.android.synthetic.main.activity_module_adapter.*

/**
 * - Author: xutao
 * - Date: 7/27/21
 * - Email: xutao@shizhuang-inc.com
 * - Description:
 */
class ModuleAdapterTestActivity: BaseActivity() {

    val listAdapter = NormalModuleAdapter()

    override fun getLayoutId(): Int = R.layout.activity_module_adapter

    override fun initView(savedInstanceState: Bundle?) {
        listAdapter.register (gridSize = 4, groupType = "listA") { AItemView(this)  }

        recyclerView.layoutManager = listAdapter.getGridLayoutManager(this)
        recyclerView.adapter = listAdapter
        recyclerView.addItemDecoration(ModuleGridSpaceDecoration(listAdapter, "listA", 4, spaceH = 1.dp(), spaceV = 8.dp(), edgeH = 40.dp()))

        val list = List(50) {
            AItemModel(it)
        }
        listAdapter.setItems(list)
    }


    class AItemModel(val index: Int)

    class AItemView @JvmOverloads constructor(
            context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
    ) : AbsModuleView<AItemModel>(context, attrs, defStyleAttr) {

        private val textView = AppCompatTextView(context)

        init {
            textView.setBackgroundColor(Color.RED)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
            textView.gravity = Gravity.CENTER
            textView.setTextColor(Color.WHITE)
            addView(textView, LayoutParams.MATCH_PARENT, 40.dp())
        }

        override fun onChanged(model: AItemModel) {
            super.onChanged(model)
            textView.setText(model.index.toString())
        }
    }
}