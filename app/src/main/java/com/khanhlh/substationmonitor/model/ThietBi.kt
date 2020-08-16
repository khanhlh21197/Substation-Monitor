package com.khanhlh.substationmonitor.model

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
    val ssid: String = ""
) : Serializable {
    val trangthai: Boolean
        get() = status == "bat"
}