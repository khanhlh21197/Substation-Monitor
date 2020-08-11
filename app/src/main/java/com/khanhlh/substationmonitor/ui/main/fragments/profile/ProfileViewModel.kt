package com.khanhlh.substationmonitor.ui.main.fragments.profile

import android.view.View
import androidx.lifecycle.MutableLiveData
import com.khanhlh.substationmonitor.MyApp
import com.khanhlh.substationmonitor.api.FirebaseCommon
import com.khanhlh.substationmonitor.base.BaseViewModel
import com.khanhlh.substationmonitor.extensions.set
import com.khanhlh.substationmonitor.model.User
import com.khanhlh.substationmonitor.utils.*
import io.reactivex.disposables.Disposable

class ProfileViewModel(app: MyApp) : BaseViewModel<Any>(app) {
    var user = MutableLiveData<User>()

    fun getProfile(): Disposable? = FirebaseCommon.getProfile()
        .subscribe {
            var id: String = ""
            var email: String = ""
            var name: String = ""
            var phoneNumber: String = ""
            var devices: String = ""
            if (it[ID] != null) id = it[ID].toString()
            if (it[EMAIL] != null) email = it[EMAIL].toString()
            if (it[NAME] != null) name = it[NAME].toString()
            if (it[PHONE_NUMBER] != null) phoneNumber = it[PHONE_NUMBER].toString()
            if (it[DEVICES] != null) devices = it[DEVICES].toString()
            user.set(
                User(id, email, name, phoneNumber, devices)
            )
        }

    fun logOut() {}

    fun showLoading() {
        loadingVisibility.value = View.VISIBLE
    }

    fun hideLoading() {
        loadingVisibility.value = View.INVISIBLE
    }
}