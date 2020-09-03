package com.tory.dmzj.comic.model


import com.google.gson.annotations.SerializedName

data class ComicAuthor(
    @SerializedName("tag_id")
    val tagId: Int? = null, // 4667
    @SerializedName("tag_name")
    val tagName: String? = null // 友藤结
)
