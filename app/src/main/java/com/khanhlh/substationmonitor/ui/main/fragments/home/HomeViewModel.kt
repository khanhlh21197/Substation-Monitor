package com.khanhlh.substationmonitor.ui.main.fragments.home

import android.annotation.SuppressLint
import android.view.View
import androidx.databinding.ObservableArrayList
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.QuerySnapshot
import com.khanhlh.substationmonitor.api.FirebaseCommon
import com.khanhlh.substationmonitor.base.BaseViewModel
import com.khanhlh.substationmonitor.enums.UpdateType
import com.khanhlh.substationmonitor.extensions.logD
import com.khanhlh.substationmonitor.model.Device
import com.khanhlh.substationmonitor.model.Room
import com.khanhlh.substationmonitor.utils.*

class HomeViewModel() : BaseViewModel<Any>() {
    val list = ObservableArrayList<Device>()
    val rooms = ObservableArrayList<Room>()
    private val idSet = mutableSetOf<String>()
    val loading = MutableLiveData<Boolean>(true)

    @SuppressLint("CheckResult", "LogNotTimber")
    fun observerAllDevices() {
        FirebaseCommon.observerAllDevices().subscribe {
            add(it.id, it.data)
        }
    }

    @SuppressLint("CheckResult")
    fun getAllRooms() {
        FirebaseCommon.getListDocument(ROOM_COLLECTION).subscribe { t: QuerySnapshot? ->
            t!!.forEach {
                var devices = ""
                var name = ""
                var numberOfDevices = ""
                val id = it.id
                it[DEVICES].let { if (it != null) devices = it as String }
                it[NAME].let { if (it != null) name = it as String }
                it[NUMBER_OF_DEVICES].let { if (it != null) numberOfDevices = it as String }
                val room = Room(id, name, devices, numberOfDevices)
                rooms.add(room)
                logD(room.toString())
            }
        }
    }

    private fun add(id: String, data: Map<String, Any>) {
        val device =
            Device(id, data[NAME].toString(), data[TEMP] as String, data[THRESHOLD] as String)
        if (!idSet.add(id)) {
            val index = idSet.indexOf(id)
            list[index] = device
        } else {
            list.add(device)
        }
    }

    fun firestore(document: String) =
        FirebaseCommon.firestore(DEVICES, document).subscribe({ logD(it.toString()) },
            { logD(it.toString()) })

    @SuppressLint("CheckResult")
    fun updateDevice(id: String, add: UpdateType) =
        FirebaseCommon.updateDevice(id, add)

    fun getDevicesOfUser() = FirebaseCommon.getDevicesOfUser()

    fun showLoading() {
        loadingVisibility.value = View.VISIBLE
    }

    fun hideLoading() {
        loadingVisibility.value = View.INVISIBLE
    }
}