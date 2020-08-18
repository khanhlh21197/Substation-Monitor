package com.khanhlh.substationmonitor.ui.login

import android.annotation.SuppressLint
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.khanhlh.substationmonitor.MyApp
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.base.BaseViewModel
import com.khanhlh.substationmonitor.extensions.*
import com.khanhlh.substationmonitor.model.ThietBiResponse
import com.khanhlh.substationmonitor.model.UserTest
import com.khanhlh.substationmonitor.mqtt.MqttHelper


class LoginActivityViewModel() : BaseViewModel<Any?>() {
    val registerButtonClicked = MutableLiveData<Boolean>()
    val isLoginSuccess = MutableLiveData<Boolean>().init(false)
    val mail = MutableLiveData<String>()
    val password = MutableLiveData<String>()

    val isChecked = MutableLiveData<Boolean>()

    var userJson = MutableLiveData<String>()
    var response = MutableLiveData<ThietBiResponse>()

    lateinit var mqttHelper: MqttHelper
    private var macAddress = ""

    val registerClickListener = View.OnClickListener {
        registerButtonClicked.value = true
    }

    init {
        connectMqtt()
    }

    private fun connectMqtt() {
        macAddress = getMacAddr()!!
        mqttHelper = MqttHelper(MyApp.applicationContext())
        macAddress.let {
            mqttHelper.connect(it, messageCallBack = object : MqttHelper.MessageCallBack {
                override fun onSuccess(message: String) {
                    val baseResponse = fromJson<ThietBiResponse>(message)
                    hideLoading()
                    if ("0" == baseResponse.errorCode && "true" == baseResponse.result) {
                        isLoginSuccess.set(true)
//                        id = baseResponse.id!!
//                        if (isChecked.get()!!)
//                            saveUser()
                            hideLoading()
                        response.value = (baseResponse)
                    } else {
                        isLoginSuccess.set(false)
                        hideLoading()
                    }
                }

                override fun onError(error: Throwable) {
                    errorMessage.set(error.toString())
                }
            })
        }
    }

    fun showLoading() {
        loadingVisibility.value = View.VISIBLE
    }

    fun hideLoading() {
        loadingVisibility.value = View.INVISIBLE
    }

    @SuppressLint("CheckResult")
    fun tryLogin() {
        showLoading()
        if (password.get()!!.length > 5 && mail.get()!!.length > 5) {
            val user = macAddress.let {
                UserTest(
                    mail.get()!!, password.get()!!, mac = macAddress
                )
            }
            userJson.value = (toJson(user))
            mqttHelper.publishMessage("loginuser", toJson(user)!!).subscribe()
        } else {
            errorMessage.value =
                getStringSrc(R.string.require_length)
        }
    }
}

