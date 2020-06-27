package com.khanhlh.substationmonitor.ui.login


import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.khanhlh.substationmonitor.MyApp
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.base.BaseViewModel
import com.khanhlh.substationmonitor.extensions.get
import com.khanhlh.substationmonitor.extensions.init
import com.khanhlh.substationmonitor.extensions.set


class LoginActivityViewModel : BaseViewModel<Any?>() {

    val registerButtonClicked = MutableLiveData<Boolean>()
    val isLoginSuccess = MutableLiveData<Boolean>().init(false)
    val mail = MutableLiveData<String>()
    val password = MutableLiveData<String>()

    val loginClickListener = View.OnClickListener {
        tryLogin()
    }

    val registerClickListener = View.OnClickListener {
        registerButtonClicked.value = true
    }

    private fun showLoading() {
        loadingVisibility.value = View.VISIBLE
    }

    private fun hideLoading() {
        loadingVisibility.value = View.INVISIBLE
    }

    @SuppressLint("CheckResult")
    fun tryLogin() {
        showLoading()
        if (password.get()!!.length > 5 && mail.get()!!.length > 5) {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(
                mail.get()!!,
                password.get()!!
            ).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    isLoginSuccess.set(true)
                    Log.d(TAG, "signInWithEmail:success")
                } else {
                    errorMessage.value = task.exception?.localizedMessage
                    isLoginSuccess.set(false)
                }
                hideLoading()
            }
        } else {
            errorMessage.value = MyApp.context.getString(R.string.require_length)
        }
    }
}

