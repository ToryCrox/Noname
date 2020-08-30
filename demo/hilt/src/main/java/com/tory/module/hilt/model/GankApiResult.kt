package com.tory.module.hilt.model

import com.google.gson.annotations.SerializedName

data class GankApiResult(
    @SerializedName("error")
    var isError: Boolean = false,
    @SerializedName("results")
    var results: List<GankItem>? = null
)
