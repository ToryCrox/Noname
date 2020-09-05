package com.tory.dmzj.comic.model


import com.google.gson.annotations.SerializedName

data class ComicRankItemModel(
    @SerializedName("authors")
    val authors: String? = null,
    @SerializedName("comic_id")
    val comicId: Int = 0,
    @SerializedName("comic_py")
    val comicPy: String? = null,
    @SerializedName("cover")
    val cover: String? = null,
    @SerializedName("last_update_chapter_name")
    val lastUpdateChapterName: String? = null,
    @SerializedName("last_updatetime")
    val lastUpdatetime: Long,
    @SerializedName("num")
    val num: Int = 0,
    @SerializedName("status")
    val status: String,
    @SerializedName("tag_id")
    val tagId: String? = null,
    @SerializedName("title")
    val title: String,
    @SerializedName("types")
    val types: String
) {
    @Transient
    var rankIndex: Int = 0
}
