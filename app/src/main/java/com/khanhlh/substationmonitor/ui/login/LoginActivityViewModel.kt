package com.khanhlh.substationmonitor.ui.login


import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.khanhlh.substationmonitor.api.FirebaseCommon
import com.khanhlh.substationmonitor.base.BaseViewModel
import com.khanhlh.substationmonitor.extensions.init
import com.khanhlh.substationmonitor.extensions.logD
import com.khanhlh.substationmonitor.extensions.set
import com.khanhlh.substationmonitor.utils.USER_COLLECTION


class LoginActivityViewModel : BaseViewModel<Any?>() {


    val registerButtonClicked = MutableLiveData<Boolean>()
    val isLoginSuccess = MutableLiveData<Boolean>().init(false)
    var mail: String = ""
    var password: String = ""

    val loginClickListener = View.OnClickListener {
        tryLogin()
    }

    val registerClickListener = View.OnClickListener {
        registerButtonClicked.value = true
    }


    fun afterMailChange(s: CharSequence) {
        mail = s.toString()
    }

    fun afterPasswordChange(s: CharSequence) {
        password = s.toString()
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
        if (password.length > 5 && mail.length > 5) {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(
                mail,
                password
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
            errorMessage.value = "Needs > 6"
        }
    }
}

