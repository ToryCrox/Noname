package com.tory.noname.main

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.vlayout.DelegateAdapter
import com.alibaba.android.vlayout.LayoutHelper
import com.alibaba.android.vlayout.VirtualLayoutManager
import com.alibaba.android.vlayout.layout.GridLayoutHelper
import com.alibaba.android.vlayout.layout.LinearLayoutHelper
import com.tory.library.base.BaseActivity
import com.tory.library.extension.dp
import com.tory.library.log.LogUtils
import com.tory.noname.R
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_vlayout_test.*

/**
 * - Author: xutao
 * - Date: 7/4/21
 * - Email: xutao@shizhuang-inc.com
 * - Description:
 */
class VLayoutTestActivity: BaseActivity() {

    override fun getLayoutId(): Int = R.layout.activity_vlayout_test

    override fun initView(savedInstanceState: Bundle?) {

        val layoutManager = VirtualLayoutManager(this)

        val delegateAdapter = DelegateAdapter(layoutManager, false)


        val adapter1 = MAdapter1(delegateAdapter)
        val adapter2 = MAdapter2(delegateAdapter)


        val list1 = List(6) {
            "$it"
        }
        adapter1.list.addAll(list1)

        val list2 = List(2) {
            "${100 + it}"
        }
        adapter2.list.addAll(list2)

        delegateAdapter.addAdapter(adapter1)
        delegateAdapter.addAdapter(adapter2)


        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = delegateAdapter
    }


    abstract class MAdapter(val delegateAdapter: DelegateAdapter) : DelegateAdapter.Adapter<CommonViewHolder>() {
        val list = mutableListOf<String>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommonViewHolder {
            val view = TextView(parent.context)
            view.layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    60.dp()
            )
            return CommonViewHolder(view)
        }

        override fun getItemCount(): Int = list.size


        //override fun onCreateLayoutHelper(): LayoutHelper = GridLayoutHelper(2)

        override fun onBindViewHolder(holder: CommonViewHolder, position: Int) {
            holder.itemView.setOnClickListener {
                val p = delegateAdapter.findOffsetPosition(holder.adapterPosition)
                list.removeAt(p)
                notifyItemRemoved(p)
            }
            holder.containerView.text = list[position]
            holder.containerView.gravity = Gravity.CENTER
        }
    }

    class MAdapter1(delegateAdapter: DelegateAdapter) : MAdapter(delegateAdapter) {

        override fun onCreateLayoutHelper(): LayoutHelper{
            LogUtils.d("onCreateLayoutHelper  11111111111")
            return LinearLayoutHelper()
        }
    }
    class MAdapter2(delegateAdapter: DelegateAdapter) : MAdapter(delegateAdapter) {
        override fun onCreateLayoutHelper(): LayoutHelper   {
            LogUtils.d("onCreateLayoutHelper  222222222222")
            return GridLayoutHelper(2)
        }
    }




    class CommonViewHolder(override val containerView: TextView) : RecyclerView.ViewHolder(containerView), LayoutContainer
}