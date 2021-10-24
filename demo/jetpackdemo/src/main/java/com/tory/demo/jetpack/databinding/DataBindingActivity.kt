package com.tory.demo.jetpack.databinding

import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.tory.demo.jetpack.R
import com.tory.demo.jetpack.databinding.model.DataBindingUser
import com.tory.demo.jetpack.databinding.model.UserObservableModel
import com.tory.library.base.BaseActivity

class DataBindingActivity: BaseActivity() {

    override fun onCreateView(savedInstanceState: Bundle?) {
        val binding = DataBindingUtil.setContentView<ActivityDatabindingBinding>(this, R.layout.activity_databinding)
        binding.lifecycleOwner = this
        binding.user = DataBindingUser("Test", "Biding")

        val observableModel = UserObservableModel()
        observableModel.firstName.set("My")
        binding.userObservable = observableModel
        binding.presenter = object : EventPresenter {
            override fun doChange() {
                Toast.makeText(this@DataBindingActivity, "Test", Toast.LENGTH_LONG)
                observableModel.firstName.set("My Test")
            }

        }
    }


    override fun initView(savedInstanceState: Bundle?) {

    }
}

interface EventPresenter {

    fun doChange()
}