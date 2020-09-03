package com.tory.dmzj.comic.model


import com.google.gson.annotations.SerializedName

data class ComicType(
    @SerializedName("tag_id")
    val tagId: Int? = null, // 8
    @SerializedName("tag_name")
    val tagName: String? = null // 爱情
)
