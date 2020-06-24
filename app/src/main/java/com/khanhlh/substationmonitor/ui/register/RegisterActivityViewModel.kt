package com.khanhlh.substationmonitor.ui.register

import android.annotation.SuppressLint
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.khanhlh.substationmonitor.MyApp
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.base.BaseViewModel
import com.khanhlh.substationmonitor.extensions.init
import com.khanhlh.substationmonitor.extensions.set

class RegisterActivityViewModel : BaseViewModel<Any?>() {
    var mail: String = ""
    var password: String = ""
    val onBackPress = MutableLiveData<Boolean>().init(false)
    val isRegisterSuccess = MutableLiveData<Boolean>().init(false)

    val registerClickListener = View.OnClickListener {
        tryRegister()
    }


    fun afterMailChange(s: CharSequence) {
        mail = s.toString()
        println(mail)
    }

    fun afterPasswordChange(s: CharSequence) {
        password = s.toString()
    }

    @SuppressLint("CheckResult")
    fun tryRegister() {
        if (password.length > 6 && mail.length > 4) {
            showLoading()
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(mail, password)
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
    }

    private fun showLoading() {
        loadingVisibility.value = View.VISIBLE
    }

    private fun hideLoading() {
        loadingVisibility.value = View.INVISIBLE
    }
}

