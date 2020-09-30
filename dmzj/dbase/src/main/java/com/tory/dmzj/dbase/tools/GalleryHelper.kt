package com.tory.dmzj.dbase.tools

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


/**
 * @author tory
 * @create 2020/9/10
 * @Describe
 */
object GalleryHelper {
    const val TAG_KEY = "tag_key"

    private val tagsModel: TagsModel by lazy {
        SpHelper.getParcelable<TagsModel>(TAG_KEY, null) ?: TagsModel()
    }

    fun addTag(tag : String) {
        if (hasTag(tag)){
            return
        }
        tagsModel.tags.add(tag)
        SpHelper.put(TAG_KEY, tagsModel)
    }

    fun removeTag(tag: String) {
        tagsModel.tags.remove(tag)
        SpHelper.put(TAG_KEY, tagsModel)
    }

    fun hasTag(tag: String): Boolean {
        return tagsModel.tags.contains(tag)
    }

    @Parcelize
    data class TagsModel(
            val tags: MutableList<String> = mutableListOf()
    ): Parcelable
}