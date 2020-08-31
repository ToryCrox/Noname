package com.tory.dmzj.home.module.commic_detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tory.dmzj.home.api.ComicRepo
import kotlinx.coroutines.launch

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/8/31
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/8/31 xutao 1.0
 * Why & What is modified:
 */
class ComicDetailViewModel: ViewModel() {


    val result: MutableLiveData<List<Any>> = MutableLiveData()

    fun fetchData(id: Int) {
        viewModelScope.launch {
            val model = ComicRepo.getComicDetail(id)
            val list = mutableListOf<Any>()
            list.add(model.toHeader())

            result.value = list
        }
    }

}
