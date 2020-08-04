package com.khanhlh.substationmonitor.model

import android.graphics.Bitmap
import android.view.View
import com.google.firebase.firestore.PropertyName

data class Device(
    val id: String = "",
    @PropertyName("name")
    val name: String = "",
    @PropertyName("temp")
    val temp: String = "0",
    @PropertyName("threshold")
    val threshold: String = "0",
    val type: Long = 0L,
    val image: Bitmap? = null,
    val wattage: String? = "",
    val status: Boolean? = false
) {
    val warning: Int
        get() = if (temp.toDouble() > threshold.toDouble()) View.VISIBLE else View.GONE
    val flashing: Boolean
        get() = temp.toDouble() > threshold.toDouble()
    val tempDisplay: String
        get() = temp + 0x00B0.toChar() + "C"
}