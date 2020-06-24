package com.khanhlh.substationmonitor.ui.login

import android.content.Intent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.auth.FirebaseAuth
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.base.BaseActivity
import com.khanhlh.substationmonitor.databinding.ActivityLoginBinding
import com.khanhlh.substationmonitor.di.ViewModelFactory
import com.khanhlh.substationmonitor.extensions.navigateToActivity
import com.khanhlh.substationmonitor.ui.main.MainActivity
import com.khanhlh.substationmonitor.ui.register.RegisterActivity


class LoginActivity : BaseActivity<ActivityLoginBinding, LoginActivityViewModel>() {
    private lateinit var viewModel: LoginActivityViewModel

    override fun initVariables() {
        baseViewModel = LoginActivityViewModel()
        baseViewModel.apply { }
        baseViewModel.attachView(this)
        baseViewModel = ViewModelProviders.of(this, ViewModelFactory(this))
            .get(LoginActivityViewModel::class.java)
        viewModel = baseViewModel
        bindView(R.layout.activity_login)
        baseDataBinding.viewModel = viewModel

        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                )
            )
        }

    }

    override fun observeViewModel() {
        super.observeViewModel()
        viewModel.registerButtonClicked.observe(this, Observer {
            navigateToActivity(RegisterActivity::class.java)
        })

        viewModel.isLoginSuccess.observe(this, Observer {
            if (it) navigateToActivity(MainActivity::class.java)
        })
    }

}




