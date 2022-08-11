package com.tory.dmzj.agallery.api

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.tory.dmzj.agallery.ui.model.GalleryImageModel
import com.tory.dmzj.agallery.ui.model.GalleryPageModel
import java.lang.RuntimeException

/**
 * - Author: tory
 * - Date: 2022/7/8
 * - Email: xutao@shizhuang-inc.com
 * - Description:
 */
class GalleryPagingSource(val tags: String? = null): PagingSource<Int, GalleryImageModel>() {

    override fun getRefreshKey(state: PagingState<Int, GalleryImageModel>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GalleryImageModel> {
        val page = params.key ?: 1
        val result = GalleryRepository.getPost(page = page, tags = tags)
        return if (result != null) {
            LoadResult.Page(
                data = result.list,
                prevKey = null,
                nextKey = page + 1
            )
        } else {
            LoadResult.Error(RuntimeException())
        }
    }
}