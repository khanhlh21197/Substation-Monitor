package com.khanhlh.substationmonitor.ui.main.fragments.home

import android.annotation.SuppressLint
import androidx.databinding.ObservableArrayList
import com.khanhlh.substationmonitor.api.FirebaseCommon
import com.khanhlh.substationmonitor.base.BaseViewModel
import com.khanhlh.substationmonitor.extensions.logD
import com.khanhlh.substationmonitor.model.Device
import com.khanhlh.substationmonitor.utils.DEVICES
import com.khanhlh.substationmonitor.utils.NAME
import com.khanhlh.substationmonitor.utils.TEMP
import com.khanhlh.substationmonitor.utils.THRESHOLD

class HomeViewModel : BaseViewModel<Any>() {
    val list = ObservableArrayList<Device>()
    private val idSet = mutableSetOf<String>()

    @SuppressLint("CheckResult", "LogNotTimber")
    fun observerAllDevices() {
        FirebaseCommon.observerAllDevices().subscribe {
            add(it.id, it.data)
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
    fun updateDevice(id: String, add: UpdateDeviceType) =
        FirebaseCommon.updateDevice(id, add)

    fun getDevicesOfUser() = FirebaseCommon.getDevicesOfUser()
}