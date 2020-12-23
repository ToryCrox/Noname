package com.tory.library.component.base

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tory.library.log.LogUtils

/**
 * 通用Adapter，适用于非vLayout
 */
class NormalModuleAdapter(private val calDiff: Boolean = false) :
    RecyclerView.Adapter<NormalModuleAdapter.MCommonViewHolder>(), IModuleAdapter {

    companion object {
        const val TAG = "DuModuleAdapter"
    }

    private val list = mutableListOf<Any>()

    val delegate: ModuleAdapterDelegate

    init {
        delegate = ModuleAdapterDelegate(
            object : IDataAdapter {
                override fun getItem(position: Int): Any? = list.getOrNull(position)
                override fun getCount(): Int = list.size
                override fun remove(position: Int) {
                    list.removeAt(position)
                }

                override fun addAll(index: Int, listData: List<Any>, clear: Boolean) {
                    if (clear) list.clear()
                    list.addAll(index, listData)
                }

                override fun notifyDataSetChange() {
                    notifyDataSetChanged()
                }
            })
    }

    override fun setDebug(debug: Boolean) {
        delegate.setDebug(debug)
    }

    override fun isEmpty(): Boolean {
        return list.isEmpty()
    }

    override fun clearItems() {
        list.clear()
        notifyDataSetChanged()
    }

    override fun appendItems(items: List<Any>) {
        list.addAll(items)
        notifyDataSetChanged()
    }

    override fun indexOf(item: Any): Int {
        return list.indexOf(item)
    }

    override fun indexOf(predicate: (Any) -> Boolean): Int {
        return list.indexOfFirst(predicate)
    }

    override fun getGroupPosition(groupType: String, position: Int): Int {
        return delegate.findGroupPosition(groupType, position)
    }

    override fun getGroupStartPosition(groupType: String): Int {
        return delegate.findGroupStartPosition(groupType)
    }

    override fun getGroupTypes(groupType: String): List<Class<*>> {
        return delegate.getGroupTypes(groupType)
    }

    override fun getGroupCount(groupType: String): Int {
        return delegate.getGroupCount(groupType)
    }

    override fun getStartPosition(): Int = 0

    override fun setItems(items: List<Any>) {
        if (calDiff && list.size > 0) {
            val result = DiffUtil.calculateDiff(RvDiffCallback(
                list,
                items))
            list.clear()
            list.addAll(items)
            result.dispatchUpdatesTo(this)
        } else if (items != list) {
            list.clear()
            list.addAll(items)
            notifyDataSetChanged()
        }
    }

    override fun getItem(position: Int): Any? = list.getOrNull(position)

    override fun getViewType(clazz: Class<*>): Int {
        return delegate.getViewTypeIndex(clazz)
    }

    fun setData(data: List<Any>) {
        if (data != this.list) {
            this.list.clear()
            this.list.addAll(data)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int {
        return delegate.getViewTypeIndex(position)
    }

    override fun getGridLayoutManager(context: Context): GridLayoutManager {
        return delegate.getGridLayoutManager(context)
    }

    override fun refresh(oldItem: Any, newItem: Any?) {
        val index = list.indexOf(oldItem)
        if (index < 0) {
            return
        }
        if (newItem != null) {
            if (oldItem != newItem) {
                list.removeAt(index)
                list.add(index, newItem)
            }
            notifyItemChanged(index)
        } else {
            notifyItemRemoved(index)
        }
    }

    override fun setLoadMoreListener(listener: OnLoadMoreListener?) {
    }

    override fun setLoadMoreEnable(enable: Boolean) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MCommonViewHolder {
        return MCommonViewHolder(
            delegate.createView(parent, viewType)).also {
            delegate.bindHolder(it.itemView, it, viewType, this)
        }
    }

    override fun onBindViewHolder(holder: MCommonViewHolder, position: Int) {
        val model = list.getOrNull(position) ?: return
        delegate.bindView(holder.itemView, model, position)
        LogUtils.d(TAG, "onBindViewHolder " + holder.layoutPosition)
    }

    override fun onViewRecycled(holder: MCommonViewHolder) {
        LogUtils.d(TAG, "onViewRecycled " + holder.layoutPosition)
        super.onViewRecycled(holder)
    }

    override fun onViewDetachedFromWindow(holder: MCommonViewHolder) {
        LogUtils.d(TAG, "onViewDetachedFromWindow " + holder.layoutPosition)
        super.onViewDetachedFromWindow(holder)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        delegate.attachToRecyclerView(recyclerView)
        for ((type, maxSize) in delegate.allRecyclerPoolSize()) {
            if (type >= 0 && maxSize > 5) {
                recyclerView.recycledViewPool.setMaxRecycledViews(type, maxSize)
            }
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        delegate.detachFromRecyclerView()
    }

    /**
     * 注册类型
     * 注: 1. 不能重复注册相同的model类, 在Fragment里面使用报错的，将register提前到view创建之前，或者使用FragmentStateAdapter
     * 2. 必需在add到RecyclerView之前调用
     * @param gridSize 每行所占的网格数
     * @param groupType 类型归类，获取position需要
     */
    inline fun <reified V, reified M : Any> register(viewClz: Class<V>) where V : IModuleView<M>, V : View {
        delegate.register(viewClz)
    }

    /**
     * 注册类型
     * 注: 1. 不能重复注册相同的model类, 在Fragment里面使用报错的，将register提前到view创建之前，或者使用FragmentStateAdapter
     * 2. 必需在add到RecyclerView之前调用
     * @param gridSize 每行所占的网格数
     * @param groupType 类型归类，获取position需要
     * @param poolSize recyclerview的缓存大小
     * @param groupMargin 不生效
     */
    inline fun <reified V, reified M : Any> register(
            gridSize: Int = 1,
            groupType: String? = null,
            poolSize: Int = -1,
            groupMargin: GroupMargin? = null,
            crossinline creator: (ViewGroup) -> V
    )
        where V : IModuleView<M>, V : View {
        delegate.register(gridSize, groupType, poolSize, groupMargin, creator)
    }

    class MCommonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
