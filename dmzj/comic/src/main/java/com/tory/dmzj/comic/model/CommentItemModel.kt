package com.tory.dmzj.comic.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tencent.smtt.utils.f
import com.tory.dmzj.dbase.NetHelper
import com.tory.library.utils.FileUtils
import kotlinx.android.parcel.Parcelize

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
/**
 * 评论
 */
@Parcelize
data class CommentItemModel(
    @SerializedName("avatar_url")
    val avatarUrl: String? = null, // https://avatar.dmzj.com/99/d4/99d4345211833e789f1cc3687af7e31f.png
    @SerializedName("content")
    val content: String? = null, // 太少了啊
    @SerializedName("create_time")
    val createTime: Long = 0, // 1598894835
    @SerializedName("id")
    val id: Long = 0, // 30071694
    @SerializedName("is_goods")
    val isGoods: String? = null, // 0
    @SerializedName("like_amount")
    val likeAmount: String? = null, // 0
    @SerializedName("nickname")
    val nickname: String? = null, // 封锁心一，
    @SerializedName("obj_id")
    val objId: Long = 0, // 44931
    @SerializedName("origin_comment_id")
    val originCommentId: String? = null, // 0
    @SerializedName("sender_uid")
    val senderUid: String? = null, // 108721935
    @SerializedName("sex")
    val sex: String? = null, // 1
    @SerializedName("upload_images")
    val uploadImages: String? = null
): Parcelable

data class CommentMainModel(
    val data: CommentItemModel
)

data class CommentSubModel(
    val data: CommentItemModel,
    val isFirst: Boolean = false,
    val isLast: Boolean =  false,
    val allSubItems: List<CommentItemModel>? = null
)

//评论图片
data class CommentImageModel(
    val imageName: String,
    val objId: Long
) {
    fun getImageUrl(isSmall: Boolean = false): String {
        val imageHost = "${NetHelper.COMMENT_IMAGE_BASE_URL}${objId % 500}/"
        val index = imageName.lastIndexOf(".")
        val finalName = if (index > 0 || isSmall) {
            imageName.substring(0, index) + "_small." + imageName.substring(index + 1)
        } else {
            imageName
        }
        return imageHost + finalName
    }
}

data class CommentImagesModel(
    val images: List<CommentImageModel>
)
