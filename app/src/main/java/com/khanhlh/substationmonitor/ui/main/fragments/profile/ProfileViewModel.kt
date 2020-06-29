package com.khanhlh.substationmonitor.ui.main.fragments.profile

import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.khanhlh.substationmonitor.api.FirebaseCommon
import com.khanhlh.substationmonitor.base.BaseViewModel
import com.khanhlh.substationmonitor.model.User
import com.khanhlh.substationmonitor.utils.*
import io.reactivex.Single
import io.reactivex.disposables.Disposable

class ProfileViewModel : BaseViewModel<Any>() {
    var user = MutableLiveData<User>()

    fun getProfile(): Disposable? = FirebaseCommon.getProfile()
        .subscribe {
            user.value = User(
                it[ID] as String,
                it[EMAIL] as String,
                it[NAME] as String,
                it[PHONE_NUMBER] as String,
                it[DEVICES] as String
            )
        }

    fun logOut(){}
}