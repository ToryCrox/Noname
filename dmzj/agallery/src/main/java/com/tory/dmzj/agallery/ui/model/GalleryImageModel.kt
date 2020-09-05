package com.tory.dmzj.agallery.ui.model


import com.google.gson.annotations.SerializedName

data class GalleryImageModel(
    @SerializedName("actual_preview_height")
    val actualPreviewHeight: Int = 0,
    @SerializedName("actual_preview_width")
    val actualPreviewWidth: Int = 0,
    @SerializedName("author")
    val author: String = "",
    @SerializedName("change")
    val change: Int = 0,
    @SerializedName("created_at")
    val createdAt: Int = 0,
    @SerializedName("creator_id")
    val creatorId: Int = 0,
    @SerializedName("file_size")
    val fileSize: Int = 0,
    @SerializedName("file_url")
    val fileUrl: String = "",
    @SerializedName("flag_detail")
    val flagDetail: GalleryFlagDetailModel? = null,
    @SerializedName("frames")
    val frames: List<String> = listOf(),
    @SerializedName("frames_pending")
    val framesPending: List<String> = listOf(),
    @SerializedName("frames_pending_string")
    val framesPendingString: String = "",
    @SerializedName("frames_string")
    val framesString: String = "",
    @SerializedName("has_children")
    val hasChildren: Boolean = false,
    @SerializedName("height")
    val height: Int = 0,
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("is_held")
    val isHeld: Boolean = false,
    @SerializedName("is_shown_in_index")
    val isShownInIndex: Boolean = false,
    @SerializedName("jpeg_file_size")
    val jpegFileSize: Int = 0,
    @SerializedName("jpeg_height")
    val jpegHeight: Int = 0,
    @SerializedName("jpeg_url")
    val jpegUrl: String = "",
    @SerializedName("jpeg_width")
    val jpegWidth: Int = 0,
    @SerializedName("md5")
    val md5: String = "",
    @SerializedName("parent_id")
    val parentId: Any? = Any(),
    @SerializedName("preview_height")
    val previewHeight: Int = 0,
    @SerializedName("preview_url")
    val previewUrl: String = "",
    @SerializedName("preview_width")
    val previewWidth: Int = 0,
    @SerializedName("rating")
    val rating: String = "",
    @SerializedName("sample_file_size")
    val sampleFileSize: Int = 0,
    @SerializedName("sample_height")
    val sampleHeight: Int = 0,
    @SerializedName("sample_url")
    val sampleUrl: String = "",
    @SerializedName("sample_width")
    val sampleWidth: Int = 0,
    @SerializedName("score")
    val score: Int = 0,
    @SerializedName("source")
    val source: String = "",
    @SerializedName("status")
    val status: String = "",
    @SerializedName("tags")
    val tags: String = "",
    @SerializedName("width")
    val width: Int = 0
) {
    var index: Int = 0
    var allImages: List<GalleryImageModel>? = null
}
