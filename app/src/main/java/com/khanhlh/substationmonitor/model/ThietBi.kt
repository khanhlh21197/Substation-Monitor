package com.khanhlh.substationmonitor.model

import com.khanhlh.substationmonitor.R
import java.io.Serializable

data class ThietBi(
    var id: String = "",
    val mac: String = "",
    val iduser: String = "",
    val tenthietbi: String = "",
    val mathietbi: String = "",
    val _id: String = "",
    var status: String = "tat",
    val ip: String = "",
    val ssid: String = "",
    val hinhanh: Int = R.drawable.trans_off
) : Serializable {
    var trangthai: Boolean = false
        get() = status == "bat"
}