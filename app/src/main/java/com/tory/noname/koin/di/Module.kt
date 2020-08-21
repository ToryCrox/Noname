package com.tory.noname.koin.di

import com.tory.noname.koin.MainViewModel
import com.tory.noname.koin.api.ApiRepo
import com.tory.noname.koin.api.MainRepo
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

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
val api = ApiRepo.api
val networkModule = module {
    single { api }
}
val repoModule = module {
    single {
        MainRepo(get())
    }
}

// ViewModelModule.kt
val viewmodelModule = module {
    viewModel {
        MainViewModel(get())
    }
}
