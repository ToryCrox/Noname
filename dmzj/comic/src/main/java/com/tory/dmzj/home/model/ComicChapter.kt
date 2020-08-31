package com.tory.dmzj.home.model


import com.google.gson.annotations.SerializedName

data class ComicChapter(
    @SerializedName("title")
    val title: String? = null, // 连载
    @SerializedName("data")
    val list: List<ComicChapterItem>? = null
)

data class ComicChapterItem(
    @SerializedName("chapter_id")
    val chapterId: Int = 0, // 102432
    @SerializedName("chapter_title")
    val chapterTitle: String? = null, // 85话
    @SerializedName("updatetime")
    val updatetime: Int = 0, // 1598781565
    @SerializedName("filesize")
    val filesize: Int = 0, // 7314083
    @SerializedName("chapter_order")
    val chapterOrder: Int? = null // 950
)
