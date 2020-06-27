package com.khanhlh.substationmonitor.ui.register

import android.annotation.SuppressLint
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.khanhlh.substationmonitor.MyApp
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.base.BaseViewModel
import com.khanhlh.substationmonitor.extensions.get
import com.khanhlh.substationmonitor.extensions.init
import com.khanhlh.substationmonitor.extensions.set

class RegisterActivityViewModel : BaseViewModel<Any?>() {
    var mail = MutableLiveData<String>()
    var password = MutableLiveData<String>()
    var rePassword = MutableLiveData<String>()
    val onBackPress = MutableLiveData<Boolean>().init(false)
    val isRegisterSuccess = MutableLiveData<Boolean>().init(false)

    val registerClickListener = View.OnClickListener {
        tryRegister()
    }

    @SuppressLint("CheckResult")
    fun tryRegister() {
        if (password.get()!!.length > 6 && mail.get()!!.length > 4) {
            if (password.get() == rePassword.get()) {
                showLoading()
                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(mail.get()!!, password.get()!!)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            isRegisterSuccess.set(true)
                            errorMessage.value = MyApp.context.getString(R.string.login_success)
                        } else {
                            isRegisterSuccess.set(false)
                            errorMessage.value = task.exception?.localizedMessage
                        }
                        hideLoading()
                    }
            } else {
                errorMessage.value = MyApp.context.getString(R.string.require_length)
            }
        } else {
            errorMessage.value = MyApp.context.getString(R.string.require_length)
        }
    }

    private fun showLoading() {
        loadingVisibility.value = View.VISIBLE
    }

    private fun hideLoading() {
        loadingVisibility.value = View.INVISIBLE
    }
}

