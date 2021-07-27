package com.tory.library.component.base

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePaddingRelative
import com.tory.library.R
import com.tory.library.extension.dp

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/4/7
 * Description: 分割线
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/4/7 xutao 1.0
 * Why & What is modified:
 */
class ModuleDividerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbsModuleView<ModuleDividerModel>(context, attrs, defStyleAttr) {

    private val dividerView = View(context)

    init {
        dividerView.setBackgroundColor(ContextCompat.getColor(context, R.color.color_divider))
        addView(dividerView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    override fun onChanged(model: ModuleDividerModel) {
        super.onChanged(model)
        updateLayoutParams<ViewGroup.LayoutParams> {
            height = model.height
        }
        updatePaddingRelative(start = model.start, end = model.end)
    }
}

/**
 * 灰色分割块
 */
class MallSpaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbsModuleView<ModuleSpaceModel>(context, attrs, defStyleAttr) {

    private val dividerView = View(context)

    init {
        dividerView.setBackgroundColor(ContextCompat.getColor(context, R.color.color_background_primary))
        addView(dividerView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    override fun onChanged(model: ModuleSpaceModel) {
        super.onChanged(model)
        updateLayoutParams<ViewGroup.LayoutParams> {
            height = model.height
        }
        updatePaddingRelative(start = model.start, end = model.end)
    }
}

/**
 * 分割块，自定义高度和颜色
 */
class ModuleSeparatorBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbsModuleMView<ModuleSeparatorBarModel>(context, attrs, defStyleAttr) {

    override fun onChanged(model: ModuleSeparatorBarModel) {
        super.onChanged(model)
        updateLayoutParams<ViewGroup.LayoutParams> {
            height = model.height
        }
        setBackgroundColor(model.color)
    }
}

/**
 * 空白分割块
 */
class MallEmptyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbsModuleMView<ModuleEmptyModel>(context, attrs, defStyleAttr) {

    override fun onChanged(model: ModuleEmptyModel) {
        super.onChanged(model)
        updateLayoutParams<ViewGroup.MarginLayoutParams> {
            height = model.height
        }
    }
}

/**
 * 空页面
 */
class ModuleEmptyContentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbsModuleView<ModuleEmptyContentModel>(context, attrs, defStyleAttr) {

    override fun getLayoutId(): Int = R.layout.common_base_empty_layout

    val emptyConvert: ImageView by lazy { findViewById<ImageView>(R.id.emptyConvert) }
    val emptyHint: TextView by lazy { findViewById<TextView>(R.id.emptyHint) }
    val emptyBt: TextView by lazy { findViewById<TextView>(R.id.emptyBt) }

    init {
        val emptyContent = getChildAt(0)
        emptyContent.updateLayoutParams {
            height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        emptyContent.setPadding(0, 100.dp(), 0, 28.dp())
    }

    override fun onChanged(model: ModuleEmptyContentModel) {
        super.onChanged(model)
        if (model.imageRes != 0) {
            emptyConvert.setImageResource(model.imageRes)
        }
        if (!model.hint.isNullOrEmpty()) {
            emptyHint.text = model.hint
        }
        emptyBt.isVisible = !model.buttonText.isNullOrEmpty()
        emptyBt.text = model.buttonText
        emptyBt.setOnClickListener {
            model.buttonClick?.invoke()
        }
    }
}


class ModuleGroupSectionView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : AbsModuleMView<ModuleGroupSectionModel>(context, attrs, defStyleAttr)
