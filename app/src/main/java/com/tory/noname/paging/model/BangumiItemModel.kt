package com.tory.noname.paging.model

import com.google.gson.annotations.SerializedName

data class BangumiItemModel(
    val title: String? = null, // Engage Kiss
    val titleTranslate: TitleTranslateModel? = null,
    val type: String? = null, // tv
    val lang: String? = null, // ja
    val officialSite: String? = null, // https://engage-kiss.com/
    val begin: String? = null, // 2022-07-02T15:30:00.000Z
    val broadcast: String? = null, // R/2022-07-02T15:30:00.000Z/P7D
    val end: String? = null,
    val comment: String? = null,
    val sites: List<BangumiSiteModel>? = null,
    val id: String? = null, // 4a1fb49d5c5a36c9ca10aab215daa839
    val pinyinTitles: List<String?>? = null
)


data class TitleTranslateModel(
    @SerializedName("zh-Hans")
    val zh: List<String>? = null
)

data class BangumiSiteModel(
    val site: String? = null, // bangumi
    val id: String? = null, // 375817
    val begin: String? = null, // 2022-07-02T17:00:00.000Z
    val broadcast: String? = null // R/2022-07-02T17:00:00.000Z/P7D
)