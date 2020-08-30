package com.tory.module.hilt

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tory.library.log.LogUtils
import com.tory.module.hilt.api.BaseModel
import com.tory.module.hilt.api.MainRepo
import com.tory.module.hilt.model.GankApiResult
import kotlinx.coroutines.launch
import java.lang.Exception

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/8/21
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/8/21 xutao 1.0
 * Why & What is modified:
 */
class MainViewModel(private val repo: MainRepo) : ViewModel() {

    var countryLiveData : MutableLiveData<BaseModel<List<String>>> = MutableLiveData()

    var gankData: MutableLiveData<GankApiResult> = MutableLiveData()

    //分类数据:  http://gank.io/api/data/数据类型/请求个数/第几页
    //数据类型： 福利 | Android | iOS | 休息视频 | 拓展资源 | 前端 | all
    //example:  http://gank.io/api/data/Android/10/1
    fun getGankData(tag: String) {
        viewModelScope.launch {
            try {
                val data= repo.getGankData(tag, 10, 1)
                gankData.value = data
            } catch (e: Exception){
                LogUtils.e("getGankData", e)
            }

        }
    }
}
