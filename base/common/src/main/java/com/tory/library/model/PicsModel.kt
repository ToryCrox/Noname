package com.tory.library.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/9/5
 * Description: 大图Model
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/9/5 xutao 1.0
 * Why & What is modified:
 */
@Parcelize
data class PicsModel(
    val items: List<PicItemModel>,
    val index: Int
): Parcelable

@Parcelize
data class PicItemModel(
    val url: String,
    val previewUrl: String? = null
):Parcelable
