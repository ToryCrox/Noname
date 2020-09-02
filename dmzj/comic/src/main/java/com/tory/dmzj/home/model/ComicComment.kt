package com.tory.dmzj.home.model


import com.google.gson.annotations.SerializedName

data class ComicComment(
    @SerializedName("comment_count")
    val commentCount: Int? = null, // 9270
    @SerializedName("latest_comment")
    val latestComment: List<CommentModel>? = null
)

data class CommentModel(
        @SerializedName("comment_id")
        val commentId: Int? = null, // 30050379
        @SerializedName("uid")
        val uid: Int? = null, // 101972852
        @SerializedName("content")
        val content: String? = null, // 王！
        @SerializedName("createtime")
        val createtime: Int = 0, // 1598803380
        @SerializedName("nickname")
        val nickname: String? = null, // 落叶隐蝶
        @SerializedName("avatar")
        val avatar: String? = null // https://avatar.dmzj.com/17/88/178819e7c1b5143751e7332183959a82.png
)



