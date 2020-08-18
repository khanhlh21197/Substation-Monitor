package com.khanhlh.substationmonitor.ui.main.fragments.room

import android.annotation.SuppressLint
import android.view.View
import androidx.databinding.ObservableArrayList
import com.khanhlh.substationmonitor.api.FirebaseCommon
import com.khanhlh.substationmonitor.base.BaseViewModel
import com.khanhlh.substationmonitor.enums.UpdateType
import com.khanhlh.substationmonitor.extensions.logD
import com.khanhlh.substationmonitor.model.Device
import com.khanhlh.substationmonitor.utils.*

class RoomViewModel() : BaseViewModel<Any>() {
    val list = ObservableArrayList<Device>()
    private val idSet = mutableSetOf<String>()

    @SuppressLint("CheckResult", "LogNotTimber")
    fun observerAllDevices(devices: String) {
        FirebaseCommon.observerAllDevices().subscribe({
            add(it.id, it.data, devices)
        }, {
            logD(it.toString())
        })
    }

    fun showLoading() {
        loadingVisibility.value = View.VISIBLE
    }

    fun hideLoading() {
        loadingVisibility.value = View.INVISIBLE
    }

    private fun add(id: String, data: Map<String, Any>, devices: String) {
        if (!devices.contains(id)) return

        var name = ""
        var temp = "0"
        var threshold = "0"
        var status = false
        var wattage = "0"
        var type = 0L
        if (data[NAME] != null) name = data[NAME] as String
        if (data[TEMP] != null) temp = data[TEMP] as String
        if (data[THRESHOLD] != null) threshold = data[THRESHOLD] as String
        if (data[WATTAGE] != null) wattage = data[WATTAGE] as String
        if (data[STATUS] != null) status = data[STATUS] as Boolean
        if (data[TYPE] != null) type = data[TYPE] as Long

        val device =
            Device(
                id,
                name,
                temp, threshold,
                type,
                null,
                wattage,
                status
            )
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
}