package com.khanhlh.substationmonitor.ui.register

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.khanhlh.substationmonitor.MyApp
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.base.BaseActivity
import com.khanhlh.substationmonitor.databinding.ActivityRegisterBinding
import com.khanhlh.substationmonitor.di.ViewModelFactory
import com.khanhlh.substationmonitor.extensions.*
import com.khanhlh.substationmonitor.model.NhaResponse
import com.khanhlh.substationmonitor.model.UserTest
import com.khanhlh.substationmonitor.mqtt.MqttHelper
import com.khanhlh.substationmonitor.ui.login.LoginActivity
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : BaseActivity<ActivityRegisterBinding, RegisterActivityViewModel>() {
    private lateinit var gson: Gson
    private lateinit var mqttHelper: MqttHelper
    private var macAddress = ""

    override fun initVariables() {
        bindView(R.layout.activity_register)
        baseViewModel = RegisterActivityViewModel(MyApp())
        baseViewModel.attachView(this)
        baseViewModel = ViewModelProvider(
            this,
            ViewModelFactory(this)
        ).get(RegisterActivityViewModel::class.java)
        binding.viewModel = baseViewModel

        initMqtt()
        checkConnection()
        btnRegister.setOnClickListener { tryRegister() }
        back.setOnClickListener { onBackPressed() }
        btnLogin.setOnClickListener { navigateToActivity(LoginActivity::class.java) }
    }

    private fun initMqtt() {
        macAddress = getMacAddr()!!

        gson = Gson()
        mqttHelper = MqttHelper(this)
        macAddress.let {
            mqttHelper.connect(it, messageCallBack = object : MqttHelper.MessageCallBack {
                override fun onSuccess(message: String) {
                    val it = fromJson<NhaResponse>(message)
                    if ("0" == it.errorCode && "true" == it.result) {
                        toast(getString(R.string.register_success))
                        navigateToActivity(LoginActivity::class.java)
                    } else {
                        toast(getString(R.string.register_error))
                    }
                }

                override fun onError(error: Throwable) {
                    toast(error.toString())
                }
            })
        }
    }

    override fun observeViewModel() {
        super.observeViewModel()
    }

    private fun tryRegister() {
        if (baseViewModel.password.get()!!.length > 6 && baseViewModel.mail.get()!!.length > 4) {
            if (baseViewModel.password.get() == baseViewModel.rePassword.get()) {
                baseViewModel.showLoading()

                val user = macAddress.let {
                    UserTest(
                        baseViewModel.mail.get()!!,
                        baseViewModel.password.get()!!,
                        baseViewModel.name.get()!!,
                        baseViewModel.phoneNumber.get()!!,
                        baseViewModel.home.get()!!,
                        it
                    )
                }
                logD(gson.toJson(user))
                mqttHelper.publishMessage("registeruser", gson.toJson(user)).subscribe()
            } else {
                baseViewModel.errorMessage.value = getString(R.string.error_re_password)
            }
        } else {
            baseViewModel.errorMessage.value = getString(R.string.require_length)
        }
    }

    override fun onStop() {
        super.onStop()
        mqttHelper.close()
        mqttHelper.isConnected.removeObservers(this)
    }

    private fun checkConnection() {
        mqttHelper.isConnected.observe(this, Observer<Boolean> { t ->
            if (t!!) {
                toast("Đã kết nối với Server")
                baseViewModel.hideLoading()
            } else {
                toast("Ngắt kết nối")
                baseViewModel.showLoading()
            }
        })
    }

}