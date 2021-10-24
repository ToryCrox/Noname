package com.tory.demo.jetpack.databinding.model

import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt

class UserObservableModel {
    val firstName = ObservableField<String>()
    val lastName = ObservableField<String>()
    val age = ObservableInt()
}