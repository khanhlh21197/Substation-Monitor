package com.khanhlh.substationmonitor.ui.main.fragments.detail

import androidx.lifecycle.MutableLiveData
import com.khanhlh.substationmonitor.api.FirebaseCommon
import com.khanhlh.substationmonitor.base.BaseViewModel
import com.khanhlh.substationmonitor.extensions.set
import com.khanhlh.substationmonitor.model.Device
import com.khanhlh.substationmonitor.utils.NAME
import com.khanhlh.substationmonitor.utils.TEMP
import com.khanhlh.substationmonitor.utils.THRESHOLD
import io.reactivex.disposables.Disposable

class DetailDeviceViewModel : BaseViewModel<Any>() {
    val device = MutableLiveData<Device>()

    fun observerDevice(id: String): Disposable = FirebaseCommon.observerDevice(id).subscribe {
        device.set(
            Device(
                id,
                it[NAME] as String,
                it[TEMP] as String,
                it[THRESHOLD] as String
            )
        )
    }
}