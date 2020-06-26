package com.khanhlh.substationmonitor.ui.main.fragments.home

import android.annotation.SuppressLint
import androidx.databinding.ObservableArrayList
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.khanhlh.substationmonitor.api.FirebaseCommon
import com.khanhlh.substationmonitor.base.BaseViewModel
import com.khanhlh.substationmonitor.extensions.get
import com.khanhlh.substationmonitor.extensions.init
import com.khanhlh.substationmonitor.extensions.logD
import com.khanhlh.substationmonitor.model.Device
import com.khanhlh.substationmonitor.utils.*
import io.reactivex.Observable
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
    private val doc = MutableLiveData<String>().init(devices[1])
    val list = ObservableArrayList<Device>()
    private val idSet = mutableSetOf<String>()

    fun getAllUsers(): Disposable =
        FirebaseCommon
            .getListDocument(USER_COLLECTION)
            .subscribe({ logD(it.toString()) }, { logD(it.toString()) })

    @SuppressLint("CheckResult", "LogNotTimber")
    fun observerAllDevices() {
        FirebaseCommon.observerAllDevices().subscribe {
            add(it.id, it.data)
        }
    }


    private val deviceList = mutableListOf<Device>()

    private fun add(id: String, data: Map<String, Any>) {
        val device = Device(id, data[NAME].toString(), data[TEMP] as String)
        if (!idSet.add(id)) {
            val index = idSet.indexOf(id)
            list[index] = device
        } else {
            list.add(device)
        }
    }

    fun observerDevice() = FirebaseCommon.observerDevice(doc.get()!!).subscribe()

    @SuppressLint("CheckResult")
    fun insertUserToFireStore(): Observable<DocumentReference> {
        currentUser = FirebaseCommon.currentUser!!
        val email = currentUser.email
        val id = currentUser.uid
        val user: HashMap<String, String> = HashMap()
        user[ID] = id
        user[EMAIL] = email!!
        user[DEVICES] = devices.joinToString()
        return FirebaseCommon.push(USER_COLLECTION, id, user)
    }

    fun firestore(document: String) =
        FirebaseCommon.firestore(DEVICES, document).subscribe({ logD(it.toString()) },
            { logD(it.toString()) })
}