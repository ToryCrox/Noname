package com.tory.jetpack.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @author tory
 * @create 2019/8/3
 * @Describe
 */
class ShoeModel : ViewModel(){

    private val brand = MutableLiveData<String>().apply {
        value = ALL
    }

    val shoes:LiveData<List<Shoe>> = brand.switchMap{

    }



    companion object {
        public const val ALL = "所有"

        public const val NIKE = "Nike"
        public const val ADIDAS = "Adidas"
        public const val OTHER = "other"
    }
}