package com.khanhlh.substationmonitor.ui.main.fragments.detail

import android.annotation.SuppressLint
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.khanhlh.substationmonitor.MyApp
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.api.FirebaseCommon
import com.khanhlh.substationmonitor.base.BaseViewModel
import com.khanhlh.substationmonitor.extensions.get
import com.khanhlh.substationmonitor.extensions.init
import com.khanhlh.substationmonitor.extensions.logD
import com.khanhlh.substationmonitor.extensions.set
import com.khanhlh.substationmonitor.model.Device
import com.khanhlh.substationmonitor.utils.*
import io.reactivex.disposables.Disposable

class DetailDeviceViewModel(app: MyApp) : BaseViewModel<Any>(app) {
    val device = MutableLiveData<Device>()
    val isFlashing = MutableLiveData<Boolean>().init(false)
    val visibility = MutableLiveData<Int>().init(View.GONE)
    val threshold = MutableLiveData<String>().init("")
    val fabClick = View.OnClickListener {  }

    fun observerDevice(id: String): Disposable = FirebaseCommon.observerDevice(id).subscribe({
        var name = ""
        var temp = ""
        var threshold = ""
        var status = false
        var wattage = ""
        var type = 0L
        if (it[NAME] != null) name = it[NAME] as String
        if (it[TEMP] != null) temp = it[TEMP] as String
        if (it[THRESHOLD] != null) threshold = it[THRESHOLD] as String
        if (it[WATTAGE] != null) wattage = it[WATTAGE] as String
        if (it[STATUS] != null) status = it[STATUS] as Boolean
        if (it[TYPE] != null) type = it[TYPE] as Long
        device.set(Device(id, name, temp, threshold, type, null, wattage, status))
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
    }, {
        logD(it.toString())
    })

    @SuppressLint("CheckResult")
    fun updateThreshold(id: String) {
        if (threshold.get() != null) {
            FirebaseCommon.update(DEVICES, id, THRESHOLD, threshold.get()!!).subscribe(
                { errorMessage.set(MyApp.context.getString(R.string.configure_threshold_success)) },
                { errorMessage.set(MyApp.context.getString(R.string.configure_threshold_fail)) }
            )
        } else {
            errorMessage.set(MyApp.context.getString(R.string.configure_threshold_success))
        }
    }

    fun stopWarning() {
        isFlashing.set(false)
        visibility.set(View.GONE)
    }

    fun onMinusClicked() {

    }

    fun showLoading() {
        loadingVisibility.value = View.VISIBLE
    }

    fun hideLoading() {
        loadingVisibility.value = View.INVISIBLE
    }
}