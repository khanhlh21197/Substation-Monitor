package com.khanhlh.substationmonitor.ui.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.khanhlh.substationmonitor.MyApp
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.base.BaseActivity
import com.khanhlh.substationmonitor.databinding.ActivityLoginBinding
import com.khanhlh.substationmonitor.di.ViewModelFactory
import com.khanhlh.substationmonitor.extensions.*
import com.khanhlh.substationmonitor.helper.shared_preference.clear
import com.khanhlh.substationmonitor.helper.shared_preference.get
import com.khanhlh.substationmonitor.helper.shared_preference.put
import com.khanhlh.substationmonitor.model.BaseResponse
import com.khanhlh.substationmonitor.model.Nha
import com.khanhlh.substationmonitor.model.NhaResponse
import com.khanhlh.substationmonitor.model.UserTest
import com.khanhlh.substationmonitor.mqtt.MqttHelper
import com.khanhlh.substationmonitor.ui.main.MainActivity
import com.khanhlh.substationmonitor.ui.register.RegisterActivity
import com.khanhlh.substationmonitor.utils.USER_PREF
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : BaseActivity<ActivityLoginBinding, LoginActivityViewModel>() {
    private lateinit var vm: LoginActivityViewModel
    private lateinit var sharedPref: SharedPreferences
    private lateinit var mqttHelper: MqttHelper
    private lateinit var gson: Gson
    private lateinit var macAddress: String
    private lateinit var id: String

    override fun initVariables() {
        baseViewModel = LoginActivityViewModel(MyApp())
        baseViewModel.apply { }
        baseViewModel.attachView(this)
        baseViewModel = ViewModelProvider(this, ViewModelFactory(this))
            .get(LoginActivityViewModel::class.java)
        vm = baseViewModel
        bindView(R.layout.activity_login)
        binding.viewModel = vm
        sharedPref = getSharedPreferences(USER_PREF, Context.MODE_PRIVATE)
        getUser()
        onSwitchListener()

        initMqtt()
    }

    private fun initMqtt() {
        macAddress = getMacAddr()!!

        gson = Gson()
        mqttHelper = MqttHelper(this)

        connectMqtt()

        btLogin.setOnClickListener {
            baseViewModel.showLoading()
            tryLogin()
        }
    }

    private fun connectMqtt() {
        macAddress.let {
            mqttHelper.connect(it, messageCallBack = object : MqttHelper.MessageCallBack {
                override fun onSuccess(message: String) {
                    val baseResponse = Gson().fromJson<NhaResponse>(message)
                    if ("0" == baseResponse.errorCode && "true" == baseResponse.result) {
                        baseViewModel.isLoginSuccess.set(true)
//                        id = baseResponse.id!!
                        if (switchSaveUser.isChecked) saveUser()
                        navigateToActivity(MainActivity::class.java, baseResponse)
                    } else {
                        baseViewModel.isLoginSuccess.set(false)
                    }
                    baseViewModel.hideLoading()
                }

                override fun onError(error: Throwable) {
                    toast(error.toString())
                }
            })
        }
    }

    private fun onSwitchListener() {
        switchSaveUser.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.put(SWITCH_STATE, isChecked)
            if (isChecked) {
                if (FirebaseAuth.getInstance().currentUser != null) {
                    startActivity(
                        Intent(
                            this,
                            MainActivity::class.java
                        )
                    )
                }
            } else {
                sharedPref.clear(this, USER_PREF)
            }
        }
    }

    private fun getUser() {
        vm.mail.set(sharedPref.get(USER_EMAIL, DEFAULT_EMAIL))
        vm.password.set(sharedPref.get(USER_PASSWORD, DEFAULT_PASSWORD))
        switchSaveUser.isChecked = sharedPref.get(SWITCH_STATE, false)
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
    }

    companion object {
        private const val USER_EMAIL = "USER_EMAIL";
        private const val USER_PASSWORD = "USER_PASSWORD";
        private const val SWITCH_STATE = "SWITCH_STATE";
        private const val DEFAULT_EMAIL = "";
        private const val DEFAULT_PASSWORD = "";
    }

    @SuppressLint("CheckResult")
    fun tryLogin() {
        baseViewModel.showLoading()
        if (baseViewModel.password.get()!!.length > 5 && baseViewModel.mail.get()!!.length > 5) {
            val user = macAddress.let {
                UserTest(
                    baseViewModel.mail.get()!!, baseViewModel.password.get()!!, mac = macAddress
                )
            }
            mqttHelper.publishMessage("loginuser", gson.toJson(user)).subscribe()
        } else {
            baseViewModel.errorMessage.value = MyApp.context.getString(R.string.require_length)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mqttHelper.close()
    }

    override fun onStop() {
        super.onStop()
        mqttHelper.close()
    }

}




