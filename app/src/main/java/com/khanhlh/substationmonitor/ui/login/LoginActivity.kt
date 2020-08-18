package com.khanhlh.substationmonitor.ui.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.base.BaseActivity
import com.khanhlh.substationmonitor.databinding.ActivityLoginBinding
import com.khanhlh.substationmonitor.di.ViewModelFactory
import com.khanhlh.substationmonitor.extensions.get
import com.khanhlh.substationmonitor.extensions.goToActivity
import com.khanhlh.substationmonitor.extensions.navigateToActivity
import com.khanhlh.substationmonitor.extensions.set
import com.khanhlh.substationmonitor.helper.shared_preference.get
import com.khanhlh.substationmonitor.helper.shared_preference.put
import com.khanhlh.substationmonitor.model.UserTest
import com.khanhlh.substationmonitor.ui.main.MainActivity
import com.khanhlh.substationmonitor.ui.register.RegisterActivity
import com.khanhlh.substationmonitor.utils.KEY_SERIALIZABLE
import com.khanhlh.substationmonitor.utils.USER_PREF
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : BaseActivity<ActivityLoginBinding, LoginActivityViewModel>() {
    private lateinit var vm: LoginActivityViewModel
    private lateinit var sharedPref: SharedPreferences

    override fun initVariables() {
        baseViewModel = LoginActivityViewModel()
        baseViewModel.apply { }
        baseViewModel.attachView(this)
        baseViewModel = ViewModelProvider(this, ViewModelFactory(this))
            .get(LoginActivityViewModel::class.java)
        vm = baseViewModel
        bindView(R.layout.activity_login)
        binding.viewModel = vm
        sharedPref = getSharedPreferences(USER_PREF, Context.MODE_PRIVATE)
        getUser()
    }

    override fun onResume() {
        super.onResume()
        checkConnection()
    }

    @SuppressLint("ResourceType")
    private fun checkConnection() {
        baseViewModel.mqttHelper.isConnected.observe(this, Observer<Boolean> { t ->
            if (t!!) {
                showError(getString(R.string.connected))
                baseViewModel.hideLoading()
            } else {
                showError(getString(R.string.disconnected))
                baseViewModel.showLoading()
            }
        })
    }

    private fun getUser() {
        vm.mail.set(sharedPref.get(USER_EMAIL, DEFAULT_EMAIL))
        vm.password.set(sharedPref.get(USER_PASSWORD, DEFAULT_PASSWORD))
        switchSaveUser.isChecked = sharedPref.get(SWITCH_STATE, false)

        try {
            val user: UserTest = intent.getSerializableExtra(KEY_SERIALIZABLE) as UserTest
            vm.mail.set(user.email)
            vm.password.set(user.pass)
        } catch (e: TypeCastException) {
            e.printStackTrace()
        }
    }

    private fun saveUser() {
        sharedPref.put(USER_EMAIL, vm.mail.get())
        sharedPref.put(USER_PASSWORD, vm.password.get())
    }

    override fun observeViewModel() {
        super.observeViewModel()
        baseViewModel.registerButtonClicked.observe(this,
            Observer<Boolean> { t ->
                if (t!!) {
                    navigateToActivity(RegisterActivity::class.java)
                }
            })
        btRegister.setOnClickListener { navigateToActivity(RegisterActivity::class.java) }

        baseViewModel.isLoginSuccess.observe(this, Observer<Boolean> {
            if (it) {
                val bundle = Bundle()
                bundle.putSerializable("thietbi", baseViewModel.response.get()!!)
                bundle.putString("user", baseViewModel.userJson.get()!!)
                goToActivity(MainActivity::class.java, bundle)
            }
        })
    }

    companion object {
        const val USER_EMAIL = "USER_EMAIL";
        const val USER_PASSWORD = "USER_PASSWORD";
        private const val SWITCH_STATE = "SWITCH_STATE";
        const val DEFAULT_EMAIL = "";
        const val DEFAULT_PASSWORD = "";
    }

    override fun onPause() {
        super.onPause()
        baseViewModel.mqttHelper.close()
        baseViewModel.mqttHelper.isConnected.removeObservers(this)
    }

}




