package com.tory.noname.views

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import com.tory.library.component.base.AbsModuleView
import com.tory.noname.R
import com.tory.noname.model.RedirectModel
import kotlinx.android.synthetic.main.item_redirect_view.view.*

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/7/31
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/7/31 xutao 1.0
 * Why & What is modified:
 */
class RedirectView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AbsModuleView<RedirectModel>(context, attrs, defStyleAttr) {

    override fun getLayoutId() = R.layout.item_redirect_view

    override fun onChanged(model: RedirectModel) {
        super.onChanged(model)
        textView.text = model.title
        setOnClickListener {
            val intent = Intent(context, model.clazz)
            context.startActivity(intent)
        }
    }
}
