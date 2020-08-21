package com.tory.noname.koin

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tory.noname.koin.api.BaseModel
import com.tory.noname.koin.api.MainRepo
import kotlinx.coroutines.launch

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

    fun getCountryData() {
        // 在Activity销毁的时候，这里会自动关闭
        viewModelScope.launch {
            val data= repo.getCountryData()
            countryLiveData.value = data
        }
    }
}
