package com.khanhlh.substationmonitor.ui.register

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.base.BaseActivity
import com.khanhlh.substationmonitor.databinding.ActivityRegisterBinding
import com.khanhlh.substationmonitor.di.ViewModelFactory
import com.khanhlh.substationmonitor.extensions.navigateToActivity
import com.khanhlh.substationmonitor.ui.login.LoginActivity
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : BaseActivity<ActivityRegisterBinding, RegisterActivityViewModel>() {

    override fun initVariables() {
        bindView(R.layout.activity_register)
        baseViewModel = RegisterActivityViewModel()
        baseViewModel.attachView(this)
        baseViewModel = ViewModelProvider(
            this,
            ViewModelFactory(this)
        ).get(RegisterActivityViewModel::class.java)
        binding.viewModel = baseViewModel

        checkConnection()
        back.setOnClickListener { onBackPressed() }
        btnLogin.setOnClickListener { navigateToActivity(LoginActivity::class.java) }
    }

    override fun observeViewModel() {
        super.observeViewModel()
        baseViewModel.isRegisterSuccess.observe(this,
            Observer<Boolean> {
                if (it) {
                    navigateToActivity(LoginActivity::class.java, baseViewModel.user)
                }
            })
    }

    override fun onStop() {
        super.onStop()
        baseViewModel.mqttHelper.close()
        baseViewModel.mqttHelper.isConnected.removeObservers(this)
    }

    private fun checkConnection() {
        baseViewModel.mqttHelper.isConnected.observe(this, Observer<Boolean> { t ->
            if (t!!) {
                showError("Đã kết nối với Server")
                baseViewModel.hideLoading()
            } else {
                showError("Ngắt kết nối")
                baseViewModel.showLoading()
            }
        })
    }

}