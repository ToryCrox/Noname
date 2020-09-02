package com.tory.dmzj.home.module.recommend

import android.util.ArrayMap
import androidx.lifecycle.MediatorLiveData
import com.shizhuang.duapp.common.component.module.ModuleSpaceModel
import com.tory.dmzj.home.BaseViewModel
import com.tory.dmzj.home.ComicConstant
import com.tory.dmzj.home.api.ComicRepo
import com.tory.dmzj.home.model.RecommendBannerModel
import com.tory.dmzj.home.model.RecommendMapModel
import com.tory.dmzj.home.model.RecommendModel
import com.tory.dmzj.home.model.RecommendTitleModel
import com.tory.library.log.LogUtils
import kotlinx.coroutines.async

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/8/30
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/8/30 xutao 1.0
 * Why & What is modified:
 */
class RecommendViewModel : BaseViewModel() {

    val topicCate = arrayOf(93, 48, 53, 55)

    private val recommendMap = ArrayMap<Int, RecommendModel>()

    val result: MediatorLiveData<List<Any>> = MediatorLiveData<List<Any>>()

    private var isReadCache = false

    override fun getCacheKey() = "comic_recommend"

    suspend fun fetchCache() {
        if (isReadCache) return
        isReadCache = true
        val mapModel = readCache<RecommendMapModel>()
        if (mapModel != null && !mapModel.map.isNullOrEmpty()) {
            recommendMap.putAll(mapModel.map)
            doResult()
        }
    }

    fun fetchData() {
        launchOnUI {
            fetchCache()

            val list = async {
                ComicRepo.getRecommendList()
            }
            val orderItem = async {
                ComicRepo.getRecommendUpdate(ComicConstant.CATE_ID_ORDER)
            }
            val likeItem = async {
                ComicRepo.getRecommendUpdate(ComicConstant.CATE_ID_MAY_LIKE)
            }
            val rList = list.await().orEmpty()
            for (recommendModel in rList) {
                recommendMap[recommendModel.categoryId] = recommendModel
            }
            recommendMap[ComicConstant.CATE_ID_ORDER] = orderItem.await()?.data
            recommendMap[ComicConstant.CATE_ID_MAY_LIKE] = likeItem.await()?.data
            doResult()

            writeCache(RecommendMapModel(recommendMap))
        }
    }

    private fun doResult() {
        val space = ModuleSpaceModel()
        val list = recommendMap.values.sortedBy { it.sort }.flatMap {
            if (it.sort == 1) {
                listOf(RecommendBannerModel(list = it.list))
            } else {
                val title = RecommendTitleModel(title = it.title.orEmpty(), categoryId = it.categoryId)
                listOf(space, title).plus(
                        if (topicCate.contains(it.categoryId)) {
                            it.list?.map { it.toTopic() }.orEmpty()
                        } else it.list?.map { it.toComic() }.orEmpty()
                )
            }
        }

        result.value = list
    }
}