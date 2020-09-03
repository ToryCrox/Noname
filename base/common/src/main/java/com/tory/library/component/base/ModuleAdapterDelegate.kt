package com.shizhuang.duapp.common.component.module

import android.content.Context
import android.graphics.Point
import android.os.SystemClock
import android.util.SparseIntArray
import android.view.View
import android.view.ViewGroup
import androidx.collection.ArrayMap
import androidx.core.os.TraceCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tory.library.BuildConfig
import com.tory.library.R
import com.tory.library.adapter.BaseLoadMoreBinder
import com.tory.library.adapter.DefaultAdapterWrapper
import com.tory.library.log.LogUtils
import java.lang.reflect.Constructor

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/4/7
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/4/7 xutao 1.0
 * Why & What is modified:
 */
class ModuleAdapterDelegate(private val dataAdapter: IDataAdapter) {

    private val viewTypes = mutableListOf<ViewType<*>>()
    private val viewTypeMap = ArrayMap<Class<*>, ViewType<*>>()
    private val groupTypes = ArrayMap<String, List<Class<*>>>() // 分类

    private val debugViewCount = SparseIntArray()
    private var isDebug: Boolean = BuildConfig.DEBUG
    private var recyclerView: RecyclerView? = null

    /**
     * 加载更多监听器
     */
    private val onLoadMoreListener: DefaultAdapterWrapper.OnLoadMoreListener? = null

    init {
        register { MallEmptyView(it.context) }
        register { MallSpaceView(it.context) }
        register {
            ModuleDividerView(it.context)
        }
        register { MallNoMoreTipView(it.context) }
        register {
            ModuleSeparatorBarView(it.context)
        }
        register {
            ModuleEmptyContentView(it.context)
        }
    }


    fun setDebug(enable: Boolean) {
        this.isDebug = enable
    }

    fun attachToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }

    fun detachFromRecyclerView() {
        this.recyclerView = null
    }
    /**
     * 注册类型
     */
    fun registerGroupType(group: String?, clazz: Class<*>) {
        val groupType = group ?: clazz.name
        val types = groupTypes[groupType]
        if (types == null) {
            groupTypes[groupType] = listOf(clazz)
        } else {
            groupTypes[groupType] = types.plus(clazz)
        }
    }

    fun checkRegister(clazz: Class<*>) {
        if (!isDebug) return
        check(indexOfViewType(clazz) < 0) {
            "register class $clazz has been registered before!!! Please Not register again"
        }
        check(recyclerView == null) { // 必需在adapter attach RecyclerView之前注册
            "register must before to attach RecyclerView"
        }
    }

    /**
     * 类型注册
     */
    inline fun <reified V, reified M : Any> register(viewClz: Class<V>) where V : IModuleView<M>, V : View {
        val clazzType = M::class.java
        checkRegister(clazzType)
        registerGroupType(null, clazzType)
        val viewType = ViewType(
            clazzType) { parent -> createView(viewClz, parent) }
        addViewType(viewType)
    }

    inline fun <reified V, reified M : Any> register(
        gridSize: Int = 1,
        groupType: String? = null,
        poolSize: Int = -1,
        groupMargin: GroupMargin? = null,
        crossinline creator: (ViewGroup) -> V
    ) where V : IModuleView<M>, V : View {
        val clazzType = M::class.java
        checkRegister(clazzType)
        registerGroupType(groupType, clazzType)
        val margin = transformMargin(groupMargin)
        val viewType = ViewType(
            clazzType,
            gridSize = gridSize,
            poolSize = poolSize,
            margin = margin) { parent -> creator(parent) }
        addViewType(viewType)
    }

    fun addViewType(viewType: ViewType<*>) {
        viewTypes.add(viewType)
        viewTypeMap[viewType.type] = viewType
    }

    fun transformMargin(groupMargin: GroupMargin?): Point? {
        val margin = if (groupMargin != null) {
            val point = Point(groupMargin.all, groupMargin.all)
            if (groupMargin.start > 0) point.x = groupMargin.start
            if (groupMargin.end > 0) point.y = groupMargin.end
            point
        } else null
        return margin
    }

    fun <V : View> createView(clazz: Class<V>, parent: ViewGroup):
        View {
        return try {
            val constructor = getConstructor(
                clazz)
            constructor.newInstance(parent.context)
        } catch (e: Exception) {
            if (isDebug) {
                throw IllegalStateException("createView Error can not create View clazz:$clazz")
            } else {
                LogUtils.e(TAG, "createView Error", e)
                View(parent.context)
            }
        }
    }

    /**
     * 获取类型
     */
    fun getViewTypeIndex(position: Int): Int {
        val model = dataAdapter.getItem(position)
        if (model == null && isDebug) {
            throw IllegalArgumentException("getItemViewType can not found view type for position: $position," +
                " please check you register the Model")
        } else if (model == null) {
            return TYPE_NONE
        }
        val viewType = getViewTypeIndex(model.javaClass)
        if (viewType < 1 && isDebug) {
            throw IllegalArgumentException("getItemViewType can not found view type " +
                "for ${model.javaClass.name} model: $model," +
                " please check you register the Model")
        }
        return viewType
    }

    /**
     * 获取adapter的viewType
     */
    fun getViewTypeIndex(clazz: Class<*>): Int {
        return indexOfViewType(clazz) + 1
    }

    private fun indexOfViewType(clazz: Class<*>): Int {
        return viewTypes.indexOfFirst { it.type == clazz }
    }

    /**
     * 通过adapter的viewType获取
     */
    private fun getViewTypeByIndex(viewType: Int): ViewType<*>? {
        val type: ViewType<*>? = viewTypes.getOrNull(viewType - 1)
        if (type == null && isDebug) {
            throw IllegalStateException("can not found viewType: viewType:$viewType")
        }
        return type
    }

    /**
     * 获取adapter中的position的viewType
     */
    fun getViewTypeByPosition(position: Int): ViewType<*>? {
        val item = dataAdapter.getItem(position) ?: return null
        return viewTypeMap[item.javaClass]
    }

    /**
     * 创建view
     */
    fun createView(parent: ViewGroup, viewType: Int): View {
        TraceCompat.beginSection("$TAG createView viewType:$viewType")
        val startTime: Long = if (isDebug) {
            SystemClock.elapsedRealtime()
        } else 0L
        val type: ViewType<*> = getViewTypeByIndex(viewType) ?: return View(parent.context)
        val view: View = type.viewCreator.invoke(parent)
        val lp = view.layoutParams
        if (lp != null) { // 有LayoutParams时不需要重新沿用
            view.layoutParams = when (lp) {
                is RecyclerView.LayoutParams -> lp
                is ViewGroup.MarginLayoutParams -> RecyclerView.LayoutParams(lp)
                else -> RecyclerView.LayoutParams(lp)
            }
        } else { // 默认LayoutParams
            view.layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        if (isDebug) {
            val timeSpent = SystemClock.elapsedRealtime() - startTime
            debugViewCount.put(viewType, debugViewCount[viewType] + 1)
            LogUtils.d("$TAG createView viewType:$viewType, " +
                "view:${view.javaClass.simpleName}, viewCount:${debugViewCount[viewType]}," +
                " timeSpent: ${timeSpent}ms")
        }
        TraceCompat.endSection()

        return view
    }

    fun bindHolder(
        view: View,
        viewHolder: RecyclerView.ViewHolder,
        viewType: Int,
        rvAdapter: IModuleAdapter
    ) {
        view.setTag(MALL_ITEM_HOLDER_TAG, object : IRvItemHolder {
            override fun getLayoutPosition(): Int = viewHolder.adapterPosition - rvAdapter.getStartPosition()

            override fun getGroupPosition(): Int {
                val type = getViewTypeByIndex(viewType) ?: return getLayoutPosition()
                return findGroupPosition(type.type, getLayoutPosition())
            }

            override fun getGroupCount(): Int {
                val type = getViewTypeByIndex(viewType) ?: return getItemCount()
                val groupType = getGroupTypeByClazz(type.type) ?: return getItemCount()
                return rvAdapter.getGroupCount(groupType)
            }

            override fun getItemCount(): Int = rvAdapter.getItemCount()
        })
    }

    /**
     * 绑定数据
     */
    fun bindView(view: View, item: Any, position: Int) {
        if (view !is IModuleView<*>) {
            return
        }
        TraceCompat.beginSection("$TAG bindView ${view.javaClass.simpleName}")
        val startTime = if (isDebug) SystemClock.elapsedRealtime() else 0L
        (view as IModuleView<Any>).update(item)
        if (isDebug) {
            val timeSpent = SystemClock.elapsedRealtime() - startTime
            LogUtils.d("$TAG bindView position:$position groupPosition:${view.groupPosition}" +
                ", view:${view.javaClass.simpleName} timeSpent: ${timeSpent}ms")
        }
        TraceCompat.endSection()
    }

    /**
     * 获取类型的第一个值
     */
    fun findGroupStartPosition(groupType: String): Int {
        val types = groupTypes[groupType] ?: return 0
        val size = dataAdapter.getCount()
        for (index in 0 until size) {
            val type = dataAdapter.getItem(index)?.javaClass ?: continue
            if (type in types) {
                return index
            }
        }
        return 0
    }

    fun findGroupPosition(groupType: String, position: Int): Int {
        val types = groupTypes[groupType] ?: return -1
        return findGroupPosition(types, position)
    }

    fun findGroupPosition(type: Class<*>, position: Int): Int {
        val types = groupTypes.values.find { type in it }
        return findGroupPosition(types ?: listOf(type), position)
    }

    private fun findGroupPosition(types: List<Class<*>>, position: Int): Int {
        if (position < 0) return -1
        var typePos = -1
        for (index in position downTo 0) {
            val item1 = dataAdapter.getItem(index)
            if (item1 != null && item1.javaClass in types) {
                typePos++
            }
        }
        return typePos
    }

    /**
     * 获取分组类型
     */
    fun getGroupTypes(groupType: String): List<Class<*>> {
        return groupTypes[groupType].orEmpty()
    }

    private fun getGroupTypeByClazz(clazz: Class<*>): String? {
        return groupTypes.entries.find { clazz in it.value }?.key
    }

    /**
     * 分组数量
     */
    fun getGroupCount(groupType: String): Int {
        val types = groupTypes[groupType].orEmpty()
        if (types.isEmpty()) return 0
        return (0 until dataAdapter.getCount()).count {
            val type = dataAdapter.getItem(it)?.javaClass
            type != null && type in types
        }
    }

    /**
     * @return <Type, Size>
     */
    fun allRecyclerPoolSize(): List<Pair<Int, Int>> {
        return viewTypes.map { getViewTypeIndex(it.type) to it.poolSize }
    }

    companion object {
        const val TAG = "ModuleAdapterDelegate"

        const val TYPE_NONE = 0

        private val CONSTRUCTOR_CACHE = ArrayMap<CKey, Constructor<*>>()

        fun <V> getConstructor(clazz: Class<V>, dataClz: Class<*>? = null): Constructor<V> {
            val cKey = CKey(clazz,
                dataClz)
            val constructor = CONSTRUCTOR_CACHE[cKey]
            if (constructor != null) {
                return constructor as Constructor<V>
            }
            val newConstructor = if (dataClz == null) clazz.getConstructor(Context::class.java)
            else clazz.getConstructor(Context::class.java, dataClz)
            CONSTRUCTOR_CACHE[cKey] = newConstructor
            return newConstructor
        }
    }

    fun getGridLayoutManager(context: Context): GridLayoutManager {
        val pair = getGridSpanLookup()
        val spanCount = pair.first
        val glm = GridLayoutManager(context, spanCount)
        glm.orientation = RecyclerView.VERTICAL
        glm.spanSizeLookup = pair.second
        return glm
    }

    fun getSpanMargin(position: Int): Point? {
        val type = getViewTypeByPosition(position) ?: return null
        return type.margin
    }

    fun getGridSpanLookup(): Pair<Int, GridLayoutManager.SpanSizeLookup> {
        var spanCount = 1
        for (value in viewTypes) {
            val size = value.gridSize
            if (size > 0 && spanCount % size != 0) {
                spanCount *= size
            }
        }
        val spanLockup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val type = getViewTypeByPosition(position) ?: return 1
                return spanCount / type.gridSize
            }
        }
        return Pair(spanCount, spanLockup)
    }
}

data class CKey(val viewClz: Class<*>, val dataClz: Class<*>? = null)

/**
 * 数据存储器
 */
interface IDataAdapter {
    fun getItem(position: Int): Any?
    fun getCount(): Int
    fun remove(position: Int)
    fun addAll(index: Int, listData: List<Any>, clear: Boolean)
    fun notifyDataSetChange()
}

/**
 * 组件类型
 */
class ViewType<T : Any>(
    val type: Class<T>,
    val gridSize: Int = 1,
    val poolSize: Int = -1,
    val margin: Point? = null,
    val viewCreator: IViewCreator
)

class GroupMargin(
    val all: Int = 0,
    val start: Int = 0,
    val end: Int = 0
)

typealias IViewCreator = (parent: ViewGroup) -> View

interface IRvItemHolder {
    fun getLayoutPosition(): Int
    fun getGroupPosition(): Int
    fun getGroupCount(): Int
    fun getItemCount(): Int
}

val MALL_ITEM_HOLDER_TAG = R.id.mall_item_holder_tag

val IModuleView<*>.rvItemHolder: IRvItemHolder?
    get() = if (this is View) {
        val tag = getTag(MALL_ITEM_HOLDER_TAG)
        if (tag is IRvItemHolder) tag else null
    } else null

val IModuleView<*>.layoutPosition: Int
    get() = rvItemHolder?.getLayoutPosition() ?: -1
val IModuleView<*>.groupPosition: Int
    get() = rvItemHolder?.getGroupPosition() ?: -1
val IModuleView<*>.groupCount: Int
    get() = rvItemHolder?.getGroupCount() ?: -1
val IModuleView<*>.itemCount: Int
    get() = rvItemHolder?.getItemCount() ?: 0

class RvDiffCallback(private val oldList: List<Any>, private val newList: List<Any>) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList.getOrNull(oldItemPosition) == newList.getOrNull(newItemPosition)

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList.getOrNull(oldItemPosition) == newList.getOrNull(newItemPosition)
}
