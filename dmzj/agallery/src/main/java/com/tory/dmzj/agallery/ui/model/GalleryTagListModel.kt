package com.tory.dmzj.agallery.ui.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * @author tory
 * @create 2020/9/9
 * @Describe
 */
@Parcelize
data class GalleryTagListModel (
        val title: String,
        val list: List<GalleryTagItemModel>
): Parcelable

@Parcelize
data class GalleryTagItemModel (
        val text: String
): Parcelable