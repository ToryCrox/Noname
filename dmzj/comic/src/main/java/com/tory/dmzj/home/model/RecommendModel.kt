package com.tory.dmzj.home.model


import com.google.gson.annotations.SerializedName

data class RecommendMapModel(
    val map: Map<Int, RecommendModel>? = null
)

data class RecommendModel(
    @SerializedName("category_id")
    val categoryId: Int = 0, // 46
    @SerializedName("title")
    val title: String? = null, // 大图推荐
    @SerializedName("sort")
    val sort: Int? = null, // 1
    @SerializedName("data")
    val list: List<RecommendItemModel>? = null
)

data class RecommendItemModel(
    @SerializedName("cover")
    val cover: String? = null, // https://images.dmzj.com/tuijian/750_480/200826ltyjfb01.jpg
    @SerializedName("title")
    val title: String? = null, // 预约赢Switch豪礼
    @SerializedName("sub_title")
    val subTitle: String? = null, // 余烬风暴
    @SerializedName("type")
    val type: Int = 0, // 6
    @SerializedName("url")
    val url: String? = null, // http://p.longtugame.com/lthd/hd20082466lc1
    @SerializedName("obj_id")
    val objId: Int = 0, // 0
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("statue")
    val statue: String? = null,
    @SerializedName("id")
    val id: Int = 0, //42108
    @SerializedName("authors")
    val authors: String? = null //RIO/猫子

) {
    fun toComic(): RecommendComicModel
        = RecommendComicModel(
            id = if (id != 0) id else objId,
            cover = cover,
            title = title,
            subTitle = if (!authors.isNullOrEmpty()) authors else subTitle,
            type = type,
            url = url,
            status = status
        )

    fun toTopic(): RecommendTopicModel
        = RecommendTopicModel(
        id = if (id != 0) id else objId,
        cover = cover,
        title = title,
        subTitle = if (!authors.isNullOrEmpty()) authors else subTitle,
        type = type,
        url = url,
        status = status
    )
}

data class RecommendTitleModel(
    val title: String,
    val categoryId: Int
)

data class RecommendBannerModel(
    val list: List<RecommendItemModel>?
)

data class RecommendComicModel(
    val id: Int = 0,
    val cover: String? = null, // https://images.dmzj.com/tuijian/750_480/200826ltyjfb01.jpg
    val title: String? = null, // 预约赢Switch豪礼
    val subTitle: String? = null, // 余烬风暴
    val type: Int? = null, // 6
    val url: String? = null, // http://p.longtugame.com/lthd/hd20082466lc1
    val status: String? = null
)

data class RecommendTopicModel(
    val id: Int = 0,
    val cover: String? = null, // https://images.dmzj.com/tuijian/750_480/200826ltyjfb01.jpg
    val title: String? = null, // 预约赢Switch豪礼
    val subTitle: String? = null, // 余烬风暴
    val type: Int? = null, // 6
    val url: String? = null, // http://p.longtugame.com/lthd/hd20082466lc1
    val status: String? = null
)
