package com.tory.dmzj.home.model


import com.google.gson.annotations.SerializedName

data class DhUrlLink(
    @SerializedName("title")
    val title: String? = null, // 网页端
    @SerializedName("list")
    val list: List<Any>? = null
)
