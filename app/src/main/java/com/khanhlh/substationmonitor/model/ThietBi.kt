package com.khanhlh.substationmonitor.model

import java.io.Serializable

data class ThietBi(
    var id: String = "",
    val mac: String = "",
    val idphong: String = "",
    val tenthietbi: String = "",
    val mathietbi: String = "",
    val _id: String = "",
    var status: String = "tat"
) : Serializable {
    val trangthai: Boolean
        get() = status == "bat"
}