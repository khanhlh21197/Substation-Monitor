package com.khanhlh.substationmonitor.ui.main.fragments.home

import android.annotation.SuppressLint
import com.google.firebase.auth.FirebaseUser
import com.khanhlh.substationmonitor.api.FirebaseCommon
import com.khanhlh.substationmonitor.base.BaseViewModel
import com.khanhlh.substationmonitor.extensions.logD
import com.khanhlh.substationmonitor.utils.DEVICES
import com.khanhlh.substationmonitor.utils.EMAIL
import com.khanhlh.substationmonitor.utils.ID
import com.khanhlh.substationmonitor.utils.USER_COLLECTION
import io.reactivex.disposables.Disposable
import kotlin.collections.set

class HomeViewModel : BaseViewModel<Any>() {
    lateinit var currentUser: FirebaseUser
    private val devices = arrayOf(
        "E3MZF8cKGWFD379S6wOG",
        "Nm8QoNl0pXcTVXEWknfN",
        "joFceFSzYO6BWu4I7WnK",
        "qa8vYO0hdUfc77IWc9v9"
    )

    fun getAllUsers(): Disposable =
        FirebaseCommon.getListDocument(USER_COLLECTION)
            .subscribe { logD(it.toString()) }

    @SuppressLint("CheckResult")
    fun insertUserToFireStore() {
        currentUser = FirebaseCommon.currentUser!!
        val email = currentUser.email
        val id = currentUser.uid
        val user: HashMap<String, String> = HashMap()
        user[ID] = id
        user[EMAIL] = email!!
        user[DEVICES] = devices.joinToString()
        FirebaseCommon.push(USER_COLLECTION, id, user).subscribe { logD(it.id) }
    }
}