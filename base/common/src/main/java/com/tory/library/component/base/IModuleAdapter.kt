package com.shizhuang.duapp.common.component.module

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import com.tory.library.component.base.OnLoadMoreListener

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
interface IModuleAdapter {
    // 设置log开关
    fun setDebug(debug: Boolean)

    // 是否为空
    fun isEmpty(): Boolean
    // 设置Item
    fun setItems(items: List<Any>)
    // 添加item
    fun appendItems(items: List<Any>)
    // 获取元素个数
    fun getItemCount(): Int
    // 查找元素位置
    fun indexOf(item: Any): Int
    // 获取元素位置
    fun indexOf(predicate: (Any) -> Boolean): Int
    // 清除元素
    fun clearItems()
    // 获取指定位置的元素
    fun getItem(position: Int): Any?

    /**
     * 获取adapter的viewType
     * @param clazz model类型
     */
    fun getViewType(clazz: Class<*>): Int

    /**
     * 获取分组中的相对位置
     * @param groupType 分组类型
     * @param position 绝对位置
     */
    fun getGroupPosition(groupType: String, position: Int): Int

    /**
     * 获取分组的第一个的绝对位置
     * @param groupType 分组类型
     */
    fun getGroupStartPosition(groupType: String): Int

    /**
     * 获致分组的所有类型
     */
    fun getGroupTypes(groupType: String): List<Class<*>>

    /**
     * 获取分组的数据个数
     */
    fun getGroupCount(groupType: String): Int

    /**
     * 获取Adapter第一个的数组
     */
    fun getStartPosition(): Int

    /**
     * 获取GridLayoutManager，获取注册的gridSize计算
     */
    fun getGridLayoutManager(context: Context): GridLayoutManager

    /**
     * 刷新指定的元素
     */
    fun refresh(oldItem: Any, newItem: Any? = oldItem)


    fun setLoadMoreListener(listener: OnLoadMoreListener?)

    fun setLoadMoreEnable(enable: Boolean)
}
