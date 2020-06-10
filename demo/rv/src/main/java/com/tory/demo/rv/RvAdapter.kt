package com.tory.demo.rv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_text_view.*
import kotlinx.android.synthetic.main.item_inner_rv.*

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/6/6
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/6/6 xutao 1.0
 * Why & What is modified:
 */
class RvAdapter: RecyclerView.Adapter<BaseViewHolder>() {

    val list: MutableList<Any> = mutableListOf()

    fun setData(data: List<Any>){
        list.clear()
        list.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_LIST){
            val  view = layoutInflater.inflate(R.layout.item_inner_rv, parent, false)
            ListViewHolder(view)
        } else {
            val  view = layoutInflater.inflate(R.layout.item_text_view, parent, false)
            TextViewHolder(view)
        }
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int {
        return if (list[position] is ListModel){
            TYPE_LIST
        } else {
            TYPE_TEXT
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = list[position]
        if (item is TextModel && holder is TextViewHolder){
            holder.bind(item)
        } else if (item is ListModel && holder is ListViewHolder){
            holder.bind(item)
        }
    }


    companion object {
        const val TYPE_TEXT = 1
        const val TYPE_LIST = 2
    }

}


open class BaseViewHolder(override val containerView: View)
    : RecyclerView.ViewHolder(containerView), LayoutContainer{


}

class TextViewHolder(containerView: View) : BaseViewHolder(containerView) {

    fun bind(item: TextModel){
        textView.text = item.text
    }
}


class ListViewHolder(containerView: View) : BaseViewHolder(containerView) {
    private val adapter = RvAdapter()

    init {
        recyclerView.layoutManager = LinearLayoutManager(containerView.context)
        recyclerView.adapter = adapter
    }

    fun bind(item: ListModel){
        adapter.setData(item.list)
    }
}
