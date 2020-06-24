package com.khanhlh.substationmonitor.ui.register

import androidx.lifecycle.ViewModelProvider
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.base.BaseActivity
import com.khanhlh.substationmonitor.databinding.ActivityRegisterBinding
import com.khanhlh.substationmonitor.di.ViewModelFactory

class RegisterActivity : BaseActivity<ActivityRegisterBinding, RegisterActivityViewModel>() {
    override fun initVariables() {
        bindView(R.layout.activity_register)
        baseViewModel = RegisterActivityViewModel()
        baseViewModel.attachView(this)
        baseViewModel = ViewModelProvider(
            this,
            ViewModelFactory(this)
        ).get(RegisterActivityViewModel::class.java)
        baseDataBinding.viewModel = baseViewModel
    }

    override fun observeViewModel() {
        super.observeViewModel()
    }

}