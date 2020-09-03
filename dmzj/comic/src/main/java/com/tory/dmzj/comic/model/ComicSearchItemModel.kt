package com.tory.dmzj.comic.model


import com.google.gson.annotations.SerializedName

data class ComicSearchItemModel(
    @SerializedName("addtime")
    val addtime: Int? = null, // 0
    @SerializedName("alias_name")
    val aliasName: String? = null, // ライドンキング,骑乘大帝
    @SerializedName("authors")
    val authors: String? = null, // 马场康志
    @SerializedName("_biz")
    val biz: String? = null, // comicacg_comics
    @SerializedName("copyright")
    val copyright: Int? = null, // 0
    @SerializedName("cover")
    val cover: String? = null, // https://images.dmzj.com/webpic/6/190528qichengfml.jpg
    @SerializedName("device_show")
    val deviceShow: Int? = null, // 7
    @SerializedName("grade")
    val grade: Int? = null, // 0
    @SerializedName("hidden")
    val hidden: Int? = null, // 0
    @SerializedName("hot_hits")
    val hotHits: Int? = null, // 1
    @SerializedName("id")
    val id: Int = 0, // 44363
    @SerializedName("last_name")
    val lastName: String? = null, // 第25话
    @SerializedName("quality")
    val quality: Int? = null, // 1
    @SerializedName("status")
    val status: Int? = null, // 0
    @SerializedName("title")
    val title: String? = null, // 骑乘之王
    @SerializedName("types")
    val types: String? = null // 冒险/魔幻
)