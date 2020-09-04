package com.tory.dmzj.comic.model


import com.google.gson.annotations.SerializedName

data class UrlLinkModel(
    @SerializedName("title")
    val title: String? = null, // 网页端
    @SerializedName("list")
    val list: List<Any>? = null
)
