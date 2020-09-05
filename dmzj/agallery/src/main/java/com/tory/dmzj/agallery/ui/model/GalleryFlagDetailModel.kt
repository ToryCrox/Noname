package com.tory.dmzj.agallery.ui.model


import com.google.gson.annotations.SerializedName

data class GalleryFlagDetailModel(
    @SerializedName("created_at")
    val createdAt: String = "",
    @SerializedName("flagged_by")
    val flaggedBy: String = "",
    @SerializedName("post_id")
    val postId: Int = 0,
    @SerializedName("reason")
    val reason: String = "",
    @SerializedName("user_id")
    val userId: String? = ""
)
