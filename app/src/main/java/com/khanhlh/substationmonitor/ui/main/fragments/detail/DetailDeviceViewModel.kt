package com.khanhlh.substationmonitor.ui.main.fragments.detail

import android.view.View
import androidx.lifecycle.MutableLiveData
import com.khanhlh.substationmonitor.api.FirebaseCommon
import com.khanhlh.substationmonitor.base.BaseViewModel
import com.khanhlh.substationmonitor.extensions.init
import com.khanhlh.substationmonitor.extensions.set
import com.khanhlh.substationmonitor.model.Device
import com.khanhlh.substationmonitor.utils.NAME
import com.khanhlh.substationmonitor.utils.TEMP
import com.khanhlh.substationmonitor.utils.THRESHOLD
import io.reactivex.disposables.Disposable

class DetailDeviceViewModel : BaseViewModel<Any>() {
    val device = MutableLiveData<Device>()
    val isFlashing = MutableLiveData<Boolean>().init(false)
    val visibility = MutableLiveData<Int>().init(View.GONE)

    fun observerDevice(id: String): Disposable = FirebaseCommon.observerDevice(id).subscribe {
        var name = ""
        var temp = ""
        var threshold = ""
        if (it[NAME] != null) name = it[NAME] as String
        if (it[TEMP] != null) temp = it[TEMP] as String
        if (it[THRESHOLD] != null) threshold = it[THRESHOLD] as String
        device.set(Device(id, name, temp, threshold))
        try {
            if (temp.toDouble() > threshold.toDouble()) {
                isFlashing.set(true)
                visibility.set(View.VISIBLE)
            } else {
                isFlashing.set(false)
                visibility.set(View.GONE)
            }
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
    }

    fun updateThreshold() {

    }

    fun stopWarning() {
        isFlashing.set(false)
        visibility.set(View.GONE)
    }
}