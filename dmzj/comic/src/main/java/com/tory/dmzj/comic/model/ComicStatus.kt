package com.tory.dmzj.comic.model


import com.google.gson.annotations.SerializedName

/**
 * 漫画连载状态
 */
data class ComicStatus(
    @SerializedName("tag_id")
    val tagId: Int? = null, // 2309
    @SerializedName("tag_name")
    val tagName: String? = null // 连载中
)
