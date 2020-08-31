package com.tory.dmzj.home.model


import com.google.gson.annotations.SerializedName

data class ComicComment(
    @SerializedName("comment_count")
    val commentCount: Int? = null, // 9270
    @SerializedName("latest_comment")
    val latestComment: List<LatestComment>? = null
)
