package com.khanhlh.substationmonitor.ui.register

import android.annotation.SuppressLint
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentReference
import com.google.gson.Gson
import com.khanhlh.substationmonitor.MyApp
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.api.FirebaseCommon
import com.khanhlh.substationmonitor.base.BaseViewModel
import com.khanhlh.substationmonitor.extensions.*
import com.khanhlh.substationmonitor.model.NhaResponse
import com.khanhlh.substationmonitor.model.UserTest
import com.khanhlh.substationmonitor.mqtt.MqttHelper
import com.khanhlh.substationmonitor.utils.EMAIL
import com.khanhlh.substationmonitor.utils.ID
import com.khanhlh.substationmonitor.utils.USER_COLLECTION
import io.reactivex.Observable
import kotlin.collections.set

class RegisterActivityViewModel() : BaseViewModel<Any?>() {
    var mail = MutableLiveData<String>()
    var password = MutableLiveData<String>()
    var rePassword = MutableLiveData<String>()
    var name = MutableLiveData<String>()
    var phoneNumber = MutableLiveData<String>()
    var home = MutableLiveData<String>()
    val isRegisterSuccess = MutableLiveData<Boolean>()

    lateinit var user: UserTest

    private lateinit var gson: Gson
    lateinit var mqttHelper: MqttHelper
    private var macAddress = ""

    init {
        initMqtt()
    }

    val registerClickListener = View.OnClickListener {
        tryRegister()
    }

    @SuppressLint("CheckResult")
    fun tryRegister() {
        if (mail.get().isNullOrEmpty()) {
            errorMessage.set(getStringSrc(R.string.not_enough))
            return
        }
        if (password.get().isNullOrEmpty()) {
            errorMessage.set(getStringSrc(R.string.not_enough))
            return
        }
        if (name.get().isNullOrEmpty()) {
            errorMessage.set(getStringSrc(R.string.not_enough))
            return
        }
        if (home.get().isNullOrEmpty()) {
            errorMessage.set(getStringSrc(R.string.not_enough))
            return
        }
        if (phoneNumber.get().isNullOrEmpty()) {
            errorMessage.set(getStringSrc(R.string.not_enough))
            return
        }
        if (rePassword.get().isNullOrEmpty()) {
            errorMessage.set(getStringSrc(R.string.not_enough))
            return
        }
        if (password.get()!!.length > 6 && mail.get()!!.length > 4) {
            if (password.get() == rePassword.get()) {
                showLoading()
                user = UserTest(
                    mail.get()!!,
                    password.get()!!,
                    name.get()!!,
                    phoneNumber.get()!!,
                    home.get()!!,
                    macAddress
                )
                mqttHelper.publishMessage("registeruser", gson.toJson(user)).subscribe()
            } else {
                errorMessage.set(getStringSrc(R.string.error_re_password))
            }
        } else {
            errorMessage.set(getStringSrc(R.string.require_length))
        }
    }

    fun showLoading() {
        loadingVisibility.value = View.VISIBLE
    }

    fun hideLoading() {
        loadingVisibility.value = View.INVISIBLE
    }

    @SuppressLint("CheckResult")
    fun insertUserToFireStore(): Observable<DocumentReference> {
        val currentUser = FirebaseCommon.currentUser!!
        val email = currentUser.email
        val id = currentUser.uid
        val user: HashMap<String, String> = HashMap()
        user[ID] = id
        user[EMAIL] = email!!
        return FirebaseCommon.push(USER_COLLECTION, id, user)
    }

    private fun initMqtt() {
        macAddress = getMacAddr()!!

        gson = Gson()
        mqttHelper = MqttHelper(MyApp.applicationContext())
        macAddress.let {
            mqttHelper.connect(it, messageCallBack = object : MqttHelper.MessageCallBack {
                override fun onSuccess(message: String) {
                    val nhaResponse = fromJson<NhaResponse>(message)
                    hideLoading()
                    if ("0" == nhaResponse.errorCode && "true" == nhaResponse.result) {
                        isRegisterSuccess.value = true
                        errorMessage.set(
                            getStringSrc(R.string.register_success)
                        )
                    } else {
                        isRegisterSuccess.value = false
                        errorMessage.set(getStringSrc(R.string.register_error))
                    }
                }

                override fun onError(error: Throwable) {
                    errorMessage.set(error.toString())
                }
            })
        }
    }
}

