package com.shizhuang.duapp.common.component.module

import android.content.Context
import android.graphics.Point
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.vlayout.LayoutHelper
import com.alibaba.android.vlayout.layout.LinearLayoutHelper
import com.tory.library.component.CommonViewHolder
import com.tory.library.component.base.ExtendGridLayoutHelper
import com.tory.library.component.base.OnLoadMoreListener
import com.tory.library.component.vlayout.VLayoutDelegateInnerAdapter
import com.tory.library.log.LogUtils

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/4/7
 * Description: 组件化Adapter，适用于vlayout
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/4/7 xutao 1.0
 * Why & What is modified:
 */
open class VLayoutModuleAdapter(
    private val calDiff: Boolean = false,
    private val adapterIndex: Int = 0,
    private val layoutHelper: LayoutHelper? = null
) : VLayoutDelegateInnerAdapter<Any>(), IModuleAdapter {

    val delegate: ModuleAdapterDelegate = ModuleAdapterDelegate(
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

    private var mStartPosition: Int = 0
    private var spanSizeLookup: ExtendGridLayoutHelper.SpanSizeLookup? = null
    private var spanCount: Int = 1

    override fun onCreateLayoutHelper(): LayoutHelper {
        if (layoutHelper != null) return layoutHelper

        val pair = delegate.getGridSpanLookup()
        spanCount = pair.first
        val lookup = pair.second
        if (spanCount > 1) {
            return ExtendGridLayoutHelper(spanCount).also {
                it.setAutoExpand(false)
                spanSizeLookup = object : ExtendGridLayoutHelper.SpanSizeLookup() {
                    override fun setStartPosition(startPosition: Int) {
                        super.setStartPosition(startPosition)
                        this@VLayoutModuleAdapter.mStartPosition = startPosition
                    }

                    override fun getSpanSize(position: Int): Int {
                        return lookup.getSpanSize(position - startPosition)
                    }

                    override fun getMargin(position: Int): Point? {
                        return delegate.getSpanMargin(position - startPosition)
                    }
                }
                it.setSpanSizeLookup(spanSizeLookup)
            }
        }
        return object : LinearLayoutHelper() {
            override fun onRangeChange(start: Int, end: Int) {
                super.onRangeChange(start, end)
                mStartPosition = start
            }
        }
    }



    override fun setDebug(debug: Boolean) {
        delegate.setDebug(debug)
    }

    override fun isEmpty() = list.isEmpty()

    override fun indexOf(item: Any): Int {
        return list.indexOf(item)
    }

    override fun indexOf(predicate: (Any) -> Boolean): Int {
        return list.indexOfFirst(predicate)
    }

    override fun getViewType(clazz: Class<*>): Int {
        return delegate.getViewTypeIndex(clazz)
    }

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

    override fun getStartPosition(): Int {
        return mStartPosition
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

    override fun getItemViewType(position: Int): Int {
        return delegate.getViewTypeIndex(position)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        delegate.attachToRecyclerView(recyclerView)
        for ((type, maxSize) in delegate.allRecyclerPoolSize()) {
            if (type >= 0 && maxSize > 5) {
                LogUtils.d("DuModuleAdapter setMaxRecycledViews type:$type, maxSize:$maxSize")
                setMaxRecycledViews(adapterIndex, type, maxSize)
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
     * @param poolSize 设置缓存大小
     * @param groupMargin 设置两边的margin，目前只对vLayout有效
     */
    inline fun <reified V, reified M : Any> register(
        gridSize: Int = 1,
        groupType: String? = null,
        poolSize: Int = -1,
        groupMargin: GroupMargin? = null,
        crossinline creator: (ViewGroup) -> V
    ) where V : IModuleView<M>, V : View {
        delegate.register(gridSize, groupType, poolSize, groupMargin, creator)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommonViewHolder {
        return CommonViewHolder(delegate.createView(parent, viewType)).also {
            delegate.bindHolder(it.itemView, it, viewType, this)
        }
    }

    override fun onBindViewHolder(holder: CommonViewHolder, position: Int) {
        val item = getItem(position) ?: return
        bindView(holder.itemView, item, position)
    }

    open fun bindView(view: View, item: Any, position: Int) {
        delegate.bindView(view, item, position)
    }

}
