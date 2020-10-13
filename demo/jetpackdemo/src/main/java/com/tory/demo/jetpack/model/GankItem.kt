package com.tory.demo.jetpack.model

import com.google.gson.annotations.SerializedName

data class GankItem(
    @SerializedName("createdAt")
    var createdAt: String? = null,
    @SerializedName("publishedAt")
    var publishedAt: String? = null,
    @SerializedName("_id")
    var id: String? = null,
    @SerializedName("source")
    var source: String? = null,
    @SerializedName("used")
    var isUsed: Boolean = false,
    @SerializedName("type")
    var type: String? = null,
    @SerializedName("url")
    var url: String? = null,
    @SerializedName("desc")
    var desc: String? = null,
    @SerializedName("who")
    var who: String? = null
)
