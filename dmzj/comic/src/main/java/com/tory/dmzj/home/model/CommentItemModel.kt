package com.tory.dmzj.home.model


import com.google.gson.annotations.SerializedName

data class CommentCollectModel(
        @SerializedName("commentIds")
        val commentIds: List<String>? = null,
        @SerializedName("comments")
        val comments: Map<String, CommentItemModel>? = null
)
//commentId
/**
 *  [
"30087353,30071694,30049892,30023235,30010638,29989476,29985360,29965130,29964072,29943660,29940723,29932018",
]
 */

data class CommentItemModel(
    @SerializedName("avatar_url")
    val avatarUrl: String? = null, // https://avatar.dmzj.com/99/d4/99d4345211833e789f1cc3687af7e31f.png
    @SerializedName("content")
    val content: String? = null, // 太少了啊
    @SerializedName("create_time")
    val createTime: String? = null, // 1598894835
    @SerializedName("id")
    val id: String? = null, // 30071694
    @SerializedName("is_goods")
    val isGoods: String? = null, // 0
    @SerializedName("like_amount")
    val likeAmount: String? = null, // 0
    @SerializedName("nickname")
    val nickname: String? = null, // 封锁心一，
    @SerializedName("obj_id")
    val objId: String? = null, // 44931
    @SerializedName("origin_comment_id")
    val originCommentId: String? = null, // 0
    @SerializedName("sender_uid")
    val senderUid: String? = null, // 108721935
    @SerializedName("sex")
    val sex: String? = null, // 1
    @SerializedName("upload_images")
    val uploadImages: String? = null
)