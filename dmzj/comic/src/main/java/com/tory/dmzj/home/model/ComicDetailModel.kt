package com.tory.dmzj.home.model


import com.google.gson.annotations.SerializedName

data class ComicDetailModel(
        @SerializedName("id")
        val id: Long = 0, // 28653
        @SerializedName("islong")
        val islong: Int? = null, // 2
        @SerializedName("direction")
        val direction: Int? = null, // 1
        @SerializedName("title")
        val title: String? = null, // 祭品公主与兽之王
        @SerializedName("is_dmzj")
        val isDmzj: Int? = null, // 0
        @SerializedName("cover")
        val cover: String? = null, // https://images.dmzj.com/webpic/12/jpgzyszw20200420.jpg
        @SerializedName("description")
        val description: String? = null, // 这个国家过去存在著吞食人类支配大地的异形一族，还有统治著魔族并吞噬祭品的国王──还记得那部《满月下的孤狼》吗？作者又一部讲述魔王的故事~这次魔王和她的祭品公主演绎美女与野兽~我的外表只是为了霸气！
        @SerializedName("last_updatetime")
        val lastUpDatetime: Int? = null, // 1598781565 上次更新时间
        @SerializedName("last_update_chapter_name")
        val lastUpdateChapterName: String? = null, // 第85话
        @SerializedName("copyright")
        val copyright: Int? = null, // 0
        @SerializedName("first_letter")
        val firstLetter: String? = null, // j
        @SerializedName("comic_py")
        val comicPy: String? = null, // jipingongzhuyushouzhiwang
        @SerializedName("hidden")
        val hidden: Int? = null, // 0
        @SerializedName("hot_num")
        val hotNum: Int? = null, // 34418570   人气
        @SerializedName("hit_num")
        val hitNum: Int? = null, // 77381197
        @SerializedName("uid")
        val uid: Any? = null, // null
        @SerializedName("is_lock")
        val isLock: Int? = null, // 0
        @SerializedName("last_update_chapter_id")
        val lastUpdateChapterId: Int? = null, // 102432
        @SerializedName("types")
        val types: List<ComicType>? = null,
        @SerializedName("status")
        val status: List<ComicStatus>? = null,
        @SerializedName("authors")
        val authors: List<ComicAuthor>? = null,
        @SerializedName("subscribe_num")
        val subscribeNum: Int? = null, // 167986  订阅
        @SerializedName("chapters")
        val chapters: List<ComicChapter>? = null,
        @SerializedName("comment")
        val comment: ComicComment? = null,
        @SerializedName("is_need_login")
        val isNeedLogin: Int? = null, // 0
        @SerializedName("url_links")
        val urlLinks: List<String>? = null,
        @SerializedName("isHideChapter")
        val isHideChapter: String? = null, // 0
        @SerializedName("dh_url_links")
        val dhUrlLinks: List<DhUrlLink>? = null,
        @SerializedName("is_dot")
        val isDot: String? = null // 0
) {
    fun toHeader(): ComicDetailHeaderModel = ComicDetailHeaderModel(
            id = id,
            title = title.orEmpty(),
            cover = cover.orEmpty(),
            authors = authors.orEmpty(),
            types = types.orEmpty(),
            status = status.orEmpty(),
            hotNum = hotNum ?: 0,
            subscribeNum = subscribeNum ?: 0,
            lastUpDatetime = lastUpDatetime ?: 0
    )

    fun toDesc(): ComicDetailDescModel = ComicDetailDescModel(
            text =  description.orEmpty()
    )
}


data class ComicDetailHeaderModel(
        val id: Long = 0,
        val title: String = "", // 祭品公主与兽之王
        val cover: String? = null, // https://images.dmzj.com/webpic/12/jpgzyszw20200420.jpg
        val authors: List<ComicAuthor> = emptyList(),
        val types: List<ComicType> = emptyList(),
        val status: List<ComicStatus> = emptyList(),
        val hotNum: Int = 0, //人气
        val subscribeNum: Int = 0, // 167986  订阅
        val lastUpDatetime: Int = 0 // 1598781565 上次更新时间
)

data class ComicDetailDescModel(
        val text: String
)
